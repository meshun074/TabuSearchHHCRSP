package org.example.Tabu;

import org.example.Data.InstancesClass;
import org.example.Main;

import java.util.*;

public class Solution {
    private List<Integer>[] genes;
    private int caregivers;
    private int first=-1;
    private int second=-1;
    private int third=-1;
    private int fourth=-1;
    private int firstPosition =-1;
    private int secondPosition =-1;
    private int thirdPosition =-1;
    private int fourthPosition =-1;
    private double fitness;
    private double totalTravelCost;
    private double totalTardiness;
    private double highestTardiness;
    private Shift[] caregiversRouteUp;
    private int lastmove = -1;
    private final Map<Integer, Set<Integer>> patientToRoutesMap = new HashMap<>();

    public Solution(List<Integer>[] genes, boolean newChromosome) {
        if (newChromosome) {
            this.genes = new ArrayList[genes.length];
            for (int i = 0; i < genes.length; i++) {
                this.genes[i] = new ArrayList(genes[i]);
            }
        }else {
            this.genes = genes;
        }

        this.caregivers = genes.length;
        this.fitness = 0.0;
        caregiversRouteUp = new Shift[caregivers];
        this.totalTravelCost = 0;
        this.totalTardiness = 0;
        this.highestTardiness = 0;
    }

    public Solution(List<Integer>[] genes, double fitness, boolean newChromosome) {
        if (newChromosome) {
            this.genes = new ArrayList[genes.length];
            for (int i = 0; i < genes.length; i++) {
                this.genes[i] = new ArrayList(genes[i]);
            }
        }else {
            this.genes = genes;
        }

        this.caregivers = genes.length;
        this.fitness = fitness;
        caregiversRouteUp = new Shift[caregivers];
        InstancesClass instance = Main.instances;
        for(int i =0; i<caregivers;i++){
            Shift s = new Shift(instance.getCaregivers()[i],new ArrayList<>(),0.0);
            caregiversRouteUp[i] = s;
        }
        this.totalTravelCost = 0;
        this.totalTardiness = 0;
        this.highestTardiness = 0;
    }
    public Solution(List<Integer>[] genes, double fitness, boolean newChromosome, Shift[] routes) {
        if (newChromosome) {
            this.genes = new ArrayList[genes.length];
            for (int i = 0; i < genes.length; i++) {
                this.genes[i] = new ArrayList(genes[i]);
            }
        }else {
            this.genes = genes;
        }

        this.caregivers = genes.length;
        this.fitness = fitness;
        caregiversRouteUp = routes;
        this.totalTravelCost = 0;
        this.totalTardiness = 0;
        this.highestTardiness = 0;
    }

    public int getMove() {
        return lastmove;
    }

    public void setMoves(int move) {
        this.lastmove = move;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getFirstPosition() {
        return firstPosition;
    }

    public void setFirstPosition(int firstPosition) {
        this.firstPosition = firstPosition;
    }

    public int getSecondPosition() {
        return secondPosition;
    }

    public void setSecondPosition(int secondPosition) {
        this.secondPosition = secondPosition;
    }

    public int getThird() {
        return third;
    }

    public void setThird(int third) {
        this.third = third;
    }

    public int getFourth() {
        return fourth;
    }

    public void setFourth(int fourth) {
        this.fourth = fourth;
    }

    public int getThirdPosition() {
        return thirdPosition;
    }

    public void setThirdPosition(int thirdPosition) {
        this.thirdPosition = thirdPosition;
    }

    public int getFourthPosition() {
        return fourthPosition;
    }

    public void setFourthPosition(int fourthPosition) {
        this.fourthPosition = fourthPosition;
    }

    public int getCaregivers() {
        return caregivers;
    }

    // Call this once when `genes` is initialized or updated
    public void buildPatientRouteMap() {
        patientToRoutesMap.clear();
        Set<Integer> patients;
        for (int i = 0; i < genes.length; i++) {
            patients = new HashSet<>(genes[i]);
            for (int patient : patients) {
                patientToRoutesMap.computeIfAbsent(patient, k -> new HashSet<>()).add(i);
            }
        }
    }
    public  Map<Integer, Set<Integer>> getPatientToRoutesMap(){
        return patientToRoutesMap;
    }

    public Set<Integer> getPatientRoutes(int patient) {
        return patientToRoutesMap.get(patient);
    }

    public void setCaregivers(int caregivers) {
        this.caregivers = caregivers;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public List<Integer>[] getGenes() {
        return genes;
    }

    public void setGenes(List<Integer>[] genes) {
        this.genes = genes;
    }

    public void setCaregiversRouteUp(Shift[] caregiversRouteUp) {
        this.caregiversRouteUp = caregiversRouteUp;
    }
    public Shift[] getCaregiversRouteUp() {
        return caregiversRouteUp;
    }

    public double getTotalTravelCost() {
        return totalTravelCost;
    }

    public void setTotalTravelCost(double totalTravelCost) {
        this.totalTravelCost = totalTravelCost;
    }
    public void updateTotalTravelCost(double totalTravelCost) {
        this.totalTravelCost += totalTravelCost;
    }

    public double getTotalTardiness() {
        return totalTardiness;
    }

    public void setTotalTardiness(double totalTardiness) {
        this.totalTardiness = totalTardiness;
    }

    public double getHighestTardiness() {
        return highestTardiness;
    }

    public void setHighestTardiness(double highestTardiness) {
        this.highestTardiness = highestTardiness;
    }
    public void updateTotalTardiness(double totalTardiness) {
        this.totalTardiness += totalTardiness;
    }
    public void showSolution(int index) {
        System.out.print("\n Best Solution : "+index+"\n");
        for (int i =0; i< genes.length; i++) {
            List<Integer> route = genes[i];
            Shift Caregiver = caregiversRouteUp[i];
            System.out.println(Caregiver.getCaregiver().getId() +" - "+ route);
            System.out.println("Travel Cost to patients\n"+Caregiver.getTravelCost());
            System.out.println("Service completed time at patients\n"+Caregiver.getCurrentTime());
            System.out.println("Route total tardiness: "+Caregiver.getTardiness().getLast()+" Route Highest tardiness: "+Caregiver.getMaxTardiness().getLast());
            System.out.println();
        }

    }
    public static Solution generateSolution() {
        List<Integer> patients= new ArrayList<>();
        InstancesClass instance = Main.instances;
        int chLength = instance.getPatients().length;
        for (int s = 0; s < chLength; s++) {
            patients.add(s);
        }
        Collections.shuffle(patients);
        int caregiverNumber = instance.getCaregivers().length;
        AssignPatients as = new AssignPatients(instance);
        RouteInitializer ri = new RouteInitializer(patients,caregiverNumber,0.0);
        as.Evaluate(Collections.singletonList(ri));
        Solution ch = new Solution(ri.getCaregiversRoute(),ri.getSolutionCost(),false, ri.getCaregiversShift());
        ch.setTotalTravelCost(ri.getTotalTravelCost());
        ch.setTotalTardiness(ri.getTotalTardiness());
        ch.setHighestTardiness(ri.getHighestTardiness());
        return ch;
    }
    public String toString() {
        StringBuilder genesStrings= new StringBuilder();
        for(List<Integer> c :genes){
            genesStrings.append(c.toString());
        }
        return genesStrings.toString();
    }
}
