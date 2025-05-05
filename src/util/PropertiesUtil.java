// src/util/PropertiesUtil.java
package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesUtil {
    private static final String PROPERTIES_FILE = "employees.properties";
    private static Properties properties = null;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
        }
    }
    
    public static List<String> getEmployeeNames() {
        if (properties == null) {
            loadProperties();
        }
        
        List<String> employeeNames = new ArrayList<>();
        for (String name : properties.stringPropertyNames()) {
            employeeNames.add(name);
        }
        return employeeNames;
    }
    
    public static int getEmployeePort(String employeeName) {
        if (properties == null) {
            loadProperties();
        }
        
        String portStr = properties.getProperty(employeeName);
        if (portStr != null) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number for employee " + employeeName);
            }
        }
        return -1; // Port not found
    }
}