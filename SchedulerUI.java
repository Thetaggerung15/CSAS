
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.EmptyBorder;
import net.proteanit.sql.DbUtils;
import javax.swing.table.*;
import org.jdesktop.swingx.JXDatePicker;

/**
 * This is the SchedulerUI that creates the UI, connects to the database, and 
 * implements the scheduling algorithm.
 * @author: Sam Clement and Hunter Rice
 * @date: 5 February 2015
 */
public class SchedulerUI extends javax.swing.JFrame {
    /**This is the Connection for connecting to the database*/
    Connection conn = null;
    /**This is the ResultSet storing data retrieved from the database*/
    ResultSet rs = null;
    /**This is the PreparedStatment for sending queries to the database*/
    PreparedStatement pst = null;
    /**
     * Creates new form SchedulerUI
     */
    public SchedulerUI() {
        /**Initializes the UI*/
        initComponents();
        /**Establishes the connection to the database*/
        conn = MySQLConnect.ConnectDb();
        
    }
    /**
     * This is the method for updating the table of the Professor's Manage
     * Screen
     */
    private void updateManageProfTable() {
        try {
            /**This is the current value in the ComboBox on the Professor Manage Screen*/
            String selectedCourse = manageProfCourseComboBox.getSelectedItem().toString();
            /**This is the query to be sent to the database*/
            String sql = "SELECT * FROM students WHERE studSchedule LIKE \"%" + selectedCourse + "%\"";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            
            /**This populates the table with the values returned by the database*/
            profManageStudentTable.setModel(DbUtils.resultSetToTableModel(rs));
            
            /**This is the query to be sent to the database*/
            sql = "SELECT * FROM tasks WHERE taskCourse LIKE \"%" + selectedCourse + "%\"";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            
            /**This populates the table with the values returned by the database*/
            profManageTaskTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, e);
            JOptionPane.showMessageDialog(null, e.getStackTrace());
        }
    }
    /**
     * This method will update all of the tables on the Admin Manage Screen with 
     * data from the database
     */
    private void updateAdminLists() {
        try {
            /**This is the query to be sent to the database*/
            String sql = "SELECT * FROM courses";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            /**This populates the table with the values returned by the database*/
            adminCourseTable.setModel(DbUtils.resultSetToTableModel(rs));
            
            /**This is the query to be sent to the database*/
            sql = "SELECT * FROM professors";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            /**This populates the table with the values returned by the database*/
            adminProfessorTable.setModel(DbUtils.resultSetToTableModel(rs));
            
            /**This is the query to be sent to the database*/
            sql = "SELECT * FROM students";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            /**This populates the table with the values returned by the database*/
            adminStudentTable.setModel(DbUtils.resultSetToTableModel(rs));
        } 
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace());
        }
    }
    /**
     * This method will update all of the Course ComboBoxes with the schedule of 
     * the user
     */
    private void updateCourseCombo() {
        try {
            /**This is the query to be sent to the database*/
            String queryString = "SELECT profSchedule FROM professors WHERE profUsername=\"" + userTextField.getText() + "\"";
            pst = conn.prepareStatement(queryString);
            rs = pst.executeQuery();
            
            /**This will remove all of the elements from the Course ComboBoxes*/
            DefaultComboBoxModel manageModel = (DefaultComboBoxModel)manageProfCourseComboBox.getModel();
            manageModel.removeAllElements();
            DefaultComboBoxModel addModel = (DefaultComboBoxModel)addAssignmentCourseComboBox.getModel();
            addModel.removeAllElements();
            DefaultComboBoxModel calModel = (DefaultComboBoxModel)calendarCourseComboBox.getModel();
            calModel.removeAllElements();
            
            /**This will hold the individual courses that the user is teaching*/
            String[] arr = null;
            while(rs.next()) {
                /**These are the values returned by the database in String form*/
                String em = rs.getString(1);
                arr = em.split(" ");
                for(int i=0; i < arr.length; i++){
                    manageProfCourseComboBox.addItem(arr[i]);
                    addAssignmentCourseComboBox.addItem(arr[i]);
                    calendarCourseComboBox.addItem(arr[i]);
                }
            }
        } 
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace());
        }
    }

    /**These are the variables used to keep track of the real day, month, and year,
     * and the current month and day the calendar is displaying
     */
    static int realYear, realMonth, realDay, currentYear, currentMonth;
     /**
     * This method will create the calendar in the UI and populate it with the 
     * current day, month, and year.
     */
    private void initializeCal() {
        /**The Calendar used to find the current date*/
        GregorianCalendar cal = new GregorianCalendar();
        realDay = cal.get(GregorianCalendar.DAY_OF_MONTH);
        realMonth = cal.get(GregorianCalendar.MONTH);
        realYear = cal.get(GregorianCalendar.YEAR);
        currentMonth = realMonth;
        currentYear = realYear;
    }
    /**
     * This method will refresh the calendar
     * @param month - This is the month to repopulate the calendar with
     * @param year - This is the year to repopulate the calendar with
     */
    public void refreshCalendar(int month, int year){
        /**This is the array of all the months in a year*/
        String[] months =  {"January", "February", "March", "April", "May", "June", 
            "July", "August", "September", "October", "November", "December"};
        /**This is the number of days in the month and the day that is the start of month*/
        int nod, som;
        
        for (int i=0; i<6; i++){
            for (int j=0; j<7; j++){
                calendarTable.setValueAt(null, i, j);
            }
        }
        
        /**This is the calendar for getting info about the month and year*/
        GregorianCalendar cal = new GregorianCalendar(year, month, 1);
        nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        som = cal.get(GregorianCalendar.DAY_OF_WEEK);
        
        //String selectedCourse = calendarCourseComboBox.getSelectedItem().toString();
        
        for (int i=1; i<=nod; i++){
            String day = i + "";
            if(i < 10) {
                day = "0" + day;
            }
            try {
                String em = "";
                String sql = "SELECT taskName FROM tasks WHERE taskDate LIKE \"%" + day + " " + currentYear + "%\"";
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                while(rs.next()){
                    em = "- " + rs.getString(1) + "\n";
                }
                int row = new Integer((i+som-2)/7);
                int column = (i+som-2)%7;
                calendarTable.setValueAt((i + "\n" + em), row, column);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getStackTrace());
            }
            
        }
        monthLabel.setText(months[month]);
        yearLabel.setText(String.valueOf(year));
        calendarTable.setShowGrid(true);
    }
    /**
     * This method will schedule the selected task
     */
    public void scheduleTask() {
        try {
            /**The statement to be sent to the database to update it*/
            Statement stmt = conn.createStatement();
            /**This is the date chosen by the user*/
            String chosenDate = taskDatePicker.getDate().toString();
            chosenDate = chosenDate.replaceAll(" [0-9]+:[0-9]+:[0-9]+ EST", "");
            /**This is the type of assignment that the user selected*/
            String selectedType = typeTaskComboBox.getSelectedItem().toString();
            /**This is the selected course for the assignment*/
            String selectedCourse = addAssignmentCourseComboBox.getSelectedItem().toString();
            /**This is the query to be sent to the database*/
            String sql = "INSERT INTO tasks VALUES (\"" + selectedType + ": " 
                    + taskDescTextField.getText() + "\", \"" + selectedCourse 
                    + "\", \"" + chosenDate + "\")";
            stmt.executeUpdate(sql);
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, e.getStackTrace());
        }
    }
    /**
     * This Finds the next three non-weekend days
     * @return Returns an array of the next three days from the current day
     */
    public String[] nextThreeDays() {
        String[] nextThreeDays = new String[5];
        /**This is the calendar to find the current day*/
        Calendar c = Calendar.getInstance();
        java.util.Date current = new java.util.Date();
        c.setTime(current);
        c.add(Calendar.DAY_OF_YEAR, 1);
        
        int count = 0;
        if(c.get(Calendar.DAY_OF_WEEK) == 1 || c.get(Calendar.DAY_OF_WEEK) == 7) {
            int days = (Calendar.SATURDAY - c.get(Calendar.DAY_OF_WEEK) + 2) % 7;  
            c.add(Calendar.DAY_OF_YEAR, days); 
        }
        while(count < 5) {
            if (c.get(Calendar.DAY_OF_WEEK) == 7) {
                int days = (Calendar.SATURDAY - c.get(Calendar.DAY_OF_WEEK) + 2) % 7;  
                c.add(Calendar.DAY_OF_YEAR, days);
            }
            else {
                String temp = c.getTime().toString();
                temp = temp.replaceAll(" [0-9]+:[0-9]+:[0-9]+ EST", "");
                nextThreeDays[count] = temp;
                c.add(Calendar.DAY_OF_YEAR, 1);
                count++;
            }
        }
        return nextThreeDays;
    }
    /**
     * 
     * @param dates - Dates to be tested for conflicts
     * @return Returns a 2D array of dates and conflicts.
     * @throws java.io.IOException
     */
    public int[] scheduler(String[] dates) throws IOException, SQLException{
        int[] conflicts = new int[5];
        ArrayList<String> courses = new ArrayList<String>();
        try {
            /**This is the current value in the ComboBox on the Add Assignment Screen*/
            String selectedCourse = addAssignmentCourseComboBox.getSelectedItem().toString();
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        loginPanel = new javax.swing.JPanel();
        loginButton = new javax.swing.JButton();
        titleLabel1 = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();
        infoLabel1 = new javax.swing.JLabel();
        aboutButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        passLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        passPasswordField = new javax.swing.JPasswordField();
        userTextField = new javax.swing.JTextField();
        aboutPanel = new javax.swing.JPanel();
        userLabel1 = new javax.swing.JLabel();
        passLabel1 = new javax.swing.JLabel();
        backAboutButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        calendarPanel = new javax.swing.JPanel();
        optionPanel = new javax.swing.JPanel();
        calendarCourseComboBox = new javax.swing.JComboBox();
        filterComboBox = new javax.swing.JComboBox();
        addAssignmentButton = new javax.swing.JButton();
        manageButton = new javax.swing.JButton();
        logoutButton = new javax.swing.JButton();
        courseLabel = new javax.swing.JLabel();
        filterLabel = new javax.swing.JLabel();
        monthLabel = new javax.swing.JLabel();
        yearLabel = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        calendarScrollPane = new javax.swing.JScrollPane();
        calendarTable = new javax.swing.JTable();
        addAssignmentPanel = new javax.swing.JPanel();
        typeTaskComboBox = new javax.swing.JComboBox();
        addAssignmentCourseComboBox = new javax.swing.JComboBox();
        taskDescTextField = new javax.swing.JTextField();
        assignTaskButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        suggestButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        firstDateLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        firstLabel = new javax.swing.JLabel();
        thirdLabel = new javax.swing.JLabel();
        secondLabel = new javax.swing.JLabel();
        cancelTaskButton = new javax.swing.JButton();
        fourthDateLabel = new javax.swing.JLabel();
        fifthDateLabel = new javax.swing.JLabel();
        fourthLabel = new javax.swing.JLabel();
        thirdLabel2 = new javax.swing.JLabel();
        taskDatePicker = new org.jdesktop.swingx.JXDatePicker();
        manageProfPanel = new javax.swing.JPanel();
        manageProfCoursePanel = new javax.swing.JPanel();
        manageProfCourseComboBox = new javax.swing.JComboBox();
        manageProfAddCourseButton = new javax.swing.JButton();
        manageProfStudentPanel = new javax.swing.JPanel();
        manageProfRemoveStudentButton = new javax.swing.JButton();
        manageProfImportStudentButton = new javax.swing.JButton();
        manageProfCourseLabel = new javax.swing.JLabel();
        manageProfStudentLabel = new javax.swing.JLabel();
        manageProfBackButton = new javax.swing.JButton();
        taskProfTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        profManageStudentTable = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        profManageTaskTable = new javax.swing.JTable();
        manageAdminPanel = new javax.swing.JPanel();
        manageAdminLogoutButton = new javax.swing.JButton();
        manageAdminCoursePanel = new javax.swing.JPanel();
        manageAdminAddCourseButton = new javax.swing.JButton();
        manageAdminImportCourseButton = new javax.swing.JButton();
        manageAdminCoursesStudentsTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        adminCourseTable = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        adminProfessorTable = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        adminStudentTable = new javax.swing.JTable();
        adminManageAdminManageLabel = new javax.swing.JLabel();
        fileChooser = new javax.swing.JFileChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Scheduler");
        setBackground(new java.awt.Color(90, 35, 135));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        mainPanel.setLayout(new java.awt.CardLayout());

        loginPanel.setBackground(new java.awt.Color(90, 45, 135));

        loginButton.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        loginButton.setText("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });

        titleLabel1.setFont(new java.awt.Font("Tahoma", 0, 64)); // NOI18N
        titleLabel1.setForeground(new java.awt.Color(255, 255, 255));
        titleLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel1.setText("WCU CS Assignment Scheduler");
        titleLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        errorLabel.setForeground(new java.awt.Color(255, 255, 255));
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        infoLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        infoLabel1.setForeground(new java.awt.Color(255, 255, 255));
        infoLabel1.setText("by: Sam Clement and Hunter Rice");

        aboutButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        aboutButton.setText("About");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(90, 45, 135));

        passLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        passLabel.setForeground(new java.awt.Color(255, 255, 255));
        passLabel.setText("Password:");

        userLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        userLabel.setForeground(new java.awt.Color(255, 255, 255));
        userLabel.setText("Username:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(userLabel)
                    .addComponent(passLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(userTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(passPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passLabel))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {passPasswordField, userTextField});

        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(titleLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1380, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(infoLabel1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loginButton)
                    .addComponent(aboutButton))
                .addGap(119, 119, 119))
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addGap(242, 242, 242)
                .addComponent(titleLabel1)
                .addGap(18, 18, 18)
                .addComponent(infoLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(loginButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addComponent(aboutButton)
                .addContainerGap())
        );

        mainPanel.add(loginPanel, "loginCard");

        aboutPanel.setBackground(new java.awt.Color(90, 45, 135));

        userLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        userLabel1.setForeground(new java.awt.Color(255, 255, 255));
        userLabel1.setText("Sam Clement");

        passLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        passLabel1.setForeground(new java.awt.Color(255, 255, 255));
        passLabel1.setText("Hunter Rice");

        backAboutButton.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        backAboutButton.setText("Back");
        backAboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backAboutButtonActionPerformed(evt);
            }
        });

        jTextArea1.setBackground(new java.awt.Color(90, 45, 135));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 18)); // NOI18N
        jTextArea1.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("My name is Hunter Rice and I am a Computer Science Major at Western Carolina University.\n\n");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea1);

        jTextArea2.setBackground(new java.awt.Color(90, 45, 135));
        jTextArea2.setColumns(20);
        jTextArea2.setFont(new java.awt.Font("Monospaced", 0, 18)); // NOI18N
        jTextArea2.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jTextArea2.setText("My name is Sam Clement and I am a Computer Science Major at Western Carolina University.");
        jTextArea2.setWrapStyleWord(true);
        jScrollPane3.setViewportView(jTextArea2);

        javax.swing.GroupLayout aboutPanelLayout = new javax.swing.GroupLayout(aboutPanel);
        aboutPanel.setLayout(aboutPanelLayout);
        aboutPanelLayout.setHorizontalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addComponent(userLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(passLabel1))
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 267, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addComponent(backAboutButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        aboutPanelLayout.setVerticalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel1)
                    .addComponent(passLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 240, Short.MAX_VALUE)
                .addComponent(backAboutButton)
                .addContainerGap())
        );

        mainPanel.add(aboutPanel, "about");

        calendarPanel.setBackground(new java.awt.Color(255, 255, 255));

        optionPanel.setBackground(new java.awt.Color(90, 45, 135));

        calendarCourseComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Course1", "Course2", "Course3" }));
        calendarCourseComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calendarCourseComboBoxActionPerformed(evt);
            }
        });

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Tests", "Quizzes", "Projects", "Homeworks" }));

        addAssignmentButton.setText("Add Assignment");
        addAssignmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAssignmentButtonActionPerformed(evt);
            }
        });

        manageButton.setText("Manage Courses");
        manageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageButtonActionPerformed(evt);
            }
        });

        logoutButton.setText("Logout");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        courseLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        courseLabel.setForeground(new java.awt.Color(255, 255, 255));
        courseLabel.setText("Course:");

        filterLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        filterLabel.setForeground(new java.awt.Color(255, 255, 255));
        filterLabel.setText("Filter:");

        monthLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        monthLabel.setForeground(new java.awt.Color(255, 255, 255));
        switch (monthLabel.getText()) {
            case "0":  monthLabel.setText("January");
            break;
            case "1":  monthLabel.setText("February");
            break;
            case "2":  monthLabel.setText("March");
            break;
            case "3":  monthLabel.setText("April");
            break;
            case "4":  monthLabel.setText("May");
            break;
            case "5":  monthLabel.setText("June");
            break;
            case "6":  monthLabel.setText("July");
            break;
            case "7":  monthLabel.setText("August");
            break;
            case "8":  monthLabel.setText("September");
            break;
            case "9":  monthLabel.setText("October");
            break;
            case "10": monthLabel.setText("November");
            break;
            case "11": monthLabel.setText("December");
            break;
            default: monthLabel.setText("Error");
            break;
        }

        yearLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        yearLabel.setForeground(new java.awt.Color(255, 255, 255));

        jButton3.setText("<<");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText(">>");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optionPanelLayout = new javax.swing.GroupLayout(optionPanel);
        optionPanel.setLayout(optionPanelLayout);
        optionPanelLayout.setHorizontalGroup(
            optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logoutButton)
                    .addGroup(optionPanelLayout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(monthLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yearLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1295, Short.MAX_VALUE)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(courseLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(filterLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(optionPanelLayout.createSequentialGroup()
                        .addComponent(calendarCourseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addAssignmentButton))
                    .addGroup(optionPanelLayout.createSequentialGroup()
                        .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(56, 56, 56)
                        .addComponent(manageButton)))
                .addContainerGap())
        );
        optionPanelLayout.setVerticalGroup(
            optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addGap(0, 11, Short.MAX_VALUE)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addAssignmentButton)
                    .addComponent(calendarCourseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logoutButton)
                    .addComponent(courseLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manageButton)
                    .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterLabel)
                    .addComponent(monthLabel)
                    .addComponent(yearLabel)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addContainerGap())
        );

        calendarTable.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        calendarTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"", null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        calendarTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        calendarTable.setFillsViewportHeight(true);
        calendarTable.setGridColor(new java.awt.Color(0, 0, 0));
        calendarTable.setOpaque(false);
        calendarTable.setRowHeight(100);
        calendarTable.setDefaultRenderer(Object.class, new MultiLineCellRenderer());
        calendarTable.setRowSelectionAllowed(false);
        calendarTable.setSelectionBackground(new java.awt.Color(90, 45, 135));
        calendarTable.setSelectionForeground(new java.awt.Color(90, 45, 135));
        calendarTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        calendarTable.getTableHeader().setReorderingAllowed(false);
        calendarTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                calendarTableMousePressed(evt);
            }
        });
        calendarScrollPane.setViewportView(calendarTable);
        if (calendarTable.getColumnModel().getColumnCount() > 0) {
            calendarTable.getColumnModel().getColumn(0).setResizable(false);
            calendarTable.getColumnModel().getColumn(1).setResizable(false);
            calendarTable.getColumnModel().getColumn(2).setResizable(false);
            calendarTable.getColumnModel().getColumn(3).setResizable(false);
            calendarTable.getColumnModel().getColumn(4).setResizable(false);
            calendarTable.getColumnModel().getColumn(5).setResizable(false);
            calendarTable.getColumnModel().getColumn(6).setResizable(false);
        }

        javax.swing.GroupLayout calendarPanelLayout = new javax.swing.GroupLayout(calendarPanel);
        calendarPanel.setLayout(calendarPanelLayout);
        calendarPanelLayout.setHorizontalGroup(
            calendarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(optionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(calendarScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        calendarPanelLayout.setVerticalGroup(
            calendarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(calendarPanelLayout.createSequentialGroup()
                .addComponent(optionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(calendarScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE))
        );

        mainPanel.add(calendarPanel, "calendarCard");

        addAssignmentPanel.setBackground(new java.awt.Color(90, 45, 135));

        typeTaskComboBox.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        typeTaskComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Test", "Quiz", "Project", "Homework" }));

        addAssignmentCourseComboBox.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        addAssignmentCourseComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Course1", "Course2", "Course3", "Course4" }));

        taskDescTextField.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

        assignTaskButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        assignTaskButton.setText("Assign Task");
        assignTaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignTaskButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Date:");

        suggestButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        suggestButton.setText("Suggest a Date");
        suggestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suggestButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("2nd Date:");

        firstDateLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        firstDateLabel.setForeground(new java.awt.Color(255, 255, 255));
        firstDateLabel.setText("1st Date:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("3rd Date:");

        firstLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        firstLabel.setForeground(new java.awt.Color(255, 255, 255));

        thirdLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        thirdLabel.setForeground(new java.awt.Color(255, 255, 255));

        secondLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        secondLabel.setForeground(new java.awt.Color(255, 255, 255));

        cancelTaskButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        cancelTaskButton.setText("Cancel");
        cancelTaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelTaskButtonActionPerformed(evt);
            }
        });

        fourthDateLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        fourthDateLabel.setForeground(new java.awt.Color(255, 255, 255));
        fourthDateLabel.setText("4th Date:");

        fifthDateLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        fifthDateLabel.setForeground(new java.awt.Color(255, 255, 255));
        fifthDateLabel.setText("5th Date:");

        fourthLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        fourthLabel.setForeground(new java.awt.Color(255, 255, 255));

        thirdLabel2.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        thirdLabel2.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout addAssignmentPanelLayout = new javax.swing.GroupLayout(addAssignmentPanel);
        addAssignmentPanel.setLayout(addAssignmentPanelLayout);
        addAssignmentPanelLayout.setHorizontalGroup(
            addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                        .addComponent(typeTaskComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(taskDescTextField))
                    .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(thirdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                        .addComponent(firstDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(firstLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(secondLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                        .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addAssignmentCourseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(taskDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(suggestButton)
                            .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                                .addComponent(assignTaskButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelTaskButton)))
                        .addGap(0, 1491, Short.MAX_VALUE))
                    .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                        .addComponent(fifthDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(thirdLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                        .addComponent(fourthDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fourthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        addAssignmentPanelLayout.setVerticalGroup(
            addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addAssignmentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addAssignmentCourseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(taskDescTextField)
                    .addComponent(typeTaskComboBox))
                .addGap(18, 18, 18)
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(taskDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(suggestButton)
                .addGap(18, 18, 18)
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(firstDateLabel)
                    .addComponent(firstLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(secondLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(thirdLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fourthDateLabel)
                    .addComponent(fourthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fifthDateLabel)
                    .addComponent(thirdLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(177, 177, 177)
                .addGroup(addAssignmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(assignTaskButton)
                    .addComponent(cancelTaskButton))
                .addContainerGap())
        );

        mainPanel.add(addAssignmentPanel, "addAssignmentCard");

        manageProfPanel.setBackground(new java.awt.Color(90, 45, 135));

        manageProfCourseComboBox.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        manageProfCourseComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageProfCourseComboBoxActionPerformed(evt);
            }
        });

        manageProfAddCourseButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        manageProfAddCourseButton.setText("Add/Remove Course");

        javax.swing.GroupLayout manageProfCoursePanelLayout = new javax.swing.GroupLayout(manageProfCoursePanel);
        manageProfCoursePanel.setLayout(manageProfCoursePanelLayout);
        manageProfCoursePanelLayout.setHorizontalGroup(
            manageProfCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageProfCoursePanelLayout.createSequentialGroup()
                .addGroup(manageProfCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(manageProfCoursePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(manageProfAddCourseButton))
                    .addGroup(manageProfCoursePanelLayout.createSequentialGroup()
                        .addGap(68, 68, 68)
                        .addComponent(manageProfCourseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(122, Short.MAX_VALUE))
        );
        manageProfCoursePanelLayout.setVerticalGroup(
            manageProfCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageProfCoursePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(manageProfCourseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(manageProfAddCourseButton)
                .addContainerGap())
        );

        manageProfRemoveStudentButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        manageProfRemoveStudentButton.setText("Add Student");
        manageProfRemoveStudentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageProfRemoveStudentButtonActionPerformed(evt);
            }
        });

        manageProfImportStudentButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        manageProfImportStudentButton.setText("Import Students");
        manageProfImportStudentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageProfImportStudentButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout manageProfStudentPanelLayout = new javax.swing.GroupLayout(manageProfStudentPanel);
        manageProfStudentPanel.setLayout(manageProfStudentPanelLayout);
        manageProfStudentPanelLayout.setHorizontalGroup(
            manageProfStudentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageProfStudentPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(manageProfRemoveStudentButton)
                .addGap(18, 18, 18)
                .addComponent(manageProfImportStudentButton)
                .addContainerGap())
        );
        manageProfStudentPanelLayout.setVerticalGroup(
            manageProfStudentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageProfStudentPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(manageProfStudentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manageProfImportStudentButton)
                    .addComponent(manageProfRemoveStudentButton))
                .addContainerGap())
        );

        manageProfCourseLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        manageProfCourseLabel.setForeground(new java.awt.Color(255, 255, 255));
        manageProfCourseLabel.setText("Courses");

        manageProfStudentLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        manageProfStudentLabel.setForeground(new java.awt.Color(255, 255, 255));
        manageProfStudentLabel.setText("Students");

        manageProfBackButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        manageProfBackButton.setText("Go Back");
        manageProfBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageProfBackButtonActionPerformed(evt);
            }
        });

        profManageStudentTable.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        profManageStudentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Courses"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        profManageStudentTable.setColumnSelectionAllowed(true);
        profManageStudentTable.setRowHeight(25);
        profManageStudentTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(profManageStudentTable);
        profManageStudentTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        taskProfTabbedPane.addTab("Students", jScrollPane1);

        profManageTaskTable.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        profManageTaskTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Name", "Courses", "Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        profManageTaskTable.setRowHeight(25);
        profManageTaskTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane7.setViewportView(profManageTaskTable);
        profManageTaskTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        taskProfTabbedPane.addTab("Tasks", jScrollPane7);

        javax.swing.GroupLayout manageProfPanelLayout = new javax.swing.GroupLayout(manageProfPanel);
        manageProfPanel.setLayout(manageProfPanelLayout);
        manageProfPanelLayout.setHorizontalGroup(
            manageProfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageProfPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manageProfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(manageProfCoursePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(manageProfPanelLayout.createSequentialGroup()
                        .addComponent(manageProfBackButton)
                        .addGap(12, 12, 12)
                        .addComponent(manageProfCourseLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(manageProfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(manageProfPanelLayout.createSequentialGroup()
                        .addGap(292, 292, 292)
                        .addComponent(manageProfStudentLabel)
                        .addContainerGap(1032, Short.MAX_VALUE))
                    .addGroup(manageProfPanelLayout.createSequentialGroup()
                        .addGroup(manageProfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(manageProfStudentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(taskProfTabbedPane))
                        .addContainerGap())))
        );
        manageProfPanelLayout.setVerticalGroup(
            manageProfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageProfPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manageProfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(manageProfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(manageProfStudentLabel)
                        .addComponent(manageProfCourseLabel))
                    .addComponent(manageProfBackButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(manageProfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(manageProfPanelLayout.createSequentialGroup()
                        .addComponent(taskProfTabbedPane)
                        .addGap(11, 11, 11)
                        .addComponent(manageProfStudentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(manageProfCoursePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        mainPanel.add(manageProfPanel, "manageProfCard");

        manageAdminPanel.setBackground(new java.awt.Color(90, 45, 135));

        manageAdminLogoutButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        manageAdminLogoutButton.setText("Logout");
        manageAdminLogoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageAdminLogoutButtonActionPerformed(evt);
            }
        });

        manageAdminAddCourseButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        manageAdminAddCourseButton.setText("Add/Remove");

        manageAdminImportCourseButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        manageAdminImportCourseButton.setText("Import");

        javax.swing.GroupLayout manageAdminCoursePanelLayout = new javax.swing.GroupLayout(manageAdminCoursePanel);
        manageAdminCoursePanel.setLayout(manageAdminCoursePanelLayout);
        manageAdminCoursePanelLayout.setHorizontalGroup(
            manageAdminCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageAdminCoursePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(manageAdminAddCourseButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addComponent(manageAdminImportCourseButton)
                .addContainerGap())
        );
        manageAdminCoursePanelLayout.setVerticalGroup(
            manageAdminCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, manageAdminCoursePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manageAdminCoursePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manageAdminAddCourseButton)
                    .addComponent(manageAdminImportCourseButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        adminCourseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Name", "Professor", "Students"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(adminCourseTable);
        if (adminCourseTable.getColumnModel().getColumnCount() > 0) {
            adminCourseTable.getColumnModel().getColumn(0).setResizable(false);
            adminCourseTable.getColumnModel().getColumn(1).setResizable(false);
            adminCourseTable.getColumnModel().getColumn(2).setResizable(false);
        }

        manageAdminCoursesStudentsTabbedPane.addTab("Courses", jScrollPane4);

        adminProfessorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "Department", "Courses", "Username", "password"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(adminProfessorTable);
        if (adminProfessorTable.getColumnModel().getColumnCount() > 0) {
            adminProfessorTable.getColumnModel().getColumn(0).setResizable(false);
            adminProfessorTable.getColumnModel().getColumn(1).setResizable(false);
            adminProfessorTable.getColumnModel().getColumn(2).setResizable(false);
            adminProfessorTable.getColumnModel().getColumn(3).setResizable(false);
            adminProfessorTable.getColumnModel().getColumn(4).setResizable(false);
        }

        manageAdminCoursesStudentsTabbedPane.addTab("Professors", jScrollPane5);

        adminStudentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Courses"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(adminStudentTable);
        if (adminStudentTable.getColumnModel().getColumnCount() > 0) {
            adminStudentTable.getColumnModel().getColumn(0).setResizable(false);
            adminStudentTable.getColumnModel().getColumn(1).setResizable(false);
        }

        manageAdminCoursesStudentsTabbedPane.addTab("Students", jScrollPane6);

        adminManageAdminManageLabel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        adminManageAdminManageLabel.setForeground(new java.awt.Color(255, 255, 255));
        adminManageAdminManageLabel.setText("Admin Manage");

        javax.swing.GroupLayout manageAdminPanelLayout = new javax.swing.GroupLayout(manageAdminPanel);
        manageAdminPanel.setLayout(manageAdminPanelLayout);
        manageAdminPanelLayout.setHorizontalGroup(
            manageAdminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageAdminPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manageAdminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(manageAdminPanelLayout.createSequentialGroup()
                        .addComponent(manageAdminCoursePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(manageAdminPanelLayout.createSequentialGroup()
                        .addGroup(manageAdminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(manageAdminCoursesStudentsTabbedPane)
                            .addGroup(manageAdminPanelLayout.createSequentialGroup()
                                .addComponent(manageAdminLogoutButton)
                                .addGap(311, 311, 311)
                                .addComponent(adminManageAdminManageLabel)
                                .addGap(0, 1089, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        manageAdminPanelLayout.setVerticalGroup(
            manageAdminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manageAdminPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manageAdminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(manageAdminLogoutButton)
                    .addComponent(adminManageAdminManageLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manageAdminCoursesStudentsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(manageAdminCoursePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainPanel.add(manageAdminPanel, "manageAdminCard");

        fileChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });
        mainPanel.add(fileChooser, "importCard");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * This will check the username and password and if they are correct it will
     * change the screen to the Calendar Screen
     */
    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        /**This is the query to send to the database to check the username and password*/
        String sql = "SELECT * FROM professors WHERE profUsername=? AND profPassword=?";
        
        try {
            pst=conn.prepareStatement(sql);
            pst.setString(1, userTextField.getText());
            pst.setString(2, passPasswordField.getText());
            rs=pst.executeQuery();
            
            if(rs.next()) {
                updateCourseCombo();
                initializeCal();
                refreshCalendar(currentMonth, currentYear);
                CardLayout card = (CardLayout)mainPanel.getLayout();
                card.show(mainPanel, "calendarCard");
            }
            else if(userTextField.getText().equals("admin") && passPasswordField.getText().equals("admin")) {
                updateAdminLists();
                CardLayout card = (CardLayout)mainPanel.getLayout();
                card.show(mainPanel, "manageAdminCard");
            }
            else {
                errorLabel.setText("Invalid Username or Password");
                passPasswordField.setText("");
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace());
        }
    }//GEN-LAST:event_loginButtonActionPerformed
    /**
     * This maximizes the window and creates the connection to the database
     */
    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        setExtendedState(MAXIMIZED_BOTH);
        conn = MySQLConnect.ConnectDb();
    }//GEN-LAST:event_formWindowOpened
    /**
     * This will change the screen to the Login Screen 
     */
    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "loginCard");
        userTextField.setText("");
        passPasswordField.setText("");
        errorLabel.setText("");
    }//GEN-LAST:event_logoutButtonActionPerformed
    /**
     * This well change the screen to the Add Assignment Screen 
     */
    private void addAssignmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAssignmentButtonActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "addAssignmentCard");
    }//GEN-LAST:event_addAssignmentButtonActionPerformed
    /**
     * This well change the screen to the Calendar Screen 
     */
    private void manageProfBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageProfBackButtonActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "calendarCard");
    }//GEN-LAST:event_manageProfBackButtonActionPerformed
    /**
     * This will change the screen to the Professor Manage Screen
     */
    private void manageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageButtonActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "manageProfCard");
        updateManageProfTable();
    }//GEN-LAST:event_manageButtonActionPerformed
    /**
     * This will change the screen to the Login Screen 
     */
    private void manageAdminLogoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageAdminLogoutButtonActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "loginCard");
        userTextField.setText("");
        passPasswordField.setText("");
        errorLabel.setText("");
    }//GEN-LAST:event_manageAdminLogoutButtonActionPerformed
    /**
     * This will assign a task and change to the Calendar Screen
     */
    private void assignTaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignTaskButtonActionPerformed
        scheduleTask();
        taskDescTextField.setText("");
        firstLabel.setText("");
        secondLabel.setText("");
        thirdLabel.setText("");
        fourthLabel.setText("");
        thirdLabel2.setText("");
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "calendarCard");
    }//GEN-LAST:event_assignTaskButtonActionPerformed
    /**
     * This will change the screen to the Import Screen
     */
    private void manageProfImportStudentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageProfImportStudentButtonActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "importCard");
    }//GEN-LAST:event_manageProfImportStudentButtonActionPerformed
    /**
     * This will assign the selected File to a variable 
     */
    private void fileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserActionPerformed
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
        }
    }//GEN-LAST:event_fileChooserActionPerformed
    /**
     * This will remove a student from a professor's course 
     */
    private void manageProfRemoveStudentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageProfRemoveStudentButtonActionPerformed
        String inputValue = JOptionPane.showInputDialog("Enter New Student Name");
        String sql = "SELECT * FROM students WHERE studName=\"" + inputValue + "\"";
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            profManageStudentTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getStackTrace());
        }
    }//GEN-LAST:event_manageProfRemoveStudentButtonActionPerformed
    /**
     * This will update the Professor's Manage Screen Table 
     */
    private void manageProfCourseComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageProfCourseComboBoxActionPerformed
        updateManageProfTable();
    }//GEN-LAST:event_manageProfCourseComboBoxActionPerformed
    /**
     * This will change the screen to the Login Screen
     */
    private void backAboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backAboutButtonActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "loginCard");
    }//GEN-LAST:event_backAboutButtonActionPerformed
    /**
     * This will change the screen to the About Screen 
     */
    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "about");
    }//GEN-LAST:event_aboutButtonActionPerformed
    /**
     * This will revert the calendar by one month 
     */
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (currentMonth == 0){
            currentMonth = 11;
            currentYear -= 1;
        }
        else{
            currentMonth -= 1;
        }
        refreshCalendar(currentMonth, currentYear);
    }//GEN-LAST:event_jButton3ActionPerformed
    /**
     * This will advance the calendar by one month 
     */
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (currentMonth == 11){
            currentMonth = 0;
            currentYear += 1;
        }
        else{
            currentMonth += 1;
        }
        refreshCalendar(currentMonth, currentYear);
    }//GEN-LAST:event_jButton4ActionPerformed
    /**
     * This will suggest the next three days for an assignment to be scheduled
     */
    private void suggestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suggestButtonActionPerformed
        String[] days = new String[5];
        int[] conflicts = new int[5];
        days = nextThreeDays();
        try {
            conflicts = scheduler(days);
        }
        catch (IOException ex) {
            //Logger.getLogger(SchedulerUI.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (SQLException ex) {
            //Logger.getLogger(SchedulerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        firstLabel.setText(conflicts[0] + " conflicts on " + days[0]);
        secondLabel.setText(conflicts[1] + " conflicts on " + days[1]);
        thirdLabel.setText(conflicts[2] + " conflicts on " + days[2]);
        fourthLabel.setText(conflicts[3] + " conflicts on " + days[3]);
        thirdLabel2.setText(conflicts[4] + " conflicts on " + days[4]);
    }//GEN-LAST:event_suggestButtonActionPerformed
    /**
     * This will open up the assignments for the selected day 
     */
    private void calendarTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_calendarTableMousePressed
        JTable table =(JTable) evt.getSource();
        Point p = evt.getPoint();
        int row = table.rowAtPoint(p);
        if (evt.getClickCount() == 2) {
            JOptionPane.showMessageDialog(null, null, "" + row, 1); 
        }
    }//GEN-LAST:event_calendarTableMousePressed

    private void cancelTaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelTaskButtonActionPerformed
        taskDescTextField.setText("");
        firstLabel.setText("");
        secondLabel.setText("");
        thirdLabel.setText("");
        fourthLabel.setText("");
        thirdLabel2.setText("");
        CardLayout card = (CardLayout)mainPanel.getLayout();
        card.show(mainPanel, "calendarCard");
    }//GEN-LAST:event_cancelTaskButtonActionPerformed

    private void calendarCourseComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarCourseComboBoxActionPerformed
        //refreshCalendar(currentMonth, currentYear);
    }//GEN-LAST:event_calendarCourseComboBoxActionPerformed

    /**
     * This will run the entire program
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SchedulerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SchedulerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SchedulerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SchedulerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SchedulerUI().setVisible(true);
            }
        });
    }
class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

  public MultiLineCellRenderer() {
    setLineWrap(true);
    setWrapStyleWord(true);
    setOpaque(true);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    } else {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }
    setFont(table.getFont());
    if (hasFocus) {
      setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
      if (table.isCellEditable(row, column)) {
        setForeground(UIManager.getColor("Table.focusCellForeground"));
        setBackground(UIManager.getColor("Table.focusCellBackground"));
      }
    } else {
      setBorder(new EmptyBorder(1, 2, 1, 2));
    }
    setText((value == null) ? "" : value.toString());
    return this;
  }
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JPanel aboutPanel;
    private javax.swing.JButton addAssignmentButton;
    private javax.swing.JComboBox addAssignmentCourseComboBox;
    private javax.swing.JPanel addAssignmentPanel;
    private javax.swing.JTable adminCourseTable;
    private javax.swing.JLabel adminManageAdminManageLabel;
    private javax.swing.JTable adminProfessorTable;
    private javax.swing.JTable adminStudentTable;
    private javax.swing.JButton assignTaskButton;
    private javax.swing.JButton backAboutButton;
    private javax.swing.JComboBox calendarCourseComboBox;
    private javax.swing.JPanel calendarPanel;
    private javax.swing.JScrollPane calendarScrollPane;
    private javax.swing.JTable calendarTable;
    private javax.swing.JButton cancelTaskButton;
    private javax.swing.JLabel courseLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel fifthDateLabel;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JComboBox filterComboBox;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JLabel firstDateLabel;
    private javax.swing.JLabel firstLabel;
    private javax.swing.JLabel fourthDateLabel;
    private javax.swing.JLabel fourthLabel;
    private javax.swing.JLabel infoLabel1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JButton loginButton;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JButton logoutButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton manageAdminAddCourseButton;
    private javax.swing.JPanel manageAdminCoursePanel;
    private javax.swing.JTabbedPane manageAdminCoursesStudentsTabbedPane;
    private javax.swing.JButton manageAdminImportCourseButton;
    private javax.swing.JButton manageAdminLogoutButton;
    private javax.swing.JPanel manageAdminPanel;
    private javax.swing.JButton manageButton;
    private javax.swing.JButton manageProfAddCourseButton;
    private javax.swing.JButton manageProfBackButton;
    private javax.swing.JComboBox manageProfCourseComboBox;
    private javax.swing.JLabel manageProfCourseLabel;
    private javax.swing.JPanel manageProfCoursePanel;
    private javax.swing.JButton manageProfImportStudentButton;
    private javax.swing.JPanel manageProfPanel;
    private javax.swing.JButton manageProfRemoveStudentButton;
    private javax.swing.JLabel manageProfStudentLabel;
    private javax.swing.JPanel manageProfStudentPanel;
    private javax.swing.JLabel monthLabel;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JLabel passLabel;
    private javax.swing.JLabel passLabel1;
    private javax.swing.JPasswordField passPasswordField;
    private javax.swing.JTable profManageStudentTable;
    private javax.swing.JTable profManageTaskTable;
    private javax.swing.JLabel secondLabel;
    private javax.swing.JButton suggestButton;
    private org.jdesktop.swingx.JXDatePicker taskDatePicker;
    private javax.swing.JTextField taskDescTextField;
    private javax.swing.JTabbedPane taskProfTabbedPane;
    private javax.swing.JLabel thirdLabel;
    private javax.swing.JLabel thirdLabel2;
    private javax.swing.JLabel titleLabel1;
    private javax.swing.JComboBox typeTaskComboBox;
    private javax.swing.JLabel userLabel;
    private javax.swing.JLabel userLabel1;
    private javax.swing.JTextField userTextField;
    private javax.swing.JLabel yearLabel;
    // End of variables declaration//GEN-END:variables
}
