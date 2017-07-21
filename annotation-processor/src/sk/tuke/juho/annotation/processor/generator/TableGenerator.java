package sk.tuke.juho.annotation.processor.generator;

import sk.tuke.juho.annotation.processor.TableGenProcessor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Juraj on 2.4.2016.
 */
public interface TableGenerator {

    boolean applicable(VariableElement field);

    String sqlString(VariableElement field, HashMap<String, ArrayList<String>> tablesFK, ProcessingEnvironment processingEnv, TableGenProcessor tgp);
}
