package org.example.Tabu;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Config1 {
    private String instanceName;
    private long instanceIndex;

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        instanceIndex = convertFilenameToLong(instanceName);
    }

    public long getInstanceIndex() {
        return instanceIndex;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public static Config1 read(File file){
        ObjectMapper om = new ObjectMapper();
        Config1 config;
        try {
            config = om.readValue(file, Config1.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }
    public static long convertFilenameToLong(String filename) {
        // Remove all non-digit characters except the decimal point
        String numbersOnly = filename.replaceAll("[^0-9.]", "");

        // Split by decimal point if present
        String[] parts = numbersOnly.split("\\.");
        StringBuilder combined = new StringBuilder();

        // Add all digits before decimal
        combined.append(parts[0]);

        // If there's a decimal part, add it (without the decimal point)
        if (parts.length > 1) {
            combined.append(parts[1]);
        }

        // Convert to long
        return Long.parseLong(combined.toString());
    }
}
