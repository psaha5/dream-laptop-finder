import java.util.*;

/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Manages the collection of Laptop items and matches them against user preferences.
 * AI Usage: Adapted Menu registry structure to LaptopRegistry, implementing collection querying and match filtering.
 */
public class LaptopRegistry {
    private final Set<Laptop> laptops = new LinkedHashSet<>();

    /**
     * Adds a laptop to the registry.
     * @param laptop the laptop to add.
     */
    public void addLaptop(Laptop laptop) {
        this.laptops.add(laptop);
    }

    /**
     * Scans all laptops to find all unique values for a filter category.
     * Appends "I don't mind" at the end of the list.
     * @param filter the filter category to scan.
     * @return distinct set of feature values.
     */
    public Set<Object> getAllFeatureValues(Filter filter) {
        Set<Object> allSubtypes = new LinkedHashSet<>();
        for (Laptop laptop : laptops) {
            if (laptop.getDreamLaptop().getAllFilters().containsKey(filter)) {
                var values = laptop.getDreamLaptop().getFilter(filter);
                if (values instanceof Collection<?>) {
                    allSubtypes.addAll((Collection<?>) values);
                } else {
                    allSubtypes.add(laptop.getDreamLaptop().getFilter(filter));
                }
            }
        }
        allSubtypes.add("I don't mind");
        return allSubtypes;
    }

    /**
     * Searches the registry for laptops matching the user's dream template and price limits.
     * @param dreamLaptop search constraints.
     * @return matching list of laptops.
     */
    public List<Laptop> findMatch(DreamLaptop dreamLaptop) {
        List<Laptop> matching = new ArrayList<>();
        for (Laptop laptop : laptops) {
            if (!laptop.getDreamLaptop().matches(dreamLaptop)) {
                continue;
            }
            if (laptop.getPrice() < dreamLaptop.getMinPrice() || laptop.getPrice() > dreamLaptop.getMaxPrice()) {
                continue;
            }
            matching.add(laptop);
        }
        return matching;
    }
}
