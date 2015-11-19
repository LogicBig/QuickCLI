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

package com.logicbig.cli.tests;


import com.logicbig.cli.annotation.Argument;
import com.logicbig.cli.annotation.Command;
import com.logicbig.cli.annotation.Option;
import com.logicbig.cli.annotation.OptionFlag;
import com.logicbig.cli.shell.QuickCLIShell;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for Quick Cli Shell
 *
 * @author Joe Khan.
 */
public class QuickCLIShellTest {

    private static final String EOL =
            System.getProperty("line.separator");
    private PrintStream console;
    private ByteArrayOutputStream bytes;
    private Method processCommandMethod;
    private QuickCLIShell shell;

    @Before
    public void setUp() throws NoSuchMethodException {
        bytes = new ByteArrayOutputStream();
        console = System.out;
        System.setOut(new PrintStream(bytes));
        processCommandMethod = QuickCLIShell.class.getDeclaredMethod("processCommand", String.class);
        processCommandMethod.setAccessible(true);
        shell = new QuickCLIShell("Test Shell ", "Shell desc");
        shell.scanCommands(TestCommandHandler.class);
    }

    @After
    public void tearDown() {
        System.setOut(console);
    }

    @Test
    public void command1() {
        assertStartsWith("cmd", "Error: No command found : cmd");
        Assert.assertEquals("null", runCommandOutput("command1"));
        Assert.assertEquals("someValue", runCommandOutput("command1 someValue"));

    }

    @Test
    public void command2() {

        assertContains("command2", "Error: All mandatory arguments must be provided : [arg1]");

        assertStartsWith("command2 someValue", "Error: Not a valid value entered for arg1 : someValue");

        assertStartsWith("command2 someValue1 someValue2", "Error: Extra arguments entered. Found : 2, Expecting : 1");

        assertStartsWith("command2 1.1", "Error: Not a valid value entered for arg1 : 1.1");

        assertStartsWith("command2 1 2 3", "Error: Extra arguments entered. Found : 3, Expecting : 1");

        Assert.assertEquals("100", runCommandOutput("command2 \"100\""));

        Assert.assertEquals("200", runCommandOutput("command2 200"));

    }

    @Test
    public void command3() {
        Assert.assertEquals("false", runCommandOutput("command3"));
        Assert.assertEquals("true", runCommandOutput("command3 -t"));
    }

    @Test
    public void command4() {
        Assert.assertEquals("false-false-false", runCommandOutput("command4"));
        Assert.assertEquals("true-false-false", runCommandOutput("command4 -t"));
        Assert.assertEquals("true-true-false", runCommandOutput("command4 -t -u"));
        Assert.assertEquals("true-true-false", runCommandOutput("command4 -u -t"));
        Assert.assertEquals("true-true-false", runCommandOutput("command4 -tu"));
        Assert.assertEquals("true-true-false", runCommandOutput("command4 -ut"));
        Assert.assertEquals("true-true-true", runCommandOutput("command4 -t -u -v"));
        Assert.assertEquals("true-true-true", runCommandOutput("command4 -tu -v"));
        Assert.assertEquals("true-true-true", runCommandOutput("command4 -t -uv"));
        Assert.assertEquals("true-true-true", runCommandOutput("command4 -tuv"));
        Assert.assertEquals("true-true-true", runCommandOutput("command4 -utv"));
        Assert.assertEquals("false-true-true", runCommandOutput("command4 -uv"));
        assertStartsWith("command4 -cz -tvba", "Error: Option Flags not recognized: [c, z, b, a]");

        //  Assert.assertEquals("true", runCommandOutput("command3 -t"));
    }

    @Test
    public void command5() {
        Assert.assertEquals("null-null", runCommandOutput("command5"));
        Assert.assertEquals("xyz-null", runCommandOutput("command5 --opa=xyz"));
        Assert.assertEquals("null-stu", runCommandOutput("command5 --opb=stu"));
        Assert.assertEquals("xyz-stu", runCommandOutput("command5 --opa=xyz --opb=stu"));
        Assert.assertEquals("null-quoted opt value", runCommandOutput("command5 --opb=\"quoted opt value\""));
        Assert.assertEquals("abc-quoted opt value", runCommandOutput("command5 --opb=\"quoted opt value\" --opa=abc"));
        Assert.assertEquals("null-quoted =opt -value", runCommandOutput("command5 --opb=\"quoted =opt -value\""));
        Assert.assertEquals("abc-\\\"xyz-quoted opt value", runCommandOutput("command5 --opb=\"quoted opt value\" --opa=\"abc-\\\"xyz\""));
        Assert.assertEquals("null-'ab'cd'", runCommandOutput("command5 --opb='ab'cd'"));
    }

