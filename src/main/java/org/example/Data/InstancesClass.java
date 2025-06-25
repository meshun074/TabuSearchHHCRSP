package org.example.Data;

import org.example.GA.CaregiverPair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InstancesClass {
    private Patient[] patients;
    private Service[] services;
    private Caregiver[] caregivers;
    private Offices[] central_offices;
    private double[][] distances;
    private static final Map<String, Set<Integer>> SERVICE_CAREGIVER_CACHE = new ConcurrentHashMap<>();

    public double[][] getDistances() {
        return distances;
    }

    public Offices[] getCentral_offices() {
        return central_offices;
    }

    public Patient[] getPatients() {
        return patients;
    }

    public Caregiver[] getCaregivers() {
        return caregivers;
    }

    //The method should be called by the jackson package after deserialization
    public void setCaregivers(Caregiver[] caregivers) {
        this.caregivers = caregivers;
        initializeCaregiverCache();
        initializePatientServiceCaregiver();
        initializeAllPossibleCaregiverCombinations();
    }

    private void initializePatientServiceCaregiver() {
        for( Patient p : patients){
            p.setPossibleFirstCaregiver(getQualifiedCaregiver(p.getRequired_caregivers()[0].getService()));
            if(p.getRequired_caregivers().length>1){
                p.setPossibleSecondCaregiver(getQualifiedCaregiver(p.getRequired_caregivers()[1].getService()));
            }
        }
    }

    private void initializeAllPossibleCaregiverCombinations() {
        for( Patient p : patients){
            if(p.getRequired_caregivers().length>1){
                List<CaregiverPair> caregiverPairs = getListOfCaregiverPairs(p);
                p.setAllPossibleCaregiverCombinations(caregiverPairs);
            }else {
                List<CaregiverPair> caregiverPairs = new ArrayList<>();
                CaregiverPair caregiverPair;
                Set<Integer>firstCaregivers = p.getPossibleFirstCaregiver();
                for (int i : firstCaregivers) {
                    caregiverPair = new CaregiverPair(i, -1);
                    caregiverPairs.add(caregiverPair);
                }
                p.setAllPossibleCaregiverCombinations(caregiverPairs);
            }
        }
    }

    private static List<CaregiverPair> getListOfCaregiverPairs(Patient p) {
        Set<Integer>firstCaregivers = p.getPossibleFirstCaregiver();
        Set<Integer>secondCaregivers = p.getPossibleSecondCaregiver();
        Set<Integer> allCaregivers = new HashSet<>(firstCaregivers);
        Set<String> allCaregiversSet = new HashSet<>();
        allCaregivers.addAll(secondCaregivers);
        p.setAllCaregiversForDoubleService(allCaregivers);
        List<CaregiverPair> caregiverPairs = new ArrayList<>();
        CaregiverPair caregiverPair;
        for (int i : firstCaregivers) {
            for(int j : secondCaregivers){
                String com = i+"-"+j;
                if(i!=j&&!allCaregiversSet.contains(com)){
                    caregiverPair = new CaregiverPair(i, j);
                    caregiverPairs.add(caregiverPair);
                    allCaregiversSet.add(com);
                }
            }
        }
        return caregiverPairs;
    }

    // Initialize the cache at startup or when dataset changes
    private void initializeCaregiverCache() {
        SERVICE_CAREGIVER_CACHE.clear();
        for (Caregiver c : caregivers) {
            for (String ability : c.getAbilities()) {
                SERVICE_CAREGIVER_CACHE
                        .computeIfAbsent(ability, k -> new HashSet<>())
                        .add(c.getCacheId());
            }
        }
        // Make cache immutable
        SERVICE_CAREGIVER_CACHE.replaceAll((k, v) -> Collections.unmodifiableSet(v));
    }
    public Set<Integer> getQualifiedCaregiver(String service) {
        // Return cached result if available
        Set<Integer> cached = SERVICE_CAREGIVER_CACHE.get(service);
        if (cached != null) {
            return cached;
        }

        // Fallback to computation if not in cache (shouldn't happen if cache was initialized)
        Set<Integer> caregiverList = new HashSet<>();
        for (Caregiver c : caregivers) {
            if (c.getAbilities().contains(service)) {
                caregiverList.add(c.getCacheId());
            }
        }
        Set<Integer> immutableSet = Collections.unmodifiableSet(caregiverList);
        SERVICE_CAREGIVER_CACHE.put(service, immutableSet);
        return immutableSet;
    }

    public Service[] getServices() {
        return services;
    }

}
