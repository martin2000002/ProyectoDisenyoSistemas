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
    
    public void modifyMeeting(Meeting meeting, List<String> previousInvitees) {
        meeting.setLastModified(LocalDateTime.now());
        
        // Verificar si se eliminaron invitados
        List<String> currentInvitees = meeting.getInvitedEmployees();
        List<String> removedInvitees = new ArrayList<>();
        
        // Identificar empleados que ya no están invitados
        for (String prevInvitee : previousInvitees) {
            if (!currentInvitees.contains(prevInvitee)) {
                removedInvitees.add(prevInvitee);
            }
        }
        
        // Paso 1: Enviar notificaciones de eliminación a los empleados que ya no están invitados
        if (!removedInvitees.isEmpty()) {
            
            // Crear una copia de la reunión marcada como eliminada
            Meeting deletedMeeting = new Meeting(
                meeting.getTopic(),
                removedInvitees, // Solo incluir a los empleados eliminados
                meeting.getOrganizer(),
                meeting.getLocation(),
                meeting.getStartTime(),
                meeting.getEndTime()
            );
            
            // Establecer el mismo UUID para que se identifique como la misma reunión
            deletedMeeting.setUuid(meeting.getUuid());
            deletedMeeting.markAsDeleted();
            
            // Asegurar que el timestamp sea más reciente
            deletedMeeting.setLastModified(LocalDateTime.now());
            
            // Enviar la notificación de eliminación
            sendMeetingToCentralServer(deletedMeeting);
            
        }
        
        meeting.setLastModified(LocalDateTime.now().plusSeconds(1));
        sendMeetingToCentralServer(meeting);
    }
    
    public void deleteMeeting(Meeting meeting) {
        // Marcar la reunión como eliminada mediante un campo especial
        meeting.setLastModified(LocalDateTime.now());
        meeting.markAsDeleted();
        sendMeetingToCentralServer(meeting);
    }
    
    // Método específico para cuando un invitado modifica solo el tema de una reunión
    public void modifyMeetingAsTopic(Meeting meeting) {
        meeting.setLastModified(LocalDateTime.now());
        // No necesitamos comparar invitados, solo enviar la actualización del tema
        sendMeetingToCentralServer(meeting);
    }
    
    private void sendMeetingToCentralServer(Meeting meeting) {
        try {
            Socket socket = new Socket(CENTRAL_SERVER_HOST, CENTRAL_SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println(meeting.toStringFormat());
            out.flush();
            
            socket.close();
        } catch (IOException e) {
            System.err.println("Error sending meeting to Central Server: " + e.getMessage());
        }
    }
    
    // Method to load existing meetings
    private List<Meeting> loadMeetings() {
        List<Meeting> meetings = new ArrayList<>();
        try {
            File file = new File(meetingsFilePath);
            
            if (!file.exists()) {
                System.out.println("No meetings file found for " + employeeName);
                return meetings;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(meetingsFilePath));
            StringBuilder meetingStr = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() && meetingStr.length() > 0) {
                    // End of a meeting, process it
                    Meeting meeting = Meeting.fromStringFormat(meetingStr.toString());
                    // No cargamos reuniones que estén marcadas como eliminadas
                    if (!meeting.isDeleted()) {
                        meetings.add(meeting);
                    }
                    meetingStr = new StringBuilder();
                } else if (!line.trim().isEmpty()) {
                    // Add line to current meeting
                    meetingStr.append(line).append("\n");
                }
            }
            
            // Process the last meeting if exists
            if (meetingStr.length() > 0) {
                Meeting meeting = Meeting.fromStringFormat(meetingStr.toString());
                if (!meeting.isDeleted()) {
                    meetings.add(meeting);
                }
            }
            
            reader.close();
        } catch (IOException e) {
            System.out.println("EmployeeClient: Error reading meetings file: " + e.getMessage());
            e.printStackTrace();
        }
        return meetings;
    }
    
    // Method to show employee selection menu dynamically from properties file
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
            scanner.nextLine();
        } catch (Exception e) {
            scanner.nextLine();
            System.out.println("Invalid input. Please enter a number.");
            return selectEmployee(scanner); // Recursion to request input again
        }
        
        if (choice >= 1 && choice <= employeeNames.size()) {
            return employeeNames.get(choice - 1);
        } else {
            System.out.println("Invalid selection. Please try again.");
            return selectEmployee(scanner); // Recursion to request input again
        }
    }
    
    // Method to select invited employees by showing a menu
    private static List<String> selectInvitedEmployees(Scanner scanner, String currentEmployee) {
        List<String> employeeNames = PropertiesUtil.getEmployeeNames();
        List<String> availableEmployees = new ArrayList<>();
        
        // Remove current employee from available list
        for (String emp : employeeNames) {
            if (!emp.equals(currentEmployee)) {
                availableEmployees.add(emp);
            }
        }
        
        System.out.println("Select Employees to Invite:");
        for (int i = 0; i < availableEmployees.size(); i++) {
            System.out.println((i + 1) + ". " + availableEmployees.get(i));
        }
        System.out.print("Enter numbers of employees to invite (comma-separated, e.g., 1,3,4): ");
        
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
    
    // Method to select meetings to delete
    private List<Integer> selectMeetingsToDelete(Scanner scanner, List<Meeting> meetings) {
        List<Integer> meetingsToDelete = new ArrayList<>();
        
        // Filtrar las reuniones para mostrar solo las que el empleado actual es organizador
        List<Meeting> organizedMeetings = new ArrayList<>();
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).getOrganizer().equals(employeeName)) {
                organizedMeetings.add(meetings.get(i));
                System.out.println((organizedMeetings.size()) + ". " + meetings.get(i).getTopic() + 
                                  " (" + meetings.get(i).getStartTime() + ")");
            }
        }
        
        if (organizedMeetings.isEmpty()) {
            System.out.println("No meetings found where you are the organizer.");
            return meetingsToDelete;
        }
        
        System.out.print("Enter numbers of meetings to delete (comma-separated, e.g., 1,3,4): ");
        
        String input = scanner.nextLine();
        String[] selections = input.split(",");
        
        for (String sel : selections) {
            try {
                int index = Integer.parseInt(sel.trim()) - 1;
                if (index >= 0 && index < organizedMeetings.size()) {
                    // Encontrar el índice original en la lista completa de meetings
                    Meeting meetingToDelete = organizedMeetings.get(index);
                    for (int i = 0; i < meetings.size(); i++) {
                        if (meetings.get(i).getUuid().equals(meetingToDelete.getUuid())) {
                            meetingsToDelete.add(i);
                            break;
                        }
                    }
                } else {
                    System.out.println("Ignoring invalid selection: " + sel);
                }
            } catch (NumberFormatException e) {
                System.out.println("Ignoring invalid input: " + sel);
            }
        }
        
        return meetingsToDelete;
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String employeeName;
        
        if (args.length >= 1) {
            // If argument provided, use it as employee name
            employeeName = args[0];
        } else {
            // Otherwise, show selection menu
            employeeName = selectEmployee(scanner);
        }
        
        EmployeeClient client = new EmployeeClient(employeeName);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        while (true) {
            System.out.println("\n===== " + employeeName + "'s Meeting Manager =====");
            System.out.println("1. Create a new meeting");
            System.out.println("2. Modify an existing meeting");
            System.out.println("3. Delete your meetings");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }
            
            if (choice == 1) {
                System.out.print("Enter meeting topic: ");
                String topic = scanner.nextLine();
                
                // Use new method to select invitees
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
                // Modify an existing meeting
                List<Meeting> meetings = client.loadMeetings();
                
                if (meetings.isEmpty()) {
                    System.out.println("No meetings found to modify.");
                    continue;
                }
                
                System.out.println("\nAvailable meetings:");
                // Mostrar todas las reuniones, indicando si eres organizador o invitado
                for (int i = 0; i < meetings.size(); i++) {
                    Meeting meeting = meetings.get(i);
                    String roleLabel = meeting.getOrganizer().equals(employeeName) ? "[Organizer]" : "[Invited]";
                    System.out.println((i + 1) + ". " + roleLabel + " " + meeting.getTopic() + 
                                      " (" + meeting.getStartTime() + ")");
                }
                
                System.out.print("Select a meeting to modify (1-" + meetings.size() + "): ");
                int meetingIndex;
                try {
                    meetingIndex = scanner.nextInt() - 1;
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine();
                    continue;
                }
                
                if (meetingIndex < 0 || meetingIndex >= meetings.size()) {
                    System.out.println("Invalid selection.");
                    continue;
                }
                
                Meeting selectedMeeting = meetings.get(meetingIndex);
                boolean isOrganizer = selectedMeeting.getOrganizer().equals(employeeName);
                
                System.out.println("\nModifying meeting: " + selectedMeeting.getTopic());
                
                if (isOrganizer) {
                    System.out.println("As the organizer, you can modify any aspect of this meeting:");
                    System.out.println("1. Topic");
                    System.out.println("2. Invited employees");
                    System.out.println("3. Location");
                    System.out.println("4. Start time");
                    System.out.println("5. End time");
                } else {
                    System.out.println("As an invited participant, you can only modify the topic:");
                    System.out.println("1. Topic");
                }
                
                System.out.print("Enter your choice: ");
                
                int modifyChoice;
                try {
                    modifyChoice = scanner.nextInt();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine();
                    continue;
                }
                
                // Verificar si la opción seleccionada es válida según el rol
                if (!isOrganizer && modifyChoice != 1) {
                    System.out.println("As an invited participant, you can only modify the topic of the meeting.");
                    continue;
                }
                
                // Guardar la lista actual de invitados para posible comparación si eres organizador
                List<String> previousInvitees = selectedMeeting.getInvitedEmployees();
                
                switch (modifyChoice) {
                    case 1:
                        System.out.print("Enter new topic: ");
                        selectedMeeting.setTopic(scanner.nextLine());
                        break;
                    case 2:
                        if (isOrganizer) {
                            // Use new method to select invitees
                            selectedMeeting.setInvitedEmployees(selectInvitedEmployees(scanner, employeeName));
                            
                            // Enviar actualización con la lista previa de invitados para comparación
                            client.modifyMeeting(selectedMeeting, previousInvitees);
                            System.out.println("Meeting modified successfully.");
                            continue; // Saltar al inicio del bucle para evitar la llamada duplicada a modifyMeeting abajo
                        }
                        break;
                    case 3:
                        if (isOrganizer) {
                            System.out.print("Enter new location: ");
                            selectedMeeting.setLocation(scanner.nextLine());
                        }
                        break;
                    case 4:
                        if (isOrganizer) {
                            System.out.print("Enter new start time (yyyy-MM-ddTHH:mm:ss): ");
                            try {
                                selectedMeeting.setStartTime(LocalDateTime.parse(scanner.nextLine(), formatter));
                            } catch (Exception e) {
                                System.out.println("Invalid date format. Please use yyyy-MM-ddTHH:mm:ss");
                                continue;
                            }
                        }
                        break;
                    case 5:
                        if (isOrganizer) {
                            System.out.print("Enter new end time (yyyy-MM-ddTHH:mm:ss): ");
                            try {
                                selectedMeeting.setEndTime(LocalDateTime.parse(scanner.nextLine(), formatter));
                            } catch (Exception e) {
                                System.out.println("Invalid date format. Please use yyyy-MM-ddTHH:mm:ss");
                                continue;
                            }
                        }
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        continue;
                }
                
                // Para todas las modificaciones excepto la lista de invitados (que ya se procesó en el case 2)
                if (isOrganizer) {
                    client.modifyMeeting(selectedMeeting, previousInvitees);
                } else {
                    client.modifyMeetingAsTopic(selectedMeeting);
                }
                System.out.println("Meeting modified successfully.");
                System.out.println("Meeting modified successfully.");
            } else if (choice == 3) {
                // Eliminar reuniones
                List<Meeting> meetings = client.loadMeetings();
                
                if (meetings.isEmpty()) {
                    System.out.println("No meetings found to delete.");
                    continue;
                }
                
                System.out.println("\nYour meetings (as organizer):");
                List<Integer> meetingsToDelete = client.selectMeetingsToDelete(scanner, meetings);
                
                if (meetingsToDelete.isEmpty()) {
                    System.out.println("No meetings selected for deletion.");
                    continue;
                }
                
                System.out.println("Are you sure you want to delete these meetings? (y/n): ");
                String confirmation = scanner.nextLine().trim().toLowerCase();
                
                if (confirmation.equals("y")) {
                    for (int index : meetingsToDelete) {
                        client.deleteMeeting(meetings.get(index));
                        System.out.println("Deleted meeting: " + meetings.get(index).getTopic());
                    }
                } else {
                    System.out.println("Deletion cancelled.");
                }
            } else if (choice == 4) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter 1, 2, 3, or 4.");
            }
        }
        
        scanner.close();
        System.out.println("Client closed.");
    }
}