    private String runCommandOutput(String command) {
        bytes.reset();
        try {
            processCommandMethod.invoke(shell, command);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String s = bytes.toString();
        return s == null ? null : s.trim();
    }

    private void assertStartsWith(String command, String s) {
        String output = runCommandOutput(command);
        Assert.assertTrue(output, output.startsWith(s));
    }

    private void assertContains(String command, String s) {
        String output = runCommandOutput(command);
        Assert.assertTrue(output, output.contains(s));
    }

    @Test
    public void command6() {
        Assert.assertEquals("arg1-arg2-opt1-opt2-true", runCommandOutput("command6 -t --optA=opt1 --optB=opt2 arg1 arg2"));
        Assert.assertEquals("arg2-null-opt1-opt2-true", runCommandOutput("command6 -t --optA=opt1 --optB=opt2 arg2"));
        Assert.assertEquals("arg2-null-null-opt1-true", runCommandOutput("command6 -t --optB=opt1  arg2"));
        Assert.assertEquals("one argument-null-null-opt1-true", runCommandOutput("command6 -t --optB=opt1  \"one argument\""));
        Assert.assertEquals("one argument-null-null-opt1-false", runCommandOutput("command6 --optB=opt1  \"one argument\""));
        Assert.assertEquals("argFirst-one argument-null-opt1-false", runCommandOutput("command6 argFirst --optB=opt1  \"one argument\""));
    }

  /*  public static void main(String... strings) {
        QuickCLIShell shell = new QuickCLIShell("Test 1", "Test 1 desc");
        shell.scanCommands(TestCommandHandler.class);
        shell.start();


    }*/

    public static class TestCommandHandler {

        @Command(name = "command1", desc = "command1 desc")
        public String method1(@Argument(name = "arg1", desc = "arg1 desc") String arg1) {
            return arg1 == null ? "null" : arg1;

        }

        @Command(name = "command2", desc = "command2 desc")
        public String method2(@Argument(name = "arg1", desc = "arg1 desc", mandatory = true) int arg1) {
            return Integer.toString(arg1);
        }

        @Command(name = "command3", desc = "command3 desc")
        public String method3(@OptionFlag(name = 't', desc = "option flg 1 desc") Boolean arg1) {
            return Boolean.toString(arg1);
        }

        @Command(name = "command4", desc = "command4 desc")
        public String method4(@OptionFlag(name = 't', desc = "option flg 1 desc") Boolean arg1,
                              @OptionFlag(name = 'u', desc = "option flg 2 desc") Boolean arg2,
                              @OptionFlag(name = 'v', desc = "option flg 3 desc") Boolean arg3) {
            return Boolean.toString(arg1) + "-" + Boolean.toString(arg2) + "-" + Boolean.toString(arg3);
        }

        @Command(name = "command5", desc = "command5 desc")
        public String method5(@Option(name = "opa", desc = "arg desc") String arg1,
                              @Option(name = "opb", desc = "arg desc") String arg2) {
            return arg1 + "-" + arg2;

        }

        @Command(name = "command6", desc = "command6 desc")
        public String method6(@Option(name = "optA", desc = "opt A desc") String opt1,
                              @OptionFlag(name = 't', desc = "opt flg desc") boolean flg1,
                              @Option(name = "optB", desc = "opt B desc", mandatory = true) String opt2,
                              @Argument(name = "arg1", desc = "arg1 desc", mandatory = true) String arg1,
                              @Argument(name = "arg2", desc = "arg2 desc") String arg2) {
            return arg1 + "-" + arg2 + "-" + opt1 + "-" + opt2 + "-" + flg1;
        }

        @Command(name = "command7", desc = "command7 desc")
        public String method7(@Argument(name = "arg2", desc = "arg2 desc") BigDecimal arg2) {
            return arg2.toPlainString();
        }

    }
    @Test
    public void command7(){
        Assert.assertEquals("5.5", runCommandOutput("command7 5.5"));
    }
}
