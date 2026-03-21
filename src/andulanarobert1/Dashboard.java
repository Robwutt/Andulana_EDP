/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package andulanarobert1;

import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.YES_OPTION;

/**
 *
 * @author student
 */
public class Dashboard extends javax.swing.JFrame {
    

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();
        loadDashboardCounts();
        loadDashboardCharts();
        startClock();
        
          jcurrentusername.setText(SessionManager.loggedInFirstName + "!");
    }
    private void loadDashboardCharts() {
    loadDailyChart();
    loadMonthlyChart();
    loadTopSellingChart();
}
  private void startClock() {
    javax.swing.JLabel clockLabel = new javax.swing.JLabel();
    clockLabel.setFont(new java.awt.Font("Segoe UI", 1, 16));
    clockLabel.setForeground(new java.awt.Color(255, 255, 255));
    clockLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    clockLabel.setText("00:00:00");

    jPanelclock.setLayout(new java.awt.BorderLayout());
    jPanelclock.add(clockLabel, java.awt.BorderLayout.CENTER);

    javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String time = String.format("%02d:%02d:%02d",
            now.getHour(), now.getMinute(), now.getSecond());
        String date = String.format("%s %02d, %d",
            now.getMonth().toString().substring(0, 3),
            now.getDayOfMonth(),
            now.getYear());
        clockLabel.setText("<html><center>" + date + "<br>" + time + "</center></html>");
    });
    timer.setInitialDelay(0);
    timer.start();
}

private void loadDailyChart() {
    org.jfree.data.category.DefaultCategoryDataset dataset =
        new org.jfree.data.category.DefaultCategoryDataset();

    java.sql.Connection con = DatabaseConnection.dbConnection();
    try {
        java.sql.ResultSet rs = con.prepareStatement(
            "SELECT DATE(sale_date) AS day, SUM(total_amount) AS total " +
            "FROM sales " +
            "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(sale_date) ORDER BY day ASC"
        ).executeQuery();
        while (rs.next()) {
            dataset.addValue(rs.getDouble("total"), "Sales", rs.getString("day"));
        }
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Daily chart error: " + e.getMessage());
    }

    org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createLineChart(
        "Daily Sales Graph",
        "Date", "Sales (₱)",
        dataset,
        org.jfree.chart.plot.PlotOrientation.VERTICAL,
        true, true, false
    );

    // Style the chart
    chart.setBackgroundPaint(java.awt.Color.WHITE);
    org.jfree.chart.plot.CategoryPlot plot =
        (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
    plot.setBackgroundPaint(java.awt.Color.WHITE);
    plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);

    org.jfree.chart.renderer.category.LineAndShapeRenderer renderer =
        (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new java.awt.Color(0, 102, 153));
    renderer.setSeriesStroke(0, new java.awt.BasicStroke(2.0f));
    renderer.setSeriesShapesVisible(0, true);

    org.jfree.chart.ChartPanel chartPanel =
        new org.jfree.chart.ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(
        jDailysalesgraph.getWidth(), jDailysalesgraph.getHeight()));
    jDailysalesgraph.removeAll();
    jDailysalesgraph.setLayout(new java.awt.BorderLayout());
    jDailysalesgraph.add(chartPanel, java.awt.BorderLayout.CENTER);
    jDailysalesgraph.validate();
    jDailysalesgraph.repaint();
}

