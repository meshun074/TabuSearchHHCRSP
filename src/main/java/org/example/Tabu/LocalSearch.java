package org.example.GA;

import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.example.GA.EvaluationFunction.*;
import static org.example.GA.GeneticAlgorithm.conflictCheck;

public class LocalSearch implements Runnable {
    @Override
    public void run() {
        ga.getLSChromosomes().put(r, search());
    }

    private GeneticAlgorithm ga;
    private Chromosome ch;
    private final Random rand;
    private final int r;
    private final int gen;
    private static Patient[] allPatients;
    private static int allCaregivers;
    private static double[][] distances;

    public LocalSearch(GeneticAlgorithm ga, Chromosome ch, int r, int gen ) {
        this.ga = ga;
        this.ch = ch;
        this.rand = ThreadLocalRandom.current();
        this.r = r;
        this.gen = gen;
    }
    public static void initialize(InstancesClass data) {
        allPatients = data.getPatients();
        distances = data.getDistances();
        allCaregivers = data.getCaregivers().length;
    }

    public Chromosome search() {
        Chromosome best = ch;
        boolean continueLS;
        boolean genCont = gen >= 100;
        int counter = 0;
        Random rand = new Random();
        do {
            continueLS = false;
            ch = localSearch(ch);
            if (ch.getFitness() < best.getFitness()|| ch.getFitness() == best.getFitness()&&rand.nextBoolean()) {
                best = ch;
                continueLS = true;
            } else {
                if (genCont && counter < 6) {
                    continueLS = true;
                }
            }
            counter++;
        } while (continueLS);
        return best;
    }
    private Chromosome localSearch(Chromosome ch) {
        List<Integer>[] p1Routes, c1Routes;
        int patientLength = allPatients.length;
        int size = (int) (patientLength * 0.2);
        Set<Integer> selectRoute = new LinkedHashSet<>(size);
        Random rand = new Random();
        while (selectRoute.size() < size) {
            int sp = rand.nextInt(patientLength);
            selectRoute.add(sp);
        }
        p1Routes = ch.getGenes();
        c1Routes = new ArrayList[p1Routes.length];
        //Removing patients of selected route from parent routes
        for (int i = 0; i < p1Routes.length; i++) {
            List<Integer> route1 = new ArrayList<>(p1Routes[i].size());
            for (int j = 0; j < p1Routes[i].size(); j++) {
                int patient = p1Routes[i].get(j);
                if (!selectRoute.contains(patient)) {
                    route1.add(patient);
                }
            }
            c1Routes[i] = route1;
        }
        //Inserting removed route
        List<Integer> route2 = new ArrayList<>(selectRoute);
        Chromosome cTemp = new Chromosome(c1Routes, 0.0, true);
        EvaluationFunction.Evaluate(cTemp);
        boolean isInvalid;
        for (int i = 0; i < route2.size(); i++) {
            isInvalid = cTemp.getFitness() == Double.POSITIVE_INFINITY;
            Chromosome bestChromosome = null;
            int patient = route2.get(i);
            Patient p = allPatients[patient];
            cTemp.buildPatientRouteMap();
            if (p.getRequired_caregivers().length > 1) {
                List<CaregiverPair> caregiverPairs = p.getAllPossibleCaregiverCombinations();
                for (int x = 0; x < caregiverPairs.size(); x++) {
                    CaregiverPair caregiverPair = caregiverPairs.get(x);
                    int first = caregiverPair.getFirst();
                    int second = caregiverPair.getSecond();
                    for (int m = 0; m <= c1Routes[first].size(); m++) {
                        for (int n = 0; n <= c1Routes[second].size(); n++) {
                            if (noEvaluationConflicts(c1Routes[first], c1Routes[second], m, n)) {
                                c1Routes[first].add(m, patient);
                                c1Routes[second].add(n, patient);
                                Chromosome temp = isInvalid?new Chromosome(c1Routes, 0.0,true): new Chromosome(c1Routes, true);
                                temp.setFirst(first);
                                temp.setFirstPosition(m);
                                temp.setSecond(second);
                                temp.setSecondPosition(n);
                                bestChromosome = evaluateMove(temp,bestChromosome,cTemp,isInvalid);
                                c1Routes[first].remove(Integer.valueOf(patient));
                                c1Routes[second].remove(Integer.valueOf(patient));
                            }
                        }
                    }
                }

                if (bestChromosome != null) {
                    isInvalid = bestChromosome.getFitness() == Double.POSITIVE_INFINITY;
                    bestChromosome = swap(bestChromosome, isInvalid, patient);
                    int first = bestChromosome.getFirst();
                    int second = bestChromosome.getSecond();
                    List<Integer>[] routes = bestChromosome.getGenes();
                    c1Routes[first] = new ArrayList<>(routes[first]);
                    c1Routes[second] = new ArrayList<>(routes[second]);
                    cTemp = bestChromosome;
                }
            } else {
                List<CaregiverPair> caregiverPairs = p.getAllPossibleCaregiverCombinations();
                for (int x = 0; x < caregiverPairs.size(); x++) {
                    CaregiverPair caregiverPair = caregiverPairs.get(x);
                    int first = caregiverPair.getFirst();
                    for (int k = 0; k <= c1Routes[first].size(); k++) {
                        c1Routes[first].add(k, patient);
                        Chromosome temp = isInvalid?new Chromosome(c1Routes, 0.0,true): new Chromosome(c1Routes, true);
                        temp.setFirst(first);
                        temp.setFirstPosition(k);
                        bestChromosome = evaluateMove(temp,bestChromosome,cTemp,isInvalid);
                        c1Routes[first].remove(Integer.valueOf(patient));
                    }
                }

                if (bestChromosome != null) {
                    isInvalid = bestChromosome.getFitness() == Double.POSITIVE_INFINITY;
                    bestChromosome = swap(bestChromosome, isInvalid, patient);
                    int first = bestChromosome.getFirst();
                    List<Integer> route = bestChromosome.getGenes()[first];
                    c1Routes[first] = new ArrayList<>(route);
                    cTemp = bestChromosome;
                }
            }
        }
        return cTemp;
    }

