package sk.tuke.juho.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Juraj on 1.4.2016.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Column {
    String name() default "";
    boolean isNotNull() default false;
    int length() default 255;
}
