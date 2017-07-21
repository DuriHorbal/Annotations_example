package sk.tuke.juho.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Juraj on 1.4.2016.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface Entity {
    String name() default "";
}
