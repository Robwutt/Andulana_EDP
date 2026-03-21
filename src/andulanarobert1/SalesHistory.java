package andulanarobert1;

import java.sql.Connection;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.YES_OPTION;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rpand
 */
public class SalesHistory extends javax.swing.JFrame {

    /**
     * Creates new form SalesHistory
     */
    public SalesHistory() {
        initComponents();
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

    addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Return to Dashboard?",
                "Exit Sales History",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == YES_OPTION) {
                dispose();
            }
        }
    });
        loadSalesHistory();

        jsearchsale.addActionListener(e -> searchSales());
        jviewtransac.addActionListener(e -> viewTransactionDetails());
        jexportpdf2.addActionListener(e -> exportToPDF());
        jexportexcel.setEnabled(false);
    }
     private void loadSalesHistory() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"sale_id", "ID", "Cashier", "Invoice no.", "Total Amount", "Payment", "Date"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jTablesalehistory.setModel(model);
        jTablesalehistory.getColumnModel().getColumn(0).setMinWidth(0);
        jTablesalehistory.getColumnModel().getColumn(0).setMaxWidth(0);
        jTablesalehistory.getColumnModel().getColumn(0).setWidth(0);

        Connection con = DatabaseConnection.dbConnection();
        try {
            ResultSet rs = con.prepareStatement(
                "SELECT s.sale_id, u.user_id, CONCAT(u.first_name, ' ', u.last_name) AS cashier, " +
                "s.invoice_code, s.total_amount, s.payment_method, s.sale_date " +
                "FROM sales s JOIN users u ON s.user_id = u.user_id " +
                "ORDER BY s.sale_date DESC"
            ).executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("sale_id"),   // ← column 0 hidden
                    rs.getInt("user_id"),   // ← column 1 visible as ID
                    rs.getString("cashier"),
                    rs.getString("invoice_code"),
                    String.format("₱%.2f", rs.getDouble("total_amount")),
                    rs.getString("payment_method"),
                    rs.getString("sale_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sales: " + e.getMessage());
        }

        loadStatistics();
    }

    private void loadStatistics() {
        Connection con = DatabaseConnection.dbConnection();
        try {
            // Total Sales
            ResultSet rs = con.prepareStatement(
                "SELECT COALESCE(SUM(total_amount), 0) FROM sales"
            ).executeQuery();
            if (rs.next())
                jtotsale.setText(String.format("₱%.2f", rs.getDouble(1)));

            // Total Transactions
            rs = con.prepareStatement(
                "SELECT COUNT(*) FROM sales"
            ).executeQuery();
            if (rs.next())
                jtottrans.setText(String.valueOf(rs.getInt(1)));

            // Total Items Sold
            rs = con.prepareStatement(
                "SELECT COALESCE(SUM(quantity), 0) FROM sale_items"
            ).executeQuery();
            if (rs.next())
                jtotitemsold.setText(String.valueOf(rs.getInt(1)));

            // Total Profit
            rs = con.prepareStatement(
                "SELECT COALESCE(SUM(total_amount), 0) FROM sales"
            ).executeQuery();
            if (rs.next())
                jtotprof.setText(String.format("₱%.2f", rs.getDouble(1)));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Stats error: " + e.getMessage());
        }
    }

    private void searchSales() {
        String filter  = jcombosalefilter.getSelectedItem().toString();
    String keyword = jsearchbar.getText().trim();

    String orderBy;
    switch (filter) {
        case "Date":
            orderBy = "s.sale_date ASC";
            break;
        case "Cashier":
            orderBy = "u.first_name ASC";
            break;
        case "Invoice":
            orderBy = "s.invoice_code ASC";
            break;
        default:
            orderBy = "s.sale_date DESC";
            break;
    }

    String whereClause = "";
    if (!keyword.isEmpty()) {
        whereClause = "WHERE s.invoice_code LIKE '%" + keyword + "%' " +
                      "OR CONCAT(u.first_name, ' ', u.last_name) LIKE '%" + keyword + "%' " +
                      "OR DATE(s.sale_date) LIKE '%" + keyword + "%'";
    }

    String sql = "SELECT s.sale_id, u.user_id, CONCAT(u.first_name, ' ', u.last_name) AS cashier, " +
             "s.invoice_code, s.total_amount, s.payment_method, s.sale_date " +
             "FROM sales s JOIN users u ON s.user_id = u.user_id " +
             whereClause + " ORDER BY " + orderBy;

    DefaultTableModel model = new DefaultTableModel(
        new String[]{"sale_id", "ID", "Cashier", "Invoice no.", "Total Amount", "Payment", "Date"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    jTablesalehistory.setModel(model);
    jTablesalehistory.getColumnModel().getColumn(0).setMinWidth(0);
    jTablesalehistory.getColumnModel().getColumn(0).setMaxWidth(0);
    jTablesalehistory.getColumnModel().getColumn(0).setWidth(0);

    Connection con = DatabaseConnection.dbConnection();
    try {
        ResultSet rs = con.prepareStatement(sql).executeQuery();
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("sale_id"),   // ← hidden, used for lookup
                rs.getInt("user_id"),   // ← shown as ID column
                rs.getString("cashier"),
                rs.getString("invoice_code"),
                String.format("₱%.2f", rs.getDouble("total_amount")),
                rs.getString("payment_method"),
                rs.getString("sale_date")
            });
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage());
    }
    }

    private void viewTransactionDetails() {
        int row = jTablesalehistory.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a transaction to view.");
        return;
    }

    int    saleId  = Integer.parseInt(jTablesalehistory.getValueAt(row, 0).toString());
    String userId  = jTablesalehistory.getValueAt(row, 1).toString();
    String cashier = jTablesalehistory.getValueAt(row, 2).toString();
    String invoice = jTablesalehistory.getValueAt(row, 3).toString();
    String total   = jTablesalehistory.getValueAt(row, 4).toString();
    String payment = jTablesalehistory.getValueAt(row, 5).toString();
    String date    = jTablesalehistory.getValueAt(row, 6).toString();

    DefaultTableModel itemModel = new DefaultTableModel(
        new String[]{"Product", "Qty", "Unit Price", "Total"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    Connection con = DatabaseConnection.dbConnection();
    try {
        ResultSet rs = con.prepareStatement(
            "SELECT p.product_name, si.quantity, si.unit_price, si.total_price " +
            "FROM sale_items si JOIN products p ON si.product_id = p.product_id " +
            "WHERE si.sale_id = " + saleId
        ).executeQuery();

        while (rs.next()) {
            itemModel.addRow(new Object[]{
                rs.getString("product_name"),
                rs.getInt("quantity"),
                String.format("₱%.2f", rs.getDouble("unit_price")),
                String.format("₱%.2f", rs.getDouble("total_price"))
            });
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        return;
    }

    javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Transaction Details", true);
    dialog.setLayout(new java.awt.BorderLayout());
    dialog.setSize(550, 450);
    dialog.setLocationRelativeTo(this);

    javax.swing.JTextArea info = new javax.swing.JTextArea(
        "Invoice #    : " + invoice + "\n" +
        "Cashier      : " + cashier + "\n" +
        "User ID      : " + userId  + "\n" +
        "Date         : " + date    + "\n" +
        "Payment      : " + payment + "\n" +
        "Total Amount : " + total   + "\n"
    );
    info.setEditable(false);
    info.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
    info.setBackground(new java.awt.Color(240, 240, 240));
    info.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

    javax.swing.JTable itemTable = new javax.swing.JTable(itemModel);
    javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(itemTable);

    dialog.add(info, java.awt.BorderLayout.NORTH);
    dialog.add(scrollPane, java.awt.BorderLayout.CENTER);
    dialog.setVisible(true);
    }

    private void exportToPDF() {
        if (jTablesalehistory.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setSelectedFile(new java.io.File("sales_history.pdf"));
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

            doc.add(new com.itextpdf.text.Paragraph("Sales History Report", titleFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));

            for (int i = 0; i < jTablesalehistory.getRowCount(); i++) {
                doc.add(new com.itextpdf.text.Paragraph(
                    "ID: "         + jTablesalehistory.getValueAt(i, 1) +
                    " | Cashier: " + jTablesalehistory.getValueAt(i, 2) +
                    " | Invoice: " + jTablesalehistory.getValueAt(i, 3) +
                    " | Total: "   + jTablesalehistory.getValueAt(i, 4) +
                    " | Payment: " + jTablesalehistory.getValueAt(i, 5) +
                    " | Date: "    + jTablesalehistory.getValueAt(i, 6),
                    normal));
            }
            doc.close();
            JOptionPane.showMessageDialog(this, "✅ PDF exported successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "PDF error: " + e.getMessage());
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
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jcombosalefilter = new javax.swing.JComboBox<>();
        jsearchbar = new javax.swing.JTextField();
        jsearchsale = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTablesalehistory = new javax.swing.JTable();
        jexportexcel = new javax.swing.JButton();
        jexportpdf2 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jviewtransac = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jtotprof = new javax.swing.JLabel();
        jtotsale = new javax.swing.JLabel();
        jtottrans = new javax.swing.JLabel();
        jtotitemsold = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(0, 102, 153));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Sales History ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addContainerGap(738, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 980, 70));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(0, 102, 153));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Total Overview");

        jcombosalefilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Date", "Cashier", "Invoice", " " }));

        jsearchsale.setText("Search");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel2)
                .addGap(94, 94, 94)
                .addComponent(jcombosalefilter, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addComponent(jsearchbar, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jsearchsale)
                .addContainerGap(105, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jcombosalefilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jsearchbar, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jsearchsale))
                .addContainerGap())
        );

        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 880, 50));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jLabel3.setText("Total Profit:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 90, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jLabel4.setText("Total Sales:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jLabel5.setText("Total Transactions:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 90, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jLabel6.setText("Total items sold:");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 90, -1, -1));

        jTablesalehistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Cashier", "Invoice no.", "Date started", "Date Ended"
            }
        ));
        jScrollPane1.setViewportView(jTablesalehistory);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 127, 670, 350));

        jexportexcel.setBackground(new java.awt.Color(102, 255, 0));
        jexportexcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jexportexcel.setForeground(new java.awt.Color(255, 255, 255));
        jexportexcel.setText("Export to Excel");
        jPanel2.add(jexportexcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 340, 130, 40));

        jexportpdf2.setBackground(new java.awt.Color(255, 51, 51));
        jexportpdf2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jexportpdf2.setForeground(new java.awt.Color(255, 255, 255));
        jexportpdf2.setText("Export to PDF");
        jPanel2.add(jexportpdf2, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 270, 130, 40));
        jPanel2.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 230, 160, 10));

        jviewtransac.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jviewtransac.setText("View");
        jviewtransac.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jviewtransacActionPerformed(evt);
            }
        });
        jPanel2.add(jviewtransac, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 190, 120, 30));

        jLabel7.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\profits.png")); // NOI18N
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 60, 40, -1));

        jLabel8.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\dollar-symbol.png")); // NOI18N
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 60, 30, -1));

        jLabel9.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\payment-method.png")); // NOI18N
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 60, 40, -1));

        jLabel10.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\checklist.png")); // NOI18N
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 60, 40, -1));

        jtotprof.setForeground(new java.awt.Color(51, 255, 51));
        jtotprof.setText("+");
        jPanel2.add(jtotprof, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 110, -1, 10));

        jtotsale.setForeground(new java.awt.Color(51, 255, 51));
        jtotsale.setText("+");
        jPanel2.add(jtotsale, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 110, -1, 10));

        jtottrans.setForeground(new java.awt.Color(51, 255, 51));
        jtottrans.setText("+");
        jPanel2.add(jtottrans, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 110, -1, 10));

        jtotitemsold.setForeground(new java.awt.Color(51, 255, 51));
        jtotitemsold.setText("+");
        jPanel2.add(jtotitemsold, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 110, -1, 10));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 880, 500));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jviewtransacActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jviewtransacActionPerformed
        // TODO add your handling code here:
         viewTransactionDetails();
    }//GEN-LAST:event_jviewtransacActionPerformed

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
            java.util.logging.Logger.getLogger(SalesHistory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SalesHistory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SalesHistory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SalesHistory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SalesHistory().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTablesalehistory;
    private javax.swing.JComboBox<String> jcombosalefilter;
    private javax.swing.JButton jexportexcel;
    private javax.swing.JButton jexportpdf2;
    private javax.swing.JTextField jsearchbar;
    private javax.swing.JButton jsearchsale;
    private javax.swing.JLabel jtotitemsold;
    private javax.swing.JLabel jtotprof;
    private javax.swing.JLabel jtotsale;
    private javax.swing.JLabel jtottrans;
    private javax.swing.JButton jviewtransac;
    // End of variables declaration//GEN-END:variables
}
