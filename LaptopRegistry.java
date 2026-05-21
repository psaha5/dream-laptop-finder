import java.util.*;

/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Manages the collection of Laptop items and matches them against user preferences.
 */
public class LaptopRegistry {
    private final Set<Laptop> laptops = new LinkedHashSet<>();

    public void addLaptop(Laptop laptop) {
        this.laptops.add(laptop);
    }

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

    public List<Laptop> findMatch(DreamLaptop dreamLaptop) {
        List<Laptop> perfectMatches = new ArrayList<>();
        List<Laptop> alternatives = new ArrayList<>();

        for (Laptop laptop : laptops) {
            boolean matchesFilters = laptop.getDreamLaptop().matches(dreamLaptop);
            boolean matchesPrice = true;
            if (dreamLaptop.getMinPrice() >= 0 && laptop.getPrice() < dreamLaptop.getMinPrice()) {
                matchesPrice = false;
            }
            if (dreamLaptop.getMaxPrice() >= 0 && laptop.getPrice() > dreamLaptop.getMaxPrice()) {
                matchesPrice = false;
            }

            if (matchesFilters && matchesPrice) {
                perfectMatches.add(laptop);
            } else {
                alternatives.add(laptop);
            }
        }

        if (perfectMatches.size() >= 4) {
            return perfectMatches;
        }

        final int needed = 4 - perfectMatches.size();
        Map<Laptop, Integer> scores = new HashMap<>();
        for (Laptop laptop : alternatives) {
            scores.put(laptop, calculateMatchScore(laptop, dreamLaptop));
        }

        alternatives.sort((a, b) -> Integer.compare(scores.get(b), scores.get(a)));

        List<Laptop> results = new ArrayList<>(perfectMatches);
        int added = 0;
        for (Laptop laptop : alternatives) {
            if (added >= needed) break;
            results.add(laptop);
            added++;
        }

        return results;
    }

    private int calculateMatchScore(Laptop laptop, DreamLaptop query) {
        int score = 0;
        double price = laptop.getPrice();
        double minPrice = query.getMinPrice();
        double maxPrice = query.getMaxPrice();

        if (minPrice >= 0 && maxPrice >= 0) {
            if (price >= minPrice && price <= maxPrice) {
                score += 25;
            } else {
                double diff = Math.min(Math.abs(price - minPrice), Math.abs(price - maxPrice));
                score += Math.max(0, (int) (25 - (diff / 50.0)));
            }
        }

        DreamLaptop dbDream = laptop.getDreamLaptop();
        for (Filter key : query.getAllFilters().keySet()) {
            if (dbDream.getAllFilters().containsKey(key)) {
                Object queryVal = query.getFilter(key);
                Object dbVal = dbDream.getFilter(key);

                if (queryVal instanceof Collection<?> && dbVal instanceof Collection<?>) {
                    int tagMatches = 0;
                    for (Object qObj : (Collection<?>) queryVal) {
                        String qStr = qObj.toString().trim().toLowerCase();
                        for (Object dObj : (Collection<?>) dbVal) {
                            String dStr = dObj.toString().trim().toLowerCase();
                            if (dStr.contains(qStr) || qStr.contains(dStr)) {
                                tagMatches++;
                            }
                        }
                    }
                    score += tagMatches * 4;
                } else {
                    if (dbVal.equals(queryVal)) {
                        score += switch (key) {
                            case TYPE -> 10;
                            case BRAND -> 8;
                            case GPU -> 7;
                            case TOUCHSCREEN -> 5;
                            case BACKLIT_KEYBOARD -> 5;
                            case NUMERIC_KEYPAD -> 5;
                            case PORTABILITY -> 5;
                            default -> 3;
                        };
                    }
                }
            }
        }
        return score;
    }
}
