/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents the laptop manufacturer brands.
 * AI Usage: Generated Brand enum constants corresponding to manufacturers in laptops.txt.
 */
public enum Brand {
    ASUS,
    DELL,
    HP,
    LENOVO,
    APPLE,
    ACER,
    MSI;

    /**
     * Provides a visually pleasing representation of the Brand constants.
     * @return String representation of the brand.
     */
    @Override
    public String toString() {
        return switch (this) {
            case ASUS -> "ASUS";
            case DELL -> "Dell";
            case HP -> "HP";
            case LENOVO -> "Lenovo";
            case APPLE -> "Apple";
            case ACER -> "Acer";
            case MSI -> "MSI";
        };
    }
}
