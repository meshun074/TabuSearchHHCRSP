package org.example.Tabu;

import com.sun.management.OperatingSystemMXBean;
import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TabuSearchMain {
    private final OperatingSystemMXBean osBean;
    private Solution currentSolution;
    private Solution bestSolution;
    private final Random random;
    private static final int MAX_ITERATION = 300;
    private final int MAX_TABU_SIZE;
    private final int MIN_TABU_SIZE;
    private final int MAX_NO_IMPROVEMENT;
    private final int searchSize;
    private final List<Integer> selectionList = new ArrayList<>();
    private long startCpuTime;
    private long startTime;
    private List<Solution> LSSolution;
    private final Queue<Integer> tabuList;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public TabuSearchMain(Solution solution, InstancesClass data) {
        currentSolution = solution;
        bestSolution = currentSolution;
        Patient[] allPatients = data.getPatients();
        for (int i = 0; i < allPatients.length; i++) {
            selectionList.add(i);
        }
        this.searchSize = 50;
        LSSolution = new ArrayList<>(searchSize);
        this.osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        this.MAX_NO_IMPROVEMENT = allPatients.length / 2;
        EvaluationFunction.initialize(data);
        this.MAX_TABU_SIZE = allPatients.length / 3;
        this.MIN_TABU_SIZE = allPatients.length / 5;
        tabuList = new LinkedList<>();
        LocalSearch.initialize(data);
        LocalSearchPlus.initialize(data);
        LocalSearchInRoute.initialize(data);
        EnhanceLocalSearch.initialize(data);
        LocalSearchBCRC.initialize(data);
        LocalSearchSwap.initialize(data);
        random = new Random();
    }

    public Solution Start() {
        startTimer();
        int iterationWithoutImprovement = 0;
        double bestFitness = bestSolution.getFitness();
        int limit = 2;
        for (int iter = 0; iter < MAX_ITERATION; iter++) {
            int tabuSize = (int) Math.round((MIN_TABU_SIZE + (MAX_TABU_SIZE - MIN_TABU_SIZE) * Math.random()));
            int lsStrategy = random.nextInt(3);
            LocalSearchStrategy localSearchStrategy = getLocalSearch(lsStrategy);
            Solution tempBestSolution = currentSolution;
            double tempBestFitness = tempBestSolution.getFitness();
            int counter = 0;
            do {
                currentSolution = findBestMove(currentSolution);
                double currentFitness = currentSolution.getFitness();
                tabuList.add(currentSolution.getMove());
                if (currentFitness < tempBestFitness) {
                    tempBestSolution = currentSolution;
                    tempBestFitness = currentFitness;
                    if (tabuSize < MAX_TABU_SIZE)
                        tabuSize++;
                } else {
                    if (tabuSize > MIN_TABU_SIZE) {
                        tabuSize--;
                    }
                    lsStrategy = (lsStrategy + 1) % 3;
                    localSearchStrategy = getLocalSearch(lsStrategy);
                    counter++;
                }
                if (tabuList.size() > tabuSize) {
                    tabuList.poll();
                }
//                System.out.println("ls "+lsStrategy);
//                System.out.println(counter+"tabuSize "+limit);

            } while (counter<limit);

            if(tempBestSolution.getFitness()<bestFitness){
                bestFitness = tempBestSolution.getFitness();
                bestSolution = tempBestSolution;
                iterationWithoutImprovement =0;
            }else {
                iterationWithoutImprovement++;
            }
            update(iter, iterationWithoutImprovement);
            if (iterationWithoutImprovement >= MAX_NO_IMPROVEMENT) {
                break;
            }
        }
        return bestSolution;
    }

    private Solution findBestMove1(Solution currentSolution) {

        LSSolution = Collections.synchronizedList(new ArrayList<>());
        double random = Math.random();
//        if(random<0.2){
//            LocalSearchSwap ls =  new LocalSearchSwap(this, currentSolution, tabuList);
//           return ls.localSearch();
//        }else if(random<0.4){
//            LocalSearchInRoute ls =  new LocalSearchInRoute(this, currentSolution,tabuList);
//            return ls.localSearch();
//        }
//        else {
//            LocalSearchBCRC ls =  new LocalSearchBCRC(this, currentSolution,tabuList);
//            return ls.localSearch();
//        }


//        for (int i = 0; i < searchSize; i++) {
//           LocalSearchSwap ls =  new LocalSearchSwap(this, currentSolution);
//           LSSolution.add(ls.localSearch());
//        }
//
//
//        List<Solution> lsChromosomes = LSSolution;
//
//        sortSolutions(lsChromosomes);
//        for (Solution solution : lsChromosomes) {
//            if (solution.getFitness() < bestSolution.getFitness()) {
//                return solution;
//            } else if (!tabuList.contains(solution.getMove())) {
//                return solution;
//            }
//        }
//
//        return lsChromosomes.get(0);
        return null;
    }

    @FunctionalInterface
    private interface LocalSearchStrategy {
        Solution execute();
    }

    private LocalSearchStrategy getLocalSearch(int ls) {
        return switch (ls) {
            case 0 -> this::bcrcLocalSearch;
            default -> this::bcrcSwapLocalSearch;
        };
    }


    private Solution inRouteLocalSearch(){
//        System.out.println("inRouteLocalSearch");
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(searchSize);
            LSSolution = Collections.synchronizedList(new ArrayList<>());

//            Collections.shuffle(selectionList);
            for (int i = 0; i < searchSize; i++) {
                tasks.add(() -> {
                    new LocalSearchInRoute(this, currentSolution).run();
                    return null;
                });
            }

            try {
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSSolution;
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
    private Solution bcrcSwapLocalSearch(){
//        System.out.println("bcrcLocalSearch");
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(searchSize);
            LSSolution = Collections.synchronizedList(new ArrayList<>());

//            Collections.shuffle(selectionList);
            for (int i = 0; i < searchSize; i++) {
//                int patient = selectionList.get(i);
//                tasks.add(() -> {
//                    new LocalSearch(this, currentSolution).run();
//                    return null;
//                });
            }

            try {
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSSolution;
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
    private Solution bcrcLocalSearch(){
//        System.out.println("bcrcLocalSearch");
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(searchSize);
            LSSolution = Collections.synchronizedList(new ArrayList<>());

//            Collections.shuffle(selectionList);
            for (int i = 0; i < searchSize; i++) {
//                int patient = selectionList.get(i);
                tasks.add(() -> {
                    new LocalSearchBCRC(this, currentSolution).run();
                    return null;
                });
            }

            try {
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSSolution;
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
    private Solution swapLocalSearch() {
//        System.out.println("swapLocalSearch");
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(searchSize);
            LSSolution = Collections.synchronizedList(new ArrayList<>());

//            Collections.shuffle(selectionList);
            for (int i = 0; i < searchSize; i++) {
                tasks.add(() -> {
//                    new LocalSearchSwap(this, currentSolution).run();
                    return null;
                });
            }

            try {
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSSolution;
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

    private Solution findBestMove(Solution currentSolution) {
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<Callable<Void>> tasks = new ArrayList<>(searchSize);
            LSSolution = Collections.synchronizedList(new ArrayList<>());
//            for(int i = 0; i < searchSize; i++) {
//                tasks.add(()->{
//                    new LocalSearch(this,currentSolution).run();
//                    return null;
//                });
//            }
//            for(int i = 0; i < searchSize; i++) {
//                tasks.add(()->{
//                    new LocalSearchBCRC(this,currentSolution).run();
//                    return null;
//                });
//            }
            double random = Math.random();
//            Collections.shuffle(selectionList);
//            if (random < -0.04) {
//                for (int i = 0; i < searchSize; i++) {
////                    int patient = selectionList.get(i);
//                    tasks.add(() -> {
//                        new LocalSearchSwap(this, currentSolution).run();
//                        return null;
//                    });
//                }
//            } else if (random < -0.08) {
//                for (int i = 0; i < searchSize; i++) {
////                    int patient = selectionList.get(i);
//                    tasks.add(() -> {
//                        new LocalSearchInRoute(this, currentSolution).run();
//                        return null;
//                    });
//                }
//            } else if (random < -0.15) {
//                for (int i = 0; i < searchSize; i++) {
////                    int patient = selectionList.get(i);
//                    tasks.add(() -> {
//                        new LocalSearch(this, currentSolution).run();
//                        return null;
//                    });
//                }
//            }else {
//                for (int i = 0; i < searchSize; i++) {
////                    int patient = selectionList.get(i);
//                    tasks.add(() -> {
//                        new LocalSearchPlus(this, currentSolution).run();
//                        return null;
//                    });
//                }
//            }
//            for(int i = 0; i < searchSize/2; i++) {
//                tasks.add(()->{
//                    new LocalSearchInRoute(this,currentSolution).run();
//                    return null;
//                });
//            }
//            for(int i = 0; i < searchSize; i++) {
//                tasks.add(()->{
//                    new EnhanceLocalSearch(this,currentSolution,tabuList).run();
//                    return null;
//                });
//            }
            try {
                executor.invokeAll(tasks);
                List<Solution> lsChromosomes = LSSolution;
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

    private void sortSolutions(List<Solution> population) {
        population.sort(Comparator.comparingDouble(Solution::getFitness));
    }

    private void update(int iteration, int iterationWithoutImprovement) {
        System.out.println("Time at: " + getTotalTimeSeconds() + " CPU Timer " + String.format("%.3f", getTotalCPUTimeSeconds()) + " seconds Iteration " + iteration + " IterationWithoutImprovement " + iterationWithoutImprovement + " Best fitness: " + bestSolution.getFitness());
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

    public List<Solution> getLSSolution() {
        return LSSolution;
    }
}
