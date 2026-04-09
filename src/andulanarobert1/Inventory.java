/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package andulanarobert1;

import java.sql.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Image;
import static javax.swing.JOptionPane.YES_OPTION;

/**
 *
 * @author rpand
 */
public class Inventory extends javax.swing.JFrame {

    /**
     * Creates new form Inventory
     */
    public Inventory() {
        initComponents();
        // Load all tables on startup
        loadStockInTable();
        loadStockOutTable();
        loadAllStock("", "All");
        loadLowStock("");
        loadTransactionHistory();
        loadCategoryFilter();

        // Stock In table selection
jtablestockin.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int row = jtablestockin.getSelectedRow();
        if (row >= 0) {
            String productId = jtablestockin.getValueAt(row, 0).toString();
            loadProductImage(productId, jLabel8);
            jLabel6.setText(new java.util.Date().toString());
        }
    }
});

// Stock Out table selection
jtablestockout.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int row = jtablestockout.getSelectedRow();
        if (row >= 0) {
            String productId = jtablestockout.getValueAt(row, 0).toString();
            loadProductImage(productId, jLabel20);
            jLabel18.setText(new java.util.Date().toString());
        }
    }
});

        // Overview search field - filters table as you type
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String search = jTextField1.getText().trim();
                String category = jComboBox1.getSelectedItem() != null ?
                    jComboBox1.getSelectedItem().toString() : "All";
                loadAllStock(search, category);
                loadLowStock(search);
            }
        });

        // Overview category filter combobox - filters table when category changes
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String search = jTextField1.getText().trim();
                String category = jComboBox1.getSelectedItem() != null ?
                    jComboBox1.getSelectedItem().toString() : "All";
                loadAllStock(search, category);
                loadLowStock(search);
            }
        });

        // Stock In buttons
        jsave1.addActionListener(e -> saveStockIn());       // jsave1 = Save button in Stock In
        jdelete1.addActionListener(e -> deleteStockIn());   // jdelete1 = Delete button in Stock In
        jclear1.addActionListener(e -> clearStockIn());     // jclear1 = Clear button in Stock In

        // Stock Out buttons
        jsave2.addActionListener(e -> saveStockOut());      // jsave2 = Save button in Stock Out
        jdelete2.addActionListener(e -> deleteStockOut());  // jdelete2 = Delete button in Stock Out
        jclear2.addActionListener(e -> clearStockOut());    // jclear2 = Clear button in Stock Out
    }

    // ==================== LOAD STOCK IN TABLE ====================
    // Loads all products with their latest Stock In transaction info
    private void loadStockInTable() {
        Connection con = DatabaseConnection.dbConnection();
        DefaultTableModel dtm = (DefaultTableModel) jtablestockin.getModel();
        dtm.setRowCount(0);
        try {
            String sql = "SELECT p.product_id, p.product_name, p.stock_quantity, " +
                         "it.reason, it.reference_number " +
                         "FROM products p " +
                         "LEFT JOIN inventory_transactions it ON p.product_id = it.product_id " +
                         "AND it.transaction_type = 'Stock In' " +
                         "GROUP BY p.product_id ORDER BY p.product_name ASC";
            ResultSet rs = con.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                dtm.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("stock_quantity"),
                    rs.getString("reason"),
                    rs.getString("reference_number")
                });
            }
        } catch (Exception ex) {
            System.out.println("loadStockInTable error: " + ex);
        }
    }

    // ==================== LOAD STOCK OUT TABLE ====================
    // Loads all products with their latest Stock Out transaction info
    private void loadStockOutTable() {
        Connection con = DatabaseConnection.dbConnection();
        DefaultTableModel dtm = (DefaultTableModel) jtablestockout.getModel();
        dtm.setRowCount(0);
        try {
            String sql = "SELECT p.product_id, p.product_name, p.stock_quantity, " +
                         "it.reason, it.reference_number " +
                         "FROM products p " +
                         "LEFT JOIN inventory_transactions it ON p.product_id = it.product_id " +
                         "AND it.transaction_type = 'Stock Out' " +
                         "GROUP BY p.product_id ORDER BY p.product_name ASC";
            ResultSet rs = con.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                dtm.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("stock_quantity"),
                    rs.getString("reason"),
                    rs.getString("reference_number")
                });
            }
        } catch (Exception ex) {
            System.out.println("loadStockOutTable error: " + ex);
        }
    }

    // ==================== LOAD ALL STOCK (Overview - All Stock tab) ====================
    // Shows all products filtered by search text and category
    private void loadAllStock(String search, String category) {
        Connection con = DatabaseConnection.dbConnection();
        DefaultTableModel dtm = (DefaultTableModel) jtablestock1.getModel();
        dtm.setRowCount(0);
        try {
            PreparedStatement pst;
            if (category == null || category.equals("All")) {
                String sql = "SELECT product_name, category_name, stock_quantity FROM products " +
                             "WHERE product_name LIKE ? ORDER BY product_name ASC";
                pst = con.prepareStatement(sql);
                pst.setString(1, "%" + search + "%");
            } else {
                String sql = "SELECT product_name, category_name, stock_quantity FROM products " +
                             "WHERE product_name LIKE ? AND category_name = ? ORDER BY product_name ASC";
                pst = con.prepareStatement(sql);
                pst.setString(1, "%" + search + "%");
                pst.setString(2, category);
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                dtm.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("category_name"),
                    rs.getInt("stock_quantity")
                });
            }
        } catch (Exception ex) {
            System.out.println("loadAllStock error: " + ex);
        }
    }

    // ==================== LOAD LOW STOCK (Overview - Low Stock tab) ====================
    // Shows products where stock_quantity <= reorder_level
    private void loadLowStock(String search) {
        Connection con = DatabaseConnection.dbConnection();
        DefaultTableModel dtm = (DefaultTableModel) jtablestock2.getModel();
        dtm.setRowCount(0);
        try {
            String sql = "SELECT product_name, category_name, stock_quantity FROM products " +
                         "WHERE stock_quantity <= reorder_level AND product_name LIKE ? " +
                         "ORDER BY stock_quantity ASC";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, "%" + search + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                dtm.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("category_name"),
                    rs.getInt("stock_quantity")
                });
            }
        } catch (Exception ex) {
            System.out.println("loadLowStock error: " + ex);
        }
    }

    // ==================== LOAD TRANSACTION HISTORY ====================
    // Shows all inventory transactions joined with product names
    private void loadTransactionHistory() {
        Connection con = DatabaseConnection.dbConnection();
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        dtm.setRowCount(0);
        try {
            String sql = "SELECT p.product_name, it.transaction_type, it.quantity, " +
                         "it.reference_number, it.transaction_date, it.reason " +
                         "FROM inventory_transactions it " +
                         "JOIN products p ON it.product_id = p.product_id " +
                         "ORDER BY it.transaction_date DESC";
            ResultSet rs = con.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                dtm.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("transaction_type"),
                    rs.getInt("quantity"),
                    rs.getString("reference_number"),
                    rs.getString("transaction_date"),
                    rs.getString("reason")
                });
            }
        } catch (Exception ex) {
            System.out.println("loadTransactionHistory error: " + ex);
        }
    }

    // ==================== LOAD CATEGORY FILTER ====================
    // Populates the jComboBox1 with categories from the database
    private void loadCategoryFilter() {
        try {
            Connection con = DatabaseConnection.dbConnection();
            jComboBox1.removeAllItems();
            jComboBox1.addItem("All");
            ResultSet rs = con.prepareStatement(
                "SELECT category_name FROM categories ORDER BY category_name ASC").executeQuery();
            while (rs.next()) {
                jComboBox1.addItem(rs.getString("category_name"));
            }
        } catch (Exception ex) {
            System.out.println("loadCategoryFilter error: " + ex);
        }
    }

    // ==================== LOAD PRODUCT IMAGE ====================
    // Fetches Base64 image from DB and displays it in the given JLabel
    private void loadProductImage(String productId, JLabel imageLabel) {
        try {
            Connection con = DatabaseConnection.dbConnection();
            PreparedStatement pst = con.prepareStatement(
                "SELECT product_image FROM products WHERE product_id = ?");
            pst.setString(1, productId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String imgBase64 = rs.getString("product_image");
                if (imgBase64 != null && !imgBase64.trim().isEmpty()) {
                    byte[] imageBytes = Base64.getDecoder().decode(imgBase64.trim());
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    Image scaled = img.getScaledInstance(
                        imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                    imageLabel.setText("");
                } else {
                    imageLabel.setIcon(null);
                    imageLabel.setText("No image");
                }
            }
        } catch (Exception ex) {
            System.out.println("loadProductImage error: " + ex);
        }
    }

    // ==================== SAVE STOCK IN ====================
    // Inserts a Stock In record and increases product stock_quantity
    private void saveStockIn() {
        int row = jtablestockin.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Please select a product from the table first.");
            return;
        }

        int productId = Integer.parseInt(jtablestockin.getValueAt(row, 0).toString());
        int qty = (Integer) jspinstockin.getValue(); // jspinstockin = quantity spinner in Stock In

        if (qty <= 0) {
            JOptionPane.showMessageDialog(null, "Quantity must be greater than 0.");
            return;
        }

        String refNo = jstockinref.getText().trim();   // jstockinref = reference number field in Stock In
        String remarks = jremarkstockin.getText().trim(); // jremarkstockin = remarks textarea in Stock In

        try {
            Connection con = DatabaseConnection.dbConnection();

            // Step 1: Insert into inventory_transactions
            String insertSql = "INSERT INTO inventory_transactions " +
                "(product_id, transaction_type, quantity, reference_number, reason) " +
                "VALUES (?, 'Stock In', ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(insertSql);
            pst.setInt(1, productId);
            pst.setInt(2, qty);
            pst.setString(3, refNo);
            pst.setString(4, remarks);
            pst.executeUpdate();

            // Step 2: Increase stock_quantity in products table
            String updateSql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
            PreparedStatement pst2 = con.prepareStatement(updateSql);
            pst2.setInt(1, qty);
            pst2.setInt(2, productId);
            pst2.executeUpdate();

            JOptionPane.showMessageDialog(null, "Stock In saved successfully!");
            clearStockIn();
            loadStockInTable();
            loadAllStock("", "All");
            loadLowStock("");
            loadTransactionHistory();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error saving Stock In: " + ex.getMessage());
        }
    }

    // ==================== DELETE STOCK IN ====================
    // Deletes the most recent Stock In transaction and reverses the stock quantity
    private void deleteStockIn() {
        int row = jtablestockin.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Please select a record to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
            "Are you sure you want to delete this record?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == YES_OPTION) {
            int productId = Integer.parseInt(jtablestockin.getValueAt(row, 0).toString());
            try {
                Connection con = DatabaseConnection.dbConnection();

                // Step 1: Get the quantity of the most recent Stock In to reverse it
                String getSql = "SELECT quantity FROM inventory_transactions " +
                                "WHERE product_id = ? AND transaction_type = 'Stock In' " +
                                "ORDER BY transaction_date DESC LIMIT 1";
                PreparedStatement pstGet = con.prepareStatement(getSql);
                pstGet.setInt(1, productId);
                ResultSet rs = pstGet.executeQuery();

                if (rs.next()) {
                    int qty = rs.getInt("quantity");

                    // Step 2: Delete the most recent Stock In transaction
                    String delSql = "DELETE FROM inventory_transactions " +
                                    "WHERE product_id = ? AND transaction_type = 'Stock In' " +
                                    "ORDER BY transaction_date DESC LIMIT 1";
                    PreparedStatement pstDel = con.prepareStatement(delSql);
                    pstDel.setInt(1, productId);
                    pstDel.executeUpdate();

                    // Step 3: Subtract back the quantity from products
                    String updateSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
                    PreparedStatement pstUpdate = con.prepareStatement(updateSql);
                    pstUpdate.setInt(1, qty);
                    pstUpdate.setInt(2, productId);
                    pstUpdate.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Record deleted successfully!");
                    clearStockIn();
                    loadStockInTable();
                    loadAllStock("", "All");
                    loadLowStock("");
                    loadTransactionHistory();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error deleting: " + ex.getMessage());
            }
        }
    }

    // ==================== CLEAR STOCK IN FORM ====================
    private void clearStockIn() {
        jspinstockin.setValue(0);       // reset spinner
        jstockinref.setText("");         // clear reference number
        jremarkstockin.setText("");      // clear remarks
        jLabel6.setText("...");          // reset date label
        jLabel8.setIcon(null);           // clear product image
        jLabel8.setText("");
    }

    // ==================== SAVE STOCK OUT ====================
    // Inserts a Stock Out record and decreases product stock_quantity
    private void saveStockOut() {
        int row = jtablestockout.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Please select a product from the table first.");
            return;
        }

        int productId = Integer.parseInt(jtablestockout.getValueAt(row, 0).toString());
        int currentStock = Integer.parseInt(jtablestockout.getValueAt(row, 2).toString());
        int qty = (Integer) jspinstockout.getValue(); // jspinstockout = quantity spinner in Stock Out

        if (qty <= 0) {
            JOptionPane.showMessageDialog(null, "Quantity must be greater than 0.");
            return;
        }

        // Check if enough stock available
        if (qty > currentStock) {
            JOptionPane.showMessageDialog(null,
                "Insufficient stock! Current stock: " + currentStock);
            return;
        }

        String refNo = jstockout.getText().trim();     // jstockout = reference number field in Stock Out
        String remarks = jTextArea2.getText().trim();   // jTextArea2 = remarks textarea in Stock Out

        try {
            Connection con = DatabaseConnection.dbConnection();

            // Step 1: Insert into inventory_transactions
            String insertSql = "INSERT INTO inventory_transactions " +
                "(product_id, transaction_type, quantity, reference_number, reason) " +
                "VALUES (?, 'Stock Out', ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(insertSql);
            pst.setInt(1, productId);
            pst.setInt(2, qty);
            pst.setString(3, refNo);
            pst.setString(4, remarks);
            pst.executeUpdate();

            // Step 2: Decrease stock_quantity in products table
            String updateSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
            PreparedStatement pst2 = con.prepareStatement(updateSql);
            pst2.setInt(1, qty);
            pst2.setInt(2, productId);
            pst2.executeUpdate();

            JOptionPane.showMessageDialog(null, "Stock Out saved successfully!");
            clearStockOut();
            loadStockOutTable();
            loadAllStock("", "All");
            loadLowStock("");
            loadTransactionHistory();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error saving Stock Out: " + ex.getMessage());
        }
    }

    // ==================== DELETE STOCK OUT ====================
    // Deletes the most recent Stock Out transaction and restores the stock quantity
    private void deleteStockOut() {
        int row = jtablestockout.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Please select a record to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
            "Are you sure you want to delete this record?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == YES_OPTION) {
            int productId = Integer.parseInt(jtablestockout.getValueAt(row, 0).toString());
            try {
                Connection con = DatabaseConnection.dbConnection();

                // Step 1: Get the quantity of the most recent Stock Out to reverse it
                String getSql = "SELECT quantity FROM inventory_transactions " +
                                "WHERE product_id = ? AND transaction_type = 'Stock Out' " +
                                "ORDER BY transaction_date DESC LIMIT 1";
                PreparedStatement pstGet = con.prepareStatement(getSql);
                pstGet.setInt(1, productId);
                ResultSet rs = pstGet.executeQuery();

                if (rs.next()) {
                    int qty = rs.getInt("quantity");

                    // Step 2: Delete the most recent Stock Out transaction
                    String delSql = "DELETE FROM inventory_transactions " +
                                    "WHERE product_id = ? AND transaction_type = 'Stock Out' " +
                                    "ORDER BY transaction_date DESC LIMIT 1";
                    PreparedStatement pstDel = con.prepareStatement(delSql);
                    pstDel.setInt(1, productId);
                    pstDel.executeUpdate();

                    // Step 3: Add back the quantity to products
                    String updateSql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
                    PreparedStatement pstUpdate = con.prepareStatement(updateSql);
                    pstUpdate.setInt(1, qty);
                    pstUpdate.setInt(2, productId);
                    pstUpdate.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Record deleted successfully!");
                    clearStockOut();
                    loadStockOutTable();
                    loadAllStock("", "All");
                    loadLowStock("");
                    loadTransactionHistory();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error deleting: " + ex.getMessage());
            }
        }
    }

    // ==================== CLEAR STOCK OUT FORM ====================
    private void clearStockOut() {
        jspinstockout.setValue(0);      // reset spinner
        jstockout.setText("");           // clear reference number
        jTextArea2.setText("");          // clear remarks
        jLabel18.setText("...");         // reset date label
        jLabel20.setIcon(null);          // clear product image
        jLabel20.setText("");
    }
        
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtablestockin = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jremarkstockin = new javax.swing.JTextArea();
        jspinstockin = new javax.swing.JSpinner();
        jdelete1 = new javax.swing.JButton();
        jclear1 = new javax.swing.JButton();
        jsave1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSpinner3 = new javax.swing.JSpinner();
        jstockinref = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtablestockout = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jremarkstockout = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jspinstockout = new javax.swing.JSpinner();
        jdelete2 = new javax.swing.JButton();
        jclear2 = new javax.swing.JButton();
        jsave2 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jSpinner4 = new javax.swing.JSpinner();
        jstockout = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jtablestock1 = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jtablestock2 = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel15 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jtablestockin.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Product ID", "Product name", "Stock Quantity", "Remarks", "Reference no."
            }
        ));
        jScrollPane1.setViewportView(jtablestockin);

        jPanel6.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 730, 130));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 36)); // NOI18N
        jLabel1.setText("Stock in ");
        jPanel6.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 180, -1));

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Remarks:");
        jPanel7.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Enter Reference no.     #");
        jPanel7.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Date:");
        jPanel7.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, -1, -1));

        jremarkstockin.setColumns(20);
        jremarkstockin.setRows(5);
        jScrollPane2.setViewportView(jremarkstockin);

        jPanel7.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 90, 240, 90));
        jPanel7.add(jspinstockin, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 170, -1));

        jdelete1.setText("Delete");
        jPanel7.add(jdelete1, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 200, 80, 30));

        jclear1.setText("Clear");
        jPanel7.add(jclear1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 200, 70, 30));

        jsave1.setText("Save");
        jPanel7.add(jsave1, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 200, 80, 30));

        jLabel6.setText("...");
        jPanel7.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 20, 120, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Product image:");
        jPanel7.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 40, -1, -1));

        jLabel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel7.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 40, 140, 140));
        jPanel7.add(jSpinner3, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 170, -1));

        jstockinref.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jstockinrefActionPerformed(evt);
            }
        });
        jPanel7.add(jstockinref, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 140, -1));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("Enter added stock:");
        jPanel7.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        jPanel6.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 730, 240));

        jPanel2.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(-4, -4, 780, 510));

        jTabbedPane1.addTab("Stock In", jPanel2);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jtablestockout.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Product ID", "Product name", "Stock Quantity", "Remarks", "Reference no."
            }
        ));
        jScrollPane3.setViewportView(jtablestockout);

        jPanel8.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 730, 130));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 3, 36)); // NOI18N
        jLabel9.setText("Stock Out");
        jPanel8.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 180, -1));

        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Remarks:");
        jPanel9.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Enter Reference no.     #");
        jPanel9.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("Date:");
        jPanel9.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 20, -1, -1));

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jremarkstockout.setViewportView(jTextArea2);

        jPanel9.add(jremarkstockout, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 90, 240, 90));
        jPanel9.add(jspinstockout, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 170, -1));

        jdelete2.setText("Delete");
        jPanel9.add(jdelete2, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 200, 80, 30));

        jclear2.setText("Clear");
        jPanel9.add(jclear2, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 200, 70, 30));

        jsave2.setText("Save");
        jPanel9.add(jsave2, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 200, 80, 30));

        jLabel18.setText("...");
        jPanel9.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 20, 120, -1));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setText("Product image:");
        jPanel9.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 40, -1, -1));

        jLabel20.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel9.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 20, 140, 160));
        jPanel9.add(jSpinner4, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 170, -1));
        jPanel9.add(jstockout, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 140, -1));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setText("Enter added stock:");
        jPanel9.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        jPanel8.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 730, 240));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 780, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 780, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 510, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Stock Out", jPanel3);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel12.setFont(new java.awt.Font("Segoe UI", 3, 36)); // NOI18N
        jLabel12.setText("Overview");
        jPanel10.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 180, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Filter by Category");
        jPanel10.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jTabbedPane2.setBackground(new java.awt.Color(0, 102, 153));
        jTabbedPane2.setForeground(new java.awt.Color(255, 255, 255));

        jPanel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jtablestock1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Product Name", "Category", "Stock Quantity"
            }
        ));
        jScrollPane5.setViewportView(jtablestock1);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("All Stock", jPanel11);

        jPanel12.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jtablestock2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Product Name", "Category", "Stock Quantity"
            }
        ));
        jScrollPane6.setViewportView(jtablestock2);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Low Stock", jPanel12);

        jPanel10.add(jTabbedPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 60, 520, 370));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Search Product");
        jPanel10.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));
        jPanel10.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 170, 30));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel10.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 170, 30));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 780, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("Stock Overview", jPanel4);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Product", "Transaction type", "Quantity", "Reference", "Transaction Date", "Remarks"
            }
        ));
        jScrollPane7.setViewportView(jTable1);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 3, 36)); // NOI18N
        jLabel15.setText("Transaction History");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 718, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel15)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 52, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Transaction History", jPanel5);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 780, 520));
        jTabbedPane1.getAccessibleContext().setAccessibleName("Stock in");
        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        jPanel1.setBackground(new java.awt.Color(0, 102, 153));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Swis721 BT", 3, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Inventory");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 12, 230, -1));

        jButton2.setBackground(new java.awt.Color(0, 102, 153));
        jButton2.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\return.png")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(668, 12, 84, 47));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, 100));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(null, "Return to Dashboard", "Are you sure?",JOptionPane.YES_NO_OPTION);
        if(confirm == YES_OPTION){
         Dashboard db = new  Dashboard();
        db.show();
        this.dispose();
        }else{
            
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // TODO add your handling code here:
         loadStockInTable();
        loadStockOutTable();
        loadAllStock("", "All");
        loadLowStock("");
        loadTransactionHistory();
        loadCategoryFilter();
    }//GEN-LAST:event_formWindowActivated

    private void jstockinrefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jstockinrefActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jstockinrefActionPerformed

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
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Inventory().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton jclear1;
    private javax.swing.JButton jclear2;
    private javax.swing.JButton jdelete1;
    private javax.swing.JButton jdelete2;
    private javax.swing.JTextArea jremarkstockin;
    private javax.swing.JScrollPane jremarkstockout;
    private javax.swing.JButton jsave1;
    private javax.swing.JButton jsave2;
    private javax.swing.JSpinner jspinstockin;
    private javax.swing.JSpinner jspinstockout;
    private javax.swing.JTextField jstockinref;
    private javax.swing.JTextField jstockout;
    private javax.swing.JTable jtablestock1;
    private javax.swing.JTable jtablestock2;
    private javax.swing.JTable jtablestockin;
    private javax.swing.JTable jtablestockout;
    // End of variables declaration//GEN-END:variables
}
