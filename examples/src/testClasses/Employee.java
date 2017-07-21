package testClasses;

import sk.tuke.juho.annotations.*;

/**
 * Created by Juraj on 23.2.2016.
 */
@Entity(name="zamestnanec")
public class Employee implements IEmployee{
    @Id(name = "id_zamestnanca")
    private int id;

    @Column(name = "meno", isNotNull = true, length = 50)
    private String name;

    @Column(name = "vek")
    private int age;

    @Column(name = "plat", isNotNull = true)
    private float salary;

    @Column(name = "pracovisko")
//    @LazyFetch(targetEntity = "testClasses.Department")
    private Department department;

    public Employee() {
    }

    // This is the constructor of the class Employee
    public Employee(String name){
        this.name = name;
    }


    // Assign the age of the Employee  to the variable age.
    public void empAge(int empAge){
        age =  empAge;
    }

    public IDepartment getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    /* Assign the salary to the variable	salary.*/
    public void empSalary(float empSalary){
        salary = empSalary;
    }

    public String toString(){
        return  "\n****************" +
                "\n*Name: "+ name +
                "\n*Age: " + age +
                "\n*Salary: " + salary +
                "\n****************";
    }
}