package tech.pedroduarte.gourmand.features.search.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import tech.pedroduarte.gourmand.features.search.application.SearchService;
import tech.pedroduarte.gourmand.features.search.application.dto.SearchResult;
import tech.pedroduarte.gourmand.features.search.domain.Cuisine;
import tech.pedroduarte.gourmand.features.search.domain.SearchCriteria;
import tech.pedroduarte.gourmand.features.search.domain.persistence.RestaurantRepository;
import tech.pedroduarte.gourmand.features.search.domain.service.RestaurantSearchDomainService;
import tech.pedroduarte.gourmand.features.search.infrastructure.persistence.CsvDataSource;
import tech.pedroduarte.gourmand.features.search.infrastructure.persistence.CsvRestaurantRepository;
import tech.pedroduarte.gourmand.shared.domain.Distance;
import tech.pedroduarte.gourmand.shared.domain.Price;
import tech.pedroduarte.gourmand.shared.domain.Rating;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "search",
        description = "Searches for local restaurants based on given criteria",
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class SearchCommand implements Callable<Integer> {

    private final static Logger logger = LoggerFactory.getLogger(SearchCommand.class);

    private SearchService searchService;

    @Spec
    private CommandSpec spec;

    @Option(
            names = {"-n", "--name"},
            description = "Restaurant name (partial match is supported)",
            paramLabel = "NAME"
    )
    private String name;

    @Option(
            names = {"-r", "--rating"},
            description = "Minimum customer rating (1-5 stars)",
            paramLabel = "RATING",
            converter = RatingConverter.class
    )
    private Rating minRating;

    @Option(
            names = {"-d", "--distance"},
            description = "Maximum distance in miles (1-10)",
            paramLabel = "DISTANCE",
            converter = DistanceConverter.class
    )
    private Distance maxDistance;

    @Option(
            names = {"-p", "--price"},
            description = "Maximum price per person in dollars (10-50)",
            paramLabel = "PRICE",
            converter = PriceConverter.class
    )
    private Price maxPrice;

    @Option(
            names = {"-c", "--cuisine"},
            description = "Cuisine type (e.g., Chinese, Italian)",
            paramLabel = "CUISINE",
            converter = CuisineTypeConverter.class
    )
    private Cuisine cuisine;

    @Option(
            names = {"--data-dir"},
            description = "Directory containing data files",
            type = Path.class
    )
    private Path dataDirectory;

    @Option(
            names = {"-v", "--verbose"},
            description = "Prints additional information"
    )
    private boolean verbose;

    @Override
    public Integer call() {
        try {
            init();

            SearchCriteria criteria = SearchCriteria.builder()
                    .name(name)
                    .minRating(minRating)
                    .maxDistance(maxDistance)
                    .maxPrice(maxPrice)
                    .cuisine(cuisine)
                    .build();

            // Print friendly message informing criteria
            spec.commandLine().getOut().printf("Searching for restaurants with criteria: %n%n%s", criteria.formattedCriteria());

            List<SearchResult> results = searchService.search(criteria);
            displayResults(results);

            return 0;
        } catch (Exception e) {
            spec.commandLine().getErr().printf("Error: %s%n", e.getMessage());
            logger.error("Error executing search", e);
            return 1;
        }
    }

    private void init() {
        // Service and repository initialization are deferred until the command is called due to the data directory option.
        // In a real application, this would be done at startup (with a real database).
        final CsvDataSource csvDataSource;
        if (dataDirectory != null) {
            csvDataSource = CsvDataSource.fromDirectory(dataDirectory);
        } else {
            csvDataSource = CsvDataSource.fromResources();
        }

        RestaurantRepository repository = new CsvRestaurantRepository(csvDataSource);
        RestaurantSearchDomainService domainService = new RestaurantSearchDomainService();
        this.searchService = new SearchService(repository, domainService);
    }

    private void displayResults(List<SearchResult> results) {
        // Get the output writer from the command spec
        PrintWriter writer = spec.commandLine().getOut();

        if (results.isEmpty()) {
            writer.println("\nNo restaurants found matching your criteria.");
            return;
        }

        writer.println("\nFound " + results.size() + " matching restaurants:\n");

        // Calculate column widths
        int nameWidth = Math.max(20,
                results.stream()
                        .mapToInt(r -> r.getName().length())
                        .max()
                        .orElse(20));

        // Print header
        String headerFormat = "%-" + nameWidth + "s  %-7s  %-8s  %-6s  %-15s%n";
        writer.printf(headerFormat, "NAME", "RATING", "DISTANCE", "PRICE", "CUISINE");
        writer.println("-".repeat(nameWidth + 45));

        // Print each result
        String rowFormat = "%-" + nameWidth + "s  %d        %.1f mi    $%-5.2f  %s%n";
        results.forEach(result ->
                writer.printf(
                        rowFormat,
                        result.getName(),
                        result.getRating(),
                        result.getDistance(),
                        result.getPrice(),
                        result.getCuisine()
                )
        );

        writer.flush();
    }

    static class RatingConverter implements CommandLine.ITypeConverter<Rating> {
        @Override
        public Rating convert(String value) {
            int rating;
            try {
                rating = Integer.parseInt(value);
                return new Rating(rating);
            } catch (IllegalArgumentException e) {
                throw new CommandLine.TypeConversionException(
                        e.getMessage()
                );
            }
        }
    }

    static class DistanceConverter implements CommandLine.ITypeConverter<Distance> {
        @Override
        public Distance convert(String value) {
            double distance;
            try {
                distance = Double.parseDouble(value);
                return new Distance(distance);
            } catch (IllegalArgumentException e) {
                throw new CommandLine.TypeConversionException(
                        e.getMessage()
                );
            }
        }
    }

    static class PriceConverter implements CommandLine.ITypeConverter<Price> {
        @Override
        public Price convert(String value) {
            double price;
            try {
                price = Double.parseDouble(value);
                return new Price(price);
            } catch (IllegalArgumentException e) {
                throw new CommandLine.TypeConversionException(
                        e.getMessage()
                );
            }

        }
    }

    static class CuisineTypeConverter implements CommandLine.ITypeConverter<Cuisine> {
        @Override
        public Cuisine convert(String value) {
            try {
                return new Cuisine(value);
            } catch (IllegalArgumentException e) {
                throw new CommandLine.TypeConversionException(
                        e.getMessage()
                );
            }
        }
    }

}