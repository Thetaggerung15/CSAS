
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
     * @return Returns a 2D array of dates and conflicts.
     * @throws java.io.IOException
     */
    public int[] scheduler(String[] dates, String selectedCourse) throws IOException, SQLException{
        int[] conflicts = new int[5];
        ArrayList<String> courses = new ArrayList<String>();
        try {
            /**This is the current value in the ComboBox on the Add Assignment Screen*/
            //String selectedCourse = addAssignmentCourseComboBox.getSelectedItem().toString();
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
                    pst=conn.prepareStatement(sql);
                    pst.setString(1, courses.get(j));
                    pst.setString(2, dates[i]);
                    rs=pst.executeQuery();
                    
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
