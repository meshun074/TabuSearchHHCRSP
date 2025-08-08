package org.example.Tabu;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Config {
    private int parameterIndex;
    private int problemSize;
    private int instanceIndex;


    public int getParameterIndex() { return parameterIndex; }
    public int getProblemSize() { return problemSize; }
    public int getInstanceIndex() { return instanceIndex; }
    public static Config read(File file){
        ObjectMapper om = new ObjectMapper();
        Config config;
        try {
            config = om.readValue(file, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }
}
