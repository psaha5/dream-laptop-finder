import java.util.ArrayList;

/**
 * Author: Partha Saha
 * GitHub: https://github.com/yourusername/dream-laptop-finder
 *
 * Represents the user's dream laptop search template.
 */
public class DreamLaptop extends Laptop {

    public DreamLaptop(Brand brand, LaptopType type,
                       double price, int ram,
                       int storage, boolean touchscreen,
                       String gpu, ArrayList<String> tags,
                       String description) {

        super(brand, type, price, ram, storage,
                touchscreen, gpu, tags, description);
    }
/**
     * Checks whether a laptop matches the user's dream laptop.
     */
    public boolean matches(Laptop laptop) {

        if (getBrand() != null && laptop.getBrand() != getBrand()) {
            return false;
        }

        if (getType() != null && laptop.getType() != getType()) {
            return false;
        }

        if (getPrice() > 0 && laptop.getPrice() > getPrice()) {
            return false;
            }

        if (getRam() > 0 && laptop.getRam() < getRam()) {
            return false;
        }

        if (getStorage() > 0 && laptop.getStorage() < getStorage()) {
            return false;
        }

        return true;
    }

    /**
     * Returns search filter information.
     */
    public String getInfo() {

        return "Dream Laptop Filters:\n" +
                "Brand: " + getBrand() +
                "\nType: " + getType() +
                "\nMax Price: $" + getPrice() +
                "\nMinimum RAM: " + getRam() + "GB" +
                "\nMinimum Storage: " + getStorage() + "GB";
    }
    }