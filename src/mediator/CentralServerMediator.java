package mediator;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import util.PropertiesUtil;

public class CentralServerMediator implements Mediator {
    private Map<String, Integer> employeePorts = new HashMap<>();
    
    public CentralServerMediator() {
        loadEmployeeProperties();
    }

    private String getEmployeeHost(String employeeName) {
        // Convertir el nombre del empleado a nombre de host de Docker
        return employeeName.toLowerCase().replace('_', '-') + "-server";
    }
    
    private void loadEmployeeProperties() {
        // Usar el PropertiesUtil para cargar los empleados
        for (String employeeName : PropertiesUtil.getEmployeeNames()) {
            int port = PropertiesUtil.getEmployeePort(employeeName);
            registerEmployee(employeeName, port);
        }
    }
    
    @Override
    public void sendMessage(String message, String sender) {
        try {
            // Extraer información del mensaje
            String[] lines = message.split("\n");
            String topic = "";
            String organizer = "";
            String invitedLine = "";
            
            for (String line : lines) {
                if (line.startsWith("TOPIC=")) {
                    topic = line.substring(6);
                } else if (line.startsWith("ORGANIZER=")) {
                    organizer = line.substring(10);
                } else if (line.startsWith("INVITED=")) {
                    invitedLine = line.substring(8);
                }
            }
            
            System.out.println("Processing meeting: " + topic + " (Organized by: " + organizer + ")");
            
            // Lista de empleados invitados
            String[] invitedEmployees = invitedLine.split(",");
            
            // Enviar a todos los empleados relevantes (organizador e invitados)
            for (Map.Entry<String, Integer> entry : employeePorts.entrySet()) {
                String employeeName = entry.getKey();
                int port = entry.getValue();
                
                // Verificar si el empleado es el organizador o está invitado
                boolean isInvolved = employeeName.equals(organizer);
                if (!isInvolved) {
                    for (String invited : invitedEmployees) {
                        if (employeeName.equals(invited.trim())) {
                            isInvolved = true;
                            break;
                        }
                    }
                }
                
                if (isInvolved) {
                    try {
                        System.out.println("Sending message to " + employeeName);
                        Socket socket = new Socket(getEmployeeHost(employeeName), port);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(message);
                        out.flush();
                        socket.close();
                        System.out.println("Message sent successfully to " + employeeName);
                    } catch (IOException e) {
                        System.err.println("Error sending message to " + employeeName + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("General error in sendMessage: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void registerEmployee(String employeeName, int port) {
        employeePorts.put(employeeName, port);
        System.out.println("Registered employee: " + employeeName + " on port " + port);
    }
}