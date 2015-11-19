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

import java.util.Arrays;
import java.util.List;

/**
 * This class represents option for a command
 *
 * @author Joe Khan.
 */
class OptionObject extends Describable {

    private final boolean mandatory;
    private final String name;
    private final String description;
    private final Class<?> type;
    private final List<String> validValues;

    /**
     * @param name        name of the OptionObject
     * @param description the description used in help
     * @param mandatory   if the option is mandatory
     * @param type
     * @param validValues the valid values, the first value is treated as default value if it's not mandatory
     */
    protected OptionObject(String name, String description, boolean mandatory, Class<?> type, String[] validValues) {
        super(name, description);
        this.name = name;
        this.description = description;
        this.type = type;
        this.validValues = validValues != null ? Arrays.asList(validValues) : null;
        if (!name.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("The name of an option should only be sequence of alphabets: " + this);
        }
        this.mandatory = mandatory;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public List<String> getValidValues() {
        return validValues;
    }

    public Class<?> getType() {
        return type;
    }
}
