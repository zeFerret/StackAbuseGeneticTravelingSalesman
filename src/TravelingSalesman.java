import java.util.*;

public class TravelingSalesman {
    // Generation size is the number of genomes/individuals in each generation/population
    private final int generationSize;
    // Genome size is the length of the genome `ArrayList`, which will be equal to the  `numberOfCities-1`.
    private final int genomeSize;
    private final int numberOfCities;
    //  Reproduction size is the number of genomes who'll be selected to reproduce to make the next generation.
    private final int reproductionSize;
    // Max iteration is the maximum number of generations the program will evolve before terminating.
    private final int maxIterations;
    // Mutation rate refers to the frequency of mutations when creating a new generation.
    private final float mutationRate;
    // Tournament size is the size of the tournament for tournament selection.
    private final int tournamentSize;
    // Selection type will determine the type of selection we're using
    private final SelectionType selectionType;
    // Travel prices is a matrix of the prices of travel between each two cities.
    private final int[][] travelPrices;
    // Starting city is the index of the starting city.
    private final int startingCity;
    // Target fitness is the fitness the best genome has to reach according to the objective function to terminate early.
    private final int targetFitness;

    public TravelingSalesman(int numberOfCities, SelectionType selectionType, int[][] travelPrices, int startingCity, int targetFitness) {
        this.numberOfCities = numberOfCities;
        this.genomeSize = numberOfCities - 1;
        this.selectionType = selectionType;
        this.travelPrices = travelPrices;
        this.startingCity = startingCity;
        this.targetFitness = targetFitness;

        generationSize = 5000;
        reproductionSize = 200;
        maxIterations = 1000;
        mutationRate = 0.1f;
        tournamentSize = 40;
    }

    public List<SalesmanGenome> initialPopulation() {
        List<SalesmanGenome> population = new ArrayList<>();
        for (int i = 0; i < generationSize; i++) {
            population.add(new SalesmanGenome(numberOfCities, travelPrices, startingCity));
        }
        return population;
    }

    // We select reproductionSize genomes based on the method
    // predefined in the attribute selectionType
    public List<SalesmanGenome> selection(List<SalesmanGenome> population) {
        List<SalesmanGenome> selected = new ArrayList<>();
        SalesmanGenome winner;
        for (int i = 0; i < reproductionSize; i++) {
            if (selectionType == SelectionType.ROULETTE) {
                selected.add(rouletteSelection(population));
            } else if (selectionType == SelectionType.TOURNAMENT) {
                selected.add(tournamentSelection(population));
            }
        }

        return selected;
    }

    public SalesmanGenome rouletteSelection(List<SalesmanGenome> population) {
        int totalFitness = population.stream().map(SalesmanGenome::getFitness).mapToInt(Integer::intValue).sum();
        // We pick a random value - a point on our roulette wheel
        Random random = new Random();
        int selectedValue = random.nextInt(totalFitness);
        // Because we're doing minimization, we need to use reciprocal
        // value so the probability of selecting a genome would be
        // inversely proportional to its fitness - the smaller the fitness
        // the higher the probability
        float recValue = (float) 1 / selectedValue;
        // We add up values until we reach out recValue, and we pick the
        // genome that crossed the threshold
        float currentSum = 0;
        for (SalesmanGenome genome : population) {
            currentSum += (float) 1 / genome.getFitness();
            if (currentSum >= recValue) {
                return genome;
            }
        }
        // In case the return didn't happen in the loop above, we just
        // select at random
        int selectRandom = random.nextInt(generationSize);
        return population.get(selectRandom);
    }

    // A helper function to pick n random elements from the population
    // so we could enter them into a tournament
    public static <E> List<E> pickNRandomElements(List<E> list, int n) {
        Random r = new Random();
        int length = list.size();

        if (length < n) return null;

        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    // A simple implementation of the deterministic tournament - the best genome
    // always wins
    public SalesmanGenome tournamentSelection(List<SalesmanGenome> population) {
        List<SalesmanGenome> selected = pickNRandomElements(population, tournamentSize);
        return Collections.min(selected);
    }

    public SalesmanGenome mutate(SalesmanGenome salesman) {
        Random random = new Random();
        float mutate = random.nextFloat();
        if (mutate < mutationRate) {
            List<Integer> genome = salesman.getGenome();
            Collections.swap(genome, random.nextInt(genomeSize), random.nextInt(genomeSize));
            return new SalesmanGenome(genome, numberOfCities, travelPrices, startingCity);
        }
        return salesman;
    }

    public List<SalesmanGenome> createGeneration(List<SalesmanGenome> population) {
        List<SalesmanGenome> generation = new ArrayList<>();
        int currentGenerationSize = 0;
        while (currentGenerationSize < generationSize) {
            List<SalesmanGenome> parents = pickNRandomElements(population, 2);
            List<SalesmanGenome> children = crossover(parents);
            children.set(0, mutate(children.get(0)));
            children.set(1, mutate(children.get(1)));
            generation.addAll(children);
            currentGenerationSize += 2;
        }
        return generation;
    }

    public List<SalesmanGenome> crossover(List<SalesmanGenome> parents) {
        // housekeeping
        Random random = new Random();
        int breakpoint = random.nextInt(genomeSize);
        List<SalesmanGenome> children = new ArrayList<>();

        // Copy parental genomes - we copy so we wouldn't modify in case they were
        // chosen to participate in crossover multiple times
        List<Integer> parent1Genome = new ArrayList<>(parents.get(0).getGenome());
        List<Integer> parent2Genome = new ArrayList<>(parents.get(1).getGenome());

        // Creating child 1
        for (int i = 0; i < breakpoint; i++) {
            int newVal;
            newVal = parent2Genome.get(i);
            Collections.swap(parent1Genome, parent1Genome.indexOf(newVal), i);
        }
        children.add(new SalesmanGenome(parent1Genome, numberOfCities, travelPrices, startingCity));
        parent1Genome = parents.get(0).getGenome(); // Resetting the edited parent

        // Creating child 2
        for (int i = breakpoint; i < genomeSize; i++) {
            int newVal = parent1Genome.get(i);
            Collections.swap(parent2Genome, parent2Genome.indexOf(newVal), i);
        }
        children.add(new SalesmanGenome(parent2Genome, numberOfCities, travelPrices, startingCity));

        return children;
    }

    public SalesmanGenome optimize() {
        List<SalesmanGenome> population = initialPopulation();
        SalesmanGenome globalBestGenome = population.get(0);
        for (int i = 0; i < maxIterations; i++) {
            List<SalesmanGenome> selected = selection(population);
            population = createGeneration(selected);
            globalBestGenome = Collections.min(population);
            if (globalBestGenome.getFitness() < targetFitness)
                break;
        }
        return globalBestGenome;
    }

    public void printGeneration(List<SalesmanGenome> generation) {
        for (SalesmanGenome genome : generation) {
            System.out.println(genome);
        }
    }
}
