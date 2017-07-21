package sk.tuke.juho.annotation.processor.generator;

import sk.tuke.juho.annotation.processor.TableGenProcessor;
import sk.tuke.juho.annotations.Id;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juraj on 2.4.2016.
 */
public class IdColumnTableGenerator implements TableGenerator {
    @Override
    public boolean applicable(VariableElement field) {
        return field.getAnnotation(Id.class) != null;
    }

    @Override
    public String sqlString(VariableElement field, HashMap<String, ArrayList<String>> tablesFK, ProcessingEnvironment processingEnv, TableGenProcessor tgp) {
        Id idTable = field.getAnnotation(Id.class);
        String columnName = idTable.name();
        if (columnName.equals("")) {
            columnName = field.getSimpleName().toString();
        }

        ArrayList<String> list = new ArrayList<>();
        list.add("CREATE SEQUENCE %s_seq START 1;");
        tablesFK.put("SEQ", list);
        return columnName + " integer PRIMARY KEY DEFAULT nextval('%s_seq')";
    }


}
