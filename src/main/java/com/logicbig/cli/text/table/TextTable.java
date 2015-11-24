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

package com.logicbig.cli.text.table;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A convenient way to write multiple columns in text format. We can define multiple columns of different type.
 * It's necessary to provide the table width during construction time. Ideally only one column of String should be
 * provide wrap=true property.
 *
 * @author : Joe Khan
 */
public class TextTable {

    private int terminalWidth;

    private Map<TextTableColumn, List<Object>> columns = new LinkedHashMap<TextTableColumn, List<Object>>();

    /**
     * TextTable constructor. If width is less than 1 then default value of 80 is used.
     *
     * @param width
     */
    public TextTable(int width) {
        terminalWidth = width <= 1 ? 80 : width;
    }

    /**
     * Add String column.
     *
     * @param rightAlign
     * @param leadingSpaces
     * @param wrapString
     * @return
     */
    public TextTable addStringColumn(boolean rightAlign, int leadingSpaces, boolean wrapString) {
        columns.put(new TextTableStringColumn(rightAlign, leadingSpaces, wrapString), new ArrayList<Object>());
        return this;
    }

    /**
     * Add a integer column. Could be byte, integer, short, long
     *
     * @param rightAlign
     * @param trailingSpaces
     * @param autoFillZeros
     * @return
     */
    public TextTable addIntegralColumn(boolean rightAlign, int trailingSpaces, boolean autoFillZeros) {
        columns.put(new TextTableIntegralColumn(rightAlign, trailingSpaces, autoFillZeros), new ArrayList<Object>());
        return this;
    }

    private TextTable addFloatingPointColumn(Class<? extends Number> dataType, boolean rightAlign, int trailingSpaces, boolean autoFillZeros, float precision) {
        //   columns.put(new TextTableFloatingPointColumn(rightAlign, trailingSpaces, autoFillZeros));
        //todo
        return this;
    }

    private String getLeadingSpaces(TextTableColumn column) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("%" + column.getLeadingSpaces() + "s", "");

