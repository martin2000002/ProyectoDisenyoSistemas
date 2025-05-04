// src/model/Meeting.java
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Meeting {
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
        this.topic = topic;
        this.invitedEmployees = new ArrayList<>(invitedEmployees);
        this.organizer = organizer;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastModified = LocalDateTime.now();
    }
    
    // Getters
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
    
    // Otras funciones de utilidad
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
        String topic = lines[0].substring(6);
        String organizer = lines[1].substring(10);
        String location = lines[2].substring(9);
        LocalDateTime startTime = LocalDateTime.parse(lines[3].substring(6), formatter);
        LocalDateTime endTime = LocalDateTime.parse(lines[4].substring(4), formatter);
        LocalDateTime lastModified = LocalDateTime.parse(lines[5].substring(14), formatter);
        
        String[] invitedArray = lines[6].substring(8).split(",");
        List<String> invitedEmployees = new ArrayList<>();
        for (String invited : invitedArray) {
            invitedEmployees.add(invited);
        }
        
        Meeting meeting = new Meeting(topic, invitedEmployees, organizer, location, startTime, endTime);
        meeting.setLastModified(lastModified);
        return meeting;
    }
    
    @Override
    public String toString() {
        return "Meeting{" +
               "topic='" + topic + '\'' +
               ", organizer='" + organizer + '\'' +
               ", location='" + location + '\'' +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", invitedEmployees=" + invitedEmployees +
               '}';
    }
}