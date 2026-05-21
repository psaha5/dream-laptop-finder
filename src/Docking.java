/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents docking station bundles.
 */
public enum Docking {
    NA;

    @Override
    public String toString() {
        return switch (this) {
            case NA -> "No Docking Station Bundle Preference";
        };
    }
}
