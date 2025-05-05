package mediator;

public interface Mediator {
    void sendMessage(String message, String sender);
    void registerEmployee(String employeeName, int port);
}