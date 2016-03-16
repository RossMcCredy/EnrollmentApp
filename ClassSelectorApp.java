
import java.util.*;
import java.sql.*;

public class ClassSelectorApp {

    static Scanner sc = new Scanner(System.in);
    static Connection con;
    static PreparedStatement myStmt;
    static ResultSet rs;
    static String student_id;
    static String student_name;
    static String hometown;
    static String userEnterIdAsName;
    static String user_entered_student_id;

    public static void main(String[] args) throws SQLException {

        while (true) {
            switch (menu()) {
                case 0:
                    return;
                case 1:
                    createStudent();
                    break;
                case 2:
                    signUp();
                    break;
                case 3:
                    listClasses();
                    break;
                default:
                    System.out.println("Invalid Input");
            }
        }
    }

    public static int menu() {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("\n Class Selection Menu");
            System.out.println("**********************************");
            System.out.println("0: Exit Menu");
            System.out.println("1: Create New Student");
            System.out.println("2: Sign Up For a Class");
            System.out.println("3: List Classes for All Students");
            System.out.println("**********************************");
            System.out.println("Enter a choice: ");
            return sc.nextInt();

        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid choice!");
        } catch (Exception e) {
            System.out.println("Something went wrong...");
        }
        return 0;
    }
    public static void getStudentInfo(){
        try{
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ClassSelector?autoReconnect=true&useSSL=false", "root", "Volks91!");
            System.out.println("Enter a Student ID: ");
            student_id = sc.nextLine();
            System.out.println("Enter Student Name: ");
            student_name = sc.nextLine();
            System.out.println("Enter Student Hometown: ");
            hometown = sc.nextLine();
        }
        catch (SQLException SQL) {
            SQL.printStackTrace();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    public static void createStudent() {
        System.out.println("\nCreate Student\n");
        try {

            getStudentInfo();
            String insertStudentValuesQuery = "INSERT INTO students" + "(student_id, student_name, hometown)" + "VALUES (?, ?, ?)";
            myStmt = con.prepareStatement(insertStudentValuesQuery);
            myStmt.setString(1, student_id);
            myStmt.setString(2, student_name);
            myStmt.setString(3, hometown);
            myStmt.executeUpdate();
            System.out.println("New Student Added");

        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("You have entered duplicate data, please try again");
        } catch (java.util.InputMismatchException mm) {
            mm.printStackTrace();
        }catch (SQLException SQL) {
            System.out.println("You have entered an incorrect value type");
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public static void signUp() {
        System.out.println("\nSign Up For a Class\n");
        try {

            System.out.println("Enter Student ID: ");
            user_entered_student_id = sc.nextLine();

            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ClassSelector?autoReconnect=true&useSSL=false", "root", "Volks91!");

            String selectStudentFromIdQuery = ("SELECT student_name FROM ClassSelector.students WHERE student_id = " + user_entered_student_id);
            myStmt = con.prepareStatement(selectStudentFromIdQuery);

            rs = myStmt.executeQuery(selectStudentFromIdQuery);
            while (rs.next()) {
                userEnterIdAsName = rs.getString("student_name");
                System.out.println("Is " + userEnterIdAsName + " the correct student? (Y/N)");
                String confirm = sc.nextLine();

                if (confirm.equalsIgnoreCase("Y")) {
                    additionalClass();

                } else if (confirm.equalsIgnoreCase("N")) {
                    System.out.println("Oops, let start over");
                    return;
                }
            }
        } catch (java.sql.SQLException SQL) {
            SQL.printStackTrace();
        } catch (Exception EXC) {
            EXC.printStackTrace();
        }

    }

    public static void additionalClass() {

        try {
            rs = myStmt.executeQuery("SELECT * FROM ClassSelector.classes");
            while (rs.next()) {
                String availableClasses = rs.getString("class_id") + "\t" + rs.getString("class_name") + "\t" + rs.getString("description");
                System.out.println(availableClasses);
            }

            System.out.println("Enter Class ID from Classes Listed Above to Join: ");
            String selectedClass = sc.nextLine();
            rs = myStmt.executeQuery("SELECT * FROM ClassSelector.student_x_class" +
                    "JOIN classes on ClassSelector.student_x_class.class_id = ClassSelector.classes.class_id" +
                    "JOIN students on ClassSelector.student_x_class.student_id = ClassSelector.students.student_id" +
                    "Where ClassSelector.student_x_class.student_id = " + selectedClass);
            while (rs.next()) {
                String innerJoin = (userEnterIdAsName + " has been added to " + rs.getString("class_name") + " " + rs.getString("class_id"));
                System.out.println(innerJoin);
                String student_x_classJoin = "INSERT IGNORE INTO student_x_class" + "(student_id,student_name, class_id, class_name)" + "VALUES (?, ?, ?, ?)";
                PreparedStatement pStmt = con.prepareStatement(student_x_classJoin);
                pStmt.setString(1, user_entered_student_id);
                pStmt.setString(2, userEnterIdAsName);
                pStmt.setString(3, rs.getString("class_id"));
                pStmt.setString(4, rs.getString("class_name"));
                pStmt.executeUpdate();
                System.out.println("Would you like to enroll " + userEnterIdAsName + " into another class? (Y/N)");
                String addAdditionalClass = sc.nextLine();
                if (addAdditionalClass.equalsIgnoreCase("Y")) {
                    additionalClass();
                } else if (addAdditionalClass.equalsIgnoreCase("N")) {
                    return;
                }
            }
        } catch (java.sql.SQLException SQL) {
            SQL.printStackTrace();
        } catch (Exception EXC) {
            EXC.printStackTrace();
        }
    }

    public static void listClasses() {
        System.out.println("\nStudent Enrollment\n");
        try {
            System.out.println("Enter Student ID to See What Classes they are enrolled in: ");
            user_entered_student_id = sc.nextLine();

            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ClassSelector?autoReconnect=true&useSSL=false", "root", "Volks91!");

            boolean found = false;
            String listStudentEnrollmentInfo =
                    "SELECT ClassSelector.student_x_class.student_id, " +
                            "ClassSelector.students.student_name, " +
                            "ClassSelector.students.hometown, " +
                            "ClassSelector.classes.class_name, " +
                            "ClassSelector.classes.description " +
                            "FROM ClassSelector.student_x_class " +
                            "JOIN classes on ClassSelector.student_x_class.class_id = ClassSelector.classes.class_id " +
                            "JOIN students on ClassSelector.student_x_class.student_id = ClassSelector.students.student_id " +
                            "Where ClassSelector.student_x_class.student_id =" + user_entered_student_id;
            rs = myStmt.executeQuery(listStudentEnrollmentInfo);
            while (rs.next()) {
                String studentInClass = (rs.getString("student_id") + "\t" + rs.getString("student_name") + " \t " + rs.getString("class_id") + " \t" + rs.getString("class_name"));
                if (user_entered_student_id.equals(rs.getString("student_id"))) {
                    System.out.println(studentInClass);
                    found = true;
                }
            }
            if (!found) {
                System.out.println("This Student does not Exist!");
            }

        } catch (java.sql.SQLException SQL) {
            SQL.printStackTrace();
        } catch (Exception EXC) {
            EXC.printStackTrace();
        }
    }
}
