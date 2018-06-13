package org.tomitribe.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.tomitribe.api.Model.Operation.CREATE;
import static org.tomitribe.api.Model.Operation.DELETE;
import static org.tomitribe.api.Model.Operation.LIST;
import static org.tomitribe.api.Model.Operation.READ;
import static org.tomitribe.api.Model.Operation.UPDATE;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {
    boolean id() default false;

    Operation[] operation() default {
            READ,
            CREATE,
            UPDATE,
            DELETE,
            LIST
    };

    String[] commands() default {};

    enum Operation {
        READ, CREATE, UPDATE, DELETE, LIST
    }

    Filter filter() default @Filter;

    boolean summary() default false;
}
