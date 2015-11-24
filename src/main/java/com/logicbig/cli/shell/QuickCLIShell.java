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


import com.logicbig.cli.annotation.Argument;
import com.logicbig.cli.annotation.Command;
import com.logicbig.cli.annotation.Option;
import com.logicbig.cli.annotation.OptionFlag;
import com.logicbig.cli.text.table.TextTable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An easy to use Command Line Interface for Shell applications. The commands are based on linux standard syntax.
 * <pre>CommandName [Options]... [Option-Flags]... [Arguments]... </pre>
 * where <ol>
 * <li>Options are key value pairs e.g. --opt1=theOptValue</li>
 * <li>Options-Flags are boolean flags e.g. -f -t -c etc. They can be combined together as -ftc</li>
 * <li>Arguments are input to the command e.g. consider linux command "<code>ls -l fileName </code>", here fileName is the argument to the ls command </li>
 * </ol>
 * <br/>
 * Please see the examples <a href="http://www.logicbig.com/projects/QuickCLI.html">here</a>
 *
 * @author Joe Khan.
 */
public class QuickCLIShell extends Describable {
    private static final int MAX_CMD_LEN = 10;
    private static final String PADDING = "  ";
    private static ConsoleWriter consoleWriter;
    private List<CommandObject> commandObjects = new ArrayList<>();

    private static final String LINE_BREAK = System.getProperty("line.separator");

    public QuickCLIShell(String name, String description) {
        super(name, description);
        consoleWriter = new ConsoleWriterImpl(this);
    }

    private CommandObject addCommand(String commandName, String description) {
        if (commandName == null) {
            throw new IllegalArgumentException("Command Name cannot be null");

        }
        if (findCommandByName(commandName) != null) {
            throw new IllegalArgumentException("Command already exists please specify a different name: " + commandName);
        }
        if (commandName.length() > MAX_CMD_LEN) {
            throw new IllegalArgumentException("Command name cannot be more than 10 charachters");
        }
        CommandObject commandObject = new CommandObject(commandName, description);
        commandObjects.add(commandObject);
        return commandObject;
    }

