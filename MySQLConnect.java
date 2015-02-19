import java.sql.*;
import javax.swing.*;
public class MySQLConnect {
    Connection conn = null;
    public static Connection ConnectDb() {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://Localhost/schedulerdatabase", "admin", "pass");
            return conn;
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
    }
}
