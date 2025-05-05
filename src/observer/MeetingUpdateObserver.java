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
                                    updatedContent.append(meetingUpdate).append("\n\n");
                                } else {
                                    updatedContent.append(meeting).append("\n\n");
                                }
                            } else {
                                // If we can't determine, use the new version
                                updatedContent.append(meetingUpdate).append("\n\n");
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
                // Add the new meeting at the end
                FileWriter writer = new FileWriter(meetingsFilePath, true);
                writer.write(meetingUpdate + "\n\n");
                writer.close();
            }
            
            System.out.println("Meeting updated for " + employeeName + ": " + topic);
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
    
    private String extractLastModified(String meetingStr) {
        Pattern pattern = Pattern.compile("LAST_MODIFIED=([^\\n]+)");
        Matcher matcher = pattern.matcher(meetingStr);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}