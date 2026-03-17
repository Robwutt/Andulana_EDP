/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package andulanarobert1;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.YES_OPTION;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.sql.ResultSet;
import java.util.Random;
import javax.swing.table.DefaultTableModel;
import java.awt.image.BufferedImage;
import java.awt.GridBagLayout;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import java.io.IOException;

/**
 *
 * @author student
 */
public class AddProduct extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
    private String imagePath = null;
    public AddProduct() {
        initComponents();
 jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
    public void mouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
         if (selectedRow < 0) return;
         
            if (selectedRow >= 0) {
                
                // Get product_id from table column 0
                String productId = jTable1.getValueAt(selectedRow, 0).toString();
                
                // Fetch full product details from DB using product_id
                try {
                    Connection con = DatabaseConnection.dbConnection();
                    String sql = "SELECT * FROM products WHERE product_id = ?";
                    PreparedStatement pst = con.prepareStatement(sql);
                    pst.setString(1, productId);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        // Fill all text fields
                        jProduct_id.setText(String.valueOf(rs.getInt("product_id")));
                        jProduct_name.setText(rs.getString("product_name"));
                        jDescription.setText(rs.getString("description"));
                        jUnit_price.setText(String.valueOf(rs.getInt("unit_price")));
                        jCost_price1.setText(String.valueOf(rs.getInt("cost_price")));
                        jSelling_price.setText(String.valueOf(rs.getInt("selling_price")));
                        jQuantity.setText(String.valueOf(rs.getInt("stock_quantity")));
                        jReorder.setText(String.valueOf(rs.getInt("reorder_level")));
                        jBarcode.setText(rs.getString("barcode") != null ? rs.getString("barcode") : "");

                        // Set category combo box using category_name from DB
                        
                        String catName = rs.getString("category_name");
                       if (catName != null) {
                       javax.swing.SwingUtilities.invokeLater(() -> {
                        pComboBox.setSelectedItem(catName);
                       });
                       }

                        // Set supplier combo box
                        String supName = rs.getString("supplier_name");
                        if (supName != null) {
                            jsupbox.setSelectedItem(supName);
                        }

                        // Set unit of measure combo box
                        String uom = rs.getString("unit_of_measure");
                        if (uom != null) {
                            uComboBox.setSelectedItem(uom);
                        }

                        // Set status combo box
                        String status = rs.getString("status");
                        if (status != null) {
                            sComboBox1.setSelectedItem(status);
                        }

                        // Handle image
                        String imgBase64 = rs.getString("product_image");
                        if (imgBase64 != null && !imgBase64.trim().isEmpty()) {
                            try {
                                byte[] imageBytes = Base64.getDecoder().decode(imgBase64.trim());
                                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                                Image scaledImg = img.getScaledInstance(
                                    jpimage.getWidth(),
                                    jpimage.getHeight(),
                                    Image.SCALE_SMOOTH
                                );
                                jpimage.setIcon(new ImageIcon(scaledImg));
                            } catch (Exception ex) {
                                jpimage.setIcon(new ImageIcon("src/images/default_product.png"));
                            }
                        } else {
                            jpimage.setIcon(new ImageIcon("src/images/default_product.png"));
                        }
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error loading product: " + ex.getMessage());
                }
            }
        }
    });
 
        
      
          jpimage.setIcon(new ImageIcon("src/images/default_product.png"));
    
    // Make it look like a profile picture frame
    jpimage.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY));
    jpimage.setOpaque(true);
    jpimage.setBackground(java.awt.Color.WHITE);
    jpimage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jpimage.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
    
    // Make the image box clickable to open file chooser
    jpimage.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            jprodaddimageActionPerformed(null); // calls your existing button method
        }
    });

}
    private void clearFields() {
    jProduct_id.setText("");
    jProduct_name.setText("");
    jDescription.setText("");
    jUnit_price.setText("");
    jCost_price1.setText("");
    jSelling_price.setText("");
    jQuantity.setText("");
    jReorder.setText("");
    jBarcode.setText("");

    // Reset combo boxes to first item
    pComboBox.setSelectedIndex(0);      // Product category
    uComboBox.setSelectedIndex(0);      // Unit of measure
    jsupbox.setSelectedIndex(0);        // Supplier
    sComboBox1.setSelectedIndex(0);     // Status

    // Reset image to default
    jpimage.setIcon(new ImageIcon("src/images/default_product.png"));
    imagePath = null;  // clear stored image path
}
    
   private String generateRandomBarcode() {
    Random rand = new Random();
    int number = 10000000 + rand.nextInt(90000000); // 8-digit number
    return String.valueOf(number);
} 
   
    private void loadSuppliers() {
    String sql = "SELECT supplier_name FROM suppliers";
    try (Connection con = DatabaseConnection.dbConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {
        
        jsupbox.removeAllItems();
        while (rs.next()) {
            jsupbox.addItem(rs.getString("supplier_name"));
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e.getMessage());
    }
}
    
    private void loadCategories() {
    try {
        Connection con = DatabaseConnection.dbConnection();
        String sql = "SELECT category_name FROM categories"; // category column in categories table
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        pComboBox.removeAllItems(); // clear old items

        while (rs.next()) {
            pComboBox.addItem(rs.getString("category_name")); // populate combo box
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error loading categories: " + e.getMessage());
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

        jProduct_id = new javax.swing.JTextField();
        jProduct_name = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jBarcode = new javax.swing.JTextField();
        jUnit_price = new javax.swing.JTextField();
        jSelling_price = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        pComboBox = new javax.swing.JComboBox<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        jDescription = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jQuantity = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jsupbox = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        uComboBox = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        jReorder = new javax.swing.JTextField();
        InsertBtn = new javax.swing.JButton();
        jpupdate = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jCost_price1 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        sComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        jpsearch = new javax.swing.JButton();
        jprodaddimage = new javax.swing.JButton();
        jpimage = new javax.swing.JLabel();
        jprodDel = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jpfilter = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jProduct_id, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 140, 120, -1));

        jProduct_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jProduct_nameActionPerformed(evt);
            }
        });
        getContentPane().add(jProduct_name, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 170, 110, -1));

        jLabel1.setText("Description");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 290, -1, -1));

        jLabel3.setText("Product Name");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 170, -1, -1));

        jLabel4.setText("Product Category");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 200, -1, -1));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 270, 280, 10));
        getContentPane().add(jBarcode, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 440, 210, 40));

        jUnit_price.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jUnit_priceActionPerformed(evt);
            }
        });
        getContentPane().add(jUnit_price, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 460, 110, -1));

        jSelling_price.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSelling_priceActionPerformed(evt);
            }
        });
        getContentPane().add(jSelling_price, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 460, 110, -1));

        jLabel5.setText("Product Status");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, 230, -1, -1));

        jLabel6.setText("Unit Price");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 460, -1, -1));

        jLabel7.setText("Selling Price");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 460, -1, -1));

        jLabel8.setText("Quantity in Stock");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 500, -1, -1));
        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 120, -1, -1));

        pComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pComboBoxActionPerformed(evt);
            }
        });
        getContentPane().add(pComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 200, 90, -1));

        jScrollPane4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jDescription.setColumns(20);
        jDescription.setRows(5);
        jScrollPane4.setViewportView(jDescription);

        getContentPane().add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 290, 210, 110));

        jPanel2.setBackground(new java.awt.Color(0, 102, 153));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setFont(new java.awt.Font("Swis721 BT", 3, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Add Product");

        jButton4.setBackground(new java.awt.Color(0, 102, 153));
        jButton4.setIcon(new javax.swing.ImageIcon("C:\\Users\\rpand\\Documents\\GitHub\\Andulana_EDP\\src\\andulanarobert1\\images\\return.png")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 884, Short.MAX_VALUE)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, 1230, 60));

        jPanel3.setBackground(new java.awt.Color(0, 102, 153));
        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 56, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 10, 60));

        jPanel4.setBackground(new java.awt.Color(0, 102, 153));
        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 56, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 10, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel9.setText("Pricing & Inventory");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 420, -1, -1));

        jLabel10.setText("Product ID");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 140, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel11.setText("️ Product Information");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 100, -1, -1));
        getContentPane().add(jQuantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 500, 70, -1));

        jLabel12.setText("Barcode");
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 440, -1, -1));

        jsupbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jsupboxActionPerformed(evt);
            }
        });
        getContentPane().add(jsupbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 500, 110, -1));

        jLabel13.setText("Unit of Measure");
        getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 460, -1, -1));

        uComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "pcs", "box", "kg", "liter" }));
        getContentPane().add(uComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 460, -1, -1));

        jLabel14.setText("Recorder Level");
        getContentPane().add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 500, -1, -1));

        jReorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jReorderActionPerformed(evt);
            }
        });
        getContentPane().add(jReorder, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 500, 110, -1));

        InsertBtn.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        InsertBtn.setText("Add Product");
        InsertBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InsertBtnActionPerformed(evt);
            }
        });
        getContentPane().add(InsertBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(1120, 510, 120, 40));

        jpupdate.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jpupdate.setText("Update");
        jpupdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jpupdateActionPerformed(evt);
            }
        });
        getContentPane().add(jpupdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 420, -1, -1));

        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setText("Clear");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 420, -1, -1));

        jCost_price1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCost_price1ActionPerformed(evt);
            }
        });
        getContentPane().add(jCost_price1, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 460, 140, -1));

        jLabel16.setText("Cost Price");
        getContentPane().add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 460, -1, -1));

        jLabel17.setText("Supplier Name");
        getContentPane().add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 500, -1, -1));

        sComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Active", "Inactive" }));
        getContentPane().add(sComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 230, 90, -1));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Product ID", "Product Name", "Category", "Unit price", "Desc.", "Supplier", "Cost", "Sell Price", "Stock Qty.", "Reorder Lvl.", "Status", "Date Added"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 920, 300));

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(553, 80, 300, -1));

        jpsearch.setText("Search");
        jpsearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jpsearchActionPerformed(evt);
            }
        });
        getContentPane().add(jpsearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 80, -1, -1));

        jprodaddimage.setText("Add image");
        jprodaddimage.setActionCommand("");
        jprodaddimage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jprodaddimageActionPerformed(evt);
            }
        });
        getContentPane().add(jprodaddimage, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 230, -1, -1));

        jpimage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jpimage.setPreferredSize(new java.awt.Dimension(100, 100));
        getContentPane().add(jpimage, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 130, -1, -1));

        jprodDel.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        jprodDel.setText("Delete");
        jprodDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jprodDelActionPerformed(evt);
            }
        });
        getContentPane().add(jprodDel, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 510, 100, 40));
        getContentPane().add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 420, 280, 10));

        jpfilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Category", "Status", "Reorder Level", "Date Added" }));
        jpfilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jpfilterActionPerformed(evt);
            }
        });
        getContentPane().add(jpfilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 80, 100, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jSelling_priceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSelling_priceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jSelling_priceActionPerformed

    private void jProduct_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jProduct_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jProduct_nameActionPerformed

    private void pComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pComboBoxActionPerformed

    private void InsertBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InsertBtnActionPerformed
        // TODO add your handling code here:
    Connection con = DatabaseConnection.dbConnection();
    if (con == null) {
        JOptionPane.showMessageDialog(null, "Database Connection Failed!");
        return;
    }

    try {
        int pID = Integer.parseInt(jProduct_id.getText());
        String productName = jProduct_name.getText();
        String categoryName = pComboBox.getSelectedItem().toString();

        // LOOKUP CATEGORY ID
        int categoryId = -1;
        String catSql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (PreparedStatement pstCat = con.prepareStatement(catSql)) {
            pstCat.setString(1, categoryName);
            try (ResultSet rsCat = pstCat.executeQuery()) {
                if (rsCat.next()) {
                    categoryId = rsCat.getInt("category_id");
                }
            }
        }

        // VALIDATE CATEGORY
        if (categoryId == -1) {
            JOptionPane.showMessageDialog(null, "Category Error: Select a valid category.");
            return;
        }

        // HANDLE IMAGE (Base64)
        String base64Image = "";
        if (imagePath != null) {
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
        }

        // INSERT
        String insertSql = "INSERT INTO products (product_id, product_name, category_name, category_id, description, "
        + "unit_price, cost_price, selling_price, stock_quantity, reorder_level, "
        + "unit_of_measure, status, supplier_name, barcode, product_image) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(insertSql)) {
    pst.setInt(1, pID);
    pst.setString(2, productName);
    pst.setString(3, categoryName);   // <-- added category_name
    pst.setInt(4, categoryId);         // <-- shifted from 3 to 4
    pst.setString(5, jDescription.getText());
    pst.setDouble(6, Double.parseDouble(jUnit_price.getText()));
    pst.setDouble(7, Double.parseDouble(jCost_price1.getText()));
    pst.setDouble(8, Double.parseDouble(jSelling_price.getText()));
    pst.setInt(9, Integer.parseInt(jQuantity.getText()));
    pst.setInt(10, Integer.parseInt(jReorder.getText()));
    pst.setString(11, uComboBox.getSelectedItem().toString());
    pst.setString(12, sComboBox1.getSelectedItem().toString());
    pst.setString(13, jsupbox.getSelectedItem().toString());
    pst.setString(14, jBarcode.getText().isEmpty() ? generateRandomBarcode() : jBarcode.getText());
    pst.setString(15, base64Image);
    pst.executeUpdate();
    JOptionPane.showMessageDialog(null, "Product successfully saved!");
    formWindowActivated(null);
    clearFields();
}

    } catch(Exception ex){
        JOptionPane.showMessageDialog(null, "Error inserting product: " + ex.getMessage());
        System.out.println(ex);
    }
    }//GEN-LAST:event_InsertBtnActionPerformed

    private void jpupdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jpupdateActionPerformed
        // TODO add your handling code here:
         int selectedRow = jTable1.getSelectedRow();

    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(null, "Please select a product first.");
        return;
    }

    try {
        Connection con = DatabaseConnection.dbConnection();

        int pID = Integer.parseInt(jProduct_id.getText());
        String productName = jProduct_name.getText();
        String categoryName = pComboBox.getSelectedItem().toString();
        String description = jDescription.getText();
        int unitPrice = Integer.parseInt(jUnit_price.getText());
        int costPrice = Integer.parseInt(jCost_price1.getText());
        int sellingPrice = Integer.parseInt(jSelling_price.getText());
        int quantity = Integer.parseInt(jQuantity.getText());
        int reorderLevel = Integer.parseInt(jReorder.getText());
        String unitOfMeasure = uComboBox.getSelectedItem().toString();
        String supplierName = jsupbox.getSelectedItem().toString();
        String status = sComboBox1.getSelectedItem().toString();
        String barcode = jBarcode.getText();

        // LOOKUP CATEGORY ID
        String catSql = "SELECT category_id FROM categories WHERE category_name = ?";
        PreparedStatement pstCat = con.prepareStatement(catSql);
        pstCat.setString(1, categoryName);
        ResultSet rsCat = pstCat.executeQuery();

        if (!rsCat.next()) {
            JOptionPane.showMessageDialog(this, "Category not found! Please select a valid category.");
            return;
        }

        int categoryId = rsCat.getInt("category_id");

        // UPDATE
        String sql = "UPDATE products SET product_name=?, category_name=?, category_id=?, description=?, unit_price=?, "
        + "cost_price=?, selling_price=?, stock_quantity=?, reorder_level=?, "
        + "unit_of_measure=?, status=?, supplier_name=?, barcode=? WHERE product_id=?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, productName);
        pst.setString(2, categoryName);   // <-- added category_name
        pst.setInt(3, categoryId);         // <-- shifted from 2 to 3
        pst.setString(4, description);
        pst.setInt(5, unitPrice);
        pst.setInt(6, costPrice);
        pst.setInt(7, sellingPrice);
        pst.setInt(8, quantity);
        pst.setInt(9, reorderLevel);
        pst.setString(10, unitOfMeasure);
        pst.setString(11, status);
        pst.setString(12, supplierName);
        pst.setString(13, barcode);
        pst.setInt(14, pID);       

        int updated = pst.executeUpdate();

        if (updated > 0) {
            JOptionPane.showMessageDialog(this, "Product updated successfully!");
            clearFields();
            formWindowActivated(null);
        } else {
            JOptionPane.showMessageDialog(this, "Update failed. Please try again.");
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
    }
    }//GEN-LAST:event_jpupdateActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    jProduct_id.setText("");
    jProduct_name.setText("");
    jDescription.setText("");
    jUnit_price.setText("");
    jSelling_price.setText("");
    jQuantity.setText("");
    jReorder.setText("");
    jBarcode.setText("");
    jCost_price1.setText("");

    pComboBox.setSelectedIndex(0);
    uComboBox.setSelectedIndex(0);
    jsupbox.setSelectedIndex(0);
    sComboBox1.setSelectedIndex(0);

    jpimage.setIcon(new ImageIcon("src/images/default_product.png"));
    imagePath = null;
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
         int confirm = JOptionPane.showConfirmDialog(null, "Return to Dashboard", "Are you sure?",JOptionPane.YES_NO_OPTION);
        if(confirm == YES_OPTION){
         Dashboard db = new  Dashboard();
        db.show();
        this.dispose();
        }else{
            
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jUnit_priceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jUnit_priceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jUnit_priceActionPerformed

    private void jReorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jReorderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jReorderActionPerformed

    private void jCost_price1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCost_price1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCost_price1ActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // TODO add your handling code here:
         loadSuppliers();
    loadCategories();
    
    Connection con = DatabaseConnection.dbConnection();
    ResultSet rs;

    javax.swing.table.DefaultTableModel dtm = 
    (javax.swing.table.DefaultTableModel) jTable1.getModel();

    dtm.setRowCount(0);

    try {
        PreparedStatement pst = con.prepareStatement(
        "SELECT * FROM products ORDER BY product_id ASC");

        rs = pst.executeQuery();

        while (rs.next()) {
            Object obj[] = {
                  rs.getInt("product_id"),
                  rs.getString("product_name"),
                  rs.getString("category_name"),
                  rs.getInt("unit_price"),
                  rs.getString("description"),
                  rs.getString("supplier_name"),
                  rs.getInt("cost_price"),
                  rs.getInt("selling_price"),
                  rs.getInt("stock_quantity"),
                  rs.getInt("reorder_level"),
                  rs.getString("status"),
                  rs.getString("date_added")
            };
            dtm.addRow(obj);
        }

    } catch (Exception ex) {
        System.out.println(ex);
    }
   
        
    }//GEN-LAST:event_formWindowActivated

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jpsearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jpsearchActionPerformed
        // TODO add your handling code here:
        String search = jTextField1.getText().trim(); // your search input
    Connection con = DatabaseConnection.dbConnection();
    DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
    dtm.setRowCount(0); // clear table before showing results

    try {
        // Search multiple columns using LIKE for partial matches
        String sql = "SELECT * FROM products WHERE "
                   + "product_id LIKE ? OR "
                   + "product_name LIKE ? OR "
                   + "category_name LIKE ? OR "
                   + "supplier_name LIKE ? OR "
                   + "reorder_level LIKE ? OR "
                   + "status LIKE ?";

        PreparedStatement pst = con.prepareStatement(sql);

        // Set search parameters (wrap with % for partial match)
        String query = "%" + search + "%";
        pst.setString(1, query); // product_id
        pst.setString(2, query); // product_name
        pst.setString(3, query); // category_name
        pst.setString(4, query); // supplier_name
        pst.setString(5, query); // reorder_level
        pst.setString(6, query); // status

        ResultSet rs = pst.executeQuery();

        boolean found = false;

        while (rs.next()) {
            found = true;
            Object obj[] = {
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("category_name"),
                rs.getInt("unit_price"),
                rs.getString("description"),
                rs.getString("supplier_name"),
                rs.getInt("cost_price"),
                rs.getInt("selling_price"),
                rs.getInt("stock_quantity"),
                rs.getInt("reorder_level"),
                rs.getString("status"),
                rs.getString("date_added")
            };

            dtm.addRow(obj);
        }

        if (!found) {
            JOptionPane.showMessageDialog(null, "No matching product found.");
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Search error: " + ex.getMessage());
    }
    }//GEN-LAST:event_jpsearchActionPerformed

    private void jprodaddimageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jprodaddimageActionPerformed
        
