import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Author: Partha Saha
 * Git Repository: https://github.com/psaha5/dream-laptop-finder.git
 * 
 * The main application controller for searching laptops.
 * AI Usage: Adapted MenuSearcher structure into FindLaptop, implementing file loading, JOptionPane UI prompts, input validation, and file writing.
 */
public class FindLaptop {
    private static final String filePath = "laptops.txt";
    private static final Icon icon = new ImageIcon("./byte_chew_small.png");
    private static LaptopRegistry registry;
    private static final String appName = "Dream Laptop Finder";

    /**
     * Entry point of the application.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        registry = loadRegistry(filePath);
        DreamLaptop dreamLaptop = getFilters();
        processSearchResults(dreamLaptop);
        System.exit(0);
    }

    /**
     * Obtains user search preferences via interactive JOptionPane dialogues.
     * @return the constructed DreamLaptop search template.
     */
    public static DreamLaptop getFilters() {
        Map<Filter, Object> filterMap = new LinkedHashMap<>();
        String[] options = {"Yes", "No", "I don't mind"};

        LaptopType type = (LaptopType) JOptionPane.showInputDialog(
                null, 
                "Which laptop category would you like?", 
                appName, 
                JOptionPane.QUESTION_MESSAGE, 
                icon, 
                LaptopType.values(), 
                LaptopType.BURGER
        );
        if (type == null) System.exit(0);
        filterMap.put(Filter.TYPE, type);

        if (type == LaptopType.BURGER) {
            Object[] allBrands = registry.getAllFeatureValues(Filter.BUN).toArray();
            String brandType = (String) JOptionPane.showInputDialog(
                    null, 
                    "Please select your preferred brand:", 
                    appName, 
                    JOptionPane.QUESTION_MESSAGE, 
                    icon, 
                    allBrands, 
                    ""
            );
            if (brandType == null) System.exit(0);
            if (!brandType.equals(allBrands[allBrands.length - 1])) {
                filterMap.put(Filter.BUN, brandType);
            }

            Set<Software> dreamSoftware = new LinkedHashSet<>();
            int choosingSoftware = 0;
            while (choosingSoftware == 0) {
                Software softChoice = (Software) JOptionPane.showInputDialog(
                        null, 
                        "Please select your preferred pre-installed software bundle:", 
                        appName, 
                        JOptionPane.QUESTION_MESSAGE, 
                        icon, 
                        Software.values(), 
                        Software.BBQ
                );
                if (softChoice == null) System.exit(0);
                if (softChoice.equals(Software.NA)) {
                    dreamSoftware = new LinkedHashSet<>();
                    break;
                } else {
                    dreamSoftware.add(softChoice);
                }
                choosingSoftware = JOptionPane.showConfirmDialog(
                        null, 
                        "Would you like to add another software requirement?", 
                        appName, 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        icon
                );
                if (choosingSoftware == 2) System.exit(0);
            }
            if (!dreamSoftware.isEmpty()) {
                filterMap.put(Filter.SAUCE_S, dreamSoftware);
            }
        }

        if (type == LaptopType.SALAD) {
            Object[] allFeatures = registry.getAllFeatureValues(Filter.LEAFY_GREENS).toArray();
            Set<String> dreamFeatures = new LinkedHashSet<>();
            int choosingFeatures = 0;
            while (choosingFeatures == 0) {
                String feature = (String) JOptionPane.showInputDialog(
                        null, 
                        "Please select your preferred key hardware feature:", 
                        appName, 
                        JOptionPane.QUESTION_MESSAGE, 
                        icon, 
                        allFeatures, 
                        ""
                );
                if (feature == null) System.exit(0);
                if (!feature.equals(allFeatures[allFeatures.length - 1])) {
                    dreamFeatures.add(feature);
                } else {
                    break;
                }
                choosingFeatures = JOptionPane.showConfirmDialog(
                        null, 
                        "Would you like to add another hardware feature requirement?", 
                        appName, 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        icon
                );
                if (choosingFeatures == 2) System.exit(0);
            }
            if (!dreamFeatures.isEmpty()) {
                filterMap.put(Filter.LEAFY_GREENS, dreamFeatures);
            }

            boolean numpad = false;
            int numpadSelection = JOptionPane.showOptionDialog(
                    null, 
                    "Would you like a dedicated numeric keypad?", 
                    appName, 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    icon, 
                    options, 
                    options[0]
            );
            if (numpadSelection == 0) numpad = true;
            if (numpadSelection == -1) System.exit(0);
            if (numpadSelection != 2) {
                filterMap.put(Filter.CUCUMBER, numpad);
            }

            Dressing dressing = (Dressing) JOptionPane.showInputDialog(
                    null, 
                    "Please select your preferred docking station bundle:", 
                    appName, 
                    JOptionPane.QUESTION_MESSAGE, 
                    icon, 
                    Dressing.values(), 
                    Dressing.NA
            );
            if (dressing == null) System.exit(0);
            if (!dressing.equals(Dressing.NA)) {
                filterMap.put(Filter.DRESSING, dressing);
            }
        }

        GPU dreamGPU = (GPU) JOptionPane.showInputDialog(
                null, 
                "Please select your preferred GPU/Processor family:", 
                appName, 
                JOptionPane.QUESTION_MESSAGE, 
                icon, 
                GPU.values(), 
                GPU.RTX4060
        );
        if (dreamGPU == null) System.exit(0);
        if (!dreamGPU.equals(GPU.NA)) {
            filterMap.put(Filter.MEAT, dreamGPU);
        }

        boolean touchscreen = false;
        int touchscreenSelection = JOptionPane.showConfirmDialog(
                null, 
                "Would you like a touchscreen display?", 
                appName, 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                icon
        );
        if (touchscreenSelection == 0) touchscreen = true;
        if (touchscreenSelection == -1) System.exit(0);
        filterMap.put(Filter.CHEESE, touchscreen);

        boolean portable = false;
        int portableSelection = JOptionPane.showOptionDialog(
                null, 
                "Would you like an ultra-portable design?", 
                appName, 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                icon, 
                options, 
                options[0]
        );
        if (portableSelection == 0) portable = true;
        if (portableSelection == -1) System.exit(0);
        if (portableSelection != 2) {
            filterMap.put(Filter.TOMATO, portable);
        }

        boolean backlit = false;
        int backlitSelection = JOptionPane.showOptionDialog(
                null, 
                "Would you like a backlit keyboard?", 
                appName, 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                icon, 
                options, 
                options[0]
        );
        if (backlitSelection == 0) backlit = true;
        if (backlitSelection == -1) System.exit(0);
        if (backlitSelection != 2) {
            filterMap.put(Filter.PICKLES, backlit);
        }

        int minPrice = -1;
        int maxPrice = -1;
        while (minPrice < 0) {
            String userInput = JOptionPane.showInputDialog(
                    null, 
                    "Please enter the lowest price limit ($):", 
                    appName, 
                    JOptionPane.QUESTION_MESSAGE
            );
            if (userInput == null) System.exit(0);
            try {
                minPrice = Integer.parseInt(userInput.trim());
                if (minPrice < 0) {
                    JOptionPane.showMessageDialog(null, "Price must be >= 0.", appName, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.", appName, JOptionPane.ERROR_MESSAGE);
            }
        }
        while (maxPrice < minPrice) {
            String userInput = JOptionPane.showInputDialog(
                    null, 
                    "Please enter the highest price limit ($):", 
                    appName, 
                    JOptionPane.QUESTION_MESSAGE
            );
            if (userInput == null) System.exit(0);
            try {
                maxPrice = Integer.parseInt(userInput.trim());
                if (maxPrice < minPrice) {
                    JOptionPane.showMessageDialog(null, "Price must be >= " + minPrice, appName, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.", appName, JOptionPane.ERROR_MESSAGE);
            }
        }
        return new DreamLaptop(filterMap, minPrice, maxPrice);
    }

    /**
     * Searches and lists the matching items, prompting the user for selection.
     * @param dreamLaptop search filters.
     */
    public static void processSearchResults(DreamLaptop dreamLaptop) {
        List<Laptop> matching = registry.findMatch(dreamLaptop);
        Laptop chosenItem = null;
        if (!matching.isEmpty()) {
            Map<String, Laptop> options = new LinkedHashMap<>();
            StringBuilder infoToShow = new StringBuilder("Number of matches: " + matching.size() + "!! The following laptops meet your criteria: \n");
            for (Laptop match : matching) {
                infoToShow.append(match.getLaptopInformation());
                options.put(match.getLaptopName(), match);
            }
            String choice = (String) JOptionPane.showInputDialog(
                    null, 
                    infoToShow + "\n\nPlease select which laptop you'd like to order:", 
                    appName, 
                    JOptionPane.INFORMATION_MESSAGE, 
                    icon, 
                    options.keySet().toArray(), 
                    ""
            );
            if (choice == null) System.exit(0);
            chosenItem = options.get(choice);
        } else {
            int custom = JOptionPane.showConfirmDialog(
                    null, 
                    "Unfortunately none of our catalog models meet your exact criteria :(\n" +
                    "\tWould you like to place a custom laptop build request?\n\n" +
                    "**Price will be calculated based on specifications at checkout.**", 
                    appName, 
                    JOptionPane.YES_NO_OPTION
            );
            if (custom == 0) {
                chosenItem = new Laptop(dreamLaptop);
            } else {
                System.exit(0);
            }
        }
        submitOrder(getUserContactInfo(), chosenItem);
        JOptionPane.showMessageDialog(
                null, 
                "Thank you! Your laptop order has been submitted.\n" +
                "Our technician will contact you shortly...", 
                appName, 
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Prompts name and validated phone number.
     * @return Customer client record.
     */
    public static Client getUserContactInfo() {
        String name = JOptionPane.showInputDialog(null, "Please enter a name for the order.", appName, JOptionPane.QUESTION_MESSAGE);
        if (name == null) System.exit(0);
        long phoneNumber = 0;
        while (phoneNumber == 0) {
            String userInput = JOptionPane.showInputDialog(null, "Please enter your 10-digit phone number.", appName, JOptionPane.QUESTION_MESSAGE);
            if (userInput == null) System.exit(0);
            try {
                phoneNumber = Long.parseLong(userInput.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid entry. Phone number must be numeric.", appName, JOptionPane.ERROR_MESSAGE);
                continue;
            }
            int length = String.valueOf(phoneNumber).length();
            if (length != 9) {
                phoneNumber = 0;
                JOptionPane.showMessageDialog(null, "Invalid entry. Enter a 10-digit phone number in format 0412 123 345.", appName, JOptionPane.ERROR_MESSAGE);
            }
        }
        return new Client(name, phoneNumber);
    }

    /**
     * Writes order files to disk.
     * @param client customer info.
     * @param laptop laptop ordered.
     */
    public static void submitOrder(Client client, Laptop laptop) {
        String safeName = client.name().replace(" ", "_");
        String filePath = safeName + "_" + laptop.getLaptopIdentifier() + ".txt";
        Path path = Path.of(filePath);
        String lineToWrite = "Order details:\n\t" +
                "Name: " + client.name() +
                " (0" + client.phoneNumber() + ")";
        if (laptop.getLaptopIdentifier().isEmpty()) {
            lineToWrite += "\n\nCUSTOM LAPTOP BUILD...\n" + laptop.getLaptopInformation();
        } else {
            lineToWrite += "\n\tItem: " + laptop.getLaptopName() + " (" + laptop.getLaptopIdentifier() + ")";
        }

        try {
            Files.writeString(path, lineToWrite);
        } catch (IOException io) {
            System.out.println("Order could not be placed. \nError message: " + io.getMessage());
            System.exit(0);
        }
    }

    /**
     * Parses the CSV dataset file.
     * @param filePath path to database.
     * @return populated registry.
     */
    public static LaptopRegistry loadRegistry(String filePath) {
        LaptopRegistry registry = new LaptopRegistry();
        Path path = Path.of(filePath);
        List<String> fileContents = null;
        try {
            fileContents = Files.readAllLines(path);
        } catch (IOException io) {
            System.out.println("File could not be found");
            System.exit(0);
        }

        for (int i = 1; i < fileContents.size(); i++) {
            String line = fileContents.get(i);
            if (line.strip().isEmpty()) continue;

            int firstBracketOpen = line.indexOf('[');
            int firstBracketClose = line.indexOf(']', firstBracketOpen);
            int secondBracketOpen = line.indexOf('[', firstBracketClose);
            int secondBracketClose = line.indexOf(']', secondBracketOpen);

            String singularInfoRaw = line.substring(0, firstBracketOpen);
            String[] singularInfo = singularInfoRaw.split(",");

            String featuresRaw = line.substring(firstBracketOpen + 1, firstBracketClose);
            String extrasRaw = line.substring(secondBracketOpen + 1, secondBracketClose);

            int descStart = line.indexOf('"', secondBracketClose);
            if (descStart == -1) {
                descStart = line.indexOf('[', secondBracketClose);
            }
            int descEnd = line.lastIndexOf('"');
            if (descEnd == -1 || descEnd <= descStart) {
                descEnd = line.lastIndexOf(']');
            }
            if (descStart == -1) {
                descStart = secondBracketClose + 2;
                descEnd = line.length();
            } else {
                descStart++;
            }
            String description = line.substring(descStart, descEnd).strip();

            String laptopIdentifier = singularInfo[0].strip();

            LaptopType type = null;
            try {
                type = LaptopType.valueOf(singularInfo[1].toUpperCase().strip());
            } catch (IllegalArgumentException e) {
                System.out.println("Error in file. Type data could not be parsed on line " + (i + 1) + ". \nError message: " + e.getMessage());
                System.exit(0);
            }

            String laptopName = singularInfo[2].strip();

            double price = 0;
            try {
                price = Double.parseDouble(singularInfo[3].strip());
            } catch (NumberFormatException n) {
                System.out.println("Error in file. Price could not be parsed on line " + (i + 1) + ". \nError message: " + n.getMessage());
                System.exit(0);
            }

            String brandName = singularInfo[4].toUpperCase().strip();
            Brand brand = null;
            try {
                brand = Brand.valueOf(brandName);
            } catch (IllegalArgumentException e) {
                System.out.println("Error in file. Brand data could not be parsed on line " + (i + 1) + ". \nError message: " + e.getMessage());
                System.exit(0);
            }

            GPU gpu = null;
            try {
                gpu = GPU.valueOf(singularInfo[5].toUpperCase().strip());
            } catch (IllegalArgumentException e) {
                System.out.println("Error in file. GPU/Processor data could not be parsed on line " + (i + 1) + ". \nError message: " + e.getMessage());
                System.exit(0);
            }

            boolean cheese = false;
            String cheeseRaw = singularInfo[6].strip().toUpperCase();
            if (cheeseRaw.equals("YES")) cheese = true;

            boolean pickles = false;
            String pickleRaw = singularInfo[7].strip().toUpperCase();
            if (pickleRaw.equals("YES")) pickles = true;

            boolean cucumber = false;
            String cucumberRaw = singularInfo[8].strip().toUpperCase();
            if (cucumberRaw.equals("YES")) cucumber = true;

            boolean tomato = false;
            String tomatoRaw = singularInfo[9].strip().toUpperCase();
            if (tomatoRaw.equals("YES")) tomato = true;

            Dressing dressing = null;
            try {
                dressing = Dressing.valueOf(singularInfo[10].toUpperCase().strip());
            } catch (IllegalArgumentException e) {
                System.out.println("Error in file. Dressing data could not be parsed on line " + (i + 1) + ". \nError message: " + e.getMessage());
                System.exit(0);
            }

            Set<String> features = new LinkedHashSet<>();
            for (String f : featuresRaw.split(";")) {
                if (!f.equalsIgnoreCase("NA") && !f.strip().isEmpty()) {
                    features.add(f.strip());
                }
            }

            Set<Software> extras = new LinkedHashSet<>();
            for (String s : extrasRaw.split(",")) {
                Software soft = null;
                try {
                    soft = Software.valueOf(s.toUpperCase().strip());
                } catch (IllegalArgumentException e) {
                    System.out.println("Error in file. Software data could not be parsed on line " + (i + 1) + ". \nError message: " + e.getMessage());
                    System.exit(0);
                }
                extras.add(soft);
            }

            Map<Filter, Object> filterMap = new LinkedHashMap<>();
            filterMap.put(Filter.TYPE, type);
            if (type.equals(LaptopType.BURGER)) {
                filterMap.put(Filter.BUN, brand.toString());
                if (!extras.isEmpty()) filterMap.put(Filter.SAUCE_S, extras);
            }
            if (!gpu.equals(GPU.NA)) filterMap.put(Filter.MEAT, gpu);
            filterMap.put(Filter.PICKLES, pickles);
            filterMap.put(Filter.CHEESE, cheese);
            filterMap.put(Filter.TOMATO, tomato);
            if (type.equals(LaptopType.SALAD)) {
                filterMap.put(Filter.DRESSING, dressing);
                filterMap.put(Filter.LEAFY_GREENS, features);
                filterMap.put(Filter.CUCUMBER, cucumber);
            }

            DreamLaptop dreamLaptop = new DreamLaptop(filterMap);
            Laptop laptop = new Laptop(laptopIdentifier, laptopName, price, description, dreamLaptop);
            registry.addLaptop(laptop);
        }
        return registry;
    }
}
