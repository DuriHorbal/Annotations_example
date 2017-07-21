package sk.tuke.juho.persistence;

import sk.tuke.juho.annotations.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

/**
 * Created by Juraj on 23.2.2016.
 */
public class PersistenceManager {


    private List<Class> clazzes;
    private DBHelper dbHelper;

    public PersistenceManager(Connection con) {
        dbHelper = DBHelper.getDBHelper(con, this);
    }

    public void initializeDatabase() {
        dbHelper.makeTables();
    }


    //z�skanie zoznamu v�etk�ch objektov zadan�ho typu.
    public List getAll(Class type) {
        String query = "SELECT * FROM " + dbHelper.getNameOfTable(type);
        return dbHelper.getResultsList(query, type);
    }

    // z�skanie konkr�tneho objektu na z�klade jeho identifik�tora.
    public <T> T get(Class<T> type, int id) {
        String query = "SELECT * FROM " + dbHelper.getNameOfTable(type) +
                " WHERE " + dbHelper.getIdNameOfClass(type) + "=" + id + " ";
        return (T) dbHelper.getResultsList(query, type).get(0);
    }


    //z�skanie objektov pod�a hodnoty �ubovo�n�ho atrib�tu.
    //<T> T get(Class<T> type, int id);
    public <T> List<T> getBy(Class<T> type, String field, Object value) {
        String val = null;
        if (value instanceof Integer || value instanceof Float || value instanceof Short) {
            val = value.toString();
        } else {
            val = "'" + value.toString() + "'";
        }
        String query = "SELECT * FROM " + dbHelper.getNameOfTable(type) +
                " WHERE " +
                field + "=" + val;
        return (List<T>) dbHelper.getResultsList(query, type);
    }

    //ulo�enie objektu do datab�zy, alebo jeho aktualiz�cia v pr�pade, �e sa v datab�ze u� nach�dza.
    public void save(Object obj) {
        if (objectExist(obj)) {
            dbHelper.updateObject(obj);
        } else {
            dbHelper.insertObject(obj);
        }
    }

    boolean objectExist(Object obj) {
        return getIdOfObject(obj) > 0;
    }

    int getIdOfObject(Object obj) {
        for (Field f : obj.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                try {
                    f.setAccessible(true);
                    return ((int) f.get(obj));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }
}
