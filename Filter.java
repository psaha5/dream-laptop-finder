/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents the search filters corresponding to the database attributes.
 * AI Usage: Generated enum constants mapping the course exemplar's Filter structure to a laptop domain.
 */
public enum Filter {
    TYPE,
    BUN,
    MEAT,
    CHEESE,
    PICKLES,
    CUCUMBER,
    TOMATO,
    DRESSING,
    LEAFY_GREENS,
    SAUCE_S;

    /**
     * Provides a visually pleasing representation of the Filter constants.
     * @return String representation of the filter.
     */
    @Override
    public String toString() {
        return switch (this) {
            case TYPE -> "Laptop Category";
            case BUN -> "Laptop Brand";
            case MEAT -> "Graphics Card (GPU)";
            case CHEESE -> "Touchscreen Support";
            case PICKLES -> "Backlit Keyboard";
            case CUCUMBER -> "Dedicated Numeric Keypad";
            case TOMATO -> "Ultra-Portable Design";
            case DRESSING -> "Docking Station Bundle";
            case LEAFY_GREENS -> "Hardware Features";
            case SAUCE_S -> "Pre-installed Software";
        };
    }
}
