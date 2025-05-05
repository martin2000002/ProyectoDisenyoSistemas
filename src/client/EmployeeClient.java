// src/client/EmployeeClient.java
package client;

import java.io.BufferedReader;
import java.io.File;
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
import util.PropertiesUtil;

public class EmployeeClient {
    private String employeeName;
    private static final int CENTRAL_SERVER_PORT = 9090;
    private static final String CENTRAL_SERVER_HOST = "central-server";
    private String meetingsFilePath;
    
    public EmployeeClient(String employeeName) {
        this.employeeName = employeeName;
        this.meetingsFilePath = "/app/data/" + employeeName + "_meetings.txt";
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
            File file = new File(meetingsFilePath);
            
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
            System.out.println("EmployeeClient: Error reading meetings file: " + e.getMessage());
            e.printStackTrace();
        }
        return meetings;
    }
    
    // Método para mostrar el menú de selección de empleado dinámicamente desde el archivo properties
    private static String selectEmployee(Scanner scanner) {
        List<String> employeeNames = PropertiesUtil.getEmployeeNames();
        
        System.out.println("\n===== Employee Selection =====");
        for (int i = 0; i < employeeNames.size(); i++) {
            System.out.println((i + 1) + ". " + employeeNames.get(i));
        }
        System.out.print("Select an employee (1-" + employeeNames.size() + "): ");
        
        int choice;
        try {
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Please enter a number.");
            return selectEmployee(scanner); // Recursión para volver a solicitar input
        }
        
        if (choice >= 1 && choice <= employeeNames.size()) {
            return employeeNames.get(choice - 1);
        } else {
            System.out.println("Invalid selection. Please try again.");
            return selectEmployee(scanner); // Recursión para volver a solicitar input
        }
    }
    
    // Método para seleccionar empleados invitados mostrando un menú
    private static List<String> selectInvitedEmployees(Scanner scanner, String currentEmployee) {
        List<String> employeeNames = PropertiesUtil.getEmployeeNames();
        List<String> availableEmployees = new ArrayList<>();
        
        // Quitar el empleado actual de la lista de disponibles
        for (String emp : employeeNames) {
            if (!emp.equals(currentEmployee)) {
                availableEmployees.add(emp);
            }
        }
        
        System.out.println("\nSelect Employees to Invite:");
        for (int i = 0; i < availableEmployees.size(); i++) {
            System.out.println("     " + (i + 1) + ". " + availableEmployees.get(i));
        }
        System.out.print("     Enter numbers of employees to invite (comma-separated, e.g., 1,3,4): ");
        
        String input = scanner.nextLine();
        String[] selections = input.split(",");
        List<String> invitedEmployees = new ArrayList<>();
        
        for (String sel : selections) {
            try {
                int index = Integer.parseInt(sel.trim()) - 1;
                if (index >= 0 && index < availableEmployees.size()) {
                    invitedEmployees.add(availableEmployees.get(index));
                } else {
                    System.out.println("Ignoring invalid selection: " + sel);
                }
            } catch (NumberFormatException e) {
                System.out.println("Ignoring invalid input: " + sel);
            }
        }
        
        return invitedEmployees;
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String employeeName;
        
        if (args.length >= 1) {
            // Si se proporciona un argumento, usarlo como nombre de empleado
            employeeName = args[0];
        } else {
            // De lo contrario, mostrar menú de selección
            employeeName = selectEmployee(scanner);
        }
        
        EmployeeClient client = new EmployeeClient(employeeName);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        while (true) {
            System.out.println("\n===== " + employeeName + "'s Meeting Manager =====");
            System.out.println("1. Create a new meeting");
            System.out.println("2. Modify an existing meeting");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume invalid input
                continue;
            }
            
            if (choice == 1) {
                System.out.print("Enter meeting topic: ");
                String topic = scanner.nextLine();
                
                // Uso del nuevo método para seleccionar invitados
                List<String> invitedEmployees = selectInvitedEmployees(scanner, employeeName);
                
                System.out.print("Enter meeting location: ");
                String location = scanner.nextLine();
                
                System.out.print("Enter start time (yyyy-MM-ddTHH:mm:ss): ");
                LocalDateTime startTime;
                try {
                    startTime = LocalDateTime.parse(scanner.nextLine(), formatter);
                } catch (Exception e) {
                    System.out.println("Invalid date format. Please use yyyy-MM-ddTHH:mm:ss");
                    continue;
                }
                
                System.out.print("Enter end time (yyyy-MM-ddTHH:mm:ss): ");
                LocalDateTime endTime;
                try {
                    endTime = LocalDateTime.parse(scanner.nextLine(), formatter);
                } catch (Exception e) {
                    System.out.println("Invalid date format. Please use yyyy-MM-ddTHH:mm:ss");
                    continue;
                }
                
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
                int meetingIndex;
                try {
                    meetingIndex = scanner.nextInt() - 1;
                    scanner.nextLine(); // Consume newline
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine(); // Consume invalid input
                    continue;
                }
                
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
                
                int modifyChoice;
                try {
                    modifyChoice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine(); // Consume invalid input
                    continue;
                }
                
                switch (modifyChoice) {
                    case 1:
                        System.out.print("Enter new topic: ");
                        selectedMeeting.setTopic(scanner.nextLine());
                        break;
                    case 2:
                        // Uso del nuevo método para seleccionar invitados
                        selectedMeeting.setInvitedEmployees(selectInvitedEmployees(scanner, employeeName));
                        break;
                    case 3:
                        System.out.print("Enter new location: ");
                        selectedMeeting.setLocation(scanner.nextLine());
                        break;
                    case 4:
                        System.out.print("Enter new start time (yyyy-MM-ddTHH:mm:ss): ");
                        try {
                            selectedMeeting.setStartTime(LocalDateTime.parse(scanner.nextLine(), formatter));
                        } catch (Exception e) {
                            System.out.println("Invalid date format. Please use yyyy-MM-ddTHH:mm:ss");
                            continue;
                        }
                        break;
                    case 5:
                        System.out.print("Enter new end time (yyyy-MM-ddTHH:mm:ss): ");
                        try {
                            selectedMeeting.setEndTime(LocalDateTime.parse(scanner.nextLine(), formatter));
                        } catch (Exception e) {
                            System.out.println("Invalid date format. Please use yyyy-MM-ddTHH:mm:ss");
                            continue;
                        }
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        continue;
                }
                
                client.modifyMeeting(selectedMeeting);
                System.out.println("Meeting modified successfully.");
            } else if (choice == 3) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
        
        scanner.close();
        System.out.println("Client closed.");
    }
}