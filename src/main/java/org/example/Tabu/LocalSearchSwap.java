package org.example.Tabu;

import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.example.Tabu.EvaluationFunction.*;

public class LocalSearchSwap implements Runnable {
    @Override
    public void run() {
        tabuSearch.getLSSolution().add(localSearch());
    }

    private final TabuSearchMain tabuSearch;
    private final Solution ch;
    private final Random rand;
    private static Patient[] allPatients;
    private static int allCaregivers;
    private static double[][] distances;

    public LocalSearchSwap(TabuSearchMain tabuSearch, Solution ch) {
        this.tabuSearch = tabuSearch;
        this.ch = ch;
        this.rand = ThreadLocalRandom.current();
    }

    public static void initialize(InstancesClass data) {
        allPatients = data.getPatients();
        distances = data.getDistances();
        allCaregivers = data.getCaregivers().length;
    }

    public Solution localSearch() {
        List<Integer>[] p1Routes, c1Routes;
        int patientLength = allPatients.length;
        int size = (int) (patientLength * 0.4);
        Set<Integer> selectRoute = new LinkedHashSet<>(size);
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            int sp = rand.nextInt(patientLength);
            selectRoute.add(sp);
        }
//        Set<Integer> selectRoute = new LinkedHashSet<>(1);
//        selectRoute.add(randomPatient);
//        while (selectRoute.isEmpty()) {
//            int sp = rand.nextInt(patientLength);
//            if(!tabu.contains(sp)) {
//                selectRoute.add(sp);
//            }
//        }
        p1Routes = ch.getGenes();
        c1Routes = new ArrayList[p1Routes.length];
        //initialize new genes for local search
        for (int i = 0; i < p1Routes.length; i++) {
            List<Integer> route1 = new ArrayList<>(p1Routes[i]);
            c1Routes[i] = route1;
        }
        //Inserting removed route
        List<Integer> route2 = new ArrayList<>(selectRoute);
        Solution cTemp = new Solution(c1Routes, 0.0, true);
        EvaluationFunction.Evaluate(cTemp);
        boolean isInvalid;
        double bestFitness = cTemp.getFitness();
//        System.out.println("Swapping patients: "+ route2);
        for (int i = 0; i < route2.size(); i++) {
            isInvalid = cTemp.getFitness() == Double.POSITIVE_INFINITY;
            Solution bestChromosome = null;
            int patient = route2.get(i);
//            System.out.println("Patient: "+patient);
//            System.out.print(" c1Routes ");
//            for(int u = 0; u < c1Routes.length; u++) {
//                System.out.print(c1Routes[u]);
//            }
//            System.out.println();
//            System.out.println(" cTemp ");
//            System.out.println(cTemp+" - "+cTemp.getFitness());
            Patient p = allPatients[patient];
            cTemp.buildPatientRouteMap();
            if (p.getRequired_caregivers().length > 1) {
                List<CaregiverPair> caregiverPairs = p.getAllPossibleCaregiverCombinations();
                List<Integer> patientIndexRoutes = new ArrayList<>(cTemp.getPatientRoutes(patient));
                int patientRouteIndex1 = patientIndexRoutes.get(0), patientRouteIndex2 = patientIndexRoutes.get(1);
                Set<Integer> firstPossibleRoutes = p.getPossibleFirstCaregiver();
                Set<Integer> secondPossibleRoutes = p.getPossibleSecondCaregiver();
                if (!firstPossibleRoutes.contains(patientRouteIndex1) || !secondPossibleRoutes.contains(patientRouteIndex2)) {
                    patientRouteIndex1 = patientIndexRoutes.get(1);
                    patientRouteIndex2 = patientIndexRoutes.get(0);
                }
                int p1Index = c1Routes[patientRouteIndex1].indexOf(patient);
                int p2Index = c1Routes[patientRouteIndex2].indexOf(patient);
                for (int x = 0; x < caregiverPairs.size(); x++) {
                    CaregiverPair caregiverPair = caregiverPairs.get(x);
                    int c1 = caregiverPair.getFirst();
                    if (c1 == patientRouteIndex1) {
                        for (int j = 0; j < c1Routes[c1].size(); j++) {
                            int otherPatient1 = c1Routes[c1].get(j);
                            swapPatients(c1Routes[c1], p1Index, j);
//                            System.out.print(" c1Routes after swapping first in c1");
//                            for(int u = 0; u < c1Routes.length; u++) {
//                                System.out.print(c1Routes[u]);
//                            }
//                            System.out.println();
                            boolean firstSwap = otherPatient1 != patient;
                            int first = Math.min(p1Index, j);
                            if (firstSwap) {
                                Solution temp = isInvalid ? new Solution(c1Routes, 0.0, true) : new Solution(c1Routes, true);
                                temp.setFirst(c1);
                                temp.setFirstPosition(first);
                                bestChromosome = evaluateMove(temp, bestChromosome, cTemp, isInvalid);
//                                System.out.println(" best "+bestChromosome+" - "+bestChromosome.getFitness());
                            }
                            int c2 = caregiverPair.getSecond();
                            if (c2 == patientRouteIndex2) {
                                for (int k = 0; k < c1Routes[c2].size(); k++) {
                                    int otherPatient2 = c1Routes[c2].get(k);
                                    swapPatients(c1Routes[c2], p2Index, k);
//                                    System.out.print(" c1Routes after swapping in c2 "+otherPatient2);
//                                    for(int u = 0; u < c1Routes.length; u++) {
//                                        System.out.print(c1Routes[u]);
//                                    }
//                                    System.out.println();
                                    boolean secondSwap = otherPatient2 != patient;
                                    if (secondSwap) {
                                        Solution temp1 = isInvalid ? new Solution(c1Routes, 0.0, true) : new Solution(c1Routes, true);
                                        int second = Math.min(p2Index, k);
                                        if (firstSwap) {
                                            temp1.setFirst(c1);
                                            temp1.setFirstPosition(first);
                                            temp1.setSecond(c2);
                                            temp1.setSecondPosition(second);
                                            bestChromosome = evaluateMove(temp1, bestChromosome, cTemp, isInvalid);
                                        } else {
                                            temp1.setFirst(c2);
                                            temp1.setFirstPosition(second);
                                            bestChromosome = evaluateMove(temp1, bestChromosome, cTemp, isInvalid);
                                        }
//                                        System.out.println(" best "+bestChromosome+" - "+bestChromosome.getFitness());
                                    }
                                    swapPatients(c1Routes[c2], p2Index, k);
//                                    System.out.print(" c1Routes after swapping back in c2 "+otherPatient2);
//                                    for(int u = 0; u < c1Routes.length; u++) {
//                                        System.out.print(c1Routes[u]);
//                                    }
//                                    System.out.println();
                                }
                            } else {
                                for (int k = 0; k < c1Routes[c2].size(); k++) {
                                    int otherPatient2 = c1Routes[c2].get(k);
                                    Patient patient2 = allPatients[otherPatient2];
                                    if(swapIsPossible(cTemp, otherPatient2, patient2,patientRouteIndex2,c2)){
                                        c1Routes[c2].set(k, patient);
                                        c1Routes[patientRouteIndex2].set(p2Index, otherPatient2);
//                                        System.out.print(" c1Routes after swapping in same c1, diff routes for c2 "+ c2+" patient "+otherPatient2);
//                                        for(int u = 0; u < c1Routes.length; u++) {
//                                            System.out.print(c1Routes[u]);
//                                        }
//                                        System.out.println();
                                        Solution temp = isInvalid ? new Solution(c1Routes, 0.0, true) : new Solution(c1Routes, true);
                                        if (firstSwap) {
                                            temp.setFirst(c1);
                                            temp.setFirstPosition(first);
                                            temp.setSecond(c2);
                                            temp.setSecondPosition(k);
                                            temp.setThird(patientRouteIndex2);
                                            temp.setThirdPosition(p2Index);
                                            bestChromosome = evaluateMove(temp, bestChromosome, cTemp, isInvalid);
                                        } else {
                                            temp.setFirst(c2);
                                            temp.setFirstPosition(k);
                                            temp.setSecond(patientRouteIndex2);
                                            temp.setSecondPosition(p2Index);
                                            bestChromosome = evaluateMove(temp, bestChromosome, cTemp, isInvalid);
                                        }
//                                        System.out.println(" best "+bestChromosome+" - "+bestChromosome.getFitness());
                                        c1Routes[c2].set(k, otherPatient2);
                                        c1Routes[patientRouteIndex2].set(p2Index, patient);
//                                        System.out.print(" c1Routes after swapping in same c1, and swapping back diff routes for c2 "+ c2+" patient "+otherPatient2);
//                                        for(int u = 0; u < c1Routes.length; u++) {
//                                            System.out.print(c1Routes[u]);
//                                        }
//                                        System.out.println();
                                    }
                                }
                            }
                            swapPatients(c1Routes[c1], p1Index, j);
                        }
                    } else {
//                        System.out.println("double swap");
                        for (int j = 0; j < c1Routes[c1].size(); j++) {
                            int otherPatient1 = c1Routes[c1].get(j);
                            Patient patient1 = allPatients[otherPatient1];
                            boolean firstSwap = swapIsPossible(cTemp,otherPatient1,patient1,patientRouteIndex1,c1);
//                            System.out.println("Firstswap "+firstSwap);
                            if (firstSwap) {
                                c1Routes[c1].set(j, patient);
                                c1Routes[patientRouteIndex1].set(p1Index, otherPatient1);
                                Solution temp = isInvalid ? new Solution(c1Routes, 0.0, true) : new Solution(c1Routes, true);
                                temp.setFirst(c1);
                                temp.setFirstPosition(j);
                                temp.setSecond(patientRouteIndex1);
                                temp.setSecondPosition(p1Index);
                                bestChromosome = evaluateMove(temp, bestChromosome, cTemp, isInvalid);
//                                System.out.println("swap at c1 "+ c1 + " patient "+ otherPatient1 );
                            }

//                            System.out.print(" c1Routes after first two routes swap for c1 ");
//                            for(int u = 0; u < c1Routes.length; u++) {
//                                System.out.print(c1Routes[u]);
//                            }
//                            System.out.println();

                            int c2 = caregiverPair.getSecond();
                            if (c2 == patientRouteIndex2) {
                                for (int k = 0; k < c1Routes[c2].size(); k++) {
                                    int otherPatient2 = c1Routes[c2].get(k);
                                    swapPatients(c1Routes[c2], p2Index, k);
                                    boolean secondSwap = otherPatient2 != patient;
                                    if (secondSwap) {
                                        Solution temp1 = isInvalid ? new Solution(c1Routes, 0.0, true) : new Solution(c1Routes, true);
                                        int second = Math.min(p2Index, k);
                                        if (firstSwap) {
                                            temp1.setFirst(c1);
                                            temp1.setFirstPosition(j);
                                            temp1.setSecond(patientRouteIndex1);
                                            temp1.setSecondPosition(p1Index);
                                            temp1.setThird(c2);
                                            temp1.setThirdPosition(second);
                                            bestChromosome = evaluateMove(temp1, bestChromosome, cTemp, isInvalid);
                                        }else
                                        {
                                            temp1.setFirst(c2);
                                            temp1.setFirstPosition(second);
                                            bestChromosome = evaluateMove(temp1, bestChromosome, cTemp, isInvalid);
                                        }
//                                        System.out.println(" best "+bestChromosome+" - "+bestChromosome.getFitness());
                                    }
//                                    System.out.print(" c1Routes after second same route swap for c2 "+c2+" patient "+otherPatient2);
//                                    for(int u = 0; u < c1Routes.length; u++) {
//                                        System.out.print(c1Routes[u]);
//                                    }
//                                    System.out.println();
                                    swapPatients(c1Routes[c2], p2Index, k);
//                                    System.out.print(" c1Routes after swapping back second same route swap c2 ");
//                                    for(int u = 0; u < c1Routes.length; u++) {
//                                        System.out.print(c1Routes[u]);
//                                    }
//                                    System.out.println();
                                }
                            } else {
                                for (int k = 0; k < c1Routes[c2].size(); k++) {
                                    int otherPatient2 = c1Routes[c2].get(k);
                                    Patient patient2 = allPatients[otherPatient2];
                                    boolean secondSwap = doubleSwapIsPossible(cTemp,otherPatient1,patientRouteIndex1,c1,firstSwap,otherPatient2,patient2,patientRouteIndex2,c2);
//                                    System.out.println("secondSwap "+secondSwap);
                                    if (secondSwap) {
                                        c1Routes[c2].set(k, patient);
                                        c1Routes[patientRouteIndex2].set(p2Index, otherPatient2);
                                        Solution temp1 = isInvalid ? new Solution(c1Routes, 0.0, true) : new Solution(c1Routes, true);
                                        if (firstSwap) {
                                            temp1.setFirst(c1);
                                            temp1.setFirstPosition(j);
                                            temp1.setSecond(patientRouteIndex1);
                                            temp1.setSecondPosition(p1Index);
                                            temp1.setThird(c2);
                                            temp1.setThirdPosition(k);
                                            temp1.setFourth(patientRouteIndex2);
                                            temp1.setFourthPosition(p2Index);
                                            bestChromosome = evaluateMove(temp1, bestChromosome, cTemp, isInvalid);
                                        } else {
                                            temp1.setFirst(c2);
                                            temp1.setFirstPosition(k);
                                            temp1.setSecond(patientRouteIndex2);
                                            temp1.setSecondPosition(p2Index);
                                            bestChromosome = evaluateMove(temp1, bestChromosome, cTemp, isInvalid);
                                        }
//                                        System.out.println(" best "+bestChromosome+" - "+bestChromosome.getFitness());

//                                        System.out.print(" c1Routes after second two routes swap c2 "+c2+" patient "+otherPatient2);
//                                        for(int u = 0; u < c1Routes.length; u++) {
//                                            System.out.print(c1Routes[u]);
//                                        }
//                                        System.out.println();
                                        c1Routes[c2].set(k, otherPatient2);
                                        c1Routes[patientRouteIndex2].set(p2Index, patient);

//                                        System.out.print(" c1Routes after swapping back second two routes swap c2");
//                                        for(int u = 0; u < c1Routes.length; u++) {
//                                            System.out.print(c1Routes[u]);
//                                        }
//                                        System.out.println();
                                    }
                                }
                            }


                            if(firstSwap){
                                c1Routes[c1].set(j, otherPatient1);
                                c1Routes[patientRouteIndex1].set(p1Index, patient);
                            }
//                            System.out.print(" c1Routes after swapping back the first two routes swap c1");
//                            for (int u = 0; u < c1Routes.length; u++) {
//                                System.out.print(c1Routes[u]);
//                            }
//                            System.out.println();
                        }

                    }
                }
                if (bestChromosome != null&& bestChromosome.getFitness()<bestFitness) {
                    int first = bestChromosome.getFirst();
                    int second = bestChromosome.getSecond();
                    int third = bestChromosome.getThird();
                    int fourth = bestChromosome.getFourth();
                    List<Integer>[] routes = bestChromosome.getGenes();
                    c1Routes[first] = new ArrayList<>(routes[first]);
                    if(second!=-1) {
                        c1Routes[second] = new ArrayList<>(routes[second]);
                        if(third!=-1) {
                            c1Routes[third] = new ArrayList<>(routes[third]);
                            if(fourth!=-1) {
                                c1Routes[fourth] = new ArrayList<>(routes[fourth]);
                            }
                        }
                    }
                    cTemp = bestChromosome;
                    bestFitness = cTemp.getFitness();
//                    System.out.print(" c1Routes after updating "+first+" "+second+" "+third+" "+fourth);
//                    for(int u = 0; u < c1Routes.length; u++) {
//                        System.out.print(c1Routes[u]);
//                    }
//                    System.out.println();
                }

//                System.out.println("Ending Double ctemp "+cTemp);
            } else {
                List<CaregiverPair> caregiverPairs = p.getAllPossibleCaregiverCombinations();
                Set<Integer> patientRouteIndexes = cTemp.getPatientRoutes(patient);
                int patientRouteIndex =-1;
                if(patientRouteIndexes==null) {
                    System.out.println("Error");
                    System.out.println("Patient :"+patient);
                    System.out.println(cTemp);
                    System.out.println("patientRouteIndexes is null");
                }
                for(int j: patientRouteIndexes ) {
                    patientRouteIndex = j;
                }
                if(patientRouteIndex ==-1){
                    System.out.println("No caregiver found for patient in Genes");
                    System.exit(1);
                }
                int pIndex = c1Routes[patientRouteIndex].indexOf(patient);

                for (int x = 0; x < caregiverPairs.size(); x++) {
                    CaregiverPair caregiverPair = caregiverPairs.get(x);
                    int c1 = caregiverPair.getFirst();
                    if(c1 == patientRouteIndex){
                        for(int k = 0; k < c1Routes[c1].size(); k++) {
                            int otherPatient = c1Routes[c1].get(k);
//                            System.out.println("k "+k + "other single patient "+otherPatient);
                            if(otherPatient != patient){
//                                System.out.println("before single 1 ");
//                                System.out.print(" c1Routes ");
//                                for(int u = 0; u < c1Routes.length; u++) {
//                                    System.out.print(c1Routes[u]);
//                                }
//                                System.out.println();
                                swapPatients(c1Routes[c1],pIndex,k);
                                int firstPosition = Math.min(pIndex,k);
//                                System.out.println("after single 1 swap");
//                                System.out.print(" c1Routes ");
//                                for(int u = 0; u < c1Routes.length; u++) {
//                                    System.out.print(c1Routes[u]);
//                                }
//                                System.out.println();
                                Solution temp = isInvalid ? new Solution(c1Routes, 0.0, true) : new Solution(c1Routes, true);
                                temp.setFirst(c1);
                                temp.setFirstPosition(firstPosition);
                                bestChromosome = evaluateMove(temp, bestChromosome, cTemp, isInvalid);
//                                System.out.println(" best "+bestChromosome+" - "+bestChromosome.getFitness());
                                swapPatients(c1Routes[c1],pIndex,k);
//                                System.out.println("after swapping back single 1 ");
//                                System.out.print(" c1Routes ");
//                                for(int u = 0; u < c1Routes.length; u++) {
//                                    System.out.print(c1Routes[u]);
//                                }
//                                System.out.println();
                            }
                        }

                    }else {
                        for(int k = 0; k < c1Routes[c1].size(); k++) {
                            int otherPatient = c1Routes[c1].get(k);
                            Patient patient2 = allPatients[otherPatient];
                            if(swapIsPossible(cTemp,otherPatient,patient2,patientRouteIndex,c1)){
//                                System.out.println("before single 2 ");
//                                System.out.print(" c1Routes ");
//                                for(int u = 0; u < c1Routes.length; u++) {
//                                    System.out.print(c1Routes[u]);
//                                }
//                                System.out.println();
                                c1Routes[c1].set(k, patient);
                                c1Routes[patientRouteIndex].set(pIndex, otherPatient);
//                                System.out.println("after swapping single 2 ");
//                                System.out.print(" c1Routes ");
//                                for(int u = 0; u < c1Routes.length; u++) {
//                                    System.out.print(c1Routes[u]);
//                                }
//                                System.out.println();
                                Solution temp = isInvalid ? new Solution(c1Routes, 0.0, true) : new Solution(c1Routes, true);
                                temp.setFirst(c1);
                                temp.setFirstPosition(k);
                                temp.setSecond(patientRouteIndex);
                                temp.setSecondPosition(pIndex);
                                bestChromosome = evaluateMove(temp, bestChromosome, cTemp, isInvalid);
//                                System.out.println(" best "+bestChromosome+" - "+bestChromosome.getFitness());
                                c1Routes[c1].set(k, otherPatient);
                                c1Routes[patientRouteIndex].set(pIndex, patient);
//                                System.out.println("after swapping back single 2 ");
//                                System.out.print(" c1Routes ");
//                                for(int u = 0; u < c1Routes.length; u++) {
//                                    System.out.print(c1Routes[u]);
//                                }
//                                System.out.println();
                            }
                        }
                    }
                }

                if (bestChromosome != null&&bestChromosome.getFitness()<bestFitness) {
                    int first = bestChromosome.getFirst();
                    int second = bestChromosome.getSecond();
                    List<Integer>[] routes = bestChromosome.getGenes();
                    List<Integer> route = routes[first];
                    c1Routes[first] = new ArrayList<>(route);
                    if(second!=-1) {
                        List<Integer> route1 = routes[second];
                        c1Routes[second] = new ArrayList<>(route1);
                    }
                    cTemp = bestChromosome;
                    bestFitness = cTemp.getFitness();
//                    System.out.print(" c1Routes after updating "+first+" "+second);
//                    for(int u = 0; u < c1Routes.length; u++) {
//                        System.out.print(c1Routes[u]);
//                    }
//                    System.out.println();
                }
//                System.out.println("Ending single ctemp "+cTemp);
            }
//            System.out.println(cTemp+" - "+cTemp.getFitness());
//            EvaluationFunction.Evaluate(cTemp);
//            System.out.println("After evaluation");
//            System.out.println(cTemp+" - "+cTemp.getFitness());
//            System.out.println("Done");
        }
        cTemp.setMoves(route2.get(route2.size() - 1));
//        System.exit(0);
        return cTemp;
    }

    private boolean doubleSwapIsPossible(Solution cTemp, int otherPatient1, int patientRouteIndex1, int c1, boolean firstSwap,
                                         int otherPatient2, Patient patient2, int patientRouteIndex2, int c2) {
        if(patient2.getRequired_caregivers().length>1){
            Set<Integer> routeIndexesOfOtherPatient2 = cTemp.getPatientRoutes(otherPatient2);
            int otherPatient2RouteIndex2 =-1;
            for(int r: routeIndexesOfOtherPatient2 ) {
                if(r != c2) {
                    otherPatient2RouteIndex2 = r;
                    break;
                }
            }
            if(otherPatient2RouteIndex2 == -1) {
                return false;
            }
            Set<Integer> possibleFirstRoute = patient2.getPossibleFirstCaregiver();
            Set<Integer> possibleSecondRoute = patient2.getPossibleSecondCaregiver();
            if(otherPatient1 == otherPatient2){
                if(firstSwap) {
                    return possibleFirstRoute.contains(patientRouteIndex1) && possibleSecondRoute.contains(patientRouteIndex2)
                            || possibleFirstRoute.contains(patientRouteIndex2) && possibleSecondRoute.contains(patientRouteIndex1);
                }else {
                    if(c1==patientRouteIndex2)
                        return false;
                    return possibleFirstRoute.contains(c1) && possibleSecondRoute.contains(patientRouteIndex2)
                            || possibleFirstRoute.contains(patientRouteIndex2) && possibleSecondRoute.contains(c1);
                }
            }else {
                if(patientRouteIndex2==otherPatient2RouteIndex2){
                    return false;
                }
                return possibleFirstRoute.contains(otherPatient2RouteIndex2) && possibleSecondRoute.contains(patientRouteIndex2)
                        || possibleFirstRoute.contains(patientRouteIndex2) && possibleSecondRoute.contains(otherPatient2RouteIndex2);
            }
        }else {
            return patient2.getPossibleFirstCaregiver().contains(patientRouteIndex2);
        }
    }

    private boolean swapIsPossible(Solution cTemp, int patientIndex, Patient patient, int routeIndex, int otherPatientRouteIndex1) {
        if (patient.getRequired_caregivers().length >1) {
            Set<Integer> routeIndexesOfOtherPatient = cTemp.getPatientRoutes(patientIndex);
            int otherPatientRouteIndex2 =-1;
            for( int r : routeIndexesOfOtherPatient) {
                if(r != otherPatientRouteIndex1) {
                    otherPatientRouteIndex2 = r;
                    break;
                }
            }
            if(otherPatientRouteIndex2 == -1||routeIndex == otherPatientRouteIndex2) {
                return false;
            }
            Set<Integer> possibleFirstRoute = patient.getPossibleFirstCaregiver();
            Set<Integer> possibleSecondRoute = patient.getPossibleSecondCaregiver();
            return possibleFirstRoute.contains(otherPatientRouteIndex2) && possibleSecondRoute.contains(routeIndex)
                    || possibleFirstRoute.contains(routeIndex) && possibleSecondRoute.contains(otherPatientRouteIndex2);

        }else {
            return patient.getPossibleFirstCaregiver().contains(routeIndex);
        }
    }

    public static void swapPatients(List<Integer> route, int p1Index, int p2Index) {
        int p1 = route.get(p1Index);
        route.set(p1Index, route.get(p2Index));
        route.set(p2Index, p1);
    }

    private Solution evaluateMove(Solution temp, Solution bestChromosome, Solution base, boolean isInvalid) {
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
        int third = temp.getThird();
        int fourth = temp.getFourth();
        int firstPosition = temp.getFirstPosition();
        int secondPosition = temp.getSecondPosition();
        int thirdPosition = temp.getThirdPosition();
        int fourthPosition = temp.getFourthPosition();
        int size = 1;
        routeEndPoint[first] = firstPosition;
        if (second != -1) {
            size++;
            routeEndPoint[second] = secondPosition;
            if (third != -1) {
                size++;
                routeEndPoint[third] = thirdPosition;
                if (fourth != -1) {
                    size++;
                    routeEndPoint[fourth] = fourthPosition;
                }
            }
        }
        int[] routeMove = new int[size];
        int[] positionMove = new int[size];
        routeMove[0] = first;
        positionMove[0] = firstPosition;
        if (size > 1) {
            routeMove[1] = second;
            positionMove[1] = secondPosition;
            if (size > 2) {
                routeMove[2] = third;
                positionMove[2] = thirdPosition;
                if (size > 3) {
                    routeMove[3] = fourth;
                    positionMove[3] = fourthPosition;
                }
            }
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
        for (Shift s : tempShifts) {
            if (s.getTravelCost().isEmpty()) {
                s.addTravelCost(0.0);
                s.addTardiness(0.0);
                s.initializeMaxTardiness(0.0);
                s.addCurrentTime(0.0);
            }
            totalTravelCost += s.getTravelCost().get(s.getTravelCost().size() - 1);
            totalTardiness += s.getTardiness().isEmpty() ? 0 : s.getTardiness().get(s.getTardiness().size() - 1);
            double maxTardiness = s.getMaxTardiness().isEmpty() ? 0 : s.getMaxTardiness().get(s.getMaxTardiness().size() - 1);
            highestTardiness = Math.max(highestTardiness, maxTardiness);
        }
        temp.setTotalTravelCost(totalTravelCost);
        temp.setTotalTardiness(totalTardiness);
        temp.setHighestTardiness(highestTardiness);
        temp.setFitness(0.0);
        evaluate(temp, routeEndPoint, bestChromosome);
        if (bestChromosome == null || temp.getFitness() < bestChromosome.getFitness()
                || temp.getFitness() == bestChromosome.getFitness() && rand.nextBoolean()) {
            return temp;
        }
        return bestChromosome;
    }

    private void evaluate(Solution temp, int[] routeEndPoint, Solution bestChromosome) {
        Shift[] shifts = temp.getCaregiversRouteUp();
        Set<Integer> track = new HashSet<>(100);
        List<Integer>[] genes = temp.getGenes();
        for (int i = 0; i < routeEndPoint.length; i++) {
            List<Integer> route = genes[i];
            Shift caregiver = shifts[i];
            int routeEnd = routeEndPoint[i];
            if (routeEnd != -1) {
                for (int j = routeEnd; j < route.size(); j++) {
                    int patient = route.get(j);
                    if (!caregiver.getRoute().contains(patient)) {
                        if (patientAssignment(temp, patient, caregiver, shifts, i, track)) {
                            temp.setFitness(Double.POSITIVE_INFINITY);
                            return;
                        }
                        UpdateCost(temp);
                        if (bestChromosome != null && temp.getFitness() > bestChromosome.getFitness()) {
                            return;
                        }
                        track.clear();
                    }
                }
            }
        }
        for (Shift s : shifts) {
            int lastLocationId = s.getRoute().isEmpty() ? 0 : s.getRoute().get(s.getRoute().size() - 1) + 1;
            double travelCost = distances[lastLocationId][0];
            temp.updateTotalTravelCost(travelCost);
            s.updateTravelCost(travelCost);
        }
        UpdateCost(temp);
    }
}
