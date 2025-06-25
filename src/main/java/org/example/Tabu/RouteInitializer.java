package org.example.GA;

import java.util.ArrayList;
import java.util.List;

public class RouteInitializer {
    private final List<Integer> alleles;
    private double solutionCost;
    private double totalTravelCost;
    private double totalTardiness;
    private double highestTardiness;
    private final Shift[] caregiversRoute;

    public RouteInitializer(List<Integer> patientOrder, int caregiverNumber, double solutionCost) {
        this.alleles = patientOrder;
        this.solutionCost = solutionCost;
        totalTravelCost = 0;
        totalTardiness = 0;
        highestTardiness = 0;
        caregiversRoute = new Shift[caregiverNumber];
    }

    public List<Integer> getAlleles() {
        return alleles;
    }

    public double getSolutionCost() {
        return solutionCost;
    }

    public void setSolutionCost(double solutionCost) {
        this.solutionCost = solutionCost;
    }

    public List<Integer>[] getCaregiversRoute() {
        List<Integer>[] routes = new ArrayList[caregiversRoute.length];
        List<Integer> route;
        for (int i = 0; i < caregiversRoute.length; i++) {
            route = new ArrayList<>(caregiversRoute[i].getRoute());
            routes[i] = route;
        }
        return routes;
    }
    public Shift[] getCaregiversShift(){
        return caregiversRoute;
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
    public void updateTotalTardiness(double totalTardiness) {
        this.totalTardiness += totalTardiness;
    }

    public double getHighestTardiness() {
        return highestTardiness;
    }

    public void setHighestTardiness(double highestTardiness) {
        this.highestTardiness = highestTardiness;
    }

    public void showString(){
        for (Shift shift : caregiversRoute) {
            System.out.println(shift.getRoute());
        }
        System.out.println("Done\n");
    }
}


