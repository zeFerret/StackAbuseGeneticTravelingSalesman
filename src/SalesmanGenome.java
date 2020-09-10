import java.util.*;

public class SalesmanGenome implements Comparable<SalesmanGenome> {
    // The list with the cities in order in which they should be visited
    // This sequence represents the solution to the problem
    List<Integer> genome;

    // Travel prices are handy to be able to calculate fitness
    int[][] travelPrices;

    // While the starting city doesn't change the solution of the problem,
    // it's handy to just pick one so you could rely on it being the same
    // across genomes
    int startingCity;
    int numberOfCities;
    int fitness;

    // Generates a random salesman
    public SalesmanGenome(int numberOfCities, int[][] travelPrices, int startingCity) {
        this.travelPrices = travelPrices;
        this.startingCity = startingCity;
        this.numberOfCities = numberOfCities;
        genome = randomSalesman();
        fitness = this.calculateFitness();
    }

    // Generates a salesman with a user-defined genome
    public SalesmanGenome(List<Integer> permutationOfCities, int numberOfCities, int[][] travelPrices, int startingCity) {
        genome = permutationOfCities;
        this.travelPrices = travelPrices;
        this.startingCity = startingCity;
        this.numberOfCities = numberOfCities;
        fitness = this.calculateFitness();
    }

    public int calculateFitness() {
        int fitness = 0;
        int currentCity = startingCity;

        // Calculating path cost
        for (int gene : genome) {
            fitness += travelPrices[currentCity][gene];
            currentCity = gene;
        }

        // We have to add going back to the starting city to complete the circle
        // the genome is missing the starting city, and indexing starts at 0, which is why we subtract 2
        fitness += travelPrices[genome.get(numberOfCities - 2)][startingCity];
        return fitness;
    }

    // Generates a random genome
    // Genomes are permutations of the list of cities, except the starting city
    // so we add them all to a list and shuffle
    private List<Integer> randomSalesman() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < numberOfCities; i++) {
            if (i != startingCity)
                result.add(i);
        }
        Collections.shuffle(result);
        return result;
    }

    public List<Integer> getGenome() {
        return genome;
    }

    public int getStartingCity() {
        return startingCity;
    }

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Path: ");
        sb.append(startingCity);
        for (int gene : genome) {
            sb.append(" ");
            sb.append(gene);
        }
        sb.append(" ");
        sb.append(startingCity);
        sb.append("\nLength: ");
        sb.append(this.fitness);
        return sb.toString();
    }


    @Override
    public int compareTo(SalesmanGenome genome) {
        return Integer.compare(this.fitness, genome.getFitness());
    }

}