    private Chromosome evaluateMove(Chromosome temp, Chromosome bestChromosome, Chromosome base, boolean isInvalid) {
        int[] routeEndPoint = new int[allCaregivers];
        Arrays.fill(routeEndPoint, -1);
        if (isInvalid) {
            EvaluationFunction.Evaluate(temp);
            if (bestChromosome == null || temp.getFitness() < bestChromosome.getFitness()
                    || temp.getFitness() == bestChromosome.getFitness() && rand.nextBoolean()) {
                return temp;
            }
            return bestChromosome;
        }
        int first = temp.getFirst();
        int second = temp.getSecond();
        int firstPosition = temp.getFirstPosition();
        int secondPosition = temp.getSecondPosition();
        int size = 1;
        routeEndPoint[first] = firstPosition;
        if (second != -1) {
            size++;
            routeEndPoint[second] = secondPosition;
        }
        int[] routeMove = new int[size];
        int[] positionMove = new int[size];
        routeMove[0] = first;
        positionMove[0] = firstPosition;
        if (size > 1) {
            routeMove[1] = second;
            positionMove[1] = secondPosition;
        }

        removeAffectedPatient(routeMove, positionMove, base, routeEndPoint);
        Shift[] shifts = base.getCaregiversRouteUp();
        Shift[] tempShifts = temp.getCaregiversRouteUp();
        for (int i = 0; i < routeEndPoint.length; i++) {
            List<Integer> route = new ArrayList<>(shifts[i].getRoute());
            List<Double> currentTime = new ArrayList<>(shifts[i].getCurrentTime());
            List<Double> travelCost = new ArrayList<>(shifts[i].getTravelCost());
            travelCost.remove(travelCost.size() - 1);
            List<Double> tardiness = new ArrayList<>(shifts[i].getTardiness());
            List<Double> maxTardiness = new ArrayList<>(shifts[i].getMaxTardiness());

            if (routeEndPoint[i] != -1) {
                int index = routeEndPoint[i];

                route.subList(index, route.size()).clear();
                index++;
                travelCost.subList(index, travelCost.size()).clear();
                currentTime.subList(index, currentTime.size()).clear();
                tardiness.subList(index, tardiness.size()).clear();
                maxTardiness.subList(index, maxTardiness.size()).clear();
                tempShifts[i] = new Shift(shifts[i].getCaregiver(), route, currentTime, travelCost, tardiness, maxTardiness);
            } else {
                tempShifts[i] = new Shift(shifts[i].getCaregiver(), route, currentTime, travelCost, tardiness, maxTardiness);
            }
        }
        double totalTravelCost = 0;
        double totalTardiness = 0;
        double highestTardiness = 0;
        for(Shift s : tempShifts){
            if( s.getTravelCost().isEmpty()){
                s.addTravelCost(0.0);
                s.addTardiness(0.0);
                s.initializeMaxTardiness(0.0);
                s.addCurrentTime(0.0);
            }
            totalTravelCost+=  s.getTravelCost().get(s.getTravelCost().size()-1);
            totalTardiness+= s.getTardiness().isEmpty()?0: s.getTardiness().get(s.getTardiness().size()-1);
            double maxTardiness = s.getMaxTardiness().isEmpty()?0: s.getMaxTardiness().get(s.getMaxTardiness().size()-1);
            highestTardiness = Math.max(highestTardiness,maxTardiness);
        }
        temp.setTotalTravelCost(totalTravelCost);
        temp.setTotalTardiness(totalTardiness);
        temp.setHighestTardiness(highestTardiness);
        temp.setFitness(0.0);
        evaluate(temp,routeEndPoint,bestChromosome);
        if (bestChromosome == null || temp.getFitness() < bestChromosome.getFitness()
                || temp.getFitness() == bestChromosome.getFitness() && rand.nextBoolean()) {
            return temp;
        }
        return bestChromosome;
    }
    private Chromosome swap(Chromosome ch, boolean isInvalid, int patient) {
        ch.buildPatientRouteMap();
        Chromosome bestChromosome = ch;
        List<Integer>[] routes = ch.getGenes();
        if (ch.getSecond() != -1) {
            int first = ch.getFirst();
            int second = ch.getSecond();
            int firstPosition = ch.getFirstPosition();
            int secondPosition = ch.getSecondPosition();
            List<Integer> route1 = routes[first];
            List<Integer> route2 = routes[second];
            for (int z = 0; z < route1.size(); z++) {
                if (Math.abs(z - firstPosition) > 1) {
                    int p3 = route1.get(z);
                    route1.set(firstPosition, p3);
                    route1.set(z, patient);
                    int newFirstPosition = Math.min(firstPosition,z);
                    Chromosome temp = isInvalid? new Chromosome(routes,0.0, true): new Chromosome(routes, true);
                    temp.setFirst(first);
                    temp.setFirstPosition(newFirstPosition);
                    temp.setSecond(second);
                    temp.setSecondPosition(secondPosition);
                    bestChromosome = evaluateMove(temp,bestChromosome,ch,isInvalid);
                    route1.set(firstPosition, patient);
                    route1.set(z, p3);
                }
            }

            for (int l = 0; l < route2.size(); l++) {
                if (Math.abs(l - secondPosition) > 1) {
                    int p4 = route2.get(l);
                    route2.set(secondPosition, p4);
                    route2.set(l, patient);
                    int newSecondPosition = Math.min(secondPosition,l);
                    Chromosome temp = isInvalid? new Chromosome(routes,0.0, true): new Chromosome(routes, true);
                    temp.setFirst(first);
                    temp.setFirstPosition(firstPosition);
                    temp.setSecond(second);
                    temp.setSecondPosition(newSecondPosition);
                    bestChromosome = evaluateMove(temp,bestChromosome,ch,isInvalid);
                    route2.set(secondPosition, patient);
                    route2.set(l, p4);
                }
            }
            for (int z = 0; z < route1.size(); z++) {
                if (Math.abs(z - firstPosition) > 1) {
                    for (int l = 0; l < route2.size(); l++) {
                        if (Math.abs(l - secondPosition) > 1) {
                            int p3 = route1.get(z);
                            int p4 = route2.get(l);
                            route1.set(firstPosition, p3);
                            route1.set(z, patient);
                            route2.set(secondPosition, p4);
                            route2.set(l, patient);
                            int newFirstPosition = Math.min(firstPosition,z);
                            int newSecondPosition = Math.min(secondPosition,l);
                            Chromosome temp = isInvalid? new Chromosome(routes,0.0,true): new Chromosome(routes, true);
                            temp.setFirst(first);
                            temp.setFirstPosition(newFirstPosition);
                            temp.setSecond(second);
                            temp.setSecondPosition(newSecondPosition);
                            bestChromosome = evaluateMove(temp,bestChromosome,ch,isInvalid);
                            route1.set(firstPosition, patient);
                            route1.set(z, p3);
                            route2.set(secondPosition, patient);
                            route2.set(l, p4);
                        }
                    }
                }
            }
        } else {
            int first = ch.getFirst();
            int firstPosition = ch.getFirstPosition();
            List<Integer> route1 = routes[first];
            for (int i = 0; i < route1.size(); i++) {
                if (Math.abs(i - firstPosition) > 1) {
                    int p2 = route1.get(i);
                    route1.set(firstPosition, p2);
                    route1.set(i, patient);
                    int newFirstPosition = Math.min(firstPosition,i);
                    Chromosome temp = isInvalid? new Chromosome(routes,0.0,true): new Chromosome(routes, true);
                    temp.setFirst(first);
                    temp.setFirstPosition(newFirstPosition);
                    bestChromosome = evaluateMove(temp,bestChromosome,ch,isInvalid);
                    route1.set(firstPosition, patient);
                    route1.set(i, p2);
                }
            }
        }
        return bestChromosome;
    }
    private void evaluate(Chromosome temp, int[] routeEndPoint, Chromosome bestChromosome) {
        Shift[] shifts = temp.getCaregiversRouteUp();
        Set<Integer> track = new HashSet<>(100);
        List<Integer>[] genes = temp.getGenes();
        for(int i = 0; i < routeEndPoint.length; i++){
            List<Integer> route = genes[i];
            Shift caregiver = shifts[i];
            int routeEnd = routeEndPoint[i];
            if(routeEnd != -1){
                for(int j = routeEnd; j < route.size(); j++){
                    int patient = route.get(j);
                    if(!caregiver.getRoute().contains(patient)){
                        if(patientAssignment(temp,patient,caregiver,shifts,i,track)){
                            temp.setFitness(Double.POSITIVE_INFINITY);
                            return;
                        }
                        UpdateCost(temp);
                        if(bestChromosome!=null && temp.getFitness()>bestChromosome.getFitness()){
                            return;
                        }
                        track.clear();
                    }
                }
            }
        }
        for(Shift s : shifts){
            int lastLocationId = s.getRoute().isEmpty()? 0:s.getRoute().get(s.getRoute().size() - 1) + 1;
            double travelCost = distances[lastLocationId][0];
            temp.updateTotalTravelCost(travelCost);
            s.updateTravelCost(travelCost);
        }
        UpdateCost(temp);
    }
    private boolean noEvaluationConflicts(List<Integer> c1Route, List<Integer> c2Route, int m, int n) {
        return conflictCheck(c1Route, c2Route, m, n);
    }
}
