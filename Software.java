/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents pre-installed software suites.
 */
public enum Software {
    OFFICE,
    ANTIVIRUS,
    CREATIVE,
    DEVELOPER,
    NA;

    @Override
    public String toString() {
        return switch (this) {
            case OFFICE -> "Microsoft Office Suite Bundle";
            case ANTIVIRUS -> "Norton Antivirus Protection Suite";
            case CREATIVE -> "Adobe Creative Cloud Pro Suite";
            case DEVELOPER -> "JetBrains Developer Pack Bundle";
            case NA -> "No Pre-installed Software Bundle Required";
        };
    }
}
