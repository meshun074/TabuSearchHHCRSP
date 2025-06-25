package org.example.GA;

import org.example.Data.Caregiver;
import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AssignPatients {
    private static Patient[] allPatients;
    private static Caregiver[] caregivers;
    private static double[][] distanceMatrix;
    private static final Random rand = new Random();

    public AssignPatients(InstancesClass data) {
        allPatients = data.getPatients();
        caregivers = data.getCaregivers();
        distanceMatrix = data.getDistances();
    }

    public void Evaluate(List<RouteInitializer> population) {
        population.parallelStream().forEach(AssignPatients::getFitness);
    }

    private static void getFitness(RouteInitializer ch) {
        List<Integer> patients = ch.getAlleles();
        boolean convHull = rand.nextBoolean();
        boolean wLoadHeuristic = rand.nextBoolean();
        Shift[] caregiversRoute = ch.getCaregiversShift();
        for (int i = 0; i < caregiversRoute.length; i++) {
            Shift s = new Shift(caregivers[i], new ArrayList<>(), 0.0);
            caregiversRoute[i] = s;
        }
        ch.setTotalTravelCost(0.0);
        ch.setSolutionCost(0.0);
        ch.setHighestTardiness(0.0);
        ch.setTotalTardiness(0.0);
        for (int i = 0; i < patients.size(); i++) {
            double currentCost = Double.POSITIVE_INFINITY;
            CaregiverPair bestCP = null;
            Patient patient = allPatients[patients.get(i)];
            List<CaregiverPair> possibleCaregivers = patient.getAllPossibleCaregiverCombinations();

            for (CaregiverPair pair : possibleCaregivers) {
                double newCost = findInsertionCost(ch, pair, caregiversRoute, patient, patients.get(i), convHull);
                if (wLoadHeuristic) {
                    newCost += caregiversRoute[pair.getFirst()].getLoad();
                    if (patient.getRequired_caregivers().length > 1)
                        newCost += caregiversRoute[pair.getSecond()].getLoad();
                }
                if (newCost < currentCost || rand.nextBoolean() && newCost == currentCost) {
                    currentCost = newCost;
                    bestCP = pair;
                }
            }
            if (bestCP != null) {
                updateRoutes(ch, caregiversRoute, bestCP, patient, patients.get(i), convHull);
                caregiversRoute[bestCP.getFirst()].updateLoad(patient.getRequired_caregivers()[0].getDuration());

                if (bestCP.getSecond() != -1) {
                    caregiversRoute[bestCP.getSecond()].updateLoad(patient.getRequired_caregivers()[1].getDuration());
                }
            }
        }
        if (!convHull) {
            for (Shift shift : caregiversRoute) {
                int last = shift.getRoute().isEmpty() ? 0 : shift.getRoute().get(shift.getRoute().size() - 1) + 1;
                ch.updateTotalTravelCost(distanceMatrix[last][0]);
            }
        }
        updateFitness(ch);
    }

    private static double findInsertionCost(RouteInitializer ch, CaregiverPair cp, Shift[] caregiverRoute, Patient patient, int pIndex, boolean convHull) {
        Shift c1 = caregiverRoute[cp.getFirst()];
        int currentLocation1 = c1.getRoute().isEmpty() ? 0 : c1.getRoute().get(c1.getRoute().size() - 1) + 1;
        int nextLocation = pIndex + 1;
        double arrivalTime1 = c1.getCurrentTime().get(c1.getCurrentTime().size() - 1) + distanceMatrix[currentLocation1][nextLocation];
        double startTime1 = Math.max(arrivalTime1, patient.getTime_window()[0]);
        if (patient.getRequired_caregivers().length > 1) {
            Shift c2 = caregiverRoute[cp.getSecond()];
            int currentLocation2 = c2.getRoute().isEmpty() ? 0 : c2.getRoute().get(c2.getRoute().size() - 1) + 1;
            double arrivalTime2 = c2.getCurrentTime().get(c2.getCurrentTime().size() - 1) + distanceMatrix[currentLocation2][nextLocation];
            double startTime2 = Math.max(arrivalTime2, patient.getTime_window()[0]);
            double travelCost = distanceMatrix[currentLocation1][nextLocation] + distanceMatrix[currentLocation2][nextLocation];
            double tardiness, maxTardiness;
            if (convHull) {
                travelCost += (2 * distanceMatrix[nextLocation][0] - distanceMatrix[currentLocation1][0] - distanceMatrix[currentLocation2][0]);
            }
            if (patient.getSynchronization().getType().equals("simultaneous")) {
                double startTime = Math.max(startTime1, startTime2);
                tardiness = startTime - patient.getTime_window()[1];
                tardiness = 2 * Math.max(tardiness, 0);
                maxTardiness = Math.max(tardiness / 2, ch.getHighestTardiness());
                tardiness += ch.getTotalTardiness();
            } else {
                startTime2 = Math.max(startTime2, startTime1 + patient.getSynchronization().getDistance()[0]);
                if (startTime2 - startTime1 > patient.getSynchronization().getDistance()[1]) {
                    startTime1 = startTime2 - patient.getSynchronization().getDistance()[1];
                }
                double tardiness1 = Math.max(0, startTime1 - patient.getTime_window()[1]);
                double tardiness2 = Math.max(0, startTime2 - patient.getTime_window()[1]);
                maxTardiness = Math.max(tardiness1, tardiness2);
                maxTardiness = Math.max(maxTardiness, ch.getHighestTardiness());
                tardiness = tardiness1 + tardiness2;
                tardiness += ch.getTotalTardiness();
            }
            travelCost += ch.getTotalTravelCost();
            return (1 / 3d * travelCost) + (1 / 3d * tardiness) + (1 / 3d * maxTardiness);
        } else {
            double tardiness = startTime1 - patient.getTime_window()[1];
            tardiness = Math.max(0, tardiness);
            double maxTardiness = Math.max(tardiness, ch.getHighestTardiness());
            tardiness += ch.getTotalTardiness();
            double travelCost = distanceMatrix[currentLocation1][nextLocation] + ch.getTotalTravelCost();
            if (convHull) {
                travelCost += (distanceMatrix[nextLocation][0] - distanceMatrix[currentLocation1][0]);
            }
            return (1 / 3d * travelCost) + (1 / 3d * tardiness) + (1 / 3d * maxTardiness);
        }
    }

    private static void updateRoutes(RouteInitializer ch, Shift[] caregiverRoute, CaregiverPair cp, Patient p, int index, boolean convHull) {
        Shift c1 = caregiverRoute[cp.getFirst()];
        int currentLocation1 = c1.getRoute().isEmpty() ? 0 : c1.getRoute().get(c1.getRoute().size() - 1) + 1;
        int nextLocation = index + 1;
        double arrivalTime1 = c1.getCurrentTime().get(c1.getCurrentTime().size() - 1) + distanceMatrix[currentLocation1][nextLocation];
        double startTime1 = Math.max(arrivalTime1, p.getTime_window()[0]);

        if (p.getRequired_caregivers().length > 1) {
            Shift c2 = caregiverRoute[cp.getSecond()];
            int currentLocation2 = c2.getRoute().isEmpty() ? 0 : c2.getRoute().get(c2.getRoute().size() - 1) + 1;
            double arrivalTime2 = c2.getCurrentTime().get(c2.getCurrentTime().size() - 1) + distanceMatrix[currentLocation2][nextLocation];
            double startTime2 = Math.max(arrivalTime2, p.getTime_window()[0]);
            if (p.getSynchronization().getType().equals("simultaneous")) {
                double startTime = Math.max(startTime1, startTime2);
                double tardiness = startTime - p.getTime_window()[1];
                tardiness = 2 * Math.max(tardiness, 0);
                double highestTardiness = Math.max(tardiness / 2, ch.getHighestTardiness());
                ch.setHighestTardiness(highestTardiness);
                ch.updateTotalTardiness(tardiness);
                c1.setCurrentTime(startTime + p.getRequired_caregivers()[0].getDuration());
                c1.updateTardiness(tardiness / 2);
                c2.setCurrentTime(startTime + p.getRequired_caregivers()[1].getDuration());
                c2.updateTardiness(tardiness / 2);
            } else {
                startTime2 = Math.max(startTime2, startTime1 + p.getSynchronization().getDistance()[0]);
                if (startTime2 - startTime1 > p.getSynchronization().getDistance()[1])
                    startTime1 = startTime2 - p.getSynchronization().getDistance()[1];
                double tardiness1 = Math.max(0, startTime1 - p.getTime_window()[1]);
                double tardiness2 = Math.max(0, startTime2 - p.getTime_window()[1]);
                ch.updateTotalTardiness(tardiness1 + tardiness2);
                double maxTardiness = Math.max(tardiness1, tardiness2);
                maxTardiness = Math.max(maxTardiness, ch.getHighestTardiness());
                ch.setHighestTardiness(maxTardiness);
                c1.setCurrentTime(startTime1 + p.getRequired_caregivers()[0].getDuration());
                c1.updateTardiness(tardiness1);
                c2.setCurrentTime(startTime2 + p.getRequired_caregivers()[1].getDuration());
                c2.updateTardiness(tardiness2);
            }
            double travelCost = distanceMatrix[currentLocation1][nextLocation] + distanceMatrix[currentLocation2][nextLocation];
            if (convHull) {
                travelCost += (2 * distanceMatrix[nextLocation][0] - distanceMatrix[currentLocation1][0] - distanceMatrix[currentLocation2][0]);
            }
            ch.updateTotalTravelCost(travelCost);
            c1.updateRoute(index);
            c1.updateTravelCost(distanceMatrix[currentLocation1][nextLocation]);
            c2.updateRoute(index);
            c2.updateTravelCost(distanceMatrix[currentLocation2][nextLocation]);
        } else {
            double tardiness = startTime1 - p.getTime_window()[1];
            tardiness = Math.max(0, tardiness);
            double maxTardiness = Math.max(tardiness, ch.getHighestTardiness());
            ch.setHighestTardiness(maxTardiness);
            ch.updateTotalTardiness(tardiness);
            double travelCost = distanceMatrix[currentLocation1][nextLocation];
            c1.updateTravelCost(travelCost);
            if (convHull) {
                travelCost += (distanceMatrix[nextLocation][0] - distanceMatrix[currentLocation1][0]);
            }
            ch.updateTotalTravelCost(travelCost);
            c1.setCurrentTime(startTime1 + p.getRequired_caregivers()[0].getDuration());
            c1.updateTardiness(tardiness);
            c1.updateRoute(index);
        }
    }

    private static void updateFitness(RouteInitializer ch) {
        double fitness = (1 / 3d * ch.getTotalTravelCost()) + (1 / 3d * ch.getTotalTardiness()) + (1 / 3d * ch.getHighestTardiness());
        ch.setSolutionCost(fitness);
    }
}
