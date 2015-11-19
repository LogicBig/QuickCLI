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
 * See the License for the specific language governing permissions and
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

package com.logicbig.cli.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class represents a single command with multiple optionObjects.
 *
 * @author Joe Khan.
 */
class CommandObject extends Describable {
    private final List<OptionObject> optionObjects = new ArrayList<>();
    private final List<OptionFlagObject> optionFlagObjects = new ArrayList<>();
    private final List<ArgumentObject> argumentObjects = new ArrayList<>();
    private Function<Map<String, String>, String> commandFunction;
    private CommandMethodInfo commandMethodInfo;

    CommandObject(String commandName, String description) {
        super(commandName, description);
    }

    CommandObject addOption(String optionName, String description, boolean mandatory, Class<?> type, String... validValues) {
        optionObjects.add(new OptionObject(optionName, description, mandatory, type, validValues));
        return this;
    }

    public CommandObject addOptionFlag(char flagName, String description) {
        optionFlagObjects.add(new OptionFlagObject(flagName, description));
        return this;
    }

    public CommandObject addArgument(String argumentName, String description) {
        argumentObjects.add(new ArgumentObject(argumentName, description, false, null));
        return this;
    }

    public CommandObject addArgument(String argumentName, String description, boolean mandatory, Class<?> type) {
        argumentObjects.add(new ArgumentObject(argumentName, description, mandatory, type));
        return this;
    }


    public List<OptionObject> getOptionObjects() {
        return optionObjects;
    }

    public void addCommandHandler(Function<Map<String, String>, String> commandFunction) {
        this.commandFunction = commandFunction;
    }


    public Function<Map<String, String>, String> getCommandFunction() {
        return commandFunction;
    }

    public List<OptionObject> getMandatoryOptions() {
        return optionObjects.stream().filter(opt -> opt.isMandatory()).collect(Collectors.toList());
    }

    public List<ArgumentObject> getMandatoryArguments() {
        return argumentObjects.stream().filter(arg -> arg.isMandatory()).collect(Collectors.toList());
    }

    public List<ArgumentObject> getArgumentObjects() {
        return argumentObjects;
    }

    public List<OptionFlagObject> getOptionFlagObjects() {
        return optionFlagObjects;
    }

    public void setCommandFunction(Function<Map<String, String>, String> commandFunction) {
        this.commandFunction = commandFunction;
    }

    public CommandMethodInfo getCommandMethodInfo() {
        return commandMethodInfo;
    }

    public void setCommandMethodInfo(CommandMethodInfo commandMethodInfo) {
        this.commandMethodInfo = commandMethodInfo;
    }
}
