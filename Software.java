/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents pre-installed software suites (mapped from BBQ, aioli, special, chilli in laptops.txt).
 * AI Usage: Generated Software enum to match raw sauce categories in laptops.txt but display as software suites.
 */
public enum Software {
    BBQ,
    AIOLI,
    SPECIAL,
    CHILLI,
    NA;

    /**
     * Provides a visually pleasing representation of the Software constants.
     * @return String representation of the pre-installed software bundle.
     */
    @Override
    public String toString() {
        return switch (this) {
            case BBQ -> "Microsoft Office Suite Bundle";
            case AIOLI -> "Norton Antivirus Protection Suite";
            case SPECIAL -> "Adobe Creative Cloud Pro Suite";
            case CHILLI -> "JetBrains Developer Pack Bundle";
            case NA -> "No Pre-installed Software Bundle Required";
        };
    }
}
