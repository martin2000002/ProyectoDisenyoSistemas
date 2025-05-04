// src/client/EmployeeClient.java
package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.Meeting;

public class EmployeeClient {
    private String employeeName;
    private static final int CENTRAL_SERVER_PORT = 9090;
    private static final String CENTRAL_SERVER_HOST = "central-server";
    private String meetingsFilePath;
    
    public EmployeeClient(String employeeName) {
        this.employeeName = employeeName;
        this.meetingsFilePath = employeeName + "_meetings.txt";
    }
    
    public void createMeeting(String topic, List<String> invitedEmployees, String location, 
                             LocalDateTime startTime, LocalDateTime endTime) {
        Meeting meeting = new Meeting(topic, invitedEmployees, employeeName, location, startTime, endTime);
        sendMeetingToCentralServer(meeting);
    }
    
    public void modifyMeeting(Meeting meeting) {
        meeting.setLastModified(LocalDateTime.now());
        sendMeetingToCentralServer(meeting);
    }
    
    private void sendMeetingToCentralServer(Meeting meeting) {
        try {
            Socket socket = new Socket(CENTRAL_SERVER_HOST, CENTRAL_SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println(meeting.toStringFormat());
            out.flush();
            
            socket.close();
            System.out.println("Meeting sent to Central Server: " + meeting.getTopic());
        } catch (IOException e) {
            System.err.println("Error sending meeting to Central Server: " + e.getMessage());
        }
    }
    
    // Método para cargar las reuniones existentes
    private List<Meeting> loadMeetings() {
        List<Meeting> meetings = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(meetingsFilePath));
            StringBuilder meetingStr = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() && meetingStr.length() > 0) {
                    // Fin de una reunión, procesarla
                    meetings.add(Meeting.fromStringFormat(meetingStr.toString()));
                    meetingStr = new StringBuilder();
                } else if (!line.trim().isEmpty()) {
                    // Añadir línea a la reunión actual
                    meetingStr.append(line).append("\n");
                }
            }
            
            // Procesar la última reunión si existe
            if (meetingStr.length() > 0) {
                meetings.add(Meeting.fromStringFormat(meetingStr.toString()));
            }
            
            reader.close();
        } catch (IOException e) {
            System.out.println("No meetings found or error reading meetings file.");
        }
        return meetings;
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java EmployeeClient <employeeName>");
            System.exit(1);
        }
        
        String employeeName = args[0];
        EmployeeClient client = new EmployeeClient(employeeName);
        
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        while (true) {
            System.out.println("\n===== " + employeeName + "'s Meeting Manager =====");
            System.out.println("1. Create a new meeting");
            System.out.println("2. Modify an existing meeting");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (choice == 1) {
                System.out.print("Enter meeting topic: ");
                String topic = scanner.nextLine();
                
                System.out.print("Enter invited employees (comma-separated): ");
                String invitedInput = scanner.nextLine();
                List<String> invitedEmployees = List.of(invitedInput.split(","));
                
                System.out.print("Enter meeting location: ");
                String location = scanner.nextLine();
                
                System.out.print("Enter start time (yyyy-MM-ddTHH:mm:ss): ");
                LocalDateTime startTime = LocalDateTime.parse(scanner.nextLine(), formatter);
                
                System.out.print("Enter end time (yyyy-MM-ddTHH:mm:ss): ");
                LocalDateTime endTime = LocalDateTime.parse(scanner.nextLine(), formatter);
                
                client.createMeeting(topic, invitedEmployees, location, startTime, endTime);
            } else if (choice == 2) {
                // Modificar una reunión existente
                List<Meeting> meetings = client.loadMeetings();
                
                if (meetings.isEmpty()) {
                    System.out.println("No meetings found to modify.");
                    continue;
                }
                
                System.out.println("\nAvailable meetings:");
                for (int i = 0; i < meetings.size(); i++) {
                    System.out.println((i + 1) + ". " + meetings.get(i).getTopic() + 
                                      " (" + meetings.get(i).getStartTime() + ")");
                }
                
                System.out.print("Select a meeting to modify (1-" + meetings.size() + "): ");
                int meetingIndex = scanner.nextInt() - 1;
                scanner.nextLine(); // Consume newline
                
                if (meetingIndex < 0 || meetingIndex >= meetings.size()) {
                    System.out.println("Invalid selection.");
                    continue;
                }
                
                Meeting selectedMeeting = meetings.get(meetingIndex);
                System.out.println("\nModifying meeting: " + selectedMeeting.getTopic());
                
                System.out.println("What would you like to modify?");
                System.out.println("1. Topic");
                System.out.println("2. Invited employees");
                System.out.println("3. Location");
                System.out.println("4. Start time");
                System.out.println("5. End time");
                System.out.print("Enter your choice: ");
                
                int modifyChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                switch (modifyChoice) {
                    case 1:
                        System.out.print("Enter new topic: ");
                        selectedMeeting.setTopic(scanner.nextLine());
                        break;
                    case 2:
                        System.out.print("Enter new invited employees (comma-separated): ");
                        String invitedInput = scanner.nextLine();
                        selectedMeeting.setInvitedEmployees(List.of(invitedInput.split(",")));
                        break;
                    case 3:
                        System.out.print("Enter new location: ");
                        selectedMeeting.setLocation(scanner.nextLine());
                        break;
                    case 4:
                        System.out.print("Enter new start time (yyyy-MM-ddTHH:mm:ss): ");
                        selectedMeeting.setStartTime(LocalDateTime.parse(scanner.nextLine(), formatter));
                        break;
                    case 5:
                        System.out.print("Enter new end time (yyyy-MM-ddTHH:mm:ss): ");
                        selectedMeeting.setEndTime(LocalDateTime.parse(scanner.nextLine(), formatter));
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        continue;
                }
                
                client.modifyMeeting(selectedMeeting);
                System.out.println("Meeting modified successfully.");
            } else if (choice == 3) {
                break;
            }
        }
        
        scanner.close();
        System.out.println("Client closed.");
    }
}