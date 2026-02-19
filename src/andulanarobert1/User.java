/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package andulanarobert1;

/**
 *
 * @author student
 */
public class User {
    private String Username;
    private String Password;
    
   
    
        public void Username(String User){
        this.Username = User;         
    }
        
        public void Password(String Pass){
        this.Password = Pass;         
    }
        public void Display(){
            System.out.println("username: "+Username);
            System.out.println("Password: "+Password);
        }
}
  

   