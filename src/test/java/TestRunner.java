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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRunner {

    private static final String START_DIR = "test-classes";
    private static final Pattern PATTERN = Pattern.compile("(^" + START_DIR + "[\\\\|/])(\\w+Test)\\.class$");

    public static void main(String[] args) throws Exception {
        final Path cwd = Paths.get(START_DIR);
        final List<String> tests = new ArrayList<>(16);
        Files.walkFileTree(cwd, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final Matcher matcher = PATTERN.matcher(file.toString());
                if (matcher.matches()) {
                    tests.add(matcher.group(2));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        for (final String test : tests) {
            try {
                runTest(test);
            } catch (Exception ex) {
                System.out.flush();
                System.err.flush();
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static void runTest(final String testClassName) throws Exception {
        System.out.flush();
        System.err.printf("++ %s\n", testClassName);
        final Class<?> testClass = Class.forName(testClassName);
        Method beforeTest = null;
        Method afterTest = null;
        Method beforeMethod = null;
        Method afterMethod = null;
        final List<Method> tests = new ArrayList<>(16);
        for (final Method method : testClass.getDeclaredMethods()) {
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
        for (final Method test : tests) {
            System.out.flush();
            System.err.printf("-- %s.%s\n", testClassName, test.getName());
            if (beforeMethod != null) {
                if (beforeMethod.getParameterCount() == 0) {
                    beforeMethod.invoke(instance);
                } else {
                    beforeMethod.invoke(instance, test);
                }
            }
            test.invoke(instance);
            if (afterMethod != null) {
                if (afterMethod.getParameterCount() == 0) {
                    afterMethod.invoke(instance);
                } else {
                    afterMethod.invoke(instance, test);
                }
            }
        }
        if (afterTest != null) {
            afterTest.invoke(instance);
        }
    }

}
