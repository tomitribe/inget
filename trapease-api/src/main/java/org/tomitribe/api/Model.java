package org.tomitribe.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.tomitribe.api.Model.Operation.CREATE;
import static org.tomitribe.api.Model.Operation.DELETE;
import static org.tomitribe.api.Model.Operation.READ;
import static org.tomitribe.api.Model.Operation.READ_ALL;
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
            READ_ALL
    };

    enum Operation {
        READ, CREATE, UPDATE, DELETE, READ_ALL, BULK_CREATE, BULK_UPDATE, BULK_DELETE
    }

    Filter filter() default @Filter;

    boolean summary() default false;
}
