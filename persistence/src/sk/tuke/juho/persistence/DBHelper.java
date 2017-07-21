package sk.tuke.juho.persistence;

import sk.tuke.juho.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juraj on 14.3.2016.
 */
class DBHelper {
    private static Connection connection;
    private PersistenceManager pm;
    private static DBHelper helper;

    private DBHelper(Connection connection, PersistenceManager pm) {
        DBHelper.connection = connection;
        this.pm = pm;
    }

    static DBHelper getDBHelper(Connection connection, PersistenceManager pm) {
        if (helper == null)
            helper = new DBHelper(connection, pm);
        return helper;
    }

    void makeTables() {
        ArrayList<String> queries = null;
        try {
            queries = (ArrayList<String>) ClassLoader.getSystemClassLoader()
                    .loadClass("sk.tuke.juho.generated.TableMaker")
                    .getMethod("getQueriesList", new Class[0]).invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (queries != null) {
            for (String query : queries) {
                executeQuery(query);
            }
        }
    }

    List getResultsList(String query, Class type) {
        List list = new ArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                list.add(setAttributesOfObject(rs, type));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            assert rs != null;
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Object setAttributesOfObject(ResultSet rs, Class clas) {
        Object obj = null;
        try {
            obj = clas.newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                Class type = field.getType();
                field.setAccessible(true);
                if (type.equals(String.class)) {
                    field.set(obj, rs.getString(getNameOfColumn(field)));
                } else if (type.equals(Integer.TYPE)) {
                    field.set(obj, rs.getInt(getNameOfColumn(field)));
                } else if (type.equals(Boolean.TYPE)) {
                    field.set(obj, rs.getBoolean(getNameOfColumn(field)));
                } else if (type.equals(Long.TYPE)) {
                    field.set(obj, rs.getLong(getNameOfColumn(field)));
                } else if (type.equals(Short.TYPE)) {
                    field.set(obj, rs.getShort(getNameOfColumn(field)));
                } else if (type.equals(Float.TYPE)) {
                    field.set(obj, rs.getFloat(getNameOfColumn(field)));
                } else if (field.isAnnotationPresent(LazyFetch.class)) {
                    Class targetClass = Class.forName(field.getDeclaredAnnotation(LazyFetch.class).targetEntity());
                    Integer idField = rs.getInt(getNameOfColumn(field) + "_id");
                    if (idField > 0)
                        field.set(obj, ProxyHelper.createProxy(targetClass, idField, pm, field, obj));
                } else {
                    Integer idField = rs.getInt(getNameOfColumn(field) + "_id");
                    if (idField > 0)
                        field.set(obj, pm.get(field.getType(), idField));
                }
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }


    private static void executeQuery(String query) {
        System.out.println(query);
        try {
            Statement stmt;
            stmt = connection.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void updateObject(Object obj) {
        Class clas = obj.getClass();
        String values = "";
        int id = 0;
        try {
            Field idF = clas.getDeclaredField("id");
            idF.setAccessible(true);
            id = (Integer) idF.get(obj);
            for (Field f : clas.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType().isPrimitive() || f.getType().equals(String.class)) {
                    if (!f.isAnnotationPresent(Id.class)) {
                        try {
                            values = values + getNameOfColumn(f) + "='" + f.get(obj).toString() + "', ";
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Object innerObject = f.get(obj);
                    if (innerObject == null) {
                        values = values + getNameOfColumn(f) + "_id=null, ";
                    } else if (!innerObject.getClass().getSimpleName().startsWith("$Proxy")) {
                        try {
                            if(f.isAnnotationPresent(LazyFetch.class))
                                innerObject = Class.forName(f.getDeclaredAnnotation(LazyFetch.class).targetEntity()).cast(innerObject);
                            else
                                innerObject = f.getType().cast(innerObject);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        pm.save(innerObject);
                        values = values + getNameOfColumn(f) + "_id=";
                        int fieldId = pm.getIdOfObject(innerObject);
                        if (fieldId > 0)
                            values = values + fieldId + ", ";
                        else {
                            values = values + "null, ";
                        }
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        values = values.substring(0, values.length() - 2);
        String query = "UPDATE " + getNameOfTable(clas) + " SET " + values + " WHERE " + getIdNameOfClass(clas) + "=" + id;

        executeQuery(query);
    }

    int insertObject(Object obj) {
        Class clas = obj.getClass();
        String fields = "", values = "";
        Field idField = null;
        for (Field f : clas.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType().isPrimitive() || f.getType().equals(String.class)) {
                if (!f.isAnnotationPresent(Id.class)) {
                    fields = fields + getNameOfColumn(f) + ", ";
                    try {
                        values = values + "'" + f.get(obj).toString() + "'" + ", ";
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    idField = f;
                }
            } else {
                try {
                    Object innerObject = f.get(obj);

                    if (innerObject != null) {
                        if (f.isAnnotationPresent(LazyFetch.class)) {
                            innerObject = Class.forName(f.getAnnotation(LazyFetch.class).targetEntity()).cast(innerObject);
                        } else
                            innerObject = f.getType().cast(innerObject);
                        fields = fields + getNameOfColumn(f) + "_id" + ", ";
                        if (pm.objectExist(innerObject)) {
                            updateObject(innerObject);
                            values = values + pm.getIdOfObject(innerObject) + ", ";
                        } else {
                            values = values + insertObject(innerObject) + ", ";
                        }
                    }
                } catch (IllegalAccessException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        fields = fields.substring(0, fields.length() - 2);
        values = values.substring(0, values.length() - 2);
        String query = "INSERT into " + getNameOfTable(clas) + " ( " +
                fields + " ) VALUES( " +
                values + " ) RETURNING " + getNameOfColumn(idField) + ";";
        int objectId = -1;
        try {
            Statement stmt;
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next())
                objectId = rs.getInt(getNameOfColumn(idField));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (objectId != -1) {
            try {
                for (Field f : clas.getDeclaredFields()) {
                    if (f.isAnnotationPresent(Id.class)) {
                        f.setAccessible(true);
                        f.set(obj, objectId);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return objectId;
    }

    private String getNameOfColumn(Field field) {
        String fieldName = "";
        if (field.isAnnotationPresent(Column.class)) {
            fieldName = field.getAnnotation(Column.class).name();
        }
        if (field.isAnnotationPresent(Id.class)) {
            fieldName = field.getAnnotation(Id.class).name();
        }
        if (fieldName.equals("")) {
            fieldName = field.getName();
        }
        return fieldName;
    }

    String getIdNameOfClass(Class c) {
        for (Field f : c.getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                if (f.getAnnotation(Id.class).name().equals(""))
                    return f.getName();
                return f.getAnnotation(Id.class).name();
            }
        }
        return "";
    }

    String getNameOfTable(Class aClass) {
        if (aClass.isAnnotationPresent(Entity.class)) {
            Annotation annotation = aClass.getDeclaredAnnotation(Entity.class);
            Class<? extends Annotation> type = annotation.annotationType();
            try {
                Object value = type.getDeclaredMethod("name").invoke(annotation, (Object[]) null);
                return (String) value;
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return aClass.getSimpleName();
    }
}
