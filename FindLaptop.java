import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class FindLaptop extends JFrame {
    private static final String filePath = "laptops.txt";
    private static final String appName = "Dream Laptop Finder";
    private static final Icon logoIcon = new ImageIcon("./byte_chew_small.png");
    private static LaptopRegistry registry;

    private JComboBox<LaptopType> categoryCombo;
    private JComboBox<Object> brandCombo;
    private JComboBox<GPU> gpuCombo;
    private JComboBox<Software> softwareCombo;
    private JCheckBox touchscreenCheck;
    private JCheckBox backlitCheck;
    private JCheckBox numpadCheck;
    private JCheckBox portableCheck;
    private JTextField minPriceField;
    private JTextField maxPriceField;
    private JTextField featureTagsField;

    private JPanel mainWorkspace;
    private CardLayout cardLayout;
    
    private JPanel resultsGrid;
    private JLabel matchesHeaderLabel;
    private JLabel budgetRangeBadge;
    
    private Laptop activeSelectedLaptop;
    private DreamLaptop activeDreamQuery;

    private JTextField checkoutNameField;
    private JTextField checkoutPhoneField;
    private JLabel checkoutProductLabel;
    private boolean isCustomCheckout = false;
    private boolean isLowerRangeCheckout = false;

    private JLabel successTitleLabel;
    private JLabel successDetailsLabel;

    public FindLaptop() {
        super(appName);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(new Color(9, 13, 26));

        JPanel sidebar = createSidebarPanel();
        contentPane.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainWorkspace = new JPanel(cardLayout);
        mainWorkspace.setBackground(new Color(9, 13, 26));

        JPanel searchResultsScreen = createSearchResultsScreen();
        JPanel checkoutScreen = createCheckoutScreen();
        JPanel successScreen = createSuccessScreen();

        mainWorkspace.add(searchResultsScreen, "RESULTS");
        mainWorkspace.add(checkoutScreen, "CHECKOUT");
        mainWorkspace.add(successScreen, "SUCCESS");

        contentPane.add(mainWorkspace, BorderLayout.CENTER);
        setContentPane(contentPane);

        updateSearch();
    }

    public static void main(String[] args) {
        registry = loadRegistry(filePath);
        SwingUtilities.invokeLater(() -> {
            FindLaptop app = new FindLaptop();
            app.setVisible(true);
        });
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(340, 1080));
        sidebar.setBackground(new Color(15, 23, 42));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 41, 59)));
        sidebar.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        headerPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel(logoIcon);
        headerPanel.add(logoLabel);

        JPanel brandTextPanel = new JPanel(new GridLayout(2, 1));
        brandTextPanel.setOpaque(false);
        JLabel brandNameLabel = new JLabel("DREAMFINDER.AI");
        brandNameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        brandNameLabel.setForeground(Color.WHITE);
        JLabel brandSubLabel = new JLabel("Premium Matcher v1.0");
        brandSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        brandSubLabel.setForeground(new Color(148, 163, 184));
        brandTextPanel.add(brandNameLabel);
        brandTextPanel.add(brandSubLabel);
        headerPanel.add(brandTextPanel);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        JPanel scrollContent = new JPanel();
        scrollContent.setBackground(new Color(15, 23, 42));
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBorder(new EmptyBorder(10, 15, 10, 15));

        scrollContent.add(createSidebarHeader("SPECIFICATIONS"));

        categoryCombo = new JComboBox<>(LaptopType.values());
        styleComboBox(categoryCombo);
        scrollContent.add(createFormGroup("Laptop Category", categoryCombo));

        Object[] brands = registry.getAllFeatureValues(Filter.BUN).toArray();
        brandCombo = new JComboBox<>(brands);
        styleComboBox(brandCombo);
        scrollContent.add(createFormGroup("Preferred Brand", brandCombo));

        gpuCombo = new JComboBox<>(GPU.values());
        styleComboBox(gpuCombo);
        scrollContent.add(createFormGroup("GPU / Processor Family", gpuCombo));

        softwareCombo = new JComboBox<>(Software.values());
        styleComboBox(softwareCombo);
        scrollContent.add(createFormGroup("Pre-installed Software", softwareCombo));

        scrollContent.add(Box.createRigidArea(new Dimension(0, 10)));
        scrollContent.add(createSidebarHeader("HARDWARE PREFERENCES"));

        touchscreenCheck = new JCheckBox("Touchscreen Display");
        styleCheckBox(touchscreenCheck);
        scrollContent.add(touchscreenCheck);

        backlitCheck = new JCheckBox("Backlit Keyboard");
        styleCheckBox(backlitCheck);
        scrollContent.add(backlitCheck);

        numpadCheck = new JCheckBox("Dedicated Numeric Keypad");
        styleCheckBox(numpadCheck);
        scrollContent.add(numpadCheck);

        portableCheck = new JCheckBox("Ultra-Portable Design");
        styleCheckBox(portableCheck);
        scrollContent.add(portableCheck);

        scrollContent.add(Box.createRigidArea(new Dimension(0, 10)));
        scrollContent.add(createSidebarHeader("FILTER HARDWARE TAGS"));
        featureTagsField = new JTextField("");
        styleTextField(featureTagsField);
        scrollContent.add(createFormGroup("Tags (e.g. Slim;RGB;Cooling)", featureTagsField));

        scrollContent.add(Box.createRigidArea(new Dimension(0, 10)));
        scrollContent.add(createSidebarHeader("BUDGET LIMITS ($)"));

        JPanel budgetPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        budgetPanel.setOpaque(false);
        minPriceField = new JTextField("0");
        styleTextField(minPriceField);
        maxPriceField = new JTextField("1500");
        styleTextField(maxPriceField);

        budgetPanel.add(createFormGroup("Min Price", minPriceField));
        budgetPanel.add(createFormGroup("Max Price", maxPriceField));
        scrollContent.add(budgetPanel);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(new Color(15, 23, 42));
        scrollPane.getViewport().setBackground(new Color(15, 23, 42));
        sidebar.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        RoundedButton searchBtn = new RoundedButton("APPLY CONFIGURATOR");
        searchBtn.addActionListener(e -> updateSearch());
        actionPanel.add(searchBtn, BorderLayout.NORTH);

        RoundedButton closeBtn = new RoundedButton("EXIT FINDER");
        closeBtn.color1 = new Color(220, 38, 38);
        closeBtn.color2 = new Color(239, 68, 68);
        closeBtn.addActionListener(e -> System.exit(0));
        actionPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.CENTER);
        actionPanel.add(closeBtn, BorderLayout.SOUTH);

        sidebar.add(actionPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createSearchResultsScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(9, 13, 26));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        matchesHeaderLabel = new JLabel("Found 0 matches for your specifications");
        matchesHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        matchesHeaderLabel.setForeground(Color.WHITE);
        topHeader.add(matchesHeaderLabel, BorderLayout.WEST);

        budgetRangeBadge = new JLabel("💳 Middle Budget Range");
        budgetRangeBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        budgetRangeBadge.setForeground(Color.WHITE);
        budgetRangeBadge.setOpaque(true);
        budgetRangeBadge.setBackground(new Color(79, 70, 229));
        budgetRangeBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241), 1),
                new EmptyBorder(5, 12, 5, 12)
        ));
        topHeader.add(budgetRangeBadge, BorderLayout.EAST);

        panel.add(topHeader, BorderLayout.NORTH);

        resultsGrid = new JPanel();
        resultsGrid.setBackground(new Color(9, 13, 26));
        resultsGrid.setLayout(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(resultsGrid);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(9, 13, 26));
        scrollPane.getViewport().setBackground(new Color(9, 13, 26));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCheckoutScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(9, 13, 26));

        RoundedPanel formPanel = new RoundedPanel(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(600, 620));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(15, 25, 15, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel title = new JLabel("SaaS Checkout Referral Portal");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(title, gbc);

        gbc.gridy = 1;
        checkoutProductLabel = new JLabel("Product: Laptop Custom Configuration");
        checkoutProductLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        checkoutProductLabel.setForeground(new Color(168, 85, 247));
        checkoutProductLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(checkoutProductLabel, gbc);

        gbc.gridy = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(51, 65, 85));
        formPanel.add(sep, gbc);

        gbc.gridy = 3;
        checkoutNameField = new JTextField("");
        styleTextField(checkoutNameField);
        formPanel.add(createFormGroup("Client Name", checkoutNameField), gbc);

        gbc.gridy = 4;
        checkoutPhoneField = new JTextField("");
        styleTextField(checkoutPhoneField);
        formPanel.add(createFormGroup("10-Digit Contact Phone Number", checkoutPhoneField), gbc);

        gbc.gridy = 5;
        RoundedButton submitBtn = new RoundedButton("COMPLETE ORDER REGISTRATION");
        submitBtn.addActionListener(e -> submitActiveCheckout());
        formPanel.add(submitBtn, gbc);

        gbc.gridy = 6;
        RoundedButton cancelBtn = new RoundedButton("RETURN TO CONFIGURATOR");
        cancelBtn.color1 = new Color(71, 85, 105);
        cancelBtn.color2 = new Color(100, 116, 139);
        cancelBtn.addActionListener(e -> cardLayout.show(mainWorkspace, "RESULTS"));
        formPanel.add(cancelBtn, gbc);

        panel.add(formPanel);
        return panel;
    }

    private JPanel createSuccessScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(9, 13, 26));

        RoundedPanel messagePanel = new RoundedPanel(new GridBagLayout());
        messagePanel.setPreferredSize(new Dimension(700, 520));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(12, 30, 12, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel checkIcon = new JLabel("✓");
        checkIcon.setFont(new Font("SansSerif", Font.BOLD, 82));
        checkIcon.setForeground(new Color(34, 197, 94));
        checkIcon.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(checkIcon, gbc);

        gbc.gridy = 1;
        successTitleLabel = new JLabel("Order Submitted Successfully");
        successTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        successTitleLabel.setForeground(Color.WHITE);
        successTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(successTitleLabel, gbc);

        gbc.gridy = 2;
        successDetailsLabel = new JLabel("Details");
        successDetailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        successDetailsLabel.setForeground(new Color(148, 163, 184));
        successDetailsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(successDetailsLabel, gbc);

        gbc.gridy = 3;
        RoundedButton finishBtn = new RoundedButton("RESET MATCHING WORKSPACE");
        finishBtn.addActionListener(e -> {
            checkoutNameField.setText("");
            checkoutPhoneField.setText("");
            cardLayout.show(mainWorkspace, "RESULTS");
            updateSearch();
        });
        messagePanel.add(finishBtn, gbc);

        panel.add(messagePanel);
        return panel;
    }

    private void updateSearch() {
        Map<Filter, Object> filterMap = new LinkedHashMap<>();
        
        LaptopType type = (LaptopType) categoryCombo.getSelectedItem();
        filterMap.put(Filter.TYPE, type);

        if (type == LaptopType.BURGER) {
            String brand = brandCombo.getSelectedItem().toString();
            if (!brand.equals("I don't mind")) {
                filterMap.put(Filter.BUN, brand);
            }

            Set<Software> softs = new LinkedHashSet<>();
            Software selectedSoft = (Software) softwareCombo.getSelectedItem();
            if (selectedSoft != Software.NA) {
                softs.add(selectedSoft);
                filterMap.put(Filter.SAUCE_S, softs);
            }
        } else {
            Set<String> leafyGreens = new LinkedHashSet<>();
            String rawTags = featureTagsField.getText().trim();
            if (!rawTags.isEmpty()) {
                for (String t : rawTags.split(";")) {
                    if (!t.strip().isEmpty()) {
                        leafyGreens.add(t.strip());
                    }
                }
            }
            if (!leafyGreens.isEmpty()) {
                filterMap.put(Filter.LEAFY_GREENS, leafyGreens);
            }

            if (numpadCheck.isSelected()) {
                filterMap.put(Filter.CUCUMBER, true);
            }
        }

        GPU gpu = (GPU) gpuCombo.getSelectedItem();
        if (gpu != GPU.NA) {
            filterMap.put(Filter.MEAT, gpu);
        }

        if (touchscreenCheck.isSelected()) {
            filterMap.put(Filter.CHEESE, true);
        }
        if (backlitCheck.isSelected()) {
            filterMap.put(Filter.PICKLES, true);
        }
        if (portableCheck.isSelected()) {
            filterMap.put(Filter.TOMATO, true);
        }

        int minPrice = 0;
        int maxPrice = 1500;
        try {
            minPrice = Integer.parseInt(minPriceField.getText().trim());
        } catch (NumberFormatException e) {
            minPriceField.setText("0");
        }
        try {
            maxPrice = Integer.parseInt(maxPriceField.getText().trim());
        } catch (NumberFormatException e) {
            maxPriceField.setText("1500");
        }

        activeDreamQuery = new DreamLaptop(filterMap, minPrice, maxPrice);

        if (maxPrice < 100) {
            budgetRangeBadge.setText("⚠️ Lower Budget Range Referral Active");
            budgetRangeBadge.setBackground(new Color(220, 38, 38));
            matchesHeaderLabel.setText("Lower Range Special Support Referral");
            showLowerRangeReferralView();
            return;
        }

        if (maxPrice >= 100 && maxPrice <= 500) {
            budgetRangeBadge.setText("💳 Middle Budget Range ($100 - $500)");
            budgetRangeBadge.setBackground(new Color(37, 99, 235));
        } else if (maxPrice >= 501 && maxPrice <= 1000) {
            budgetRangeBadge.setText("💎 High Budget Range ($501 - $1000)");
            budgetRangeBadge.setBackground(new Color(147, 51, 234));
        } else {
            budgetRangeBadge.setText("👑 Premium Budget Range (> $1000)");
            budgetRangeBadge.setBackground(new Color(5, 150, 105));
        }

        List<Laptop> matches = registry.findMatch(activeDreamQuery);
        matchesHeaderLabel.setText("Found " + matches.size() + " matches for your specifications");

        resultsGrid.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        int row = 0;
        if (!matches.isEmpty()) {
            for (Laptop match : matches) {
                gbc.gridy = row++;
                resultsGrid.add(createLaptopCard(match), gbc);
            }
        } else {
            gbc.gridy = row++;
            resultsGrid.add(createEmptyStateCard(), gbc);
        }

        resultsGrid.revalidate();
        resultsGrid.repaint();
        cardLayout.show(mainWorkspace, "RESULTS");
    }

    private void showLowerRangeReferralView() {
        resultsGrid.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;
        gbc.gridx = 0;

        RoundedPanel alertPanel = new RoundedPanel(new GridBagLayout());
        alertPanel.setPreferredSize(new Dimension(800, 480));

        GridBagConstraints subGbc = new GridBagConstraints();
        subGbc.insets = new java.awt.Insets(15, 30, 15, 30);
        subGbc.fill = GridBagConstraints.HORIZONTAL;
        subGbc.weightx = 1.0;

        subGbc.gridx = 0;
        subGbc.gridy = 0;
        JLabel title = new JLabel("⚠️ Special Budget Assist referral Required");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(239, 68, 68));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        alertPanel.add(title, subGbc);

        subGbc.gridy = 1;
        JLabel desc = new JLabel("<html><center>Since your budget maximum is in our <b>Lower Range (< $100)</b>, we do not inventory catalog models at this price level.<br>However, our technician will contact you directly to evaluate refurbished stocks or apply UNE student support subsidies!</center></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setForeground(new Color(226, 232, 240));
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        alertPanel.add(desc, subGbc);

        subGbc.gridy = 2;
        RoundedButton callBtn = new RoundedButton("SUBMIT HARDWARE TECHNICIAN REFERRAL");
        callBtn.color1 = new Color(220, 38, 38);
        callBtn.color2 = new Color(239, 68, 68);
        callBtn.addActionListener(e -> {
            isCustomCheckout = false;
            isLowerRangeCheckout = true;
            checkoutProductLabel.setText("Service: Lower Budget Range Technician Call Request");
            checkoutProductLabel.setForeground(new Color(239, 68, 68));
            cardLayout.show(mainWorkspace, "CHECKOUT");
        });
        alertPanel.add(callBtn, subGbc);

        resultsGrid.add(alertPanel, gbc);
        resultsGrid.revalidate();
        resultsGrid.repaint();
        cardLayout.show(mainWorkspace, "RESULTS");
    }

    private JPanel createLaptopCard(Laptop laptop) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(25, 0));
        card.setPreferredSize(new Dimension(850, 180));
        card.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel details = new JPanel(new GridLayout(3, 1, 0, 5));
        details.setOpaque(false);

        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBar.setOpaque(false);
        JLabel name = new JLabel(laptop.getLaptopName());
        name.setFont(new Font("SansSerif", Font.BOLD, 19));
        name.setForeground(Color.WHITE);
        titleBar.add(name);

        JLabel idBadge = new JLabel("ID: " + laptop.getLaptopIdentifier());
        idBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        idBadge.setForeground(new Color(99, 102, 241));
        idBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241), 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
        titleBar.add(idBadge);
        details.add(titleBar);

        JLabel desc = new JLabel(laptop.getDescription());
        desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        desc.setForeground(new Color(148, 163, 184));
        details.add(desc);

        JPanel specsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        specsPanel.setOpaque(false);
        
        specsPanel.add(createSpecBadge(laptop.getDreamLaptop().getFilter(Filter.TYPE).toString()));
        if (laptop.getDreamLaptop().getAllFilters().containsKey(Filter.BUN)) {
            specsPanel.add(createSpecBadge(laptop.getDreamLaptop().getFilter(Filter.BUN).toString()));
        }
        if (laptop.getDreamLaptop().getAllFilters().containsKey(Filter.MEAT)) {
            specsPanel.add(createSpecBadge(laptop.getDreamLaptop().getFilter(Filter.MEAT).toString()));
        }
        details.add(specsPanel);

        card.add(details, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel price = new JLabel("$" + String.format("%.2f", laptop.getPrice()));
        price.setFont(new Font("SansSerif", Font.BOLD, 24));
        price.setForeground(new Color(34, 197, 94));
        price.setHorizontalAlignment(SwingConstants.RIGHT);
        actions.add(price, gbc);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(15, 0, 0, 0);
        RoundedButton orderBtn = new RoundedButton("ORDER NOW");
        orderBtn.setPreferredSize(new Dimension(140, 38));
        orderBtn.addActionListener(e -> {
            activeSelectedLaptop = laptop;
            isCustomCheckout = false;
            isLowerRangeCheckout = false;
            checkoutProductLabel.setText("Product: " + laptop.getLaptopName() + " (" + laptop.getLaptopIdentifier() + ")");
            checkoutProductLabel.setForeground(new Color(6, 182, 212));
            cardLayout.show(mainWorkspace, "CHECKOUT");
        });
        actions.add(orderBtn, gbc);

        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private JPanel createEmptyStateCard() {
        RoundedPanel card = new RoundedPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(850, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 25, 10, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel title = new JLabel("🔍 No Catalog Matches Found");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(239, 68, 68));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, gbc);

        gbc.gridy = 1;
        JLabel desc = new JLabel("Unfortunately, none of our current catalog models match your specifications. Custom orders are built in our UNE laboratory.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setForeground(new Color(148, 163, 184));
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(desc, gbc);

        gbc.gridy = 2;
        RoundedButton customBtn = new RoundedButton("REQUEST CUSTOM BUILT LAPTOP");
        customBtn.color1 = new Color(147, 51, 234);
        customBtn.color2 = new Color(168, 85, 247);
        customBtn.addActionListener(e -> {
            activeSelectedLaptop = new Laptop(activeDreamQuery);
            isCustomCheckout = true;
            isLowerRangeCheckout = false;
            checkoutProductLabel.setText("Service: Custom Laptop Build Request");
            checkoutProductLabel.setForeground(new Color(168, 85, 247));
            cardLayout.show(mainWorkspace, "CHECKOUT");
        });
        card.add(customBtn, gbc);

        return card;
    }

    private void submitActiveCheckout() {
        String name = checkoutNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a client name.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        String phoneStr = checkoutPhoneField.getText().trim();
        long phoneNumber = 0;
        try {
            phoneNumber = Long.parseLong(phoneStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid entry. Phone number must be numeric.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        int length = String.valueOf(phoneNumber).length();
        if (length != 9) {
            JOptionPane.showMessageDialog(this, "Invalid entry. Enter a 10-digit phone number in format 0412 123 345.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        Client client = new Client(name, phoneNumber);

        if (isLowerRangeCheckout) {
            submitLowerRangeInquiry(client, activeDreamQuery);
        } else {
            submitOrder(client, activeSelectedLaptop);
        }
    }

    private void submitOrder(Client client, Laptop laptop) {
        String safeName = client.name().replace(" ", "_");
        String filename = safeName + "_" + laptop.getLaptopIdentifier() + ".txt";
        Path path = Path.of(filename);

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
            
            successTitleLabel.setText("Order Registered Successfully");
            successDetailsLabel.setText("<html><center>Order persistent file created at:<br><b>" + path.toAbsolutePath() + "</b><br><br>Our UNE hardware technicians will assemble your specifications and contact you shortly.</center></html>");
            cardLayout.show(mainWorkspace, "SUCCESS");
        } catch (IOException io) {
            JOptionPane.showMessageDialog(this, "Error writing order file: " + io.getMessage(), appName, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitLowerRangeInquiry(Client client, DreamLaptop query) {
        String safeName = client.name().replace(" ", "_");
        String filename = safeName + "_lower_range.txt";
        Path path = Path.of(filename);

        String lineToWrite = "Lower Range Budget Inquiry:\n\t" +
                "Name: " + client.name() +
                "\n\tPhone: 0" + client.phoneNumber() +
                "\n\tBudget Limit Range: $" + query.getMinPrice() + " - $" + query.getMaxPrice() +
                "\n\nRequested Specs:\n" + query.getInfo() +
                "\n\n* Technician Assignment: Assessor/Technician will call this client within 24 hours to match lower-range/refurbished/student discount machines.";

        try {
            Files.writeString(path, lineToWrite);
            
            successTitleLabel.setText("Technician Call Scheduled");
            successDetailsLabel.setText("<html><center>Inquiry referral file created at:<br><b>" + path.toAbsolutePath() + "</b><br><br>A certified technician will call you directly at <b>0" + client.phoneNumber() + "</b> to assist with budget options!</center></html>");
            cardLayout.show(mainWorkspace, "SUCCESS");
        } catch (IOException io) {
            JOptionPane.showMessageDialog(this, "Error writing inquiry file: " + io.getMessage(), appName, JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createSpecBadge(String text) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("SansSerif", Font.PLAIN, 12));
        badge.setForeground(new Color(203, 213, 225));
        badge.setOpaque(true);
        badge.setBackground(new Color(51, 65, 85));
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    private JLabel createSidebarHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(99, 102, 241));
        label.setBorder(new EmptyBorder(15, 0, 5, 0));
        return label;
    }

    private JPanel createFormGroup(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(226, 232, 240));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(new Color(30, 41, 59));
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105)));
    }

    private void styleCheckBox(JCheckBox check) {
        check.setOpaque(false);
        check.setForeground(new Color(226, 232, 240));
        check.setFont(new Font("SansSerif", Font.PLAIN, 13));
        check.setBorder(new EmptyBorder(5, 0, 5, 0));
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(30, 41, 59));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

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
                System.out.println("Error parsing type data on line " + (i + 1));
                System.exit(0);
            }

            String laptopName = singularInfo[2].strip();

            double price = 0;
            try {
                price = Double.parseDouble(singularInfo[3].strip());
            } catch (NumberFormatException n) {
                System.out.println("Error parsing price data on line " + (i + 1));
                System.exit(0);
            }

            String brandName = singularInfo[4].toUpperCase().strip();
            Brand brand = null;
            try {
                brand = Brand.valueOf(brandName);
            } catch (IllegalArgumentException e) {
                System.out.println("Error parsing brand data on line " + (i + 1));
                System.exit(0);
            }

            GPU gpu = null;
            try {
                gpu = GPU.valueOf(singularInfo[5].toUpperCase().strip());
            } catch (IllegalArgumentException e) {
                System.out.println("Error parsing GPU data on line " + (i + 1));
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
                System.out.println("Error parsing dressing data on line " + (i + 1));
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
                    System.out.println("Error parsing software data on line " + (i + 1));
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

    private static class RoundedPanel extends JPanel {
        private final int cornerRadius = 15;
        private final Color backgroundColor = new Color(30, 41, 59, 180);
        private final Color borderColor = new Color(51, 65, 85);

        public RoundedPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);

            graphics.setColor(borderColor);
            graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }

    private static class RoundedButton extends JButton {
        public Color color1 = new Color(79, 70, 229);
        public Color color2 = new Color(99, 102, 241);

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    color1 = color1.brighter();
                    color2 = color2.brighter();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    color1 = color1.darker();
                    color2 = color2.darker();
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
