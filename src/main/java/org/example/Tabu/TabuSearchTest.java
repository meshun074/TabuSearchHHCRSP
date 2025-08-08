package org.example.Tabu;

import com.sun.management.OperatingSystemMXBean;
import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TabuSearchTest {
    private final OperatingSystemMXBean osBean;
    private Solution currentSolution;
    private Solution bestSolution;
    private static final int  MAX_ITERATION = 2000;
    private final int MAX_TABU_SIZE;
    private final int MAX_NO_IMPROVEMENT;
    private final int searchSize;
    private final int LSRate;
    private final int patientCount;
    private long startCpuTime;
    private long startTime;
    private List<Solution> LSChromosomes;
    private final Queue<Integer> tabuList;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public TabuSearchTest(Solution solution, InstancesClass data) {
        currentSolution = solution;
        bestSolution = currentSolution;
        Patient[] allPatients = data.getPatients();
        patientCount = allPatients.length;
        LSChromosomes  = new ArrayList<>(patientCount);
        this.osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        this.MAX_NO_IMPROVEMENT = patientCount/2;
//        this.searchSize = Math.max(patientCount / 5, 50);
        this.searchSize = 100;
        this.LSRate = MAX_NO_IMPROVEMENT/5;
        EvaluationFunction.initialize(data);
        this.MAX_TABU_SIZE = patientCount/4;
        tabuList = new LinkedList<>();
        //LocalSearch.initialize(data);
        LocalSearchRelocate.initialize(data);
        LocalSearchSwap.initialize(data);
        IntensificationSearch.initialize(data);
    }

    public Solution Start(){
        startTimer();
        int iterationWithoutImprovement = 0;
        double bestFitness = bestSolution.getFitness();
        for(int iter = 1; iter <= MAX_ITERATION; iter++) {
            currentSolution = findBestMove(currentSolution);
            int currentMove = currentSolution.getMove();
            tabuList.remove(currentMove);
            tabuList.add(currentMove);
            if(tabuList.size() > MAX_TABU_SIZE){
                tabuList.poll();
            }
            double currentFitness = currentSolution.getFitness();
            if(currentFitness < bestFitness){
                bestSolution = currentSolution;
                bestFitness = currentFitness;
                iterationWithoutImprovement =0;
            }else {
                if(iterationWithoutImprovement%LSRate == 0){
                    currentSolution = localSearch(currentSolution,0);
                    currentFitness = currentSolution.getFitness();
                    if(currentFitness < bestFitness){
                        bestSolution = currentSolution;
                        bestFitness = currentFitness;
                        iterationWithoutImprovement =0;
                    }else
                        iterationWithoutImprovement++;
                }else
                    iterationWithoutImprovement++;
            }
            update(iter, iterationWithoutImprovement);
            if(iterationWithoutImprovement >= MAX_NO_IMPROVEMENT){
                break;
            }
//            System.exit(1);
        }
        return bestSolution;
    }


    @FunctionalInterface
    private interface LocalSearchStrategy {
        Solution execute(Solution solution);
    }

    private LocalSearchStrategy getLocalSearch(int ls) {
        return switch (ls) {
            case 0 -> this::relocateLocalSearch;
            default -> this::swapLocalSearch;
        };
    }

    private Solution relocateLocalSearch(Solution currentSolution) {
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(patientCount);
            LSChromosomes = Collections.synchronizedList(new ArrayList<>());
            for (int i = 0; i < patientCount; i++) {
                int finalI = i;
                tasks.add(() -> {
                    new LocalSearchRelocate(this, currentSolution,finalI).run();
                    return null;
                });
            }

            try {
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSChromosomes;
                synchronized (lsChromosomes) {
                    sortSolutions(lsChromosomes);
                    for (Solution solution : lsChromosomes) {
                        if (solution.getFitness() < bestSolution.getFitness()) {
                            return solution;
                        } else if (!tabuList.contains(solution.getMove())) {
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
    private Solution swapLocalSearch(Solution currentSolution) {
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(patientCount);
            LSChromosomes = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < patientCount; i++) {
                int finalI = i;
                tasks.add(() -> {
                    new LocalSearchSwap(this, currentSolution, finalI).run();
                    return null;
                });
            }


            try {
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSChromosomes;
                synchronized (lsChromosomes) {
                    sortSolutions(lsChromosomes);
                    for (Solution solution : lsChromosomes) {
                        if (solution.getFitness() < bestSolution.getFitness()) {
                            return solution;
                        } else if (!tabuList.contains(solution.getMove())) {
                            return solution;
                        }
                    }
                    return lsChromosomes.get(0);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
//        LSChromosomes = Collections.synchronizedList(new ArrayList<>());
//        for (int i = 0; i < patientCount; i++) {
//            int finalI = i;
//            LocalSearchSwap ls = new LocalSearchSwap(this, currentSolution, finalI);
//            LSChromosomes.add(ls.localSearch());
//        }
//        List<Solution> lsChromosomes = LSChromosomes;
//        sortSolutions(lsChromosomes);
//        for (Solution solution : lsChromosomes) {
//            if (solution.getFitness() < bestSolution.getFitness()) {
//                return solution;
//            } else if (!tabuList.contains(solution.getMove())) {
//                return solution;
//            }
//        }
//        return lsChromosomes.get(0);
    }

    private Solution findBestMove2(Solution currentSolution) {
        Random random = new Random();
        int firstSearch = random.nextInt(2);
        double bestFitness = bestSolution.getFitness();
        Solution best = currentSolution;
        LocalSearchStrategy localSearchStrategy = getLocalSearch(firstSearch);
        do{
            if(currentSolution.getFitness() < bestFitness){
                bestFitness = currentSolution.getFitness();
                best = currentSolution;
            }
            currentSolution = localSearchStrategy.execute(currentSolution);
        }
        while (currentSolution.getFitness() < bestFitness);
        firstSearch = ++firstSearch % 2;
        localSearchStrategy = getLocalSearch(firstSearch);
        currentSolution = best;
        do{
            if(currentSolution.getFitness() < bestFitness){
                bestFitness = currentSolution.getFitness();
                best = currentSolution;
            }
            currentSolution = localSearchStrategy.execute(currentSolution);
        }
        while (currentSolution.getFitness() < bestFitness);
        return best;
    }
    private Solution findBestMove1(Solution currentSolution) {
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(patientCount);
            LSChromosomes = Collections.synchronizedList(new ArrayList<>());
            for(int i = 0; i < patientCount; i++) {
                int finalI = i;
                tasks.add(()->{
                    new LocalSearchRelocate(this,currentSolution, finalI).run();
                    return null;
                });
                tasks.add(()->{
                    new LocalSearchSwap(this,currentSolution, finalI).run();
                    return null;
                });
            }
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
    private Solution findBestMove(Solution currentSolution) {
        Random random = new Random();
        int firstSearch = random.nextInt(2);
        double bestFitness = bestSolution.getFitness();
        Solution best = currentSolution;
        LocalSearchStrategy localSearchStrategy = getLocalSearch(firstSearch);
        currentSolution = localSearchStrategy.execute(currentSolution);
        if(currentSolution.getFitness() < bestFitness){
            bestFitness = currentSolution.getFitness();
            best = currentSolution;
        }
        firstSearch = ++firstSearch % 2;
        localSearchStrategy = getLocalSearch(firstSearch);
        currentSolution = best;
        currentSolution = localSearchStrategy.execute(currentSolution);
        if(currentSolution.getFitness() < bestFitness){
            bestFitness = currentSolution.getFitness();
            best = currentSolution;
        }
        if(bestFitness < bestSolution.getFitness()){
            return best;
        }
        return currentSolution;
    }
    private Solution localSearch(Solution currentSolution, int iteration) {
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(searchSize);
            LSChromosomes = Collections.synchronizedList(new ArrayList<>());
            for(int i = 0; i < searchSize; i++) {
                tasks.add(()->{
                    new IntensificationSearch(this,currentSolution).run();
                    return null;
                });
            }
            try{
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSChromosomes;
                synchronized (lsChromosomes) {
                    sortSolutions(lsChromosomes);
                    Solution solution = lsChromosomes.get(0);

                    if(solution.getFitness()<bestSolution.getFitness()){
                        return solution;
                    }else if(iteration < 6){
                        iteration++;
                        return localSearch(solution,iteration);
                    }

                    return solution;
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
