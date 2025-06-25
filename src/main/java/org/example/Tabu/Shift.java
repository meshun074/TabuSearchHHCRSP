package org.example.GA;

import org.example.Data.Caregiver;

import java.util.ArrayList;
import java.util.List;

public class Shift {
    private Caregiver caregiver;
    private List<Integer> route;
    private List<Double> currentTime;
    private List<Double> travelCost;
    private List<Double> tardiness;
    private List<Double> maxTardiness;
    private double load;

    public Shift(Caregiver caregiver, List<Integer> route, double currentTime) {
        this.caregiver = caregiver;
        this.route = route;
        this.currentTime = new ArrayList<>();
        this.currentTime.add(currentTime);
        this.travelCost = new ArrayList<>();
        this.tardiness = new ArrayList<>();
        this.maxTardiness = new ArrayList<>();
        this.load = 0.0;
        travelCost.add(0.0);
        tardiness.add(0.0);
        maxTardiness.add(0.0);
    }
    public Shift(Caregiver caregiver,  List<Integer> route, List<Double> currentTime, List<Double> travelCost, List<Double> tardiness, List<Double> maxTardiness) {
        this.caregiver = caregiver;
        this.route = route;
        this.currentTime = currentTime;
        this.travelCost = travelCost;
        this.tardiness = tardiness;
        this.maxTardiness = maxTardiness;
    }
    public void resetShift(){
        this.route = new ArrayList<>();
        this.currentTime = new ArrayList<>();
        this.travelCost = new ArrayList<>();
        this.tardiness = new ArrayList<>();
        this.maxTardiness = new ArrayList<>();
        this.currentTime.add(0.0);
        this.load = 0.0;
        travelCost.add(0.0);
        tardiness.add(0.0);
        maxTardiness.add(0.0);
    }


    public Caregiver getCaregiver() {
        return caregiver;
    }

    public void setCaregiver(Caregiver caregiver) {
        this.caregiver = caregiver;
    }

    public List<Integer> getRoute() {
        return route;
    }

    public void setRoute(List<Integer> route) {
        this.route = route;
    }
    public void updateRoute(int patient) {
        route.add(patient);
    }

    public List<Double> getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime.add(currentTime);
    }

    public void addCurrentTime(double currentTime) {
        this.currentTime.add(currentTime);
    }

    public List<Double> getTardiness() {
        return tardiness;
    }

    public void setTardiness(double tardiness) {
        this.tardiness.add(tardiness);
    }

    public void updateTardiness(double tardiness) {
        double newValue = this.tardiness.getLast() + tardiness;
        this.tardiness.add(newValue);
        updateMaxTardiness(tardiness);
    }
    public void addTardiness(double tardiness) {
        this.tardiness.add(tardiness);
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }
    public void updateLoad(double load) {
        this.load += load;
    }
    public List<Double> getMaxTardiness() {
        return maxTardiness;
    }

    public void updateMaxTardiness(double maxTardiness) {
        double currentMax = this.maxTardiness.getLast();
        this.maxTardiness.add(Math.max(currentMax, maxTardiness));
    }

    public void initializeMaxTardiness(double maxTardiness) {
        this.maxTardiness.add(maxTardiness);
    }

    public List<Double> getTravelCost() {
        return travelCost;
    }

    public void setTravelCost(ArrayList<Double> travelCost) {
        this.travelCost= new ArrayList<>(travelCost);
    }

    public void updateTravelCost(double travelCost) {
        double newValue = this.travelCost.getLast() + travelCost;
        this.travelCost.add(newValue);
    }

    public void addTravelCost(double travelCost) {
        this.travelCost.add(travelCost);
    }



    public void showInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Route -- ").append(route).append("\n")
                .append("Time -- ").append(currentTime).append("\n")
                .append("Tardiness -- ").append(tardiness).append("\n")
                .append("MaxTardiness -- ").append(maxTardiness).append("\n")
                .append("Travel cost -- ").append(travelCost);
        System.out.println(sb);
    }
}

