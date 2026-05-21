/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents the laptop category type (mapped from burger/salad raw data).
 * AI Usage: Created LaptopType mapping to match CSV categories (BURGER = Gaming, SALAD = Ultrabook).
 */
public enum LaptopType {
    BURGER,
    SALAD;

    /**
     * Provides a visually pleasing representation of the LaptopType constants.
     * @return String representation of the laptop category.
     */
    @Override
    public String toString() {
        return switch (this) {
            case BURGER -> "Gaming Laptop";
            case SALAD -> "Ultrabook/Office Laptop";
        };
    }
}
