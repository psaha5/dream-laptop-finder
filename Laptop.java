import java.text.DecimalFormat;

/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * Represents a laptop domain object in the system.
 * AI Usage: Adapted MenuItem structure to Laptop, utilizing aggregation to store generic search filters.
 */
public class Laptop {
    private final String laptopIdentifier;
    private final String laptopName;
    private final String description;
    private final double price;
    private final DreamLaptop dreamLaptop;

    /**
     * Constructs a standard Laptop item with all properties.
     * @param laptopIdentifier unique identifier of the laptop.
     * @param laptopName the model name.
     * @param price the price of the laptop.
     * @param description text description of the laptop.
     * @param dreamLaptop aggregated DreamLaptop holding its searchable properties.
     */
    public Laptop(String laptopIdentifier, String laptopName, double price, String description, DreamLaptop dreamLaptop) {
        this.laptopIdentifier = laptopIdentifier;
        this.laptopName = laptopName;
        this.price = price;
        this.description = description;
        this.dreamLaptop = dreamLaptop;
    }

    /**
     * Constructs a custom Laptop order from user preferences.
     * @param dreamLaptop the aggregated customer preferences.
     */
    public Laptop(DreamLaptop dreamLaptop) {
        this.laptopIdentifier = "";
        this.laptopName = "CUSTOM LAPTOP BUILD";
        this.price = -1;
        this.description = "Custom build - customized specifications";
        this.dreamLaptop = dreamLaptop;
    }

    /**
     * Gets the laptop's identifier.
     * @return identifier.
     */
    public String getLaptopIdentifier() {
        return laptopIdentifier;
    }

    /**
     * Gets the laptop's name.
     * @return laptop model name.
     */
    public String getLaptopName() {
        return laptopName;
    }

    /**
     * Gets the laptop's description.
     * @return description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the price of the laptop.
     * @return price.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the aggregated search properties.
     * @return aggregated properties.
     */
    public DreamLaptop getDreamLaptop() {
        return dreamLaptop;
    }

    /**
     * Formats and returns the detailed laptop details.
     * @return formatted details block.
     */
    public String getLaptopInformation() {
        DecimalFormat df = new DecimalFormat("0.00");
        String output = "\n*******************************************";
        if (!getLaptopIdentifier().isEmpty()) {
            output += "\n" + this.getLaptopName() + " (" + getLaptopIdentifier() + ")\n" + this.getDescription();
        }
        output += getDreamLaptop().getInfo();
        if (price == -1) {
            return output;
        } else {
            return output + "\nPrice: $" + df.format(this.getPrice());
        }
    }
}
