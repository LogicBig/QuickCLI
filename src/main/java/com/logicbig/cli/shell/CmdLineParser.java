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

import java.util.*;

/**
 * Parses command and sort out arguments entered by user.
 *
 * @author Joe Khan.
 */
class CmdLineParser {

    private final String line;
    private String remainingLine;
    private final List<CommandObject> commandObjects;
    private final List<String> errors = new ArrayList<>();
    private CommandObject parsedCommandObject;
    private List<Character> optionFlagValues;
    private LinkedList<String> argumentValues;
    private LinkedHashMap<String, String> optionsMap;

    public CmdLineParser(String line, List<CommandObject> commandObjects) {
        this.commandObjects = commandObjects;
        this.line = line != null ? line.trim() : line;
        remainingLine = line;
    }

    public void parse() {
        if (line == null || line.length() == 0) {
            return;
        }

        parsedCommandObject = parseCommand();
        if (parsedCommandObject == null) {
            return;
        }

        optionsMap = new LinkedHashMap<>();
        argumentValues = new LinkedList<>();
        optionFlagValues = new LinkedList<>();

        for (String s : new LineParser(remainingLine).getTokens()) {
            if (s == null || s.length() == 0) {
                continue;
            }
            if (s.startsWith("--")) {
                int i = s.indexOf("=");
                if (i == -1) {
                    errors.add("Error: Option must contained a value followed by =, value entered: " + s);
                }
                optionsMap.put(s.substring(2, i), validateAndCleanArgValue(s.substring(i + 1)));

            } else if (s.startsWith("-")) {
                if (s.length() == 1) {
                    errors.add("Error: Invalid token " + s);
                }
                char[] chars = s.toCharArray();
                for (int i = 1; i < s.length(); i++) {
                    optionFlagValues.add(chars[i]);
                }
            } else {
                argumentValues.add(validateAndCleanArgValue(s));

            }
        }
    }

    private String validateAndCleanArgValue(String s) {
        if (s.startsWith("\"")) {
            if (s.endsWith("\\\"")) {
                errors.add("Error: Argument values should not end with escaped quote: " + s);
            } else if (!s.endsWith("\"")) {
                errors.add("Error: Argument values should end with double quote: " + s);
            } else {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }


    public List<String> getErrors() {
        return errors;
    }

    public CommandObject getParsedCommandObject() {
        return parsedCommandObject;
    }

    public List<Character> getOptionFlagValues() {
        return optionFlagValues;
    }

    public List<String> getArgumentValues() {
        return argumentValues;
    }

    public Map<String, String> getOptionsMap() {
        return optionsMap;
    }

    private CommandObject parseCommand() {
        String cmd = null;
        int i = line.indexOf(" ");
        if (i != -1) {
            cmd = line.substring(0, i);
            remainingLine = line.substring(i + 1).trim();

        } else {
            cmd = line.trim();
            remainingLine = "";
        }

        return findCommandByName(cmd);
    }


    private CommandObject findCommandByName(String cmd) {
        Optional<CommandObject> found = commandObjects.stream().filter(command -> command.getName().
                equalsIgnoreCase(cmd)).findFirst();
        if (found.isPresent()) {
            return found.get();
        }

        errors.add("Error: No command found : " + cmd);
        return null;
    }


    public static void main(String[] args) throws Exception {
        String line = "\\\"333 \\\"444-666\" -x=9 -s=true";

        System.out.println(new LineParser(line).getTokens());

    }


    private static class LineParser {
        private String cmdString;

        public LineParser(String cmdString) {
            this.cmdString = cmdString;
        }

        public List<String> getTokens() {
            ArrayList<String> finalTokens = new ArrayList<String>();
            ArrayList<StringBuffer> tokens = new ArrayList<StringBuffer>();
            char inArray[] = this.cmdString.toCharArray();
            StringBuffer token = new StringBuffer();
            int valid = checkIfTheStringIsValid(inArray);
            if (valid == -1) {
                for (int i = 0; i <= inArray.length; i++) {

                    if (i != inArray.length) {
                        if ((inArray[i] != ' ') && (inArray[i] != '"')) {
                            token.append(inArray[i]);
                        }

                        if ((inArray[i] == '"') && (i == 0 || inArray[i - 1] != '\\')) {
                            token.append(inArray[i]);
                            i = i + 1;
                            while (checkIfLastQuote(inArray, i)) {
                                token.append(inArray[i]);
                                i++;
                            }
                            token.append('"');
                        }

                    }
                    if (i == inArray.length) {
                        tokens.add(token);
                        token = new StringBuffer();
                    } else if (inArray[i] == ' ' && inArray[i] != '"') {
                        tokens.add(token);
                        token = new StringBuffer();
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "Invalid command. Couldn't identify sequence at position "
                                + valid);
            }
            for (StringBuffer tok : tokens) {
                finalTokens.add(tok.toString());
            }
            return finalTokens;
        }

        private static int checkIfTheStringIsValid(char[] inArray) {
            Stack myStack = new Stack<Character>();
            int pos = 0;
            for (int i = 0; i < inArray.length; i++) {
                if (inArray[i] == '"' && (i == 0 || inArray[i - 1] != '\\')) {
                    pos = i;
                    if (myStack.isEmpty())
                        myStack.push(inArray[i]);
                    else
                        myStack.pop();
                }
            }
            if (myStack.isEmpty())
                return -1;
            else
                return pos;
        }

        private static boolean checkIfLastQuote(char inArray[], int i) {
            if (inArray[i] == '"') {
                if (inArray[i - 1] == '\\') {
                    return true;
                } else
                    return false;
            } else
                return true;
        }
    }
}
