// src/server/EmployeeServer.java
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import observer.MeetingUpdateObserver;
import observer.Observer;
import observer.Subject;

public class EmployeeServer implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private String employeeName;
    private int port;
    
    public EmployeeServer(String employeeName, int port) {
        this.employeeName = employeeName;
        this.port = port;
        registerObserver(new MeetingUpdateObserver(employeeName));
    }
    
    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
    
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Employee Server " + employeeName + " started on port " + port);
            System.out.println("Waiting for connections...");
            
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Conexión recibida en servidor de " + employeeName);
                    new Thread(() -> handleClientConnection(clientSocket)).start();
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR - Employee Server " + employeeName + " could not start on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleClientConnection(Socket clientSocket) {
        try {
            System.out.println("Procesando mensaje para " + employeeName);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            StringBuilder messageBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                messageBuilder.append(line).append("\n");
                System.out.println("Leyendo línea: " + line);
            }
            
            String message = messageBuilder.toString();
            System.out.println("Mensaje completo recibido: " + message);
            notifyObservers(message);
            
            clientSocket.close();
            System.out.println("Conexión cerrada.");
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // Los argumentos deben ser el nombre del empleado y el puerto
        if (args.length < 2) {
            System.err.println("Usage: java EmployeeServer <employeeName> <port>");
            System.exit(1);
        }
        
        String employeeName = args[0];
        int port = Integer.parseInt(args[1]);
        
        System.out.println("Iniciando servidor para " + employeeName + " en puerto " + port);
        EmployeeServer server = new EmployeeServer(employeeName, port);
        server.start();
    }
}