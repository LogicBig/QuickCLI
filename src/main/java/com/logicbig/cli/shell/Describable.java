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

package com.logicbig.cli.shell;

/**
 * Abstract class with basic info about a command
 *
 * @author Joe Khan.
 */
abstract class Describable {
    private final String name;
    private final String description;

    protected Describable(String name, String description) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Name cannot be empty " + name);
        }
        this.name = name;
        this.description = description;
    }

     String getName() {
        return name;
    }

     String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
