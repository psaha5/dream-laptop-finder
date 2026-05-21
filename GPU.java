/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents the graphics card or processor configurations.
 * AI Usage: Generated GPU enum matching specific graphics cards and processors in laptops.txt.
 */
public enum GPU {
    RTX4060,
    INTEGRATED,
    M3,
    RTX3050,
    RTX4050,
    RTX4080,
    M2,
    RTX4070,
    RTX4090,
    NA;

    /**
     * Provides a visually pleasing representation of the GPU constants.
     * @return String representation of the graphics processor.
     */
    @Override
    public String toString() {
        return switch (this) {
            case RTX4060 -> "NVIDIA GeForce RTX 4060";
            case INTEGRATED -> "Integrated Graphics";
            case M3 -> "Apple M3 Chip";
            case RTX3050 -> "NVIDIA GeForce RTX 3050";
            case RTX4050 -> "NVIDIA GeForce RTX 4050";
            case RTX4080 -> "NVIDIA GeForce RTX 4080";
            case M2 -> "Apple M2 Chip";
            case RTX4070 -> "NVIDIA GeForce RTX 4070";
            case RTX4090 -> "NVIDIA GeForce RTX 4090";
            case NA -> "No Preference";
        };
    }
}
