// src/mediator/CentralServerMediator.java
package mediator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CentralServerMediator implements Mediator {
    private Map<String, Integer> employeePorts = new HashMap<>();
    
    public CentralServerMediator() {
        loadEmployeeProperties();
    }
    
    private void loadEmployeeProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("employees.properties"));
            for (String name : properties.stringPropertyNames()) {
                int port = Integer.parseInt(properties.getProperty(name));
                registerEmployee(name, port);
            }
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
        }
    }
    
    @Override
    public void sendMessage(String message, String sender) {
        // Extraer los empleados involucrados del mensaje
        // Por ahora, enviar a todos para simplificar
        for (Map.Entry<String, Integer> entry : employeePorts.entrySet()) {
            String employeeName = entry.getKey();
            int port = entry.getValue();
            
            // Construir el nombre de host basado en el nombre del empleado
            String hostname = employeeName.toLowerCase().replace('_', '-') + "-server";
            
            try {
                Socket socket = new Socket(hostname, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);
                socket.close();
            } catch (IOException e) {
                System.err.println("Error sending message to " + employeeName + ": " + e.getMessage());
            }
        }
    }
    
    @Override
    public void registerEmployee(String employeeName, int port) {
        employeePorts.put(employeeName, port);
        System.out.println("Registered employee: " + employeeName + " on port " + port);
    }
}