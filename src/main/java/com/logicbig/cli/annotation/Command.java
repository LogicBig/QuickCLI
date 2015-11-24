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

import java.lang.annotation.*;

/**
 * Method annotated with Command gets called when the associated command is fired from the command line. This
 * annotation along with argument type annotations(which are annotated on method parameters) defines a single command.
 * Created by Joe on 11/14/2015.
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /**
     * The name of the command.
     *
     * @return
     */
    String name();

    /**
     * The description of the command which is displayed as help.
     *
     * @return
     */
    String desc();
}