    private void processCommand(String line) throws Exception {
        if (line == null || line.isEmpty()) {
            return;
        }

        CmdLineParser parser = new CmdLineParser(line, commandObjects);
        parser.parse();
        List<String> errors = parser.getErrors();
        CommandObject commandObject = parser.getParsedCommandObject();

        if (errors.size() > 0) {
            printErrors(commandObject, errors);
            return;

        } else {
            Map<String, String> options = parser.getOptionsMap();
            List<String> arguments = parser.getArgumentValues();
            List<Character> optionFlags = parser.getOptionFlagValues();
            List<ArgumentObject> mandatoryArgumentObjects = commandObject.getMandatoryArguments();
            List<String> mandatoryArgNames = mandatoryArgumentObjects.stream().map(arg -> arg.getName()).collect(Collectors.toList());
            if (mandatoryArgumentObjects.size() > arguments.size()/*0 && !arguments.stream().allMatch(arg -> mandatoryArgNames.contains(arg))*/) {
                printLine("Error: All mandatory arguments must be provided : " + mandatoryArgNames);
                printHelp(false, commandObject.getName());
                return;
            }

            List<OptionObject> mandatoryOpts = commandObject.getMandatoryOptions();
            List<String> mandatoryOptNames = mandatoryOpts.stream().map(opt -> opt.getName()).collect(Collectors.toList());
            Set<String> optionKeys = options.keySet();
            if (mandatoryOpts.size() > 0 && !mandatoryOptNames.stream().allMatch(opt -> optionKeys.contains(opt))) {
                printLine("Error: All mandatory options must be provided : " + mandatoryOptNames);
                printHelp(false, commandObject.getName());
                return;
            }

            if (options.size() > mandatoryOpts.size()) {
                List<String> allOptionNames = commandObject.getOptionObjects().stream().map(opt -> opt.getName()).collect(Collectors.toList());
                List<String> undefinedOptions = options.keySet().stream().filter(opt -> !allOptionNames.contains(opt)).collect(Collectors.toList());
                if (undefinedOptions.size() > 0) {
                    printLine("Error: Options not recognized : " + undefinedOptions);
                    printHelp(false, commandObject.getName());
                    return;
                }
            }

            if (!commandObject.getOptionObjects().stream().allMatch(opt -> {
                String s = options.get(opt.getName());
                if (s != null && opt.getValidValues() != null && opt.getValidValues().size() > 0 && !opt.getValidValues().contains(s)) {
                    printLine("Error: Option value should be one of : " + opt.getValidValues() + ". Found : " + s);
                    printHelp(false, commandObject.getName());
                    return false;
                } else if (s == null && opt.getValidValues() != null && opt.getValidValues().size() > 0) {
                    options.put(opt.getName(), opt.getValidValues().get(0));
                }
                return true;
            })) {
                return;
            }


            if (optionFlags.size() > 0) {
                List<Character> allFlags = commandObject.getOptionFlagObjects().stream().map(flg -> flg.getFlgName()).collect(Collectors.toList());
                List<Character> undefinedFlags = optionFlags.stream().filter(f -> !allFlags.contains(f)).collect(Collectors.toList());
                if (undefinedFlags.size() > 0) {
                    printLine("Error: Option Flags not recognized: " + undefinedFlags);
                    printHelp(false, commandObject.getName());
                    return;
                }

            }

            final List<String> allArgumentNames = new ArrayList<>(mandatoryArgNames);

            commandObject.getArgumentObjects().stream().forEach(a -> {
                if (!a.isMandatory()) {
                    allArgumentNames.add(a.getName());
                }
            });

            if (arguments.size() > allArgumentNames.size()) {

                printLine("Error: Extra arguments entered. Found : " + arguments.size() + ", Expecting : " + allArgumentNames.size());
                printHelp(false, commandObject.getName());
                return;
            }


            Map<String, String> finalInputMap = new HashMap<>(options);
            IntStream.range(0, arguments.size()).forEach(index ->
                    {
                        if (index < allArgumentNames.size()) {
                            finalInputMap.put(allArgumentNames.get(index), arguments.get(index));
                        }
                    }
            );

            finalInputMap.putAll(options);

            optionFlags.forEach(f -> finalInputMap.put(Character.toString(f), "true"));


            if (commandObject.getCommandMethodInfo() != null) {
                List<Object> args = new ArrayList<>();
                for (Map.Entry<String, Class<?>> entry : commandObject.getCommandMethodInfo().getArgumentFieldTypeMap().entrySet()) {
                    String s = finalInputMap.get(entry.getKey());

                    try {
                        Class<?> fieldClass = entry.getValue();
                        if (fieldClass == String.class) {
                            args.add(s);
                        } else if (fieldClass == Float.class || fieldClass == float.class) {
                            args.add(Float.parseFloat(s));
                        } else if (fieldClass == BigDecimal.class) {
                            args.add(new BigDecimal(s));
                        } else if (fieldClass.equals(Long.class) || fieldClass == long.class) {
                            args.add(Long.parseLong(s));
                        } else if (fieldClass == Double.class || fieldClass == double.class) {
                            args.add(Double.parseDouble(s));
                        } else if (fieldClass == Short.class || fieldClass == short.class) {
                            args.add(Short.parseShort(s));
                        } else if (fieldClass == BigInteger.class) {
                            args.add(new BigInteger(s));
                        } else if (fieldClass == Integer.class || fieldClass == int.class) {
                            args.add(Integer.parseInt(s));
                        } else if (fieldClass == boolean.class || fieldClass == Boolean.class) {
                            args.add(Boolean.parseBoolean(s));
                        } else {
                            printErrors(commandObject, "Error: Method parameter type is not supported : " + fieldClass.getName() + ", " +
                                    commandObject.getCommandMethodInfo().getHandlerInstance().getClass() + "#" + commandObject.getCommandMethodInfo().getCommandMethod().getName());
                            return;
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Error: Not a valid value entered for " + entry.getKey() + " : " + s + "." +
                                " The value should be compatible with " + entry.getValue().getSimpleName());
                    }
                }
                if (errors.size() > 0) {
                    printErrors(commandObject, errors);
                    return;
                } else {
                    CommandMethodInfo info = commandObject.getCommandMethodInfo();
                    boolean accessible = info.getCommandMethod().isAccessible();
                    if (!accessible) {
                        info.getCommandMethod().setAccessible(true);
                    }
                    Object output = info.getCommandMethod().invoke(info.getHandlerInstance(), args.toArray());
                    if (output != null) {
                        if (output instanceof String) {
                            printLine(((String) output).replaceAll("\n", LINE_BREAK + PADDING));
                        }
                    }
                    if (info.getCommandMethod().isAccessible() != accessible) {
                        info.getCommandMethod().setAccessible(accessible);
                    }

                }

            } else if (commandObject.getCommandFunction() != null) {


                String output = commandObject.getCommandFunction().apply(finalInputMap);
                if (output != null) {
                    printLine(output.replaceAll("\n", LINE_BREAK + PADDING));
                }
            }
        }
    }

