# Gourmand CLI 🍽️

A command-line tool to help you find the perfect local restaurant for your next meal. Gourmand CLI allows you to search through local restaurants based on multiple criteria including name, rating, distance, price, and cuisine type.

## Features 🌟

- Search restaurants by multiple criteria:
    - Restaurant name (partial matches supported)
    - Customer rating (1-5 stars)
    - Distance (1-10 miles)
    - Price ($10-$50 per person)
    - Cuisine type
- Flexible data source:
    - Use embedded data files
    - Load data from custom directory
- Smart result ranking:
    - Sorted by distance
    - Higher ratings prioritized
    - Lower prices preferred
- Clean, formatted output
- Comprehensive error handling
- Extensive input validation

## Requirements 📋

- Java 17 or higher
- Gradle 8.0 or higher (wrapper included)

## Installation 🚀

1. Clone the repository:
```bash
git clone https://github.com/yourusername/gourmand-cli.git
cd gourmand-cli
```

2. Build the project:
```bash
./gradlew clean build
```

3. Create executable JAR:
```bash
./gradlew shadowJar
```

The executable JAR will be created at `build/libs/gourmand.jar`

## Usage 💡

### Basic Command Structure

```bash
java -jar gourmand.jar search [OPTIONS]
```

### Search Options

```
-c, --cuisine=CUISINE     Cuisine type (e.g., Chinese, Italian)
-d, --distance=DISTANCE   Maximum distance in miles (1-10)
    --data-dir=<dataDirectory>
                          Directory containing data files
-h, --help                Show this help message and exit.
-n, --name=NAME           Restaurant name (partial match is supported)
-p, --price=PRICE         Maximum price per person in dollars (10-50)
-r, --rating=RATING       Minimum customer rating (1-5 stars)
-v, --verbose             Prints additional information
-V, --version             Print version information and exit.

```

### Examples

1. Search by name:
```bash
java -jar gourmand.jar search --name "Pizza"
java -jar gourmand.jar search -n "gRovE TABLE"
```

2. Find high-rated restaurants nearby:
```bash
java -jar gourmand.jar search --rating 4 --distance 2
```

3. Search with multiple criteria:
```bash
java -jar gourmand.jar search -c Italian -r 4 -p 25
```

4. Use custom data directory:
```bash
java -jar gourmand.jar search --data-dir /path/to/data --name "Cafe"
```

5. Run with detailed logging:
```bash
java -jar gourmand.jar search --name "Pizza" --verbose
```

## Data Files 📁

### Default Data Location
The application looks for data files in the following locations:
1. Custom directory if specified with `--data-dir`
2. Application resources (default)

### File Format

#### cuisines.csv
```csv
id,name
1,American
2,Chinese
...
```

#### restaurants.csv
```csv
name,customer_rating,distance,price,cuisine_id
Deliciousgenix,4,1,10,11
Herbed Delicious,4,7,20,9
...
```

## Search Algorithm 🔍

### Matching Rules
- **Name**: Partial string match (case-insensitive)
- **Rating**: Equal to or higher than requested
- **Distance**: Equal to or less than requested
- **Price**: Equal to or less than requested
- **Cuisine**: Partial string match (case-insensitive)

### Result Ranking
Results are sorted in the following order:
1. Distance (closest first)
2. Rating (highest first)
3. Price (lowest first)


### Space Complexity
- **Overall**: O(n), where n is the number of restaurants
  - Main data structure: List of restaurants is kept in memory
  - Temporary filtered list: O(m) where m ≤ n
  - Output list: O(1) as it's limited to 5 items
  - Auxiliary space: O(1) for comparison operations

### Time Complexity

#### 1. Data Loading Phase: O(n)

- Each restaurant is read exactly once
- Data is loaded into memory

#### 2. Filtering Phase: O(n)

- Each restaurant is processed exactly once

Individual Operations:
- **Name Matching**: O(k) - String contains operation
- **Rating Matching**: O(1) - Numeric comparison
- **Distance Matching**: O(1) - Numeric comparison
- **Price Matching**: O(1) - Numeric comparison
- **Cuisine Matching**: O(m) - String contains operation