private void loadMonthlyChart() {
    org.jfree.data.category.DefaultCategoryDataset dataset =
        new org.jfree.data.category.DefaultCategoryDataset();

    java.sql.Connection con = DatabaseConnection.dbConnection();
    try {
        java.sql.ResultSet rs = con.prepareStatement(
            "SELECT DATE_FORMAT(sale_date, '%Y-%m') AS month, SUM(total_amount) AS total " +
            "FROM sales " +
            "GROUP BY DATE_FORMAT(sale_date, '%Y-%m') ORDER BY month ASC"
        ).executeQuery();
        while (rs.next()) {
            dataset.addValue(rs.getDouble("total"), "Sales", rs.getString("month"));
        }
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Monthly chart error: " + e.getMessage());
    }

    org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
        "Monthly Sales Chart",
        "Month", "Sales (₱)",
        dataset,
        org.jfree.chart.plot.PlotOrientation.VERTICAL,
        true, true, false
    );

    // Style the chart
    chart.setBackgroundPaint(java.awt.Color.WHITE);
    org.jfree.chart.plot.CategoryPlot plot =
        (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
    plot.setBackgroundPaint(java.awt.Color.WHITE);
    plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);

    org.jfree.chart.renderer.category.BarRenderer renderer =
        (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new java.awt.Color(255, 102, 153));

    org.jfree.chart.ChartPanel chartPanel =
        new org.jfree.chart.ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(
        jMonthlysaleschart.getWidth(), jMonthlysaleschart.getHeight()));
    jMonthlysaleschart.removeAll();
    jMonthlysaleschart.setLayout(new java.awt.BorderLayout());
    jMonthlysaleschart.add(chartPanel, java.awt.BorderLayout.CENTER);
    jMonthlysaleschart.validate();
    jMonthlysaleschart.repaint();
}

