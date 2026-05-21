/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents docking station bundles (mapped from dressing column in laptops.txt).
 * AI Usage: Generated Dressing enum to keep matching interface with the exemplar structure.
 */
public enum Dressing {
    NA;

    /**
     * Provides a visually pleasing representation of the Dressing constants.
     * @return String representation of the bundle.
     */
    @Override
    public String toString() {
        return switch (this) {
            case NA -> "No Docking Station Bundle Preference";
        };
    }
}
