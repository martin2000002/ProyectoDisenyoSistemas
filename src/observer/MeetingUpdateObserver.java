// src/observer/MeetingUpdateObserver.java
package observer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeetingUpdateObserver implements Observer {
    private String employeeName;
    private String meetingsFilePath;
    
    public MeetingUpdateObserver(String employeeName) {
        this.employeeName = employeeName;
        this.meetingsFilePath = "/app/data/" + employeeName + "_meetings.txt";
        
        // Create file if it doesn't exist
        try {
            File file = new File(meetingsFilePath);
            if (!file.exists()) {
                boolean created = file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error creating meetings file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void update(String meetingUpdate) {
        // Update the meetings file
        try {
            // Extract UUID from meeting
            String uuid = extractUUID(meetingUpdate);
            String topic = extractTopic(meetingUpdate);
            boolean isDeleted = extractDeleted(meetingUpdate);
            
            // Read the current file
            String currentContent = "";
            try {
                currentContent = new String(Files.readAllBytes(Paths.get(meetingsFilePath)));
            } catch (IOException e) {
                // Empty file or doesn't exist
            }
            
            // Check if meeting exists by UUID
            boolean meetingExists = false;
            if (uuid != null) {
                meetingExists = currentContent.contains("UUID=" + uuid);
            }
            
            if (meetingExists) {
                // Resolve conflict (last-write-wins)
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                String lastModifiedStr = extractLastModified(meetingUpdate);
                LocalDateTime newLastModified = null;
                
                if (lastModifiedStr != null) {
                    newLastModified = LocalDateTime.parse(lastModifiedStr, formatter);
                }
                
                // Replace the existing meeting with new version
                String[] meetings = currentContent.split("\n\n");
                StringBuilder updatedContent = new StringBuilder();
                
                for (String meeting : meetings) {
                    if (!meeting.trim().isEmpty()) {
                        if (meeting.contains("UUID=" + uuid)) {
                            // Check if we should update (last-write-wins)
                            String existingLastModifiedStr = extractLastModified(meeting);
                            if (existingLastModifiedStr != null && newLastModified != null) {
                                LocalDateTime existingLastModified = LocalDateTime.parse(existingLastModifiedStr, formatter);
                                
                                // Only update if new version is newer or same time
                                if (newLastModified.isAfter(existingLastModified) || 
                                    newLastModified.isEqual(existingLastModified)) {
                                    
                                    // Si la reunión está marcada como eliminada:
                                // - Si el empleado actual es uno de los invitados en el mensaje de eliminación,
                                //   entonces eliminar la reunión de su archivo
                                // - Si el empleado actual es el organizador, NO eliminar la reunión,
                                //   pues recibirá la versión actualizada después
                                String organizer = extractOrganizer(meetingUpdate);
                                boolean isInvitedInDeleteMessage = isEmployeeInInvitedList(employeeName, meetingUpdate);
                                
                                if (isDeleted) {
                                    // Si estamos procesando un mensaje de eliminación
                                    if (isInvitedInDeleteMessage && !employeeName.equals(organizer)) {
                                        // Si el empleado está en la lista del mensaje de eliminación y no es el organizador, 
                                        // eliminamos la reunión (no la añadimos al contenido actualizado)
                                        System.out.println("Removing deleted meeting: " + topic + " from " + employeeName + "'s file (employee was uninvited)");
                                    } else {
                                        // Si es el organizador o no está en la lista de eliminación, mantenemos la reunión
                                        // (será actualizada por un mensaje posterior)
                                        updatedContent.append(meeting).append("\n\n");
                                    }
                                } else {
                                    // Si no es un mensaje de eliminación, actualizar normalmente
                                    updatedContent.append(meetingUpdate).append("\n\n");
                                }
                                } else {
                                    updatedContent.append(meeting).append("\n\n");
                                }
                            } else {
                                // If we can't determine, use the new version
                                // Pero verificar si está eliminada y el empleado actual no es el organizador
                                String organizer = extractOrganizer(meetingUpdate);
                                if (isDeleted && !employeeName.equals(organizer)) {
                                    // No hacemos nada (no añadimos la reunión eliminada al contenido)
                                    System.out.println("Removing deleted meeting: " + topic + " from " + employeeName + "'s file");
                                } else {
                                    updatedContent.append(meetingUpdate).append("\n\n");
                                }
                            }
                        } else {
                            updatedContent.append(meeting).append("\n\n");
                        }
                    }
                }
                
                // Write back to the file
                FileWriter writer = new FileWriter(meetingsFilePath);
                writer.write(updatedContent.toString());
                writer.close();
            } else {
                // Solo agregamos la reunión si no está marcada como eliminada o si el empleado actual es el organizador
                String organizer = extractOrganizer(meetingUpdate);
                if (!isDeleted || employeeName.equals(organizer)) {
                    // Add the new meeting at the end
                    FileWriter writer = new FileWriter(meetingsFilePath, true);
                    writer.write(meetingUpdate + "\n\n");
                    writer.close();
                }
            }
            
            if (isDeleted) {
                System.out.println("Meeting deleted for " + employeeName + ": " + topic);
            } else {
                System.out.println("Meeting updated for " + employeeName + ": " + topic);
            }
        } catch (IOException e) {
            System.err.println("Error updating meetings file: " + e.getMessage());
        }
    }
    
    private String extractUUID(String meetingStr) {
        Pattern pattern = Pattern.compile("UUID=([^\\n]+)");
        Matcher matcher = pattern.matcher(meetingStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String extractTopic(String meetingStr) {
        Pattern pattern = Pattern.compile("TOPIC=([^\\n]+)");
        Matcher matcher = pattern.matcher(meetingStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String extractOrganizer(String meetingStr) {
        Pattern pattern = Pattern.compile("ORGANIZER=([^\\n]+)");
        Matcher matcher = pattern.matcher(meetingStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private String extractLastModified(String meetingStr) {
        Pattern pattern = Pattern.compile("LAST_MODIFIED=([^\\n]+)");
        Matcher matcher = pattern.matcher(meetingStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private boolean extractDeleted(String meetingStr) {
        Pattern pattern = Pattern.compile("DELETED=([^\\n]+)");
        Matcher matcher = pattern.matcher(meetingStr);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        return false;
    }
    
    private boolean isEmployeeInInvitedList(String employeeName, String meetingStr) {
        String invitedListStr = "";
        Pattern pattern = Pattern.compile("INVITED=([^\\n]+)");
        Matcher matcher = pattern.matcher(meetingStr);
        if (matcher.find()) {
            invitedListStr = matcher.group(1);
        }
        
        String[] invitedArray = invitedListStr.split(",");
        for (String invited : invitedArray) {
            if (invited.trim().equals(employeeName)) {
                return true;
            }
        }
        return false;
    }
}
