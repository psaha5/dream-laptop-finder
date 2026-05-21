/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents the search filters corresponding to the database attributes.
 */
public enum Filter {
    TYPE,
    BRAND,
    GPU,
    TOUCHSCREEN,
    BACKLIT_KEYBOARD,
    NUMERIC_KEYPAD,
    PORTABILITY,
    DOCKING,
    FEATURES,
    SOFTWARE;

    @Override
    public String toString() {
        return switch (this) {
            case TYPE -> "Laptop Category";
            case BRAND -> "Laptop Brand";
            case GPU -> "Graphics Card (GPU)";
            case TOUCHSCREEN -> "Touchscreen Support";
            case BACKLIT_KEYBOARD -> "Backlit Keyboard";
            case NUMERIC_KEYPAD -> "Dedicated Numeric Keypad";
            case PORTABILITY -> "Ultra-Portable Design";
            case DOCKING -> "Docking Station Bundle";
            case FEATURES -> "Hardware Features";
            case SOFTWARE -> "Pre-installed Software";
        };
    }
}
