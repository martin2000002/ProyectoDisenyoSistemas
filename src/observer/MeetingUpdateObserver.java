// src/observer/MeetingUpdateObserver.java
package observer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MeetingUpdateObserver implements Observer {
    private String employeeName;
    private String meetingsFilePath;
    
    public MeetingUpdateObserver(String employeeName) {
        this.employeeName = employeeName;
        this.meetingsFilePath = employeeName + "_meetings.txt";
        
        // Crear archivo si no existe
        try {
            File file = new File(meetingsFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error creating meetings file: " + e.getMessage());
        }
    }
    
    @Override
    public void update(String meetingUpdate) {
        // Actualizar el archivo de reuniones
        try {
            // Extraer el identificador de la reunión (tema)
            String[] lines = meetingUpdate.split("\n");
            String topic = lines[0].substring(6);
            
            // Leer el archivo actual
            String currentContent = "";
            try {
                currentContent = new String(Files.readAllBytes(Paths.get(meetingsFilePath)));
            } catch (IOException e) {
                // Archivo vacío o no existe
            }
            
            // Verificar si la reunión ya existe
            if (currentContent.contains("TOPIC=" + topic)) {
                // Resolver conflicto (last-write-wins)
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime newLastModified = LocalDateTime.parse(lines[5].substring(14), formatter);
                
                // Por simplicidad, reemplazamos toda la reunión
                // En una implementación real, se debería analizar mejor el conflicto
                
                // Eliminar la reunión existente
                String[] meetings = currentContent.split("\n\n");
                StringBuilder updatedContent = new StringBuilder();
                
                for (String meeting : meetings) {
                    if (!meeting.trim().isEmpty() && !meeting.contains("TOPIC=" + topic)) {
                        updatedContent.append(meeting).append("\n\n");
                    }
                }
                
                // Agregar la nueva versión
                updatedContent.append(meetingUpdate).append("\n\n");
                
                // Escribir de vuelta al archivo
                FileWriter writer = new FileWriter(meetingsFilePath);
                writer.write(updatedContent.toString());
                writer.close();
            } else {
                // Agregar la nueva reunión al final
                FileWriter writer = new FileWriter(meetingsFilePath, true);
                writer.write(meetingUpdate + "\n\n");
                writer.close();
            }
            
            System.out.println("Meeting updated for " + employeeName + ": " + topic);
        } catch (IOException e) {
            System.err.println("Error updating meetings file: " + e.getMessage());
        }
    }
}