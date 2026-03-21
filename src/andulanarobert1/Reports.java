/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package andulanarobert1;

import java.sql.Connection;
import java.sql.ResultSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author rpand
 */
public class Reports extends javax.swing.JFrame {

    /**
     * Creates new form Reports
     */
    public Reports() {
        initComponents();
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
            }
        });

        loadDailySalesChart();
        loadMonthlySalesChart();
        loadProfitChart();
        loadInventoryReport();
        loadLowStockReport();

        jinventoryreportcombobox.addActionListener(e -> loadInventoryReport());
        jComboBox2.addActionListener(e -> loadLowStockReport());
    }

    // ════════════════════════════════════════
    // DAILY SALES CHART (Last 7 days)
    // ════════════════════════════════════════
    private void loadDailySalesChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Connection con = DatabaseConnection.dbConnection();
        try {
            ResultSet rs = con.prepareStatement(
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

        JFreeChart chart = ChartFactory.createBarChart(
            "Daily Sales - Last 7 Days",
            "Date", "Amount (₱)",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(
            jPaneldailysales.getWidth(), jPaneldailysales.getHeight()));
        jPaneldailysales.removeAll();
        jPaneldailysales.setLayout(new java.awt.BorderLayout());
        jPaneldailysales.add(chartPanel, java.awt.BorderLayout.CENTER);
        jPaneldailysales.validate();
        jPaneldailysales.repaint();
    }

    // ════════════════════════════════════════
    // MONTHLY SALES CHART
    // ════════════════════════════════════════
    private void loadMonthlySalesChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Connection con = DatabaseConnection.dbConnection();
        try {
            ResultSet rs = con.prepareStatement(
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

        JFreeChart chart = ChartFactory.createBarChart(
            "Monthly Sales",
            "Month", "Amount (₱)",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(
            jPanelmonthsales.getWidth(), jPanelmonthsales.getHeight()));
        jPanelmonthsales.removeAll();
        jPanelmonthsales.setLayout(new java.awt.BorderLayout());
        jPanelmonthsales.add(chartPanel, java.awt.BorderLayout.CENTER);
        jPanelmonthsales.validate();
        jPanelmonthsales.repaint();
    }

    // ════════════════════════════════════════
    // PROFIT LINE CHART
    // ════════════════════════════════════════
    private void loadProfitChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Connection con = DatabaseConnection.dbConnection();
        try {
            ResultSet rs = con.prepareStatement(
                "SELECT DATE_FORMAT(sale_date, '%Y-%m') AS month, SUM(total_amount) AS profit " +
                "FROM sales " +
                "GROUP BY DATE_FORMAT(sale_date, '%Y-%m') ORDER BY month ASC"
            ).executeQuery();
            while (rs.next()) {
                dataset.addValue(rs.getDouble("profit"), "Profit", rs.getString("month"));
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Profit chart error: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Profit Trend",
            "Month", "Amount (₱)",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(
            jpanelprofitreport.getWidth(), jpanelprofitreport.getHeight()));
        jpanelprofitreport.removeAll();
        jpanelprofitreport.setLayout(new java.awt.BorderLayout());
        jpanelprofitreport.add(chartPanel, java.awt.BorderLayout.CENTER);
        jpanelprofitreport.validate();
        jpanelprofitreport.repaint();
    }

    // ════════════════════════════════════════
    // INVENTORY REPORT TABLE
    // ════════════════════════════════════════
    private void loadInventoryReport() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new String[]{"Date Added", "Category", "Item", "Quantity"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jinventoryreptable.setModel(model);

        String filter = jinventoryreportcombobox.getSelectedItem().toString();
        String orderBy = filter.equals("Category") ? "p.category_name ASC" : "p.date_added DESC";

        Connection con = DatabaseConnection.dbConnection();
        try {
            ResultSet rs = con.prepareStatement(
                "SELECT p.date_added, p.category_name, p.product_name, p.stock_quantity " +
                "FROM products p ORDER BY " + orderBy
            ).executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("date_added"),
                    rs.getString("category_name"),
                    rs.getString("product_name"),
                    rs.getInt("stock_quantity")
                });
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Inventory report error: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════
    // LOW STOCK REPORT TABLE
    // ════════════════════════════════════════
    private void loadLowStockReport() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new String[]{"Date Added", "Category", "Item", "Restock Level"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jlowstockreptable.setModel(model);

        String filter = jComboBox2.getSelectedItem().toString();
        String orderBy = filter.equals("Category") ? "p.category_name ASC" : "p.date_added DESC";

        Connection con = DatabaseConnection.dbConnection();
        try {
            ResultSet rs = con.prepareStatement(
                "SELECT p.date_added, p.category_name, p.product_name, p.reorder_level " +
                "FROM products p " +
                "WHERE p.stock_quantity <= p.reorder_level " +
                "ORDER BY " + orderBy
            ).executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("date_added"),
                    rs.getString("category_name"),
                    rs.getString("product_name"),
                    rs.getInt("reorder_level")
                });
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Low stock error: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════
    // EXPORT CHARTS TO PDF
    // ════════════════════════════════════════
    private void exportChartsToPDF() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setSelectedFile(new java.io.File("charts_report.pdf"));
        if (chooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".pdf")) path += ".pdf";

        com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(
                doc, new java.io.FileOutputStream(path));
            doc.open();

            com.itextpdf.text.Font titleFont =
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16,
                    com.itextpdf.text.Font.BOLD);

            doc.add(new com.itextpdf.text.Paragraph("Sales & Profit Report", titleFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));
            doc.add(new com.itextpdf.text.Paragraph(
                "Charts have been generated and are visible in the Reports window."));
            doc.add(new com.itextpdf.text.Paragraph(" "));
            doc.add(new com.itextpdf.text.Paragraph("Generated on: " + new java.util.Date()));

            doc.close();
            javax.swing.JOptionPane.showMessageDialog(this, "✅ PDF exported successfully!");

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "PDF error: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════
    // EXPORT MISC TO PDF
    // ════════════════════════════════════════
    private void exportMiscToPDF() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setSelectedFile(new java.io.File("misc_report.pdf"));
        if (chooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".pdf")) path += ".pdf";

        com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(
                doc, new java.io.FileOutputStream(path));
            doc.open();

            com.itextpdf.text.Font titleFont =
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16,
                    com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normal =
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                    com.itextpdf.text.Font.NORMAL);

            doc.add(new com.itextpdf.text.Paragraph("Inventory Report", titleFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));
            for (int i = 0; i < jinventoryreptable.getRowCount(); i++) {
                doc.add(new com.itextpdf.text.Paragraph(
                    "Date: "    + jinventoryreptable.getValueAt(i, 0) +
                    " | Cat: "  + jinventoryreptable.getValueAt(i, 1) +
                    " | Item: " + jinventoryreptable.getValueAt(i, 2) +
                    " | Qty: "  + jinventoryreptable.getValueAt(i, 3),
                    normal));
            }

            doc.add(new com.itextpdf.text.Paragraph(" "));

            doc.add(new com.itextpdf.text.Paragraph("Low Stock Report", titleFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));
            for (int i = 0; i < jlowstockreptable.getRowCount(); i++) {
                doc.add(new com.itextpdf.text.Paragraph(
                    "Date: "       + jlowstockreptable.getValueAt(i, 0) +
                    " | Cat: "     + jlowstockreptable.getValueAt(i, 1) +
                    " | Item: "    + jlowstockreptable.getValueAt(i, 2) +
                    " | Restock: " + jlowstockreptable.getValueAt(i, 3),
                    normal));
            }

            doc.close();
            javax.swing.JOptionPane.showMessageDialog(this, "✅ PDF exported successfully!");

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "PDF error: " + e.getMessage());
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
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPaneldailysales = new javax.swing.JPanel();
        jPanelmonthsales = new javax.swing.JPanel();
        jpanelprofitreport = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jlowstockcombobox = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jinventoryreptable = new javax.swing.JTable();
        jinventoryreportcombobox = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jlowstockreptable = new javax.swing.JTable();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(0, 102, 153));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Reports");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(422, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 570, 60));
        getContentPane().add(jTabbedPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 120, -1, -1));

        jTabbedPane4.setBackground(new java.awt.Color(0, 102, 153));
        jTabbedPane4.setForeground(new java.awt.Color(255, 255, 255));

        jPaneldailysales.setBackground(new java.awt.Color(255, 255, 255));
        jPaneldailysales.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPaneldailysalesLayout = new javax.swing.GroupLayout(jPaneldailysales);
        jPaneldailysales.setLayout(jPaneldailysalesLayout);
        jPaneldailysalesLayout.setHorizontalGroup(
            jPaneldailysalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 436, Short.MAX_VALUE)
        );
        jPaneldailysalesLayout.setVerticalGroup(
            jPaneldailysalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 221, Short.MAX_VALUE)
        );

        jTabbedPane4.addTab("Daily Sales", jPaneldailysales);

        jPanelmonthsales.setBackground(new java.awt.Color(255, 255, 255));
        jPanelmonthsales.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanelmonthsalesLayout = new javax.swing.GroupLayout(jPanelmonthsales);
        jPanelmonthsales.setLayout(jPanelmonthsalesLayout);
        jPanelmonthsalesLayout.setHorizontalGroup(
            jPanelmonthsalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 436, Short.MAX_VALUE)
        );
        jPanelmonthsalesLayout.setVerticalGroup(
            jPanelmonthsalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 221, Short.MAX_VALUE)
        );

        jTabbedPane4.addTab("Monthly Sales", jPanelmonthsales);

        jpanelprofitreport.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jpanelprofitreportLayout = new javax.swing.GroupLayout(jpanelprofitreport);
        jpanelprofitreport.setLayout(jpanelprofitreportLayout);
        jpanelprofitreportLayout.setHorizontalGroup(
            jpanelprofitreportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 440, Short.MAX_VALUE)
        );
        jpanelprofitreportLayout.setVerticalGroup(
            jpanelprofitreportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 225, Short.MAX_VALUE)
        );

        jTabbedPane4.addTab("Profit Reports", jpanelprofitreport);

        getContentPane().add(jTabbedPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 440, 260));

        jPanel6.setBackground(new java.awt.Color(0, 102, 153));

        jlowstockcombobox.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jinventoryreptable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Date", "Category", "item", "Quantity"
            }
        ));
        jScrollPane1.setViewportView(jinventoryreptable);

        jinventoryreportcombobox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Date", "Category" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jinventoryreportcombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jinventoryreportcombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jlowstockcombobox.addTab("Inventory Report", jPanel2);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jlowstockreptable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Date", "Category", "item", "Restock level"
            }
        ));
        jScrollPane2.setViewportView(jlowstockreptable);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Date", "Category" }));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jlowstockcombobox.addTab("Low Stock Report", jPanel4);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Misc.");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jlowstockcombobox)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlowstockcombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        getContentPane().add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 350, 440, 310));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 570, 10));

        jToggleButton1.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jToggleButton1.setText("Export to PDF");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jToggleButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 350, 110, 30));

        jToggleButton3.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jToggleButton3.setText("Export to PDF");
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jToggleButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 100, 110, 30));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        // TODO add your handling code here:
        exportChartsToPDF();
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // TODO add your handling code here:
        exportMiscToPDF();
    }//GEN-LAST:event_jToggleButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(Reports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Reports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Reports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Reports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Reports().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPaneldailysales;
    private javax.swing.JPanel jPanelmonthsales;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JComboBox<String> jinventoryreportcombobox;
    private javax.swing.JTable jinventoryreptable;
    private javax.swing.JTabbedPane jlowstockcombobox;
    private javax.swing.JTable jlowstockreptable;
    private javax.swing.JPanel jpanelprofitreport;
    // End of variables declaration//GEN-END:variables
}
