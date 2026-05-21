/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents the laptop category type (GAMING = Gaming, ULTRABOOK = Ultrabook/Office).
 */
public enum LaptopType {
    GAMING,
    ULTRABOOK;

    @Override
    public String toString() {
        return switch (this) {
            case GAMING -> "Gaming Laptop";
            case ULTRABOOK -> "Ultrabook/Office Laptop";
        };
    }
}
