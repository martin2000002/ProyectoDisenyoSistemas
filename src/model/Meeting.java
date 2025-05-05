// src/model/Meeting.java
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Meeting {
    private String uuid;
    private String topic;
    private List<String> invitedEmployees;
    private String organizer;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastModified;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public Meeting(String topic, List<String> invitedEmployees, String organizer, 
                   String location, LocalDateTime startTime, LocalDateTime endTime) {
        this.uuid = UUID.randomUUID().toString();
        this.topic = topic;
        this.invitedEmployees = new ArrayList<>(invitedEmployees);
        this.organizer = organizer;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastModified = LocalDateTime.now();
    }
    
    // Constructor with UUID for existing meetings
    private Meeting(String uuid, String topic, List<String> invitedEmployees, String organizer, 
                   String location, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime lastModified) {
        this.uuid = uuid;
        this.topic = topic;
        this.invitedEmployees = new ArrayList<>(invitedEmployees);
        this.organizer = organizer;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastModified = lastModified;
    }
    
    // Getters
    public String getUuid() {
        return uuid;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public List<String> getInvitedEmployees() {
        return new ArrayList<>(invitedEmployees);
    }
    
    public String getOrganizer() {
        return organizer;
    }
    
    public String getLocation() {
        return location;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    // Setters
    public void setTopic(String topic) {
        this.topic = topic;
        this.lastModified = LocalDateTime.now();
    }
    
    public void setInvitedEmployees(List<String> invitedEmployees) {
        this.invitedEmployees = new ArrayList<>(invitedEmployees);
        this.lastModified = LocalDateTime.now();
    }
    
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
        this.lastModified = LocalDateTime.now();
    }
    
    public void setLocation(String location) {
        this.location = location;
        this.lastModified = LocalDateTime.now();
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.lastModified = LocalDateTime.now();
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.lastModified = LocalDateTime.now();
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    // Other utility functions
    public void addInvitedEmployee(String employee) {
        this.invitedEmployees.add(employee);
        this.lastModified = LocalDateTime.now();
    }
    
    public void removeInvitedEmployee(String employee) {
        this.invitedEmployees.remove(employee);
        this.lastModified = LocalDateTime.now();
    }
    
    public String toStringFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID=").append(uuid).append("\n");
        sb.append("TOPIC=").append(topic).append("\n");
        sb.append("ORGANIZER=").append(organizer).append("\n");
        sb.append("LOCATION=").append(location).append("\n");
        sb.append("START=").append(startTime.format(formatter)).append("\n");
        sb.append("END=").append(endTime.format(formatter)).append("\n");
        sb.append("LAST_MODIFIED=").append(lastModified.format(formatter)).append("\n");
        sb.append("INVITED=");
        for (int i = 0; i < invitedEmployees.size(); i++) {
            sb.append(invitedEmployees.get(i));
            if (i < invitedEmployees.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    public static Meeting fromStringFormat(String meetingString) {
        String[] lines = meetingString.split("\n");
        String uuid = "";
        String topic = "";
        String organizer = "";
        String location = "";
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        LocalDateTime lastModified = null;
        String invitedStr = "";
        
        for (String line : lines) {
            if (line.startsWith("UUID=")) {
                uuid = line.substring(5);
            } else if (line.startsWith("TOPIC=")) {
                topic = line.substring(6);
            } else if (line.startsWith("ORGANIZER=")) {
                organizer = line.substring(10);
            } else if (line.startsWith("LOCATION=")) {
                location = line.substring(9);
            } else if (line.startsWith("START=")) {
                startTime = LocalDateTime.parse(line.substring(6), formatter);
            } else if (line.startsWith("END=")) {
                endTime = LocalDateTime.parse(line.substring(4), formatter);
            } else if (line.startsWith("LAST_MODIFIED=")) {
                lastModified = LocalDateTime.parse(line.substring(14), formatter);
            } else if (line.startsWith("INVITED=")) {
                invitedStr = line.substring(8);
            }
        }
        
        String[] invitedArray = invitedStr.split(",");
        List<String> invitedEmployees = new ArrayList<>();
        for (String invited : invitedArray) {
            if (!invited.trim().isEmpty()) {
                invitedEmployees.add(invited.trim());
            }
        }
        
        // Handle old format meetings without UUID
        if (uuid.isEmpty()) {
            Meeting meeting = new Meeting(topic, invitedEmployees, organizer, location, startTime, endTime);
            meeting.setLastModified(lastModified);
            return meeting;
        } else {
            return new Meeting(uuid, topic, invitedEmployees, organizer, location, startTime, endTime, lastModified);
        }
    }
    
    @Override
    public String toString() {
        return "Meeting{" +
               "uuid='" + uuid + '\'' +
               ", topic='" + topic + '\'' +
               ", organizer='" + organizer + '\'' +
               ", location='" + location + '\'' +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", invitedEmployees=" + invitedEmployees +
               '}';
    }
}