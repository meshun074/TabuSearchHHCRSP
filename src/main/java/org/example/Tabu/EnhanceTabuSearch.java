package org.example.Tabu;

import com.sun.management.OperatingSystemMXBean;
import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnhanceTabuSearch {
    private final OperatingSystemMXBean osBean;
    private Solution currentSolution;
    private Solution bestSolution;
    private static final int  MAX_ITERATION = 2000;
    private final int MAX_TABU_SIZE;
    private final int MIN_TABU_SIZE;
    private final int MAX_NO_IMPROVEMENT;
    private final int searchSize;
    private long startCpuTime;
    private long startTime;
    private List<Solution> LSChromosomes;
    private final Queue<Integer> tabuList;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public EnhanceTabuSearch(Solution solution, InstancesClass data, int searchSize) {
        currentSolution = solution;
        this.searchSize = searchSize;
        LSChromosomes  = new ArrayList<>(searchSize);
        bestSolution = currentSolution;
        Patient[] allPatients = data.getPatients();
        this.osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        this.MAX_NO_IMPROVEMENT = allPatients.length/2;
        EvaluationFunction.initialize(data);
        this.MAX_TABU_SIZE = allPatients.length/3;
        this.MIN_TABU_SIZE = allPatients.length/5;
        tabuList = new LinkedList<>();
        EnhanceLocalSearch.initialize(data);
    }
    public Solution start(){
        int tabuSize = (int) Math.round((MIN_TABU_SIZE+(MAX_TABU_SIZE-MIN_TABU_SIZE)*Math.random()));
        startTimer();
        int iterationWithoutImprovement = 0;
        double bestFitness = bestSolution.getFitness();
        for(int iter = 0; iter < MAX_ITERATION; iter++) {
            currentSolution = findBestMove(currentSolution);
            tabuList.add(currentSolution.getMove());
            if(tabuList.size() > tabuSize){
                tabuList.poll();
            }
            double currentFitness = currentSolution.getFitness();
            if(currentFitness < bestFitness){
                bestSolution = currentSolution;
                bestFitness = currentFitness;
                iterationWithoutImprovement =0;
                if(tabuSize<MAX_TABU_SIZE)
                    tabuSize++;
            }else {
                if(tabuSize>MIN_TABU_SIZE){
                    tabuSize--;
                }
                iterationWithoutImprovement++;
            }
            update(iter, iterationWithoutImprovement);
            if(iterationWithoutImprovement >= MAX_NO_IMPROVEMENT){
                break;
            }
        }
        return bestSolution;
    }
    private Solution findBestMove(Solution currentSolution) {
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(searchSize);
            LSChromosomes = Collections.synchronizedList(new ArrayList<>());
//            for(int i = 0; i < searchSize; i++) {
//                tasks.add(()->{
//                    new EnhanceLocalSearch(this,currentSolution,tabuList).run();
//                    return null;
//                });
//            }
            try{
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSChromosomes;
                synchronized (lsChromosomes) {
                    sortSolutions(lsChromosomes);
                    for(Solution solution : lsChromosomes) {
                        if(solution.getFitness()<bestSolution.getFitness()){
                            return solution;
                        }else if(!tabuList.contains(solution.getMove())){
                            return solution;
                        }
                    }
                    return lsChromosomes.get(0);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void sortSolutions(List<Solution> population) {
        population.sort(Comparator.comparingDouble(Solution::getFitness));
    }
    private void update(int iteration, int iterationWithoutImprovement)
    {
        System.out.println("Time at: " + getTotalTimeSeconds() + " CPU Timer " + String.format("%.3f", getTotalCPUTimeSeconds()) + " seconds Iteration " + iteration + " IterationWithoutImprovement "+ iterationWithoutImprovement + " Best fitness: " + bestSolution.getFitness());
    }
    public void startTimer() {
        this.startCpuTime = osBean.getProcessCpuTime();
        this.startTime = System.currentTimeMillis();
    }
    public double getTotalCPUTimeSeconds() {
        long endCpuTime = osBean.getProcessCpuTime();
        return (endCpuTime - startCpuTime) / 1_000_000_000.0;
    }

    public double getTotalTimeSeconds() {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1_000.0;
    }
    public List<Solution> getLSChromosomes() {
        return LSChromosomes;
    }
}
