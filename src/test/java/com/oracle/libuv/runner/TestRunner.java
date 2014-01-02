/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.libuv.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestRunner {

    private static final String TEST_REPORTS_DIR = "test-reports";

    public static void main(final String[] args) throws Exception {
        final String test = System.getProperty("test");
        if (test != null) {
            runATest(test);
            System.out.flush();
            System.err.flush();
            System.exit(0);
        }

        for (final String arg : args) {
            final String[] files = arg.split(";");
            for (final String file : files) {
                runATest(file);
            }
            System.out.flush();
            System.err.flush();
            System.exit(0);
        }
    }

    public static void runATest(final String file) throws FileNotFoundException {
        final String testClassName = file.replaceAll(".java$", "").replaceAll("\\\\|/", ".");
        final PrintStream out = new PrintStream(TEST_REPORTS_DIR + File.separator + testClassName + ".txt");
        System.setOut(out);
        try {
            runTest(testClassName);
        } catch (final Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        } finally {
            out.close();
        }
    }

    private static void runTest(final String testClassName) throws Exception {
        System.err.printf("++ %s\n", testClassName);
        final Class<?> testClass = Class.forName(testClassName);
        Method beforeTest = null;
        Method afterTest = null;
        Method beforeMethod = null;
        Method afterMethod = null;
        final List<Method> tests = new ArrayList<>(16);
        for (final Method method : testClass.getDeclaredMethods()) {
            final int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            for (final Annotation ann : method.getDeclaredAnnotations()) {
                if (BeforeTest.class.isAssignableFrom(ann.annotationType())) {
                    beforeTest = method;
                } else if (AfterTest.class.isAssignableFrom(ann.annotationType())) {
                    afterTest = method;
                } else if (BeforeMethod.class.isAssignableFrom(ann.annotationType())) {
                    beforeMethod = method;
                } else if (AfterMethod.class.isAssignableFrom(ann.annotationType())) {
                    afterMethod = method;
                } else if (Test.class.isAssignableFrom(ann.annotationType())) {
                    tests.add(method);
                }
            }
        }
        final Object instance = testClass.newInstance();
        if (beforeTest != null) {
            beforeTest.invoke(instance);
        }
        try {
            for (final Method test : tests) {
                System.err.printf("   %s.%s\n", testClassName, test.getName());
                callSetupMethod(beforeMethod, instance, test);
                try {
                    test.invoke(instance);
                } finally {
                    callSetupMethod(afterMethod, instance, test);
                }
            }
        } finally {
            if (afterTest != null) {
                afterTest.invoke(instance);
            }
        }
    }

    private static void callSetupMethod(final Method method,
                                        final Object instance,
                                        final Object... args) throws Exception {
        if (method != null) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
                method.invoke(instance);
            } else {
                method.invoke(instance, args);
            }
        }
    }

}