    /**
     * Starts the Shell application
     */
    public void start() {
        printLineSeparator();
        addCommand("help", "prints help").addArgument("command", "The command name")
                .addCommandHandler(this::printCommandHelp);
        addCommand("exit", "terminates shell").addCommandHandler(this::exitCLI);


        printLine(getName());
        printLine(getDescription());
        printLineSeparator();
        printHelp(true, null);

        Scanner scanIn = new Scanner(System.in);

        while (true) {
            try {
                // printBreak();
                System.out.print(getName() + ">");
                String line = scanIn.nextLine();
                processCommand(line);
            } catch (Throwable t) {
                printLine("Error: " + getCause(t));
                if ("dev".equals(System.getProperty("env"))) {
                    t.printStackTrace();
                }

            }
        }

    }

    private Throwable getCause(Throwable t) {
        Throwable throwable = t;
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

    private void printErrors(CommandObject commandObject, String... errors) {
        printErrors(commandObject, Arrays.asList(errors));
    }

    private void printErrors(CommandObject commandObject, List<String> errors) {
        TextTable errorTable = createTextTableLayout();
        errorTable.addStringColumn(false, PADDING.length(), true);

        for (String s : errors) {
            errorTable.addRow(s);
        }
        errorTable.printTable();
        if (commandObject == null) {
            printHelp(true, null);
            return;
        }

        printLine("Command help: ");
        TextTable helpTableLayout = createHelpTableLayout();
        printCommandHelp(commandObject, helpTableLayout);
        helpTableLayout.printTable();
        printGeneralHelpMessage();
    }

    private void printLineSeparator() {
        printLine("---------------------------------------------------------------------------");
    }

    private String exitCLI(Map<String, String> stringStringMap) {
        System.exit(1);
        return null;
    }

    private String printCommandHelp(Map<String, String> optionMap) {
        printHelp(false, optionMap.get("command"));
        return null;
    }

    private void printHelp(boolean brief, String cmd) {

        if (brief) {
            String out = "";
            out += "Valid Commands: " + (commandObjects.stream().map(c -> c.getName()).collect(Collectors.toList()));
            printLine(out);
            printLine("Please use 'help' command to view details");
        } else {
            if (cmd != null) {
                CommandObject commandObject = findCommandByName(cmd);
                if (commandObject == null) {
                    printLine("No command found : " + cmd);
                    printBreak();
                    printAllHelp();
                } else {
                    TextTable helpTableLayout = createHelpTableLayout();
                    printCommandHelp(commandObject, helpTableLayout);
                    helpTableLayout.printTable();
                    printLineSeparator();
                    printGeneralHelpMessage();
                }
            } else {
                printAllHelp();
            }
        }
    }

    private void printGeneralHelpMessage() {
        printLine("Please use double quotes for argument and option values if they contain non alphabetical characters");
        printLine("Option Flags can be combined together e.g. -a -b -c can be combined as -abc");
        printLine("Options must start with double hyphen e.g. --details=value");
    }

    private void printCommandHelp(CommandObject commandObject, TextTable table) {

        table.addRow(commandObject.getName(), "Description", commandObject.getDescription());
        table.addRow("", "Usage", getCommandFormat(commandObject));

        boolean first = true;

        for (OptionFlagObject opt : commandObject.getOptionFlagObjects()) {
            if (first) {
                table.addRow("", "Option Flag" + getHeadingPostfix(commandObject.getOptionFlagObjects()), "");
            }

            table.addRow("", "-" + opt.getName(), opt.getDescription());
            first = false;
        }

        first = true;
        for (OptionObject opt : commandObject.getOptionObjects()) {
            if (first) {
                table.addRow("", "Option" + getHeadingPostfix(commandObject.getOptionObjects()), "");
            }
            boolean m = opt.isMandatory();
            table.addRow("", "--" + opt.getName(), opt.getDescription() +
                    (opt.getValidValues() != null && opt.getValidValues().size() > 0 ? ". Valid values: " +
                            opt.getValidValues() + ". The default value is " + opt.getValidValues().get(0) : "") + (m ? " (Mandatory)" : ""));
            first = false;
        }

        first = true;

        for (ArgumentObject argumentObject : commandObject.getArgumentObjects()) {
            if (first) {
                table.addRow("", "Argument" + getHeadingPostfix(commandObject.getArgumentObjects()), "");
            }
            boolean m = argumentObject.isMandatory();
            table.addRow("", argumentObject.getName(), argumentObject.getDescription() + (m ? "(Mandatory)" : ""));
            first = false;

        }


        table.addRow("", "", "");
    }

    private String getHeadingPostfix(Collection<?> collection) {
        return (collection.size() == 1 ? "" : "s") + ":";
    }

    private TextTable createHelpTableLayout() {
        TextTable layout = new TextTable(90);
        layout.addStringColumn(true, PADDING.length(), false).addStringColumn(true, 2, false).addStringColumn(false, 2, true);
        return layout;
    }

    private TextTable createTextTableLayout() {
        return new TextTable(90);
    }


    private void printAllHelp() {
        printLineSeparator();
        TextTable helpTable = createHelpTableLayout();
        commandObjects.forEach(command -> {
            printCommandHelp(command, helpTable);
        });
        helpTable.printTable();
        printLineSeparator();
        printGeneralHelpMessage();
    }

    private String getCommandFormat(CommandObject commandObject) {
        String format = commandObject.getName() + "";

        for (OptionFlagObject optionFlagObject : commandObject.getOptionFlagObjects()) {
            format += " [-" + optionFlagObject.getName() + "]";
        }

        for (OptionObject optionObject : commandObject.getOptionObjects()) {
            boolean m = optionObject.isMandatory();
            format += " " + (!m ? "[" : "") + "--" + optionObject.getName() + "=<" +
                    optionObject.getName().toLowerCase() + "_value>" + (!m ? "]" : "");
        }

        for (ArgumentObject argumentObject : commandObject.getArgumentObjects()) {
            if (argumentObject.isMandatory()) {
                format += " <" + argumentObject.getName() + ">";
            }
        }

        for (ArgumentObject argumentObject : commandObject.getArgumentObjects()) {
            if (!argumentObject.isMandatory()) {
                format += " [<" + argumentObject.getName() + ">]";
            }
        }

        return format;
    }

    private OptionObject findOptionByName(CommandObject commandObject, String opt, List<String> errors) {
        List<OptionObject> collect = commandObject.getOptionObjects().stream().filter(option -> option.getName().equalsIgnoreCase(opt)).
                collect(Collectors.toList());
        if (collect.size() == 1) {
            return collect.get(0);
        } else if (collect.size() > 1) {
            errors.add("Multiple options with same name found :" + opt);
        }
        return null;
    }

    private CommandObject findCommandByName(String cmd) {
        Optional<CommandObject> found = commandObjects.stream().filter(command -> command.getName().
                equalsIgnoreCase(cmd)).findFirst();
        if (found.isPresent()) {
            return found.get();
        }
        return null;
    }


    private static void printLine(String s) {
        System.out.println(PADDING + s);
    }

    private static void printBreak() {
        printLine(LINE_BREAK);
    }


    /**
     * Scans the classes for commands and does the necessary initializing for the shell application.
     *
     * @param classesToBeScanned
     */
    public void scanCommands(Class<?>... classesToBeScanned) {
        for (Class<?> aClass : classesToBeScanned) {
            Object handlerInstance;
            try {
                handlerInstance = aClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    if (method.getReturnType() != void.class && method.getReturnType() != String.class) {
                        throw new IllegalArgumentException("The command method return type should be either String or void " + aClass.getName() + "#" + method.getName());
                    }
                    Map<String, Class<?>> argFieldTypeMap = new LinkedHashMap<>();


                    Command cmd = method.getAnnotation(Command.class);
                    CommandObject commandObject = addCommand(cmd.name(), cmd.desc());
                    for (Parameter parameter : method.getParameters()) {
                        Option option = parameter.getAnnotation(Option.class);
                        if (option != null) {
                            checkParameterType(option.name(), method, parameter.getType());
                            commandObject.addOption(option.name(), option.desc(), option.mandatory(), parameter.getType(), option.valuesAllowed());
                            if (argFieldTypeMap.containsKey(option.name())) {
                                throw new IllegalArgumentException("Option defined more than once :" + option.name() + ", method: " + aClass.getName() + "#" + method.getName());
                            }
                            argFieldTypeMap.put(option.name(), parameter.getType());
                        }
                        OptionFlag optionFlag = parameter.getAnnotation(OptionFlag.class);
                        if (optionFlag != null) {
                            if (parameter.getType() != boolean.class && parameter.getType() != Boolean.class) {
                                throw new IllegalArgumentException("Parameter annotated with OptionFlg must be of boolean type : " + optionFlag.name() +
                                        ", type found : " + parameter.getType() + ", method: " + aClass.getName() + "#" + method.getName());
                            }
                            try {
                                commandObject.addOptionFlag(optionFlag.name(), optionFlag.desc());
                            } catch (Throwable e) {
                                throw new IllegalArgumentException("Option flag name is not valid : " + optionFlag.name() + ", method: " + aClass.getName() + "#" + method.getName(), e);
                            }
                            if (argFieldTypeMap.containsKey(optionFlag.name() + "")) {
                                throw new IllegalArgumentException("Option flag defined more than once :" + optionFlag.name() + ", method: " + aClass.getName() + "#" + method.getName());
                            }
                            argFieldTypeMap.put(optionFlag.name() + "", parameter.getType());
                        }
                        Argument argument = parameter.getAnnotation(Argument.class);
                        if (argument != null) {
                            checkParameterType(argument.name(), method, parameter.getType());
                            commandObject.addArgument(argument.name(), argument.desc(), argument.mandatory(), parameter.getType());
                            argFieldTypeMap.put(argument.name(), parameter.getType());
                        }
                    }


                    commandObject.setCommandMethodInfo(new CommandMethodInfo(method, handlerInstance, argFieldTypeMap));
                }
            }

        }

    }

