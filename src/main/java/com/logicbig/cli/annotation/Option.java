/*
 * Copyright 2015 LogicBig.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.logicbig.cli.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method parameter annotated with Option defines an option argument. The method must be annotated with
 * Command annotation. Option is specified with double hyphen. e.g. --myoption=&lt;option-value&gt;
 * Created by Joe on 11/14/2015.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Option {
    /**
     * The name of the option
     *
     * @return
     */
    String name();

    /**
     * The description of the option.
     *
     * @return
     */
    String desc();

    /**
     * Values allowed for this option.
     *
     * @return
     */
    String[] valuesAllowed() default {};

    /**
     * Specifies if this option is mandatory. If yes user must have to enter the value for it.
     *
     * @return
     */
    boolean mandatory() default false;
}