        return sb.toString();

    }

    // convert everything into string.. don't apply wrapping logging yet
    private String doConversion(Object object, TextTableColumn column) {

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(column.getFormat(), object);

        return sb.toString();
    }

    /**
     * Add a row. The number of objects provided should be equal to number of columns defined.
     *
     * @param objects
     */
    public void addRow(Object... objects) {
        checkNull(objects);
        validateSize(Arrays.asList(objects));
        addRow(Arrays.asList(objects));
    }

    /**
     * Add a row. The size of objects provided should be equal to number of columns defined.
     *
     * @param objects
     */
    public void addRow(List<Object> objects) {

        ArrayList<Map.Entry<TextTableColumn, List<Object>>> columnEntries = new ArrayList<>(columns.entrySet());

        for (int i = 0; i < columnEntries.size(); i++) {

            Map.Entry<TextTableColumn, List<Object>> entry = columnEntries.get(i);
            TextTableColumn column = entry.getKey();
            Object o = objects.get(i);
            checkCompatibility(o, column);
            entry.getValue().add(o);
        }
    }

    private void checkCompatibility(Object o, TextTableColumn column) {
        Class<?> oClass = o.getClass();
        if ((column instanceof TextTableStringColumn && oClass != String.class) &&
                (column instanceof TextTableIntegralColumn && (oClass != Integer.class || oClass != Byte.class || oClass != Short.class || oClass != Long.class || oClass != BigDecimal.class))
            //todo: do the same for charachter and floating point
                ) {
            //  throw new JCFormatException("Object type is not compatible with specified column type. Object type=" + o.getClass() + " Column type=" + column.getDataType());
        }
    }

    private void checkNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Objects cannot be null " + object);
        }
    }

    private void validateSize(List<Object> objects) {
        if (columns.size() != objects.size()) {
            throw new IllegalArgumentException("printRow expect equal number of arguments as column : columns=" + columns.size() + " objects=" + objects.size());
        }
    }

    /**
     * Prints the table as console output.
     */
    public void printTable() {
        ArrayList<Map.Entry<TextTableColumn, List<Object>>> columnEntries = new ArrayList<Map.Entry<TextTableColumn, List<Object>>>(columns.entrySet());


        int totalStringDisplayWrapWidth = 0;
        int rows = -1;
        for (int i = 0; i < columnEntries.size(); i++) {
            Map.Entry<TextTableColumn, List<Object>> entry = columnEntries.get(i);
            TextTableColumn column = entry.getKey();
            List<Object> objects = entry.getValue();
            if (rows == -1) {
                rows = objects.size();
                if (rows == 0) {
                    return;
                }
            }
            if (column instanceof TextTableStringColumn) {
                List<String> stringRow = objects.stream().map(o -> o.toString()).collect(Collectors.toList());
                Integer integer = stringRow.stream().map(s -> s.length()).reduce(0, (c, len) -> Math.max(c, len));
                column.setDisplayWidth(integer == null ? 0 : integer);
                if (!((TextTableStringColumn) column).isWrapString()) {
                    terminalWidth -= column.getDisplayWidth();
                } else {
                    totalStringDisplayWrapWidth += column.getDisplayWidth();
                }
            } else if (column instanceof TextTableIntegralColumn) {

                List<String> stringRow = objects.stream().map(o -> o.toString()).collect(Collectors.toList());
                Integer integer = stringRow.stream().map(s -> s.length()).reduce(0, (c, len) -> Math.max(c, len));
                column.setDisplayWidth(integer == null ? 0 : integer);
                terminalWidth -= column.getDisplayWidth();
            } else if (column instanceof TextTableFloatingPointColumn) {
                //todo: do it later
            }

            //totalDisplayWidth += column.getDisplayWidth();


        }

        for (TextTableColumn column : columns.keySet()) {
            if (column instanceof TextTableStringColumn && ((TextTableStringColumn) column).isWrapString()) {
                column.setDisplayWidth((column.getDisplayWidth() * terminalWidth) / totalStringDisplayWrapWidth);
            }
            column.buildFormat();
        }


        List<List<List<String>>> rowLines = new ArrayList<List<List<String>>>();
        List<Integer> maxLinesPerRow = new ArrayList<Integer>();

        for (int i = 0; i < rows; i++) {
            List<List<String>> row = new ArrayList<List<String>>();
            rowLines.add(row);
            int maxLines = 1;

            for (Map.Entry<TextTableColumn, List<Object>> entry : columns.entrySet()) {
                TextTableColumn column = entry.getKey();
                Object object = entry.getValue().get(i);

                row.add(Arrays.asList(getLeadingSpaces(column)));
                if (column instanceof TextTableStringColumn) {
                    String stringValue = object.toString();
                    if (stringValue.length() > column.getDisplayWidth()) {
                        List<String> lines = wrapString(stringValue, column.getDisplayWidth());
                        List<String> formattedLines = new ArrayList<String>();
                        for (String uf : lines) {
                            formattedLines.add(doConversion(uf, column));
                        }
                        row.add(formattedLines);
                        maxLines = Math.max(maxLines, lines.size());

                    } else {
                        row.add(Arrays.asList(doConversion(stringValue, column)));
                    }

                } else if (column instanceof TextTableIntegralColumn) {
                    row.add(Arrays.asList(doConversion(object, column)));
                } else if (column instanceof TextTableFloatingPointColumn) {
                    //todo: do it later
                }
            }
            maxLinesPerRow.add(maxLines);
        }


        List<TextTableColumn> cols = new ArrayList<TextTableColumn>(columns.keySet());
        for (int i = 0; i < rowLines.size(); i++) {
            List<List<String>> row = rowLines.get(i);
            int lines = maxLinesPerRow.get(i);

            int totalC = cols.size() * 2;


            for (int r = 0; r < lines; r++) {
                for (int loop = 0, c = 0; c < totalC; c = c + 2, loop++) {

                    String spaces = row.get(c).get(0);
                    System.out.printf(spaces);

                    List<String> cellData = row.get(c + 1);

                    if (r < cellData.size()) {

                        System.out.print(cellData.get(r));
                    } else {
                        System.out.printf("%" + cols.get(loop).getDisplayWidth() + "s", "");
                    }


                }
                System.out.println();
            }


        }
    }

    private static List<String> breakString(String bigString, int maxWidth) {
        List<String> lines = new ArrayList<String>();
        if (bigString == null || bigString.length() == 0) {
            return lines;
        }

        if (bigString.length() <= maxWidth) {
            lines.add(bigString);
            return lines;
        }

        String temp = bigString;
        while (true) {
            if (temp.length() <= maxWidth) {
                lines.add(temp);
                break;
            }
            lines.add(temp.substring(0, maxWidth));
            temp = temp.substring(maxWidth);
        }


        return lines;
    }

    private static String breakString(String temp, int maxWidth, List<String> lines) {
        if (temp.length() >= maxWidth) {
            List<String> brokenStrings = breakString(temp, maxWidth);
            temp = brokenStrings.get(brokenStrings.size() - 1);
            brokenStrings.remove(brokenStrings.size() - 1);
            lines.addAll(brokenStrings);
        }
        return temp;
    }


    static List<String> wrapString(String bigString, int maxWidth) {
        List<String> lines = new ArrayList<String>();

        if (bigString == null || bigString.length() == 0) {
            return lines;
        }

        String[] strings = bigString.split("\\s+");

        if (strings.length == 0) {
            return lines;
        }
        String temp = strings[0];
        temp = breakString(temp, maxWidth, lines);

        if (strings.length == 1) {
            lines.add(temp);
            return lines;
        }

        for (int i = 1; i < strings.length; i++) {
            String s = strings[i];

            if (temp.length() + s.length() + 1 > maxWidth) {
                if (temp.length() >= maxWidth) {
                    lines.addAll(breakString(temp, maxWidth));
                } else {
                    lines.add(temp);
                }

                if (i == strings.length - 1) {
                    lines.addAll(breakString(s, maxWidth));
                    break;
                } else {
                    temp = s;
                    continue;
                }
            } else {
                temp += " " + s;
            }

            if (i == strings.length - 1) {
                lines.add(temp);
            }

        }
        return lines;
    }

    public static void main(String... strings) {
        TextTable textTable = new TextTable(80);
        textTable.addStringColumn(false, 5, false).addStringColumn(false, 2, true).addIntegralColumn(true, 2, true);
        for (int i = 8; i < 12; i++) {
            textTable.addRow("sdfdfsdfdfsdfdf" + i, "FsdfsdfsFsdfsdfsFsdfsdfsFsdfsdfsFsdfsdfsFsdfsdfsFsdfsdfsFsdfsdfsFsd" + i, i);
        }
        textTable.printTable();

    }


}