JFileChooser chooser = new JFileChooser();
    chooser.showOpenDialog(null);
    File f = chooser.getSelectedFile();
    
    if (f != null) {
        imagePath = f.getAbsolutePath();
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            Image scaledImg = img.getScaledInstance(jpimage.getWidth(), jpimage.getHeight(), Image.SCALE_SMOOTH);
            jpimage.setIcon(new ImageIcon(scaledImg));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error loading image");
        }
    }
    }//GEN-LAST:event_jprodaddimageActionPerformed

    private void jsupboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jsupboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jsupboxActionPerformed

    private void jprodDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jprodDelActionPerformed
        // TODO add your handling code here:
         // Check if a row is selected
     String productId = jProduct_id.getText().trim(); // ✅ was jProductId (doesn't exist)

    if (productId.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please select a product first.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to delete this product?",
        "Confirm Delete", JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) return;

    try {
        Connection con = DatabaseConnection.dbConnection();

        // Check if inventory transactions are linked
        String checkSQL = "SELECT COUNT(*) FROM inventory_transactions WHERE product_id = ?";
        PreparedStatement checkStmt = con.prepareStatement(checkSQL);
        checkStmt.setString(1, productId);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            int choice = JOptionPane.showConfirmDialog(this,
                "This product has " + rs.getInt(1) + " transaction record(s).\n" +
                "Delete the transactions and the product?",
                "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) return;

            PreparedStatement delTrans = con.prepareStatement(
                "DELETE FROM inventory_transactions WHERE product_id = ?");
            delTrans.setString(1, productId);
            delTrans.executeUpdate();
        }

        PreparedStatement pst = con.prepareStatement(
            "DELETE FROM products WHERE product_id = ?");
        pst.setString(1, productId);
        pst.executeUpdate();

        JOptionPane.showMessageDialog(this, "Product deleted successfully!");
        clearFields();           // ✅ clear form
        formWindowActivated(null); // ✅ refresh table

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage());
    }
    }//GEN-LAST:event_jprodDelActionPerformed

    private void jpfilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jpfilterActionPerformed
        // TODO add your handling code here:
        String filter = (String) jpfilter.getSelectedItem();
    
    if (filter == null || filter.equals("All") || filter.trim().isEmpty()) {
        // If "All" or nothing selected, just reload everything (reuse your search with empty string)
        String search = jTextField1.getText().trim();
        // fallback to normal search logic
        return;
    }
    
    String search = jTextField1.getText().trim();
    Connection con = DatabaseConnection.dbConnection();
    DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
    dtm.setRowCount(0);
    
    try {
        String column;
        switch (filter) {
            case "Category":    column = "category_name"; break;
            case "Status":      column = "status"; break;
            case "Reorder Level": column = "reorder_level"; break;
            case "Date Added":  column = "date_added"; break;
            default:            column = "product_name"; break;
        }
        
        String sql = "SELECT * FROM products WHERE " + column + " LIKE ? ORDER BY " + column + " ASC";
        PreparedStatement pst = con.prepareStatement(sql);
        String query = "%" + search + "%";
        pst.setString(1, query);
        
        ResultSet rs = pst.executeQuery();
        boolean found = false;
        while (rs.next()) {
            found = true;
            Object obj[] = {
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("category_name"),
                rs.getInt("unit_price"),
                rs.getString("description"),
                rs.getString("supplier_name"),
                rs.getInt("cost_price"),
                rs.getInt("selling_price"),
                rs.getInt("stock_quantity"),
                rs.getInt("reorder_level"),
                rs.getString("status"),
                rs.getString("date_added")
            };
            dtm.addRow(obj);
        }
        if (!found) {
            JOptionPane.showMessageDialog(null, "No matching product found.");
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Filter error: " + ex.getMessage());
    }
        
    }//GEN-LAST:event_jpfilterActionPerformed

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
            java.util.logging.Logger.getLogger(AddProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (ClassNotFoundException ex) {
        java.util.logging.Logger.getLogger(AddProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
        java.util.logging.Logger.getLogger(AddProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
        java.util.logging.Logger.getLogger(AddProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
        java.util.logging.Logger.getLogger(AddProduct.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AddProduct().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton InsertBtn;
    private javax.swing.JTextField jBarcode;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JTextField jCost_price1;
    private javax.swing.JTextArea jDescription;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jProduct_id;
    private javax.swing.JTextField jProduct_name;
    private javax.swing.JTextField jQuantity;
    private javax.swing.JTextField jReorder;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jSelling_price;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jUnit_price;
    private javax.swing.JComboBox<String> jpfilter;
    private javax.swing.JLabel jpimage;
    private javax.swing.JButton jprodDel;
    private javax.swing.JButton jprodaddimage;
    private javax.swing.JButton jpsearch;
    private javax.swing.JButton jpupdate;
    private javax.swing.JComboBox<String> jsupbox;
    private javax.swing.JComboBox<String> pComboBox;
    private javax.swing.JComboBox<String> sComboBox1;
    private javax.swing.JComboBox<String> uComboBox;
    // End of variables declaration//GEN-END:variables
}
