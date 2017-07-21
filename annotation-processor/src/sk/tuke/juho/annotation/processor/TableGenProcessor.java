package sk.tuke.juho.annotation.processor;

import sk.tuke.juho.annotation.processor.generator.ColumnTableGenerator;
import sk.tuke.juho.annotation.processor.generator.IdColumnTableGenerator;
import sk.tuke.juho.annotation.processor.generator.TableGenerator;
import sk.tuke.juho.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Created by Duri on 2.4.2016.
 */

@SupportedAnnotationTypes("sk.tuke.juho.annotations.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TableGenProcessor extends AbstractProcessor {

    private List<TableGenerator> generators = new LinkedList<>();
    private Set<? extends Element> elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        generators.add(new IdColumnTableGenerator());
        generators.add(new ColumnTableGenerator());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        elements = roundEnv.getElementsAnnotatedWith(Entity.class);
        String stringOfAll = "";
        String alterTables = "";
        for (Element entity : elements) {
            String tableName = getNameOfColumn(entity, entity.getAnnotation(Entity.class).name());

            TypeElement typeElement = (TypeElement) entity;
            String alterTable = "\t\tString alter" + tableName + " =\"ALTER TABLE " + tableName + " ";
            stringOfAll += "\t\tString table" + tableName + "=\"" +
                    "CREATE TABLE " + tableName + " ( ";

            HashMap<String, ArrayList<String>> alterQueries = new HashMap<>();
            boolean isFirstField = true;
            for (Element element : typeElement.getEnclosedElements()) {
                if (element.getKind() == ElementKind.FIELD) {
                    VariableElement field = (VariableElement) element;
                    for (TableGenerator tableGenerator : generators) {
                        if (tableGenerator.applicable(field)) {
                            if (isFirstField) {
                                isFirstField = false;
                            } else {
                                stringOfAll += ", ";
                            }
                            stringOfAll += tableGenerator.sqlString(field, alterQueries, processingEnv, this);
                        }
                    }
                }
            }
            for (String key : alterQueries.keySet()) {
                if (key.equals("FK")) {
                    ArrayList<String> fks = alterQueries.get(key);
                    isFirstField = true;
                    for (String fk : fks) {
                        if (isFirstField) {
                            isFirstField = false;
                        } else {
                            alterTable += ", ";
                        }
                        alterTable += fk;
                    }
                    alterTable += "\";\n" +
                            "\t\tqueries.add(alter" + tableName + ");\n";
                } else if (key.equals("SEQ")) {
                    String sequence = alterQueries.get(key).get(0).replace("%s", tableName);
                    stringOfAll = createSequention(tableName + "_seq", sequence) + stringOfAll;
                }
            }
            stringOfAll = stringOfAll.replace("%s", tableName);
            stringOfAll += ");\";\n" +
                    "\t\tqueries.add(table" + tableName + ");\n";
            alterTables += alterTable;
        }
        stringOfAll += alterTables;
        try {
            JavaFileObject jfo = processingEnv.getFiler()
                    .createSourceFile("sk.tuke.juho.generated.TableMaker");
            try (
                    Writer writer = jfo.openWriter();
            ) {
                writer.write(header());
                writer.write(stringOfAll);
                writer.write(footer());
            }
        } catch (IOException e) {
//            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "---" + e.getMessage());
        }
        return true;
    }

    private String header() {
        ArrayList<String> a = new ArrayList<>();
        return "package sk.tuke.juho.generated;\n\n" +
                "import java.util.*;\n\n" +
                "public class TableMaker {\n\n" +
                "\tpublic static ArrayList<String> getQueriesList() {\n" +
                "\t\tArrayList<String> queries = new ArrayList<>();\n\n";
    }

    private String createSequention(String name, String query) {
        return "\t\tString " + name + " =\"" + query + "\";\n" +
                "\t\tqueries.add(" + name + ");\n";
    }


    private String footer() {
        return "\n\t\treturn queries;\n" +
                "\t}\n" +
                "}";
    }

    /**
     * @param column             premenna triedy
     * @param nameFromAnnotation nazov premennej ziskany z anotacie
     * @return
     */
    public static String getNameOfColumn(Element column, String nameFromAnnotation) {
        if (nameFromAnnotation.equals("")) {
            nameFromAnnotation = column.getSimpleName().toString();
        }
        return nameFromAnnotation;
    }

    /**
     * @param claz trieda, z ktorej chceme ziskat nazov jej ID
     * @return nazov ID pre stlpec tabulky
     */
    public String getNameOfIdForClass(String claz) {
        for (Element element : elements) {
            if (element.getSimpleName().toString().equals(claz)) {
                for (Element fieldElement : element.getEnclosedElements()) {
                    if (fieldElement.getKind() == ElementKind.FIELD) {
                        VariableElement field = (VariableElement) fieldElement;
                        if (field.getAnnotation(Id.class) != null) {
                            return getNameOfColumn(field, field.getAnnotation(Id.class).name());
                        }
                    }
                }
            }
        }
        return claz;
    }

    public String getNameOfClass(String claz) {
        for (Element element : elements) {
            if (element.getSimpleName().toString().equals(claz)) {
                return getNameOfColumn(element, element.getAnnotation(Entity.class).name());
            }
        }
        return claz;
    }

}