#### 3. Sorting Phase: O(n log n)

- Primary sort (distance): O(1) comparison
- Secondary sort (rating): O(1) comparison
- Tertiary sort (price): O(1) comparison
- Overall sorting: O(n log n) due to Java's TimSort algorithm

#### 3. Result Limitation: O(1)
- Taking top 5 results: O(1)

### Total Complexity
- **Time**: O(n log n)
  - Dominated by the sorting phase
    - In practice, n should be small because of the limited result set
- **Space**: O(n)
  - Linear space for storing filtered results
  - Constant space for output

## Performance Considerations

### Optimizations Implemented
1. **Early Termination**
  - Filtering stops as soon as a criterion fails
  - Stream operations are lazily evaluated

2. **Efficient String Matching**
  - Case-insensitive comparison for names and cuisines
  - No regex for simple string matching

3. **Memory Efficiency**
  - Immutable domain objects
  - No unnecessary object creation during comparisons

### Bottlenecks and Limitations
1. **Sorting Cost**
  - Always O(n log n) even when returning few results
  - Could be optimized for small result sets

2. **String Operations**
  - Name and cuisine matching involve string operations
  - Case sensitivity handling adds overhead

3. **Memory Usage**
  - Full restaurant list kept in memory
  - Could be problematic with very large datasets

## Scale Considerations

### Current Implementation
Suitable for datasets where:
- n < 100,000 restaurants
- Response time < 100ms
- Memory usage < 1GB

### Potential Improvements for Larger Scale
1. **Database Integration**
   ```sql
   SELECT * FROM restaurants
   WHERE distance <= ? 
     AND rating >= ?
     AND price <= ?
     AND LOWER(name) LIKE LOWER(?)
     AND cuisine_id = ?
   ORDER BY distance ASC, rating DESC, price ASC
   LIMIT 5;
   ```

2. **Indexing Strategy**
  - B-tree index on distance (primary sort)
  - Composite index (distance, rating, price)
  - Full-text search index for name

3. **Caching Layer**
   ```java
   public class RestaurantSearchCache {
       private LoadingCache<SearchCriteria, List<Restaurant>> cache;
       
       public RestaurantSearchCache() {
           this.cache = Caffeine.newBuilder()
               .maximumSize(1000)
               .expireAfterWrite(Duration.ofMinutes(10))
               .build(this::executeSearch);
       }
   }
   ```

## Development 👩‍💻

### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tech/pedroduarte/gourmand/
│   │   │       ├── common/
│   │   │       ├── features/
│   │   │       └── shared/
│   │   └── resources/
│   │       └── data/
│   └── test/
│       ├── java/
│       └── resources/
├── build.gradle
└── README.md
```

### Building from Source

1. Clone the repository
2. Run tests:
```bash
./gradlew test
```

3. Build the application:
```bash
./gradlew build
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "tech.pedroduarte.gourmand.features.search.SearchServiceTest"
```

## Design Decisions 🎨

- **DDD, Clean Architecture, and Vertical Slicing**: Separated concerns for better maintainability and testability
- **Custom Search Implementation**: Built custom basic search logic instead of using search engines for simplicity and based on requirements
- **Immutable Objects**: Used immutability for thread safety and reduced bugs
- **Flexible Data Source**: Supports both embedded and external data files for flexibility
- **Command Pattern**: Used Picocli for robust CLI handling
- **Rich Domain Model**: Implemented strong domain types for better validation and type safety
- **CLI instead of REST**: Chose CLI for simplicity and ease of use

## Assumptions 📝

1. Each restaurant offers only one cuisine type
2. Ratings are whole numbers between 1 and 5
3. Distances are in miles, between 1 and 10
4. Prices are in dollars, between \$10 and \$50
5. Restaurant names and cuisines use standard ASCII characters
6. CSV files are UTF-8 encoded
7. Data files are small enough to fit in memory

## Error Handling 🚨

- Invalid input parameters show helpful error messages
- Missing or invalid data files produce clear error messages
- Runtime errors are logged with appropriate detail
- Verbose mode available for detailed error information
