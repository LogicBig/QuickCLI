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

import com.logicbig.cli.text.table.TextTable;

/**
 * ConsoleWriter is a convenient way to write to console.
 *
 * @author Joe Khan.
 */
public interface ConsoleWriter {

    /**
     * Prints a line
     *
     * @param line, line to be printed to the console
     */
    void printLine(String line);

    /**
     * Prints all registered command help
     */
    void printAllHelp();

    /**
     * Print the provided command help
     *
     * @param commandName, the command name to be printed
     */
    void printHelp(String commandName);

    /**
     * Creates the instance of TextTable
     *
     * @return the instance of the TextTable
     */
    TextTable createTextTable();


    /**
     * Prints the provided errors.
     *
     * @param commandName commandName for which help is printed after help.
     *                    Can be null
     * @param errors      the errors to be printed
     */
    void printErrors(String commandName, String... errors);

    /**
     * Prints a line separator
     */
    void printLineSeparator();

    /**
     * Prints a Line break;
     */
    void printLineBreak();
}
