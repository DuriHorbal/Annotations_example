package testClasses;

import sk.tuke.juho.annotations.*;

/**
 * Created by Juraj on 23.2.2016.
 */
@Entity(name="Pracovisko")
public class Department implements IDepartment {
    @Id(name = "idDepartmentu")
    private int id;

    @Column(name = "meno",isNotNull = true, length = 50)
    private String name;

    @Column(name = "kod", length = 20)
    private String code;

    @Column(name = "sef")
    @LazyFetch(targetEntity = "testClasses.Employee")
    private IEmployee boss;

    public Department(){
    }

    public Department(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String toString(){
        return "\n****************" +
                "*Department: "+name +
                "\n*Code: "+code +
                "\n*id: "+id +
                "\n****************";
    }

    public void setBoss(Employee boss) {
        this.boss = boss;
    }

    public IEmployee getBoss() {
        return boss;
    }
}
