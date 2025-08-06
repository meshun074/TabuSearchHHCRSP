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
            double bestFitness = Double.MAX_VALUE;
            double averageFitness = 0;
            for (int i =0; i< 30; i++){
                File instancesFile = new File("src/main/java/org/example/Data/instance/200_6.json");
                instances = ReadData.read(instancesFile);
                TabuSearchTest ts = new TabuSearchTest(Solution.generateSolution(), instances);
                Solution best = ts.Start();
                System.out.println("Best fitness: " + best.getFitness() + best);
                if(best.getFitness() < bestFitness){
                    bestFitness = best.getFitness();
                }
                averageFitness += best.getFitness();
            }
            System.out.println("Best: " + bestFitness);
            System.out.println("Average fitness: " + averageFitness/30);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}