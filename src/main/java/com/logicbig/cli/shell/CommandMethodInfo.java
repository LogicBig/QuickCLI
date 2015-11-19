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

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds information about the method annotated with @Command
 *
 * @author Joe Khan.
 */
class CommandMethodInfo {
    private Method commandMethod;
    private Object handlerInstance;
    private Map<String, Class<?>> argumentFieldTypeMap = new LinkedHashMap<>();

    public CommandMethodInfo(Method commandMethod, Object handlerInstance, Map<String, Class<?>> argumentFieldTypeMap) {
        this.commandMethod = commandMethod;
        this.handlerInstance = handlerInstance;
        this.argumentFieldTypeMap = argumentFieldTypeMap;
    }

    public Method getCommandMethod() {
        return commandMethod;
    }

    public Object getHandlerInstance() {
        return handlerInstance;
    }

    public Map<String, Class<?>> getArgumentFieldTypeMap() {
        return argumentFieldTypeMap;
    }
}
