package org.apache.commons.collections4;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Nested
@ExtendWith(NestedEnabledIfOuterCondition.class)
public @interface NestedEnabledIfOuter {
    /**
     * The name of a method within the nested test class's enclosing class.
     */
    String value();
}
