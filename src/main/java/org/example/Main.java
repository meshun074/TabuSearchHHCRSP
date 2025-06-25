package org.example;
import org.example.Data.InstancesClass;
import org.example.Data.ReadData;
import org.example.Tabu.*;

import java.io.File;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static InstancesClass instances;
    public static void main(String[] args) {
        try {
            File instancesFile = new File("src/main/java/org/example/Data/instance/200_6.json");
            instances = ReadData.read(instancesFile);
            TabuSearchMain ts = new TabuSearchMain(Solution.generateSolution(), instances);
            Solution best = ts.Start();
            System.out.println("Best fitness: " + best.getFitness()+best);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}