/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package andulanarobert1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.print.PrinterException;
import javax.swing.JTextArea;
import javax.swing.JFileChooser;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 *
 * @author rpand
 */
public class postool extends javax.swing.JFrame {

     private double VAT_RATE   = 0.12;
     private int    lastSaleId = -1;
    /**
     * Creates new form postool
     */
    public postool() {
        initComponents(); 
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

addWindowListener(new java.awt.event.WindowAdapter() {
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {

        // 🚫 Prevent closing if cart is not empty
        if (jTablecartitems.getRowCount() > 0) {
            JOptionPane.showMessageDialog(
                null,
                "Finish or cancel the current sale first."
            );
            return;
        }

        // ✅ Ask for confirmation
        int confirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to exit the POS system?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose(); // closes only this window
        }
    }
});
        initPOS(); 
    }
    
        private Connection getConnection() throws Exception {
        return DatabaseConnection.dbConnection();
    }
        
    private void initPOS() {
        initCartTable();
        resetTotals();
        loadItemsTable();
        setupBarcodeField();
        setupCashTenderedListener();
        setupNumpad();
        setupActionButtons();   // ← wires Checkout, Print, PDF, Cancel
        setupCartButtons(); 
    }
    private void initCartTable() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Product ID","Product","Quantity","Price","Discount","Subtotal"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jTablecartitems.setModel(model);
    }
 
    /** Items tab – loads all products from DB */
    private void loadItemsTable() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Product ID","Product Name","Category","Barcode"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jitemtable.setModel(model);
 
        String sql = "SELECT product_id, product_name, category_name, barcode "
                   + "FROM products ORDER BY product_name ASC";
 
        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
 
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category_name"),
                    rs.getString("barcode")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }
 
        // Double-click a row in Items tab → add that product to cart
        jitemtable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = jitemtable.getSelectedRow();
                   
                }
            }
        });
    }
 
    /** Pressing Enter in the Barcode field triggers add-to-cart */
    private void setupBarcodeField() {
        jBarcodesearcher.addActionListener(e -> {
            String barcode = jBarcodesearcher.getText().trim();
            if (!barcode.isEmpty()) {
                addToCartByBarcode(barcode);
                jBarcodesearcher.setText("");
            }
        });
    }
 
    /** Typing in Cash Tendered recalculates change live */
    private void setupCashTenderedListener() {
        jcashtend.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                calculateChange();
            }
        });
    }
    private void setupCartButtons() {
    // Remove selected row from cart
    jremoveitem.addActionListener(e -> {
        int row = jTablecartitems.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }
        DefaultTableModel model = (DefaultTableModel) jTablecartitems.getModel();
        model.removeRow(row);
        calculateTotals();
    });

    // Clear entire cart with confirmation
    jclearcart.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Clear all items from cart?",
            "Clear Cart", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) clearCart();
    });

    // Add item button (same as pressing Enter in barcode field)
    Jbarcodesearchbutton.addActionListener(e -> {
        String barcode = jBarcodesearcher.getText().trim();
        if (!barcode.isEmpty()) {
            addToCartByBarcode(barcode);
            jBarcodesearcher.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a barcode.");
        }
    });
}

