/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package andulanarobert1;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author student
 */
public class DatabaseConnection {
  public static Connection dbConnection(){
      String host = "localhost"; //127.0.0.1
      String username = "root";
      String password = "";
      String databaseName = "pos_rob";
      
      
      try{
          Connection con = DriverManager.getConnection("jdbc:mysql://"+host+"/"+databaseName,username,password);
          return con;
      }
      catch(SQLException ex){
          System.out.println(ex);
      }
      
      return null;
  }  
}
