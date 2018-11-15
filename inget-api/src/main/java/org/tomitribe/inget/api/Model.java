/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.tomitribe.inget.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.tomitribe.inget.api.Model.Operation.CREATE;
import static org.tomitribe.inget.api.Model.Operation.DELETE;
import static org.tomitribe.inget.api.Model.Operation.READ;
import static org.tomitribe.inget.api.Model.Operation.READ_ALL;
import static org.tomitribe.inget.api.Model.Operation.UPDATE;

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
