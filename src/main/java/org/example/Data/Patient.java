package org.example.Data;

import org.example.GA.CaregiverPair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Patient {
    private String id;
    private double[] location;
    private double[] time_window;
    private Required_Caregiver[] required_caregivers;
    private Synchronization synchronization;
    private Set<Integer> possibleFirstCaregiver;
    private Set<Integer> possibleSecondCaregiver;
    private List<Integer> possibleFirstCaregiverList;
    private List<Integer> possibleSecondCaregiverList;
    private Set<Integer> allCaregiversForDoubleService;
    private List<CaregiverPair> allPossibleCaregiverCombinations;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public Set<Integer> getPossibleFirstCaregiver() {
        return possibleFirstCaregiver;
    }

    public void setPossibleFirstCaregiver(Set<Integer> possibleFirstCaregiver) {
        this.possibleFirstCaregiver = new HashSet<>(possibleFirstCaregiver);
        this.possibleFirstCaregiverList = new ArrayList<>(possibleFirstCaregiver);
    }

    public Set<Integer> getPossibleSecondCaregiver() {
        return possibleSecondCaregiver;
    }

    public void setPossibleSecondCaregiver(Set<Integer> possibleSecondCaregiver) {
        this.possibleSecondCaregiver = new HashSet<>(possibleSecondCaregiver);
        this.possibleSecondCaregiverList = new ArrayList<>(possibleSecondCaregiver);
    }

    public List<CaregiverPair> getAllPossibleCaregiverCombinations() {
        return allPossibleCaregiverCombinations;
    }

    public void setAllPossibleCaregiverCombinations(List<CaregiverPair> allPossibleCaregiverCombinations) {
        this.allPossibleCaregiverCombinations = allPossibleCaregiverCombinations;
    }

    public Set<Integer> getAllCaregiversForDoubleService() {
        return allCaregiversForDoubleService;
    }

    public void setAllCaregiversForDoubleService(Set<Integer> allCaregiversForDoubleService) {
        this.allCaregiversForDoubleService = allCaregiversForDoubleService;
    }

    public CaregiverPair getRandomCaregiverPair(){
        int firstCaregiver = possibleFirstCaregiverList.get(random.nextInt(possibleFirstCaregiver.size()));
        int secondCaregiver = -1;
        if(required_caregivers.length > 1){
            do {
                secondCaregiver = possibleSecondCaregiverList.get(random.nextInt(possibleSecondCaregiver.size()));
            }
            while (firstCaregiver == secondCaregiver);
        }
        return new CaregiverPair(firstCaregiver, secondCaregiver);
    }

    public String getId() {
        return id;
    }

    public double[] getLocation() {
        return location;
    }

    public double[] getTime_window() {
        return time_window;
    }

    public Required_Caregiver[] getRequired_caregivers() {
        return required_caregivers;
    }

    public Synchronization getSynchronization() {
        return synchronization;
    }

}