private void updateCartQuantity() {
    int row = jTablecartitems.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select an item to update.");
        return;
    }

    DefaultTableModel model = (DefaultTableModel) jTablecartitems.getModel();
    int    productId   = Integer.parseInt(model.getValueAt(row, 0).toString());
    String productName = model.getValueAt(row, 1).toString();
    int    currentQty  = Integer.parseInt(model.getValueAt(row, 2).toString());

    String input = JOptionPane.showInputDialog(this,
        "Enter new quantity for \"" + productName + "\":\n(Current: " + currentQty + ")",
        "Update Quantity", JOptionPane.PLAIN_MESSAGE);

    if (input == null || input.trim().isEmpty()) return;

    int newQty;
    try {
        newQty = Integer.parseInt(input.trim());
        if (newQty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
            return;
        }
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid quantity entered.");
        return;
    }

    // Check stock in DB before allowing update
    String stockSql = "SELECT stock_quantity FROM products WHERE product_id = ?";
    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(stockSql)) {
        ps.setInt(1, productId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int available = rs.getInt("stock_quantity");
                if (newQty > available) {
                    JOptionPane.showMessageDialog(this,
                        "Insufficient stock. Available: " + available);
                    return;
                }
            }
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Stock check error: " + ex.getMessage());
        return;
    }

    model.setValueAt(newQty, row, 2);
    calculateTotals();
    JOptionPane.showMessageDialog(this, "Quantity updated successfully.");
}
 
    /** Wire up every numpad button */
    private void setupNumpad() {
        java.awt.event.ActionListener digitListener = e -> {
            String digit = ((javax.swing.JButton) e.getSource()).getText();
            jcashtend.setText(jcashtend.getText() + digit);
            calculateChange();
        };
 
        jButton0.addActionListener(digitListener);
        jButton1.addActionListener(digitListener);
        jButton18.addActionListener(digitListener); // "2"
        jButton3.addActionListener(digitListener);
        jButton4.addActionListener(digitListener);
        jButton5.addActionListener(digitListener);
        jButton6.addActionListener(digitListener);
        jButton7.addActionListener(digitListener);
        jButton8.addActionListener(digitListener);
        jButton9.addActionListener(digitListener);
 
        // "C" – backspace one character
        jButton20.addActionListener(e -> {
            String t = jcashtend.getText();
            if (!t.isEmpty()) {
                jcashtend.setText(t.substring(0, t.length() - 1));
                calculateChange();
            }
        });
 
        // "Clear" – wipe cash-tendered completely
        jButtonclear.addActionListener(e -> {
            jcashtend.setText("");
            jchange.setText("0.00");
        });
 
        // Quick-cash buttons
        jButton100.addActionListener(e  -> quickCash(100));
        jButton500.addActionListener(e  -> quickCash(500));
        jButton1000.addActionListener(e -> quickCash(1000));
    }
 
    /** Wire up Checkout, Print Receipt, Export PDF, Cancel Sale */
    private void setupActionButtons() {
        jCheckout.addActionListener(e      -> processCheckout());
        jprintreciept.addActionListener(e  -> printReceipt());
        jexportpdf.addActionListener(e     -> exportReceiptToPDF());
        jcancelsale.addActionListener(e    -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this sale?",
                "Cancel Sale", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) clearCart();
        });
    }
 
    private void quickCash(double amount) {
        try {
            double current = jcashtend.getText().trim().isEmpty()
                    ? 0 : Double.parseDouble(jcashtend.getText().trim());
            jcashtend.setText(String.format("%.2f", current + amount));
        } catch (NumberFormatException ex) {
            jcashtend.setText(String.format("%.2f", amount));
        }
        calculateChange();
    }
 
    // ════════════════════════════════════════════════════════
    // ADD TO CART
    // ════════════════════════════════════════════════════════
    private void addToCartByBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) return;
 
        String sql = "SELECT product_id, product_name, selling_price, "
                   + "stock_quantity, product_image "
                   + "FROM products WHERE barcode = ?";
 
        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
 
            pst.setString(1, barcode);
 
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this,
                        "Product not found for barcode: " + barcode);
                    return;
                }
 
                int    productId   = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double price       = rs.getDouble("selling_price");
                int    stock       = rs.getInt("stock_quantity");
                String imgBase64   = rs.getString("product_image");
 
                if (stock <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Out of stock: " + productName);
                    return;
                }
 
                // Show product image in jpointimage panel
                if (imgBase64 != null && !imgBase64.trim().isEmpty()) {
                    try {
                        byte[] bytes = Base64.getDecoder().decode(imgBase64.trim());
                        BufferedImage bImg = ImageIO.read(new ByteArrayInputStream(bytes));
                        Image scaled = bImg.getScaledInstance(
                                jpointimage.getWidth(),
                                jpointimage.getHeight(),
                                Image.SCALE_SMOOTH);
                        jpointimage.setIcon(new ImageIcon(scaled));
                    } catch (Exception ignored) {
                        jpointimage.setIcon(null);
                    }
                } else {
                    jpointimage.setIcon(null);
                }
 
                DefaultTableModel model = (DefaultTableModel) jTablecartitems.getModel();
                int existingRow = findCartRow(productId);
 
                if (existingRow != -1) {
                    int currentQty = Integer.parseInt(
                            model.getValueAt(existingRow, 2).toString());
                    int newQty = currentQty + 1;
 
                    if (newQty > stock) {
                        JOptionPane.showMessageDialog(this,
                            "Insufficient stock for: " + productName);
                        return;
                    }
                    model.setValueAt(newQty, existingRow, 2);
                } else {
                    model.addRow(new Object[]{
                        productId,
                        productName,
                        1,
                        String.format("%.2f", price),
                        "0.00",
                        String.format("%.2f", price)
                    });
                }
 
                calculateTotals();
            }
 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Add to cart error: " + e.getMessage());
        }
    }
 
    private int findCartRow(int productId) {
        for (int i = 0; i < jTablecartitems.getRowCount(); i++) {
            if (Integer.parseInt(
                    jTablecartitems.getValueAt(i, 0).toString()) == productId)
                return i;
        }
        return -1;
    }
 
    // ════════════════════════════════════════════════════════
    // TOTALS
    // ════════════════════════════════════════════════════════
    private void calculateTotals() {
        double subtotal      = 0;
        double totalDiscount = 0;
 
        DefaultTableModel model = (DefaultTableModel) jTablecartitems.getModel();
 
        for (int i = 0; i < model.getRowCount(); i++) {
            int    qty       = Integer.parseInt(model.getValueAt(i, 2).toString());
            double price     = Double.parseDouble(model.getValueAt(i, 3).toString());
            double itemTotal = qty * price;
 
            double itemDiscount = 0;
            if      (qty >= 5) itemDiscount = itemTotal * 0.10;
            else if (qty >= 3) itemDiscount = itemTotal * 0.05;
 
            double lineSub = itemTotal - itemDiscount;
 
            model.setValueAt(String.format("%.2f", itemDiscount), i, 4);
            model.setValueAt(String.format("%.2f", lineSub),      i, 5);
 
            subtotal      += itemTotal;
            totalDiscount += itemDiscount;
        }
 
        double vat   = subtotal * VAT_RATE;
        double total = subtotal + vat - totalDiscount;
 
        jsubtotal.setText(String.format("%.2f", subtotal));
        jVAT.setText(String.format("%.2f", vat));
        jdiscount.setText(String.format("%.2f", totalDiscount));
        jtamount.setText(String.format("%.2f", total));
 
        calculateChange();
    }
 
    private void calculateChange() {
        try {
            String cashText = jcashtend.getText().trim();
            if (cashText.isEmpty()) { jchange.setText("0.00"); return; }
            double cash  = Double.parseDouble(cashText);
            double total = Double.parseDouble(jtamount.getText());
            jchange.setText(String.format("%.2f", cash - total));
        } catch (NumberFormatException e) {
            jchange.setText("0.00");
        }
    }
 
    private void resetTotals() {
        jsubtotal.setText("0.00");
        jVAT.setText("0.00");
        jdiscount.setText("0.00");
        jtamount.setText("0.00");
        jchange.setText("0.00");
        jcashtend.setText("");
        jpointimage.setIcon(null);
    }
 
    private void clearCart() {
        ((DefaultTableModel) jTablecartitems.getModel()).setRowCount(0);
        resetTotals();
        jBarcodesearcher.setText("");
        invoicegenerate.setText("");
    }
 
    // ════════════════════════════════════════════════════════
    // CHECKOUT
    // ════════════════════════════════════════════════════════
    private void processCheckout() {
        if (jTablecartitems.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }
 
        try {
            double totalAmount  = Double.parseDouble(jtamount.getText());
            String cashText     = jcashtend.getText().trim();
            double cashTendered = cashText.isEmpty() ? 0 : Double.parseDouble(cashText);
            double changeAmt    = cashTendered - totalAmount;
 
            if (cashTendered < totalAmount) {
                JOptionPane.showMessageDialog(this, "Cash tendered is insufficient.");
                return;
            }
 
            String paymentMethod = jpaymethod.getSelectedItem().toString();
            int    userId        = SessionManager.loggedInUserId;
            String invoiceCode   = generateInvoiceCode();
            invoicegenerate.setText(invoiceCode);
 
            try (Connection con = getConnection()) {
                con.setAutoCommit(false);
 
                try {
                    // ── Insert sale header ───────────────────────────
                    String saleSql =
                        "INSERT INTO sales (invoice_code, user_id, total_amount, payment_method, "
                          + "cash_tendered, change_amount, sale_date, created_at) "
                          + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
 
                    int saleId;
                    try (PreparedStatement pstSale =
                            con.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                        pstSale.setString(1, invoiceCode);
                        pstSale.setInt(2, userId);
                        pstSale.setDouble(3, totalAmount);
                        pstSale.setString(4, paymentMethod);
                        pstSale.setDouble(5, cashTendered);
                        pstSale.setDouble(6, changeAmt);
                        pstSale.executeUpdate();
 
                        try (ResultSet rs = pstSale.getGeneratedKeys()) {
                            if (!rs.next()) throw new Exception("Failed to get sale ID.");
                            saleId = rs.getInt(1);
                            lastSaleId = saleId;
                        }
                    }
 
                    // ── Insert sale items & deduct stock ─────────────
                    String itemSql =
                        "INSERT INTO sale_items "
                      + "(sale_id, product_id, quantity, unit_price, total_price) "
                      + "VALUES (?, ?, ?, ?, ?)";
                    String stockReadSql   =
                        "SELECT stock_quantity FROM products WHERE product_id = ?";
                    String stockUpdateSql =
                        "UPDATE products SET stock_quantity = ? WHERE product_id = ?";
 
                    DefaultTableModel model = (DefaultTableModel) jTablecartitems.getModel();
 
                    for (int i = 0; i < model.getRowCount(); i++) {
                        int    prodId  = Integer.parseInt(model.getValueAt(i, 0).toString());
                        int    qty     = Integer.parseInt(model.getValueAt(i, 2).toString());
                        double price   = Double.parseDouble(model.getValueAt(i, 3).toString());
                        double lineSub = Double.parseDouble(model.getValueAt(i, 5).toString());
 
                        int currentStock = 0;
                        try (PreparedStatement ps = con.prepareStatement(stockReadSql)) {
                            ps.setInt(1, prodId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) currentStock = rs.getInt("stock_quantity");
                            }
                        }
 
                        if (qty > currentStock)
                            throw new Exception(
                                "Insufficient stock for product ID: " + prodId);
 
                        try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                            ps.setInt(1, saleId);
                            ps.setInt(2, prodId);
                            ps.setInt(3, qty);
                            ps.setDouble(4, price);
                            ps.setDouble(5, lineSub);
                            ps.executeUpdate();
                        }
 
                        try (PreparedStatement ps = con.prepareStatement(stockUpdateSql)) {
                            ps.setInt(1, currentStock - qty);
                            ps.setInt(2, prodId);
                            ps.executeUpdate();
                        }
                    }
 
                    con.commit();
 
                    JOptionPane.showMessageDialog(this,
    "✅ Checkout successful!\nChange: ₱"
    + String.format("%.2f", changeAmt));

// Ask user if they want to print receipt
int printChoice = JOptionPane.showConfirmDialog(this,
    "Would you like to print the receipt?",
    "Print Receipt",
    JOptionPane.YES_NO_OPTION);

if (printChoice == JOptionPane.YES_OPTION) {
    printReceipt();
}

clearCart();
loadItemsTable();
 
                } catch (Exception e) {
                    con.rollback();
                    throw e;
                }
            }
 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Checkout error: " + e.getMessage());
        }
    }
    private String generateInvoiceCode() {
    int code = (int)(Math.random() * 900000) + 100000; // always 6 digits
    return String.valueOf(code);
    
    
}
 
    // ════════════════════════════════════════════════════════
    // RECEIPT TEXT
    // ════════════════════════════════════════════════════════
    private String buildReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================\n");
        sb.append("       POS INVENTORY SYSTEM         \n");
        sb.append("====================================\n");
        sb.append("Payment: ").append(jpaymethod.getSelectedItem()).append("\n");
        // Add this line right after the payment line
        sb.append("Invoice #    : ").append(invoicegenerate.getText()).append("\n");
        sb.append("====================================\n");
        sb.append(String.format("%-15s %4s %8s %8s\n",
                "Item", "Qty", "Price", "Total"));
        sb.append("------------------------------------\n");
 
        for (int i = 0; i < jTablecartitems.getRowCount(); i++) {
            String name = jTablecartitems.getValueAt(i, 1).toString();
            int    qty  = Integer.parseInt(
                    jTablecartitems.getValueAt(i, 2).toString());
            double prc  = Double.parseDouble(
                    jTablecartitems.getValueAt(i, 3).toString());
            double sub  = Double.parseDouble(
                    jTablecartitems.getValueAt(i, 5).toString());
            if (name.length() > 15) name = name.substring(0, 15);
            sb.append(String.format("%-15s %4d %8.2f %8.2f\n",
                    name, qty, prc, sub));
        }
 
        sb.append("====================================\n");
        sb.append("Subtotal     : ").append(jsubtotal.getText()).append("\n");
        sb.append("VAT (12%)    : ").append(jVAT.getText()).append("\n");
        sb.append("Discount     : ").append(jdiscount.getText()).append("\n");
        sb.append("Total Amount : ").append(jtamount.getText()).append("\n");
        sb.append("Cash Tendered: ").append(jcashtend.getText()).append("\n");
        sb.append("Change       : ").append(jchange.getText()).append("\n");
        sb.append("====================================\n");
        sb.append("     Thank you for your purchase!   \n");
        sb.append("====================================\n");
        return sb.toString();
    }
 
    // ════════════════════════════════════════════════════════
    // PRINT RECEIPT  (jprintreciept button)
    // ════════════════════════════════════════════════════════
    private void printReceipt() {
        if (jTablecartitems.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }
        try {
            JTextArea ta = new JTextArea(buildReceiptText());
            ta.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 10));
            ta.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Print error: " + e.getMessage());
        }
    }
 
    // ════════════════════════════════════════════════════════
    // EXPORT TO PDF  (jexportpdf button)
    // ════════════════════════════════════════════════════════
    private void exportReceiptToPDF() {
        if (jTablecartitems.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }
 
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("receipt.pdf"));
 
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
 
        String filePath = chooser.getSelectedFile().getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".pdf")) filePath += ".pdf";
 
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
 
            Font titleFont  = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.COURIER,   11, Font.NORMAL);
 
            document.add(new Paragraph("POS INVENTORY SYSTEM RECEIPT", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(buildReceiptText(), normalFont));
 
            document.close();
            JOptionPane.showMessageDialog(this, "✅ PDF exported successfully!");
 
        } catch (DocumentException | java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "PDF export error: " + e.getMessage());
        }
    }
 
    // ════════════════════════════════════════════════════════
    // GENERATED CODE (do not modify)
    // ════════════════════════════════════════════════════════
   
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel24 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jpointimage = new javax.swing.JLabel();
        jpaymethod = new javax.swing.JComboBox<>();
        jchange = new javax.swing.JLabel();
        jsubtotal = new javax.swing.JLabel();
        jVAT = new javax.swing.JLabel();
        jdiscount = new javax.swing.JLabel();
        jtamount = new javax.swing.JLabel();
        jcashtend = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton0 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButtonclear = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton100 = new javax.swing.JButton();
        jButton1000 = new javax.swing.JButton();
        jButton500 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jexportpdf = new javax.swing.JButton();
        jcancelsale = new javax.swing.JButton();
        jprintreciept = new javax.swing.JButton();
        jCheckout = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        invoicegenerate = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTablecartitems = new javax.swing.JTable();
        jupdatequant = new javax.swing.JButton();
        jremoveitem = new javax.swing.JButton();
        jclearcart = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jaddtocart = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jitemtable = new javax.swing.JTable();
        jBarcodesearcher = new javax.swing.JTextField();
        Jbarcodesearchbutton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setText("Barcode");

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Subtotal:");
        jPanel5.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 26, -1, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Discount:");
        jPanel5.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 90, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("VAT:");
        jPanel5.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 58, -1, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("Total Amount:");
        jPanel5.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 128, -1, -1));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Cash Tendered:");
        jPanel5.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 160, -1, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Change:");
        jPanel5.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 200, -1, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("Payment Method:");
        jPanel5.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 240, -1, -1));

        jpointimage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.add(jpointimage, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 178, 226));

        jpaymethod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cash", "G-cash", "Paypal" }));
        jPanel5.add(jpaymethod, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 230, 109, 30));

        jchange.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jchange.setText("0.00");
        jPanel5.add(jchange, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 200, 43, -1));

        jsubtotal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jsubtotal.setText("0.00");
        jPanel5.add(jsubtotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 30, 43, -1));

        jVAT.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jVAT.setText("0.00");
        jPanel5.add(jVAT, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 60, 43, -1));

        jdiscount.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jdiscount.setText("0.00");
        jPanel5.add(jdiscount, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 90, 43, -1));

        jtamount.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jtamount.setText("0.00");
        jPanel5.add(jtamount, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 130, 43, -1));

        jcashtend.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jcashtend.setMargin(new java.awt.Insets(2, 10, 2, 6));
        jcashtend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcashtendActionPerformed(evt);
            }
        });
        jPanel5.add(jcashtend, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 170, 130, 30));
        jPanel5.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 450, 10));

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton7.setText("7");
        jPanel5.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, 100, 30));

        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton8.setText("8");
        jPanel5.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 280, 100, 30));

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton4.setText("4");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 320, 100, 30));

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton5.setText("5");
        jPanel5.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 320, 100, 30));

        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton6.setText("6");
        jPanel5.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 320, 100, 30));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton1.setText("1");
        jPanel5.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 360, 100, 30));

        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton3.setText("3");
        jPanel5.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 360, 100, 30));

        jButton0.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton0.setText("0");
        jPanel5.add(jButton0, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 400, 100, 30));

        jButton18.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton18.setText("2");
        jPanel5.add(jButton18, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 360, 100, 30));

        jButtonclear.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButtonclear.setText("Clear");
        jPanel5.add(jButtonclear, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 400, 100, 30));

        jButton20.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton20.setText("C");
        jPanel5.add(jButton20, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 400, 100, 30));

        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton9.setText("9");
        jPanel5.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 280, 100, 30));

        jButton100.setBackground(new java.awt.Color(204, 255, 204));
        jButton100.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton100.setText("₱100");
        jButton100.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton100ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton100, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 380, 90, 40));

        jButton1000.setBackground(new java.awt.Color(0, 255, 51));
        jButton1000.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton1000.setText("₱1000");
        jButton1000.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1000ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton1000, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 280, 90, 40));

        jButton500.setBackground(new java.awt.Color(102, 255, 102));
        jButton500.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton500.setText("₱500");
        jButton500.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton500ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton500, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 330, 90, 40));
        jPanel5.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 440, 440, -1));

        jexportpdf.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jexportpdf.setForeground(new java.awt.Color(153, 51, 0));
        jexportpdf.setText("Export to pdf");
        jPanel5.add(jexportpdf, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 500, 120, 30));

        jcancelsale.setBackground(new java.awt.Color(255, 51, 51));
        jcancelsale.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jcancelsale.setForeground(new java.awt.Color(255, 255, 255));
        jcancelsale.setText("Cancel Sale");
        jPanel5.add(jcancelsale, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 500, 110, 30));

        jprintreciept.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jprintreciept.setText("Print Reciept");
        jprintreciept.setToolTipText("");
        jPanel5.add(jprintreciept, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 450, 120, 30));

        jCheckout.setBackground(new java.awt.Color(0, 204, 0));
        jCheckout.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jCheckout.setForeground(new java.awt.Color(255, 255, 255));
        jCheckout.setText("Checkout");
        jCheckout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckoutActionPerformed(evt);
            }
        });
        jPanel5.add(jCheckout, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 450, 110, 30));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel2.setText("#");
        jPanel5.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 480, 10, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel3.setText("Invoice code");
        jPanel5.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 510, -1, -1));

        invoicegenerate.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        invoicegenerate.setText(".");
        jPanel5.add(invoicegenerate, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 480, 120, -1));

        jTabbedPane1.setBackground(new java.awt.Color(0, 102, 153));
        jTabbedPane1.setForeground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(0, 102, 153));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTablecartitems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Product ID", "Product", "Quantity", "Price", "Discount", "Subtotal"
            }
        ));
        jScrollPane1.setViewportView(jTablecartitems);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 690, 340));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 16, 690, 340));

        jupdatequant.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jupdatequant.setText("Update Qty.");
        jupdatequant.setActionCommand("");
        jupdatequant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jupdatequantActionPerformed(evt);
            }
        });
        jPanel1.add(jupdatequant, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 370, 160, 40));

        jremoveitem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jremoveitem.setText("Remove item");
        jPanel1.add(jremoveitem, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 160, 40));

        jclearcart.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jclearcart.setText("Clear Cart");
        jclearcart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jclearcartActionPerformed(evt);
            }
        });
        jPanel1.add(jclearcart, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 370, 160, 40));

        jTabbedPane1.addTab("Cart Items", jPanel1);

        jPanel2.setBackground(new java.awt.Color(0, 102, 153));

        jaddtocart.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jaddtocart.setText("Add to Cart");
        jaddtocart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jaddtocartActionPerformed(evt);
            }
        });

        jitemtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product ID", "Product Name", "Category", "Barcode"
            }
        ));
        jScrollPane2.setViewportView(jitemtable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jaddtocart, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 687, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addComponent(jaddtocart, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        jTabbedPane1.addTab("items", jPanel2);

        Jbarcodesearchbutton.setText("Add item");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBarcodesearcher, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Jbarcodesearchbutton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 725, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jBarcodesearcher, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Jbarcodesearchbutton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 511, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jcashtendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcashtendActionPerformed
        // TODO add your handling code here:
         calculateChange();
    }//GEN-LAST:event_jcashtendActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton100ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton100ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton100ActionPerformed

    private void jButton1000ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1000ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1000ActionPerformed

    private void jButton500ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton500ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton500ActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // TODO add your handling code here:
         loadItemsTable();
    }//GEN-LAST:event_formWindowActivated

    private void jupdatequantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jupdatequantActionPerformed
        // TODO add your handling code here:
          updateCartQuantity();
    }//GEN-LAST:event_jupdatequantActionPerformed

    private void jaddtocartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jaddtocartActionPerformed
        // TODO add your handling code here:
                                            
    int row = jitemtable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a product first.");
        return;
    }
    String barcode = jitemtable.getValueAt(row, 3).toString();
    addToCartByBarcode(barcode);

    }//GEN-LAST:event_jaddtocartActionPerformed

    private void jCheckoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckoutActionPerformed
        // TODO add your handling code here:
         processCheckout();
    }//GEN-LAST:event_jCheckoutActionPerformed

    private void jclearcartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jclearcartActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jclearcartActionPerformed

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
            java.util.logging.Logger.getLogger(postool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(postool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(postool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(postool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
               public void run() {
        new LoginScreen().setVisible(true);
    }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Jbarcodesearchbutton;
    private javax.swing.JLabel invoicegenerate;
    private javax.swing.JTextField jBarcodesearcher;
    private javax.swing.JButton jButton0;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton100;
    private javax.swing.JButton jButton1000;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton500;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonclear;
    private javax.swing.JButton jCheckout;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTablecartitems;
    private javax.swing.JLabel jVAT;
    private javax.swing.JButton jaddtocart;
    private javax.swing.JButton jcancelsale;
    private javax.swing.JTextField jcashtend;
    private javax.swing.JLabel jchange;
    private javax.swing.JButton jclearcart;
    private javax.swing.JLabel jdiscount;
    private javax.swing.JButton jexportpdf;
    private javax.swing.JTable jitemtable;
    private javax.swing.JComboBox<String> jpaymethod;
    private javax.swing.JLabel jpointimage;
    private javax.swing.JButton jprintreciept;
    private javax.swing.JButton jremoveitem;
    private javax.swing.JLabel jsubtotal;
    private javax.swing.JLabel jtamount;
    private javax.swing.JButton jupdatequant;
    // End of variables declaration//GEN-END:variables
}
