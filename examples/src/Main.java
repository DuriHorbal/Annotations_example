
import sk.tuke.juho.persistence.*;
import testClasses.Department;
import testClasses.Employee;
import testClasses.IDepartment;
import testClasses.IEmployee;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Juraj on 23.2.2016.
 */
public class Main {

    public static void main(String[] args) throws URISyntaxException {
        Connection c= makeConnection();
        PersistenceManager pm = new PersistenceManager(c);
        pm.initializeDatabase();

        Employee emp0 = new Employee("Adam");
        emp0.empAge(30);
        emp0.empSalary(1000);
        pm.save(emp0);

        Employee bosss = new Employee("Sefko");
        bosss.empAge(32);
        bosss.empSalary(1200);

        Department d1 = new Department("PracoviskoA", "PA");
        d1.setBoss(bosss);

        Employee emp1 = new Employee("Jan");
        emp1.empAge(125);
        emp1.empSalary(385);
        emp1.setDepartment(d1);

        pm.save(emp1);

        Employee worker = pm.getBy(Employee.class, "meno", "Jan").get(0);
        System.out.println(worker.getDepartment().toString());
        IDepartment department = worker.getDepartment();
        System.out.println(department.getBoss().toString());
        IEmployee boss = department.getBoss();
        boss.empSalary(3000);
//        department.setBoss(null);

        pm.save(worker);
        System.out.println();

        for(Object o : pm.getAll(Employee.class)){
            System.out.println(o.toString());
        }
        try {
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection makeConnection() {
        Connection connection = null;
        try {

            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/postgres", "postgres",
                    "0000");

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }

        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
        return connection;
    }
}
