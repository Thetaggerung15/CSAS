
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author the taggerung 15
 */
public class Algorithm extends SchedulerUI{
    /**
     * 
     * @param dates - Dates to be tested for conflicts
     * @param selectedCourse - The course currently displayed in the course ComboBox
     * @param assignmentType - 
     * @return Returns a 2D array of dates and conflicts.
     * @throws java.io.IOException
     */
    public int[] scheduler(String[] dates, String selectedCourse, String assignmentType) throws IOException, SQLException{
        int[] conflicts = new int[5];
        ArrayList<String> courses = new ArrayList<String>();
        if(assignmentType.equals("Out-of-Class")) {
            try {
                /**This is the query to be sent to the database*/
                String sql = "SELECT * FROM students WHERE studSchedule LIKE \"%" + selectedCourse + "%\"";
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                
                while(rs.next()) {
                    /**These are the values returned by the database in String form*/
                    String em = rs.getString(2);
                    /**This will hold the individual courses that the user is teaching*/
                    String[] arr = em.split(" ");
                    for(int i=0; i < arr.length; i++){
                        courses.add(arr[i]);
                    }
                }
                int[] hours = new int[12];
                for(int i=0; i<courses.size(); i++) {
                    sql = "SELECT * FROM courses WHERE courseName=?";
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, courses.get(i));
                    rs = pst.executeQuery();
                    
                    while(rs.next()) {
                        /**These are the values returned by the database in String form*/
                        String em = rs.getString(3);
                        em = em.replaceAll(":[0-9]+", "");
                        //JOptionPane.showMessageDialog(null, em);
                        String[] hrs = em.split("-");
                        //JOptionPane.showMessageDialog(null, Arrays.toString(hrs));
                        for(int j = 0; j < hrs.length; j++) {
                            int intHours = Integer.parseInt(hrs[j]);
                            if(7 < intHours && intHours < 13) {
                                hours[intHours-8]++;
                            }
                            else if(0 < intHours && intHours < 8) {
                                hours[intHours+5]++;
                            }
                        }
                        
                    }
                }
                JOptionPane.showMessageDialog(null, Arrays.toString(hours));
            }
            catch(Exception e) {
                //JOptionPane.showMessageDialog(null, e.getStackTrace());
            }
            return conflicts;
        }
        else {
            try {
                /**This is the query to be sent to the database*/
                String sql = "SELECT * FROM students WHERE studSchedule LIKE \"%" + selectedCourse + "%\"";
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
        
                while(rs.next()) {
                    /**These are the values returned by the database in String form*/
                    String em = rs.getString(2);
                    /**This will hold the individual courses that the user is teaching*/
                    String[] arr = em.split(" ");
                    for(int i=0; i < arr.length; i++){
                        courses.add(arr[i]);
                    }
                }
                for(int i=0; i<dates.length; i++) {
                    for(int j=0; j<courses.size(); j++) {
                        sql = "SELECT * FROM tasks WHERE taskCourse=? AND taskDate=?";
                        pst = conn.prepareStatement(sql);
                        pst.setString(1, courses.get(j));
                        pst.setString(2, dates[i]);
                        rs = pst.executeQuery();
                    
                        while(rs.next()) {
                            conflicts[i]++;
                        }
                    }
                }
            }
            catch(Exception e) {
                JOptionPane.showMessageDialog(null, e.getStackTrace());
            }
            return conflicts;
        }
    }
}