    private static final List<Class<? extends Number>> SUPPORTED_NUMBER_TYPES =
            Arrays.asList(Float.class, BigDecimal.class, Long.class, Double.class,
                    Short.class, BigInteger.class, Byte.class, Integer.class, float.class,
                    long.class, double.class, short.class, byte.class, int.class);

    private void checkParameterType(String fieldName, Method method, Class<?> type) {
        if (type != String.class && SUPPORTED_NUMBER_TYPES.stream().noneMatch(n -> n == type)) {
            throw new IllegalArgumentException(
                    "Option/Argument type must be either String or any sub type of following Number type: " +
                            SUPPORTED_NUMBER_TYPES + "\n Found: " + type.getName() + "\n Field: " + fieldName + "\n Method: " + method + "\n");
        }
    }

    private static class ConsoleWriterImpl implements ConsoleWriter {
        private QuickCLIShell shell;

        private ConsoleWriterImpl(QuickCLIShell cliShell) {
            this.shell = cliShell;

        }

        @Override
        public void printLine(String line) {
            shell.printLine(line);

        }

        @Override
        public void printAllHelp() {
            shell.printAllHelp();
        }

        @Override
        public void printHelp(String commandName) {
            shell.printHelp(false, commandName);
        }

        @Override
        public TextTable createTextTable() {
            return shell.createTextTableLayout();
        }

        @Override
        public void printErrors(String commandName, String... errors) {
            CommandObject commandByName = shell.findCommandByName(commandName);
            shell.printErrors(commandByName, errors);

        }

        @Override
        public void printLineSeparator() {
            shell.printLineSeparator();

        }

        @Override
        public void printLineBreak() {
            shell.printBreak();
        }
    }

    /**
     * Resturn the instance of ConsoleWriter.
     *
     * @return Console Writer
     */
    public static ConsoleWriter getWriter() {
        return consoleWriter;
    }
}
