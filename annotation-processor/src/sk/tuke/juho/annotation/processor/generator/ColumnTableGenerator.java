package sk.tuke.juho.annotation.processor.generator;

import sk.tuke.juho.annotation.processor.TableGenProcessor;
import sk.tuke.juho.annotations.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juraj on 2.4.2016.
 */
public class ColumnTableGenerator implements TableGenerator {

    @Override
    public boolean applicable(VariableElement field) {
        return field.getAnnotation(Column.class) != null;
    }

    @Override
    public String sqlString(VariableElement field, HashMap<String, ArrayList<String>> tablesFK, ProcessingEnvironment processingEnv, TableGenProcessor tgp) {
        Column column = field.getAnnotation(Column.class);
        String columnName = TableGenProcessor.getNameOfColumn(field, column.name());

        StringBuilder builder = new StringBuilder();
        builder.append(columnName);

        if (Integer.class.getName().equals(field.asType().toString()) || "int".equals(field.asType().toString()))
            builder.append(" integer");
        else if (Boolean.class.getName().equals(field.asType().toString()) || "boolean".equals(field.asType().toString()))
            builder.append(" boolean");
        else if (Long.class.getName().equals(field.asType().toString()) || "long".equals(field.asType().toString()))
            builder.append(" long");
        else if (Short.class.getName().equals(field.asType().toString()) || "short".equals(field.asType().toString()))
            builder.append(" short");
        else if (Float.class.getName().equals(field.asType().toString()) || "float".equals(field.asType().toString()))
            builder.append(" float");
        else if (String.class.getName().equals(field.asType().toString())) {
            builder.append(" varchar(").append(column.length()).append(")");
        } else {
            String query = " ADD FOREIGN KEY (" + columnName
                    + "_id) REFERENCES " +
                    tgp.getNameOfClass(getSimpleClassName(field)) + " (" + tgp.getNameOfIdForClass(getSimpleClassName(field)) + ")";
            if (tablesFK.containsKey("FK")) {
                tablesFK.get("FK").add(query);
            } else {
                ArrayList<String> list = new ArrayList<>();
                list.add(query);
                tablesFK.put("FK", list);
            }
            builder.append("_id integer");
        }
        if (column.isNotNull()) {
            builder.append(" NOT NULL");
        }
        return builder.toString();
    }


    /**
     * @param field premenna ktorej triedu chceme ziskat
     * @return vrati nazov triedy
     */
    private String getSimpleClassName(VariableElement field) {
        String fullClassName = field.asType().toString();
        if (field.getAnnotation(LazyFetch.class) != null) {
            String target = field.getAnnotation(LazyFetch.class).targetEntity();
            if (!target.equals(""))
                fullClassName = target;
        }
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1, fullClassName.length());
    }

}
