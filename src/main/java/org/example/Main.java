package org.example;
import org.example.Data.InstancesClass;
import org.example.Data.ReadData;
import org.example.Tabu.*;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static InstancesClass instances;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java GeneticAlgorithmRunner <config-file>");
            return;
        }
        long instanceNumber;
        String instanceName;
        long randomSeed;
        try {
            File configFile = new File(args[0]);
            if(args[0].contains("HHCRSP")){
                Config1 config1 = Config1.read(configFile);
                instanceNumber = config1.getInstanceIndex();
                instanceName = config1.getInstanceName();
                int runCount = Integer.parseInt(args[1]);
                randomSeed = System.currentTimeMillis() + runCount;
                //create result directory
                String resultDir = "src/main/java/org/example/Tabu_" + instanceName + "_" + instanceNumber + "_results";
                new File(resultDir).mkdirs();
                // Read dataset
                PrintStream fileout = new PrintStream(resultDir + "/Result_" + instanceName + "_" + instanceNumber + "_" + runCount + "_" + randomSeed + ".txt");
                System.setOut(fileout);
                System.out.printf("Config Parameters: parameterIndex=44, instanceNumber=%d, seed=%d\n", instanceNumber, randomSeed);

                instances = ReadData.read(new File("src/main/java/org/example/Data/kummer/" + instanceName));

            }else
            {
                Config config = Config.read(configFile);

                // Extract parameters from JSON
                int paramIndex = config.getParameterIndex();
                int problemSize = config.getProblemSize();
                instanceNumber = config.getInstanceIndex();

                int runCount = Integer.parseInt(args[1]);
                randomSeed = System.currentTimeMillis() + runCount;

                String[] Instances = {"10", "25", "50", "75", "100", "200", "300"};
                instanceName = Instances[problemSize];

                //create result directory
                String resultDir = "src/main/java/org/example/Tabu_" + paramIndex + "_" + problemSize + "_" + instanceNumber + "_results";
                new File(resultDir).mkdirs();

                // Read dataset
                PrintStream fileout = new PrintStream(resultDir + "/Result_" + instanceName + "_" + instanceNumber + "_" + runCount + "_" + randomSeed + ".txt");
                System.setOut(fileout);
                System.out.printf("Config Parameters: parameterIndex=%d, ProblemSize=%d, instanceNumber=%d\n", paramIndex, problemSize, instanceNumber);

                instances = ReadData.read(new File("src/main/java/org/example/Data/instance/" + instanceName + "_" + instanceNumber + ".json"));
            }
            TabuSearchMain ts = new TabuSearchMain(Solution.generateSolution(), instances);
            Solution bestSolution = ts.Start();
            System.out.println("Best fitness: " + bestSolution.getFitness());
            System.out.println("----------------- Solution ----------------------");
            System.out.println("Instance_" + instanceName + "_" + instanceNumber + " Best Fitness: " + bestSolution.getFitness() );
            System.out.println("Total Distance: " + bestSolution.getTotalTravelCost() + " Total Tardiness: " + bestSolution.getTotalTardiness() + " Highest Tardiness: " + bestSolution.getHighestTardiness());
            bestSolution.showSolution(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}