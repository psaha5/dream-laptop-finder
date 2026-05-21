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
    private static final String filePath = "resources/laptops.txt";
    private static final String appName = "Dream Laptop Finder";
    private static final Icon logoIcon = new ImageIcon("resources/byte_chew_small.png");
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
        
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 768));

        JPanel contentPane = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(238, 242, 255),
                    getWidth(), getHeight(), new Color(250, 245, 255)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(199, 210, 254, 120));
                g2.fillOval(getWidth() / 3, -100, 500, 500);
                g2.setColor(new Color(243, 207, 243, 100));
                g2.fillOval(getWidth() - 300, getHeight() - 400, 450, 450);
                g2.dispose();
            }
        };

        JPanel sidebar = createSidebarPanel();
        contentPane.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainWorkspace = new JPanel(cardLayout);
        mainWorkspace.setOpaque(false);

        JPanel searchResultsScreen = createSearchResultsScreen();
        JPanel checkoutScreen = createCheckoutScreen();
        JPanel successScreen = createSuccessScreen();

        mainWorkspace.add(searchResultsScreen, "RESULTS");
        mainWorkspace.add(checkoutScreen, "CHECKOUT");
        mainWorkspace.add(successScreen, "SUCCESS");

        contentPane.add(mainWorkspace, BorderLayout.CENTER);
        setContentPane(contentPane);

        hookupRealtimeListeners();
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
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 110));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(340, 1080));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(255, 255, 255, 160)));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        headerPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel(logoIcon);
        headerPanel.add(logoLabel);

        JPanel brandTextPanel = new JPanel(new GridLayout(2, 1));
        brandTextPanel.setOpaque(false);
        JLabel brandNameLabel = new JLabel("DREAMFINDER.AI");
        brandNameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        brandNameLabel.setForeground(new Color(15, 23, 42));
        JLabel brandSubLabel = new JLabel("Premium Matcher v1.0");
        brandSubLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        brandSubLabel.setForeground(new Color(71, 85, 105));
        brandTextPanel.add(brandNameLabel);
        brandTextPanel.add(brandSubLabel);
        headerPanel.add(brandTextPanel);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        JPanel scrollContent = new JPanel();
        scrollContent.setOpaque(false);
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBorder(new EmptyBorder(10, 15, 10, 15));

        scrollContent.add(createSidebarHeader("SPECIFICATIONS"));

        categoryCombo = new JComboBox<>(LaptopType.values());
        styleComboBox(categoryCombo);
        scrollContent.add(createFormGroup("Laptop Category", categoryCombo));

        Object[] brands = registry.getAllFeatureValues(Filter.BRAND).toArray();
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
        featureTagsField = new RoundedTextField("");
        scrollContent.add(createFormGroup("Tags (e.g. Slim;RGB;Cooling)", featureTagsField));

        scrollContent.add(Box.createRigidArea(new Dimension(0, 10)));
        scrollContent.add(createSidebarHeader("BUDGET LIMITS ($)"));

        JPanel budgetPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        budgetPanel.setOpaque(false);
        minPriceField = new RoundedTextField("0");
        maxPriceField = new RoundedTextField("1500");

        budgetPanel.add(createFormGroup("Min Price", minPriceField));
        budgetPanel.add(createFormGroup("Max Price", maxPriceField));
        scrollContent.add(budgetPanel);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        sidebar.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        RoundedButton searchBtn = new RoundedButton("APPLY CONFIGURATOR");
        searchBtn.setPreferredSize(new Dimension(310, 44));
        searchBtn.addActionListener(e -> {
            updateSearch();
            animateResultsFlash();
        });
        actionPanel.add(searchBtn);

        RoundedButton closeBtn = new RoundedButton("EXIT FINDER");
        closeBtn.setPreferredSize(new Dimension(310, 44));
        closeBtn.color1 = new Color(220, 38, 38);
        closeBtn.color2 = new Color(239, 68, 68);
        closeBtn.addActionListener(e -> System.exit(0));
        actionPanel.add(closeBtn);

        sidebar.add(actionPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private void hookupRealtimeListeners() {
        categoryCombo.addActionListener(e -> updateSearch());
        brandCombo.addActionListener(e -> updateSearch());
        gpuCombo.addActionListener(e -> updateSearch());
        softwareCombo.addActionListener(e -> updateSearch());
        
        touchscreenCheck.addActionListener(e -> updateSearch());
        backlitCheck.addActionListener(e -> updateSearch());
        numpadCheck.addActionListener(e -> updateSearch());
        portableCheck.addActionListener(e -> updateSearch());

        addRealtimeTextListener(minPriceField);
        addRealtimeTextListener(maxPriceField);
        addRealtimeTextListener(featureTagsField);
    }

    private void addRealtimeTextListener(JTextField field) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateSearch();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateSearch();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateSearch();
            }
        });
    }

    private JPanel createSearchResultsScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setBorder(new EmptyBorder(0, 0, 20, 0));

        matchesHeaderLabel = new JLabel("Found 0 matches for your specifications");
        matchesHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        matchesHeaderLabel.setForeground(new Color(15, 23, 42));
        topHeader.add(matchesHeaderLabel, BorderLayout.WEST);

        budgetRangeBadge = new JLabel("💳 Middle Budget Range") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        budgetRangeBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        budgetRangeBadge.setOpaque(false);
        budgetRangeBadge.setBackground(new Color(224, 231, 255, 200));
        budgetRangeBadge.setForeground(new Color(67, 56, 202));
        budgetRangeBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241, 100), 1),
                new EmptyBorder(6, 15, 6, 15)
        ));
        topHeader.add(budgetRangeBadge, BorderLayout.EAST);

        panel.add(topHeader, BorderLayout.NORTH);

        resultsGrid = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (getBackground() != null && getBackground().getAlpha() > 0) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        resultsGrid.setOpaque(false);
        resultsGrid.setLayout(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(resultsGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCheckoutScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

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
        title.setForeground(new Color(15, 23, 42));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(title, gbc);

        gbc.gridy = 1;
        checkoutProductLabel = new JLabel("Product: Laptop Custom Configuration");
        checkoutProductLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        checkoutProductLabel.setForeground(new Color(109, 40, 217));
        checkoutProductLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(checkoutProductLabel, gbc);

        gbc.gridy = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        formPanel.add(sep, gbc);

        gbc.gridy = 3;
        checkoutNameField = new RoundedTextField("");
        formPanel.add(createFormGroup("Client Name", checkoutNameField), gbc);

        gbc.gridy = 4;
        checkoutPhoneField = new RoundedTextField("");
        formPanel.add(createFormGroup("10-Digit Contact Phone Number", checkoutPhoneField), gbc);

        gbc.gridy = 5;
        RoundedButton submitBtn = new RoundedButton("COMPLETE ORDER REGISTRATION");
        submitBtn.setPreferredSize(new Dimension(500, 44));
        submitBtn.addActionListener(e -> submitActiveCheckout());
        formPanel.add(submitBtn, gbc);

        gbc.gridy = 6;
        RoundedButton cancelBtn = new RoundedButton("RETURN TO CONFIGURATOR");
        cancelBtn.setPreferredSize(new Dimension(500, 44));
        cancelBtn.color1 = new Color(100, 116, 139);
        cancelBtn.color2 = new Color(71, 85, 105);
        cancelBtn.addActionListener(e -> cardLayout.show(mainWorkspace, "RESULTS"));
        formPanel.add(cancelBtn, gbc);

        panel.add(formPanel);
        return panel;
    }

    private JPanel createSuccessScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

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
        checkIcon.setForeground(new Color(22, 163, 74));
        checkIcon.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(checkIcon, gbc);

        gbc.gridy = 1;
        successTitleLabel = new JLabel("Order Submitted Successfully");
        successTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        successTitleLabel.setForeground(new Color(15, 23, 42));
        successTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(successTitleLabel, gbc);

        gbc.gridy = 2;
        successDetailsLabel = new JLabel("Details");
        successDetailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        successDetailsLabel.setForeground(new Color(71, 85, 105));
        successDetailsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messagePanel.add(successDetailsLabel, gbc);

        gbc.gridy = 3;
        RoundedButton finishBtn = new RoundedButton("RESET MATCHING WORKSPACE");
        finishBtn.setPreferredSize(new Dimension(600, 44));
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

        String brand = brandCombo.getSelectedItem().toString();
        if (!brand.equals("I don't mind")) {
            filterMap.put(Filter.BRAND, brand);
        }

        Set<Software> softs = new LinkedHashSet<>();
        Software selectedSoft = (Software) softwareCombo.getSelectedItem();
        if (selectedSoft != Software.NA) {
            softs.add(selectedSoft);
            filterMap.put(Filter.SOFTWARE, softs);
        }

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
            filterMap.put(Filter.FEATURES, leafyGreens);
        }

        if (numpadCheck.isSelected()) {
            filterMap.put(Filter.NUMERIC_KEYPAD, true);
        }

        GPU gpu = (GPU) gpuCombo.getSelectedItem();
        if (gpu != GPU.NA) {
            filterMap.put(Filter.GPU, gpu);
        }

        if (touchscreenCheck.isSelected()) {
            filterMap.put(Filter.TOUCHSCREEN, true);
        }
        if (backlitCheck.isSelected()) {
            filterMap.put(Filter.BACKLIT_KEYBOARD, true);
        }
        if (portableCheck.isSelected()) {
            filterMap.put(Filter.PORTABILITY, true);
        }

        int minPrice = 0;
        int maxPrice = 1500;
        try {
            minPrice = Integer.parseInt(minPriceField.getText().trim());
        } catch (NumberFormatException e) {
            minPrice = 0;
        }
        try {
            maxPrice = Integer.parseInt(maxPriceField.getText().trim());
        } catch (NumberFormatException e) {
            maxPrice = 1500;
        }

        activeDreamQuery = new DreamLaptop(filterMap, minPrice, maxPrice);

        if (maxPrice < 100) {
            budgetRangeBadge.setText("⚠️ Lower Range Referral Program Active");
            budgetRangeBadge.setBackground(new Color(254, 226, 226, 200));
            budgetRangeBadge.setForeground(new Color(220, 38, 38));
            budgetRangeBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(239, 68, 68, 100), 1),
                    new EmptyBorder(6, 15, 6, 15)
            ));
            matchesHeaderLabel.setText("Lower Range Special Support referral");
            showLowerRangeReferralView();
            return;
        }

        if (maxPrice >= 100 && maxPrice <= 500) {
            budgetRangeBadge.setText("💳 Middle Budget Range ($100 - $500)");
            budgetRangeBadge.setBackground(new Color(224, 231, 255, 200));
            budgetRangeBadge.setForeground(new Color(67, 56, 202));
            budgetRangeBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(99, 102, 241, 100), 1),
                    new EmptyBorder(6, 15, 6, 15)
            ));
        } else if (maxPrice >= 501 && maxPrice <= 1000) {
            budgetRangeBadge.setText("💎 High Budget Range ($501 - $1000)");
            budgetRangeBadge.setBackground(new Color(243, 232, 255, 200));
            budgetRangeBadge.setForeground(new Color(107, 33, 168));
            budgetRangeBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(168, 85, 247, 100), 1),
                    new EmptyBorder(6, 15, 6, 15)
            ));
        } else {
            budgetRangeBadge.setText("👑 Premium Budget Range (> $1000)");
            budgetRangeBadge.setBackground(new Color(209, 250, 229, 200));
            budgetRangeBadge.setForeground(new Color(6, 95, 70));
            budgetRangeBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(16, 185, 129, 100), 1),
                    new EmptyBorder(6, 15, 6, 15)
            ));
        }

        List<Laptop> matches = registry.findMatch(activeDreamQuery);
        matchesHeaderLabel.setText("Found " + matches.size() + " matches for your specifications");

        resultsGrid.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(12, 0, 12, 0);
        gbc.gridx = 0;

        int row = 0;
        if (!matches.isEmpty()) {
            for (Laptop match : matches) {
                gbc.gridy = row++;
                resultsGrid.add(createLaptopCard(match), gbc);
            }
            gbc.gridy = row++;
            gbc.weighty = 1.0;
            resultsGrid.add(Box.createGlue(), gbc);
        } else {
            gbc.gridy = row++;
            resultsGrid.add(createEmptyStateCard(), gbc);
        }

        resultsGrid.revalidate();
        resultsGrid.repaint();
        cardLayout.show(mainWorkspace, "RESULTS");
    }

    private void animateResultsFlash() {
        Color baseColor = new Color(255, 255, 255, 0);
        long startTime = System.currentTimeMillis();
        long duration = 300;
        javax.swing.Timer timer = new javax.swing.Timer(15, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) {
                resultsGrid.setBackground(baseColor);
                resultsGrid.repaint();
                timer.stop();
            } else {
                float pct = (float) elapsed / duration;
                float inversePct = 1.0f - pct;
                int alpha = (int) (160 * inversePct);
                resultsGrid.setBackground(new Color(255, 255, 255, alpha));
                resultsGrid.repaint();
            }
        });
        timer.start();
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
        subGbc.insets = new java.awt.Insets(20, 30, 20, 30);
        subGbc.fill = GridBagConstraints.HORIZONTAL;
        subGbc.weightx = 1.0;

        subGbc.gridx = 0;
        subGbc.gridy = 0;
        JLabel title = new JLabel("⚠️ Special Budget Assist referral Required");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(220, 38, 38));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        alertPanel.add(title, subGbc);

        subGbc.gridy = 1;
        JLabel desc = new JLabel("<html><center>Since your budget maximum is in our <b>Lower Range (< $100)</b>, we do not inventory catalog models at this price level.<br>However, our technician will contact you directly to evaluate refurbished stocks or apply UNE student support subsidies!</center></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setForeground(new Color(71, 85, 105));
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        alertPanel.add(desc, subGbc);

        subGbc.gridy = 2;
        RoundedButton callBtn = new RoundedButton("SUBMIT HARDWARE TECHNICIAN REFERRAL");
        callBtn.setPreferredSize(new Dimension(500, 44));
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

        String brand = laptop.getDreamLaptop().getAllFilters().containsKey(Filter.BRAND) 
            ? laptop.getDreamLaptop().getFilter(Filter.BRAND).toString() 
            : "L";
        card.add(createBrandMonogram(brand), BorderLayout.WEST);

        JPanel details = new JPanel(new GridLayout(3, 1, 0, 5));
        details.setOpaque(false);

        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBar.setOpaque(false);
        JLabel name = new JLabel(laptop.getLaptopName());
        name.setFont(new Font("SansSerif", Font.BOLD, 19));
        name.setForeground(new Color(15, 23, 42));
        titleBar.add(name);

        JLabel idBadge = new JLabel("ID: " + laptop.getLaptopIdentifier()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(224, 231, 255, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        idBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        idBadge.setForeground(new Color(67, 56, 202));
        idBadge.setOpaque(false);
        idBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241, 80), 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
        titleBar.add(idBadge);

        boolean isPerfect = true;
        if (activeDreamQuery != null) {
            isPerfect = laptop.getDreamLaptop().matches(activeDreamQuery);
            if (activeDreamQuery.getMinPrice() >= 0 && laptop.getPrice() < activeDreamQuery.getMinPrice()) {
                isPerfect = false;
            }
            if (activeDreamQuery.getMaxPrice() >= 0 && laptop.getPrice() > activeDreamQuery.getMaxPrice()) {
                isPerfect = false;
            }
        }
        if (!isPerfect) {
            JLabel altBadge = new JLabel("✨ Recommended Alternative") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 251, 235, 200));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            altBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
            altBadge.setForeground(new Color(180, 83, 9));
            altBadge.setOpaque(false);
            altBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(245, 158, 11, 100), 1),
                    new EmptyBorder(2, 6, 2, 6)
            ));
            titleBar.add(altBadge);
        }

        details.add(titleBar);

        JLabel desc = new JLabel(laptop.getDescription());
        desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        desc.setForeground(new Color(71, 85, 105));
        details.add(desc);

        JPanel specsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        specsPanel.setOpaque(false);
        
        specsPanel.add(createSpecBadge(laptop.getDreamLaptop().getFilter(Filter.TYPE).toString()));
        if (laptop.getDreamLaptop().getAllFilters().containsKey(Filter.BRAND)) {
            specsPanel.add(createSpecBadge(laptop.getDreamLaptop().getFilter(Filter.BRAND).toString()));
        }
        if (laptop.getDreamLaptop().getAllFilters().containsKey(Filter.GPU)) {
            specsPanel.add(createSpecBadge(laptop.getDreamLaptop().getFilter(Filter.GPU).toString()));
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
        price.setForeground(new Color(5, 150, 105));
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
            checkoutProductLabel.setForeground(new Color(8, 145, 178));
            cardLayout.show(mainWorkspace, "CHECKOUT");
        });
        actions.add(orderBtn, gbc);

        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private JPanel createBrandMonogram(String brand) {
        String letter = brand.substring(0, 1).toUpperCase();
        Color bgStart;
        Color bgEnd;
        switch (brand.toUpperCase()) {
            case "ASUS" -> {
                bgStart = new Color(59, 130, 246);
                bgEnd = new Color(29, 78, 216);
            }
            case "DELL" -> {
                bgStart = new Color(14, 165, 233);
                bgEnd = new Color(3, 105, 161);
            }
            case "HP" -> {
                bgStart = new Color(13, 148, 136);
                bgEnd = new Color(15, 118, 110);
            }
            case "LENOVO" -> {
                bgStart = new Color(249, 115, 22);
                bgEnd = new Color(194, 65, 12);
            }
            case "APPLE" -> {
                bgStart = new Color(107, 114, 128);
                bgEnd = new Color(55, 65, 81);
            }
            case "MSI" -> {
                bgStart = new Color(239, 68, 68);
                bgEnd = new Color(185, 28, 28);
            }
            default -> {
                bgStart = new Color(99, 102, 241);
                bgEnd = new Color(67, 56, 202);
            }
        }
        JPanel monogram = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, bgStart, 0, getHeight(), bgEnd);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(letter)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(letter, x, y);
                g2.dispose();
            }
        };
        monogram.setPreferredSize(new Dimension(54, 54));
        monogram.setOpaque(false);
        return monogram;
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
        desc.setForeground(new Color(71, 85, 105));
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(desc, gbc);

        gbc.gridy = 2;
        RoundedButton customBtn = new RoundedButton("REQUEST CUSTOM BUILT LAPTOP");
        customBtn.setPreferredSize(new Dimension(500, 44));
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
        try {
            String name = checkoutNameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a client name.", appName, JOptionPane.ERROR_MESSAGE);
                return;
            }

            String phoneStr = checkoutPhoneField.getText().trim().replaceAll("[^0-9]", "");
            if (phoneStr.length() != 10) {
                JOptionPane.showMessageDialog(this, "Invalid entry. Enter a 10-digit phone number in format 0412 123 345.", appName, JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            long phoneNumber = 0;
            try {
                phoneNumber = Long.parseLong(phoneStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid entry. Phone number must be numeric.", appName, JOptionPane.ERROR_MESSAGE);
                return;
            }

            Client client = new Client(name, phoneNumber);

            if (isLowerRangeCheckout) {
                submitLowerRangeInquiry(client, activeDreamQuery);
            } else {
                submitOrder(client, activeSelectedLaptop);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred during submission: " + ex.getMessage(), appName, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitOrder(Client client, Laptop laptop) {
        String safeName = client.name().replace(" ", "_");
        String filename = safeName + "_" + laptop.getLaptopIdentifier() + ".txt";
        Path path = Path.of("orders", filename);

        String lineToWrite = "Order details:\n\t" +
                "Name: " + client.name() +
                " (0" + client.phoneNumber() + ")";
        if (laptop.getLaptopIdentifier().isEmpty()) {
            lineToWrite += "\n\nCUSTOM LAPTOP BUILD...\n" + laptop.getLaptopInformation();
        } else {
            lineToWrite += "\n\tItem: " + laptop.getLaptopName() + " (" + laptop.getLaptopIdentifier() + ")";
        }

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, lineToWrite);
            
            successTitleLabel.setText("Order Registered Successfully");
            successDetailsLabel.setText("<html><center>Order persistent file created at:<br><b>" + path.toAbsolutePath() + "</b><br><br>Our UNE hardware technicians will contact you shortly.</center></html>");
            cardLayout.show(mainWorkspace, "SUCCESS");
        } catch (IOException io) {
            JOptionPane.showMessageDialog(this, "Error writing order file: " + io.getMessage(), appName, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitLowerRangeInquiry(Client client, DreamLaptop query) {
        String safeName = client.name().replace(" ", "_");
        String filename = safeName + "_lower_range.txt";
        Path path = Path.of("orders", filename);

        String lineToWrite = "Lower Range Budget Inquiry:\n\t" +
                "Name: " + client.name() +
                "\n\tPhone: 0" + client.phoneNumber() +
                "\n\tBudget Limit Range: $" + query.getMinPrice() + " - $" + query.getMaxPrice() +
                "\n\nRequested Specs:\n" + query.getInfo() +
                "\n\n* Technician Assignment: Assessor/Technician will call this client within 24 hours to match lower-range/refurbished/student discount machines.";

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, lineToWrite);
            
            successTitleLabel.setText("Technician Call Scheduled");
            successDetailsLabel.setText("<html><center>Inquiry referral file created at:<br><b>" + path.toAbsolutePath() + "</b><br><br>A certified technician will call you directly at <b>0" + client.phoneNumber() + "</b> to assist with budget options!</center></html>");
            cardLayout.show(mainWorkspace, "SUCCESS");
        } catch (IOException io) {
            JOptionPane.showMessageDialog(this, "Error writing inquiry file: " + io.getMessage(), appName, JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createSpecBadge(String text) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setFont(new Font("SansSerif", Font.PLAIN, 12));
        badge.setForeground(new Color(51, 65, 85));
        badge.setOpaque(false);
        badge.setBackground(new Color(226, 232, 240, 200));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225, 150), 1),
                new EmptyBorder(2, 8, 2, 8)
        ));
        return badge;
    }

    private JLabel createSidebarHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(79, 70, 229));
        label.setBorder(new EmptyBorder(15, 0, 5, 0));
        return label;
    }

    private JPanel createFormGroup(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(51, 65, 85));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(71, 85, 105));
                        int[] xPoints = {getWidth() / 2 - 4, getWidth() / 2 + 4, getWidth() / 2};
                        int[] yPoints = {getHeight() / 2 - 2, getHeight() / 2 - 2, getHeight() / 2 + 3};
                        g2.fillPolygon(xPoints, yPoints, 3);
                        g2.dispose();
                    }
                };
                button.setBackground(new Color(255, 255, 255, 0));
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 160));
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }
        });
        combo.setBackground(new Color(255, 255, 255, 160));
        combo.setForeground(new Color(15, 23, 42));
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(224, 231, 255) : Color.WHITE);
                setForeground(isSelected ? new Color(67, 56, 202) : new Color(15, 23, 42));
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
    }

    private void styleCheckBox(JCheckBox check) {
        check.setOpaque(false);
        check.setForeground(new Color(30, 41, 59));
        check.setFont(new Font("SansSerif", Font.PLAIN, 13));
        check.setBorder(new EmptyBorder(5, 0, 5, 0));
    }

    public static LaptopRegistry loadRegistry(String filePath) {
        LaptopRegistry registry = new LaptopRegistry();
        Path path = Path.of(filePath);
        List<String> fileContents = null;
        try {
            fileContents = Files.readAllLines(path);
        } catch (IOException io) {
            JOptionPane.showMessageDialog(null, "Database file laptops.txt not found. Starting with empty catalog.", appName, JOptionPane.WARNING_MESSAGE);
            fileContents = new ArrayList<>();
        }

        for (int i = 1; i < fileContents.size(); i++) {
            String line = fileContents.get(i);
            if (line.strip().isEmpty()) continue;

            try {
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

                LaptopType type = LaptopType.valueOf(singularInfo[1].toUpperCase().strip());
                String laptopName = singularInfo[2].strip();
                double price = Double.parseDouble(singularInfo[3].strip());
                String brandName = singularInfo[4].toUpperCase().strip();
                Brand brand = Brand.valueOf(brandName);
                GPU gpu = GPU.valueOf(singularInfo[5].toUpperCase().strip());

                boolean touchscreen = singularInfo[6].strip().equalsIgnoreCase("YES");
                boolean backlit = singularInfo[7].strip().equalsIgnoreCase("YES");
                boolean numpad = singularInfo[8].strip().equalsIgnoreCase("YES");
                boolean portable = singularInfo[9].strip().equalsIgnoreCase("YES");

                Docking docking = Docking.valueOf(singularInfo[10].toUpperCase().strip());

                Set<String> features = new LinkedHashSet<>();
                for (String f : featuresRaw.split(";")) {
                    if (!f.equalsIgnoreCase("NA") && !f.strip().isEmpty()) {
                        features.add(f.strip());
                    }
                }

                Set<Software> extras = new LinkedHashSet<>();
                for (String s : extrasRaw.split(",")) {
                    if (!s.equalsIgnoreCase("NA") && !s.strip().isEmpty()) {
                        extras.add(Software.valueOf(s.toUpperCase().strip()));
                    }
                }

                Map<Filter, Object> filterMap = new LinkedHashMap<>();
                filterMap.put(Filter.TYPE, type);
                filterMap.put(Filter.BRAND, brand.toString());
                if (!extras.isEmpty()) {
                    filterMap.put(Filter.SOFTWARE, extras);
                }
                if (!gpu.equals(GPU.NA)) {
                    filterMap.put(Filter.GPU, gpu);
                }
                filterMap.put(Filter.BACKLIT_KEYBOARD, backlit);
                filterMap.put(Filter.TOUCHSCREEN, touchscreen);
                filterMap.put(Filter.PORTABILITY, portable);
                filterMap.put(Filter.DOCKING, docking);
                if (!features.isEmpty()) {
                    filterMap.put(Filter.FEATURES, features);
                }
                filterMap.put(Filter.NUMERIC_KEYPAD, numpad);

                DreamLaptop dreamLaptop = new DreamLaptop(filterMap);
                Laptop laptop = new Laptop(laptopIdentifier, laptopName, price, description, dreamLaptop);
                registry.addLaptop(laptop);
            } catch (Exception e) {
                System.err.println("Skipping malformed row " + (i + 1) + ": " + e.getMessage());
            }
        }
        return registry;
    }

    private static class RoundedPanel extends JPanel {
        private final int cornerRadius = 18;
        private final Color backgroundColor = new Color(255, 255, 255, 170);
        private final Color borderColor = new Color(255, 255, 255, 220);

        public RoundedPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            graphics.setColor(backgroundColor);
            graphics.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

            graphics.setColor(new Color(209, 213, 219, 140));
            graphics.setStroke(new java.awt.BasicStroke(1.0f));
            graphics.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

            graphics.setColor(borderColor);
            graphics.setStroke(new java.awt.BasicStroke(1.5f));
            graphics.drawRoundRect(1, 1, width - 3, height - 3, cornerRadius - 2, cornerRadius - 2);

            graphics.dispose();
        }
    }

    private static class RoundedButton extends JButton {
        public Color color1 = new Color(79, 70, 229);
        public Color color2 = new Color(99, 102, 241);
        private Color baseColor1;
        private Color baseColor2;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (baseColor1 == null || baseColor1 != color1 || baseColor2 != color2) {
                baseColor1 = color1;
                baseColor2 = color2;
            }

            Color c1 = baseColor1;
            Color c2 = baseColor2;
            if (isPressed) {
                c1 = adjustColor(baseColor1, -30);
                c2 = adjustColor(baseColor2, -30);
            } else if (isHovered) {
                c1 = adjustColor(baseColor1, 20);
                c2 = adjustColor(baseColor2, 20);
            }

            GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            
            g2.dispose();
        }

        private Color adjustColor(Color c, int amount) {
            int r = Math.min(255, Math.max(0, c.getRed() + amount));
            int g = Math.min(255, Math.max(0, c.getGreen() + amount));
            int b = Math.min(255, Math.max(0, c.getBlue() + amount));
            return new Color(r, g, b, c.getAlpha());
        }
    }

    private static class RoundedTextField extends JTextField {
        public RoundedTextField(String text) {
            super(text);
            setOpaque(false);
            setBackground(new Color(255, 255, 255, 200));
            setForeground(new Color(15, 23, 42));
            setCaretColor(new Color(79, 70, 229));
            setFont(new Font("SansSerif", Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hasFocus()) {
                g2.setColor(new Color(99, 102, 241, 200));
                g2.setStroke(new java.awt.BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new java.awt.BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }
    }
}
