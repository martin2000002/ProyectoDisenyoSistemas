// src/server/CentralServer.java
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import mediator.CentralServerMediator;
import mediator.Mediator;

public class CentralServer {
    private Mediator mediator;
    private static final int PORT = 9090;
    
    public CentralServer() {
        this.mediator = new CentralServerMediator();
    }
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Central Server started on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClientConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Central Server error: " + e.getMessage());
        }
    }
    
    private void handleClientConnection(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            StringBuilder messageBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                messageBuilder.append(line).append("\n");
            }
            
            String message = messageBuilder.toString();
            // Extraer el remitente del mensaje
            String sender = extractSender(message);
            
            // Reenviar el mensaje a todos los empleados relevantes
            mediator.sendMessage(message, sender);
            
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        }
    }
    
    private String extractSender(String message) {
        // Extraer el nombre del remitente del mensaje
        // Por ahora, usamos un enfoque simple
        String[] lines = message.split("\n");
        return lines[1].substring(10); // ORGANIZER=NombreDelEmpleado
    }
    
    public static void main(String[] args) {
        CentralServer server = new CentralServer();
        server.start();
    }
}