private void loadTopSellingChart() {
    org.jfree.data.category.DefaultCategoryDataset dataset =
        new org.jfree.data.category.DefaultCategoryDataset();

    java.sql.Connection con = DatabaseConnection.dbConnection();
    try {
        java.sql.ResultSet rs = con.prepareStatement(
            "SELECT p.product_name, SUM(si.quantity) AS total_sold " +
            "FROM sale_items si " +
            "JOIN products p ON si.product_id = p.product_id " +
            "GROUP BY p.product_name " +
            "ORDER BY total_sold DESC LIMIT 5"
        ).executeQuery();
        while (rs.next()) {
            dataset.addValue(rs.getInt("total_sold"), "Qty Sold",
                rs.getString("product_name"));
        }
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Top selling error: " + e.getMessage());
    }

    org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
        "Top Selling Products",
        "Product", "Quantity Sold",
        dataset,
        org.jfree.chart.plot.PlotOrientation.VERTICAL,
        true, true, false
    );

    // Style the chart
    chart.setBackgroundPaint(java.awt.Color.WHITE);
    org.jfree.chart.plot.CategoryPlot plot =
        (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
    plot.setBackgroundPaint(java.awt.Color.WHITE);
    plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);

    org.jfree.chart.renderer.category.BarRenderer renderer =
        (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new java.awt.Color(153, 102, 255));

    org.jfree.chart.ChartPanel chartPanel =
        new org.jfree.chart.ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(
        jtopsellingproducts.getWidth(), jtopsellingproducts.getHeight()));
    jtopsellingproducts.removeAll();
    jtopsellingproducts.setLayout(new java.awt.BorderLayout());
    jtopsellingproducts.add(chartPanel, java.awt.BorderLayout.CENTER);
    jtopsellingproducts.validate();
    jtopsellingproducts.repaint();
}
    private void loadDashboardCounts() {
    String url = "jdbc:mysql://localhost:3306/pos_rob";
    String user = "root";
    String password = ""; // change if you have a password

    try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, user, password)) {

        // Total Users
        try (java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next()) totalusers.setText(String.valueOf(rs.getInt(1)));
        }

        // Total Categories
        try (java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM categories")) {
            if (rs.next()) totalcategories.setText(String.valueOf(rs.getInt(1)));
        }

        // Total Products
        try (java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
            if (rs.next()) totalproducts.setText(String.valueOf(rs.getInt(1)));
        }

        // Total Stock on Hand (sum of all stock_quantity)
        try (java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(stock_quantity), 0) FROM products")) {
            if (rs.next()) totalstock.setText(String.valueOf(rs.getInt(1)));
        }

    } catch (java.sql.SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
    }
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        LogoutBtn = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jCategories = new javax.swing.JButton();
        jSupplies = new javax.swing.JButton();
        jAccounts = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanelclock = new javax.swing.JPanel();
        jcurrentusername = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        totalusers = new javax.swing.JLabel();
        totalcategories = new javax.swing.JLabel();
        totalproducts = new javax.swing.JLabel();
        totalstock = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton5 = new javax.swing.JButton();
        Reports = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jDailysalesgraph = new javax.swing.JPanel();
        jMonthlysaleschart = new javax.swing.JPanel();
        jtopsellingproducts = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 102));

        jPanel1.setBackground(new java.awt.Color(0, 102, 153));
        jPanel1.setToolTipText("");

        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Products");

        LogoutBtn.setBackground(new java.awt.Color(255, 51, 0));
        LogoutBtn.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\Exit.png")); // NOI18N
        LogoutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogoutBtnActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(0, 102, 153));
        jButton1.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\order.png")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Tahoma", 3, 50)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("POS STORE");

        jButton2.setBackground(new java.awt.Color(0, 102, 153));
        jButton2.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\inventory (3).png")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jCategories.setBackground(new java.awt.Color(0, 102, 153));
        jCategories.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\categories.png")); // NOI18N
        jCategories.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCategoriesActionPerformed(evt);
            }
        });

        jSupplies.setBackground(new java.awt.Color(0, 102, 153));
        jSupplies.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\delivery-man.png")); // NOI18N
        jSupplies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSuppliesActionPerformed(evt);
            }
        });

        jAccounts.setBackground(new java.awt.Color(0, 102, 153));
        jAccounts.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\Accsec.png")); // NOI18N
        jAccounts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAccountsActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Inventory");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Category");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Suppliers");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Accounts");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSupplies, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addGap(137, 137, 137)
                        .addComponent(jLabel10))
                    .addComponent(jAccounts, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addComponent(LogoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(428, 428, 428)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(60, 60, 60)
                                        .addComponent(jLabel3))
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel5))
                                .addGap(0, 1, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 32, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jCategories, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSupplies, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jAccounts, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addGap(6, 6, 6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(LogoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addGap(12, 12, 12))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jPanel5.setBackground(new java.awt.Color(0, 102, 153));

        jLabel1.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\home.png")); // NOI18N
        jLabel1.setText("jLabel1");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Welcome,");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("to the POS Inventory system!");

        jPanelclock.setBackground(new java.awt.Color(0, 102, 153));

        javax.swing.GroupLayout jPanelclockLayout = new javax.swing.GroupLayout(jPanelclock);
        jPanelclock.setLayout(jPanelclockLayout);
        jPanelclockLayout.setHorizontalGroup(
            jPanelclockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 123, Short.MAX_VALUE)
        );
        jPanelclockLayout.setVerticalGroup(
            jPanelclockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 41, Short.MAX_VALUE)
        );

        jcurrentusername.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jcurrentusername.setForeground(new java.awt.Color(255, 255, 255));
        jcurrentusername.setText(".");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcurrentusername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addGap(159, 159, 159)
                .addComponent(jPanelclock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelclock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jcurrentusername))
                        .addComponent(jLabel1)))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jLabel17.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\group.png")); // NOI18N

        jLabel15.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\categpics.png")); // NOI18N

        jLabel16.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\boxes.png")); // NOI18N

        jLabel18.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\box.png")); // NOI18N

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setText("Total Users:");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel20.setText("Categories:");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setText("Products:");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel22.setText("Stock on Hand:");

        totalusers.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        totalusers.setText("0");

        totalcategories.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        totalcategories.setText("0");

        totalproducts.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        totalproducts.setText("0");

        totalstock.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        totalstock.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(totalusers)
                .addGap(71, 71, 71)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalcategories, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(73, 73, 73)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalproducts, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalstock, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(53, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel18)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel19)
                        .addComponent(totalusers))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel20)
                        .addComponent(totalcategories))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel22)
                        .addComponent(totalstock))
                    .addComponent(jLabel16)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel21)
                        .addComponent(totalproducts)))
                .addGap(0, 19, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jPanel7.setBackground(new java.awt.Color(0, 102, 153));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\wrench.png")); // NOI18N
        jLabel23.setText("Tools");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel23)
                .addGap(23, 23, 23))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel23)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jButton3.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\cashier.png")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\payrec.png")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel24.setText("POS register");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel25.setText("Sales History");

        jButton5.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\ReportModul.png")); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        Reports.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        Reports.setText("Reports");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel24)
                                .addComponent(jLabel25)))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGap(14, 14, 14)
                            .addComponent(Reports, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButton3)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Reports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jDailysalesgraph.setBackground(new java.awt.Color(255, 255, 255));
        jDailysalesgraph.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jDailysalesgraphLayout = new javax.swing.GroupLayout(jDailysalesgraph);
        jDailysalesgraph.setLayout(jDailysalesgraphLayout);
        jDailysalesgraphLayout.setHorizontalGroup(
            jDailysalesgraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 470, Short.MAX_VALUE)
        );
        jDailysalesgraphLayout.setVerticalGroup(
            jDailysalesgraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jMonthlysaleschart.setBackground(new java.awt.Color(255, 255, 255));
        jMonthlysaleschart.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jMonthlysaleschartLayout = new javax.swing.GroupLayout(jMonthlysaleschart);
        jMonthlysaleschart.setLayout(jMonthlysaleschartLayout);
        jMonthlysaleschartLayout.setHorizontalGroup(
            jMonthlysaleschartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 443, Short.MAX_VALUE)
        );
        jMonthlysaleschartLayout.setVerticalGroup(
            jMonthlysaleschartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 142, Short.MAX_VALUE)
        );

        jtopsellingproducts.setBackground(new java.awt.Color(255, 255, 255));
        jtopsellingproducts.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jtopsellingproductsLayout = new javax.swing.GroupLayout(jtopsellingproducts);
        jtopsellingproducts.setLayout(jtopsellingproductsLayout);
        jtopsellingproductsLayout.setHorizontalGroup(
            jtopsellingproductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jtopsellingproductsLayout.setVerticalGroup(
            jtopsellingproductsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 142, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jDailysalesgraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jMonthlysaleschart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jtopsellingproducts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jMonthlysaleschart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtopsellingproducts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jDailysalesgraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(23, 23, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
         AddProduct db = new  AddProduct();
        db.show();
        this.dispose();
       
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
         Inventory db = new  Inventory();
        db.show();
        this.dispose();
       
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jCategoriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCategoriesActionPerformed
        // TODO add your handling code here:
        Categories db = new  Categories();
        db.show();
        this.dispose();
    }//GEN-LAST:event_jCategoriesActionPerformed

    private void jSuppliesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSuppliesActionPerformed
        // TODO add your handling code here:
        Suppliers db = new  Suppliers();
        db.show();
        this.dispose();
    }//GEN-LAST:event_jSuppliesActionPerformed

    private void jAccountsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAccountsActionPerformed
        // TODO add your handling code here:
        createaccount db = new  createaccount();
        db.show();
        this.dispose();
    }//GEN-LAST:event_jAccountsActionPerformed

    private void LogoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogoutBtnActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(null, "Do you want to Logout?", "logout",JOptionPane.YES_NO_OPTION);
        if(confirm == YES_OPTION){
            LoginScreen db = new LoginScreen ();
            db.show();
            this.dispose();
        }else{

        }
    }//GEN-LAST:event_LogoutBtnActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
          postool db = new  postool();
        db.show();
       
       
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
         SalesHistory db = new  SalesHistory();
        db.show();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        Reports db = new  Reports();
        db.show();
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton LogoutBtn;
    private javax.swing.JLabel Reports;
    private javax.swing.JButton jAccounts;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jCategories;
    private javax.swing.JPanel jDailysalesgraph;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jMonthlysaleschart;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanelclock;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton jSupplies;
    private javax.swing.JLabel jcurrentusername;
    private javax.swing.JPanel jtopsellingproducts;
    private javax.swing.JLabel totalcategories;
    private javax.swing.JLabel totalproducts;
    private javax.swing.JLabel totalstock;
    private javax.swing.JLabel totalusers;
    // End of variables declaration//GEN-END:variables
}
