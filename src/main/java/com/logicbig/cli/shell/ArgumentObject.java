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
 * This holds Argument information specified annotation @Argument
 *
 * @author Joe Khan.
 */
class ArgumentObject extends Describable {
    private final boolean mandatory;
    private final Class<?> type;

    public ArgumentObject(String name, String description, boolean mandatory, Class<?> type) {
        super(name, description);
        this.mandatory = mandatory;
        this.type = type;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public Class<?> getType() {
        return type;
    }
}
