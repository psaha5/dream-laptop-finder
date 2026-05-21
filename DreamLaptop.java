import java.util.*;

/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents the search template containing user filters for a dream laptop.
 * AI Usage: Adapted DreamMenuItem structure to DreamLaptop, using Map aggregation and generic matching.
 */
public class DreamLaptop {
    private final Map<Filter, Object> filterMap;
    private final double minPrice;
    private final double maxPrice;

    /**
     * Constructs a DreamLaptop search template with a specified price range.
     * @param filterMap the map of hardware filters.
     * @param minPrice the minimum price constraint.
     * @param maxPrice the maximum price constraint.
     */
    public DreamLaptop(Map<Filter, Object> filterMap, double minPrice, double maxPrice) {
        this.filterMap = new LinkedHashMap<>(filterMap);
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    /**
     * Constructs a DreamLaptop search template without price constraints.
     * @param filterMap the map of hardware filters.
     */
    public DreamLaptop(Map<Filter, Object> filterMap) {
        this.filterMap = new LinkedHashMap<>(filterMap);
        this.minPrice = -1;
        this.maxPrice = -1;
    }

    /**
     * Gets a copy of all search filters.
     * @return map of search filters.
     */
    public Map<Filter, Object> getAllFilters() {
        return new LinkedHashMap<>(filterMap);
    }

    /**
     * Gets the value of a specific search filter.
     * @param key the filter key to lookup.
     * @return the value associated with the filter.
     */
    public Object getFilter(Filter key) {
        return getAllFilters().get(key);
    }

    /**
     * Gets the minimum price limit.
     * @return minimum price limit.
     */
    public double getMinPrice() {
        return minPrice;
    }

    /**
     * Gets the maximum price limit.
     * @return maximum price limit.
     */
    public double getMaxPrice() {
        return maxPrice;
    }

    /**
     * Generates a visually pleasing description of the search criteria.
     * @return search template details.
     */
    public String getInfo() {
        StringBuilder description = new StringBuilder();
        StringBuilder extras = new StringBuilder("\nExtras: ");
        for (Filter key : filterMap.keySet()) {
            if (getFilter(key) instanceof Collection<?>) {
                description.append("\n").append(key).append(":");
                for (Object x : ((Collection<?>) getFilter(key)).toArray()) {
                    description.append("\n").append(" --> ").append(x);
                }
            } else if (getFilter(key).equals(true)) {
                extras.append(key).append(", ");
            } else if (!getFilter(key).equals(false)) {
                description.append("\n").append(key).append(": ").append(getFilter(key));
            }
        }
        if (extras.length() > 9) {
            description.append(extras.substring(0, extras.length() - 2));
        }
        return description.toString();
    }

    /**
     * Performs a generic comparison between this search template and another laptop's properties.
     * @param dreamLaptop the target properties to match against.
     * @return true if all set filters match, false otherwise.
     */
    public boolean matches(DreamLaptop dreamLaptop) {
        for (Filter key : dreamLaptop.getAllFilters().keySet()) {
            if (this.getAllFilters().containsKey(key)) {
                if (getFilter(key) instanceof Collection<?> && dreamLaptop.getFilter(key) instanceof Collection<?>) {
                    boolean hasMatch = false;
                    for (Object sObj : (Collection<?>) dreamLaptop.getFilter(key)) {
                        String s = sObj.toString().trim().toLowerCase();
                        for (Object dbObj : (Collection<?>) getFilter(key)) {
                            String dbVal = dbObj.toString().trim().toLowerCase();
                            if (dbVal.contains(s) || s.contains(dbVal)) {
                                hasMatch = true;
                                break;
                            }
                        }
                        if (hasMatch) break;
                    }
                    if (!hasMatch) return false;
                } else {
                    if (!this.getFilter(key).equals(dreamLaptop.getFilter(key))) return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}