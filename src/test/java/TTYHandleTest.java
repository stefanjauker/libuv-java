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

import net.java.libuv.LibUV;
import net.java.libuv.NativeException;
import net.java.libuv.StreamCallback;
import net.java.libuv.handles.LoopHandle;
import net.java.libuv.handles.TTYHandle;

import java.nio.ByteBuffer;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TTYHandleTest {

    static {
        // call a LibUV method just to ensure that the native lib is loaded
        System.out.println(TTYHandleTest.class.getSimpleName() + " in " + LibUV.cwd());
    }

    @DataProvider
    public Object[][] stdOutErrProvider() {
        final Object[][] fds = {
            {"stdout", 1},
            {"stderr", 2}
        };
        return fds;
    }

    @DataProvider
    public Object[][] stdInProvider() {
        final Object[][] fds = {
                {"stdin", 0}
        };
        return fds;
    }

    private TTYHandle newTTY(final LoopHandle loop, final int fd, final boolean readable) {
        final TTYHandle tty;
        try {
            tty = new TTYHandle(loop, fd, readable);
        } catch (final NativeException nx) {
            if ("EBADF".equals(nx.errnoString())) {
                // running under a test runner or ant
                System.out.println("EBADF error creating tty " + fd);
                return null;
            }
            throw nx; // any other error fails the test
        }
        if (!TTYHandle.isTTY(fd)) {
            System.out.println("not a tty " + fd);
            return null;
        }
        return tty;
    }

    @Test(dataProvider = "stdOutErrProvider")
    public void testStdOutErrWindowSize(String name, int fd) {
        final LoopHandle loop = new LoopHandle();
        final TTYHandle tty = newTTY(loop, fd, false);
        if (tty == null) return;
        testWindowSize(name, tty);
    }

    @Test(dataProvider = "stdInProvider")
    public void testStdinWindowSize(String name, int fd) {
        final LoopHandle loop = new LoopHandle();
        final TTYHandle tty = newTTY(loop, fd, true);
        if (tty == null) return;
        testWindowSize(name, tty);
    }

    private void testWindowSize(final String name, final TTYHandle tty) {
        final int[] windowSize = tty.getWindowSize();
        Assert.assertNotNull(windowSize);
        Assert.assertEquals(windowSize.length, 2);
        Assert.assertNotNull(windowSize[0]);
        Assert.assertNotNull(windowSize[1]);
        Assert.assertTrue(windowSize[0] > 10);
        Assert.assertTrue(windowSize[1] > 10);
        System.out.println("tty " + name + " window size: " + windowSize[0] + " : " + windowSize[1]);
    }

    @Test(dataProvider = "stdOutErrProvider")
    public void testWrite(String name, int fd) throws Exception {
        final LoopHandle loop = new LoopHandle();
        final TTYHandle tty = newTTY(loop, fd, false);
        if (tty == null) return;
        tty.setWriteCallback(new StreamCallback() {
            @Override
            public void call(Object[] args) throws Exception {
                System.out.println(args[0]);
            }
        });
        tty.write("written to " + name + "\n");
        loop.run();
    }

    @Test(dataProvider = "stdInProvider")
    public void testRead(String name, int fd) throws Exception {
        final LoopHandle loop = new LoopHandle();
        final TTYHandle tty = newTTY(loop, fd, true);
        if (tty == null) return;
        final String prompt = "\ntype something (^D to exit) > ";
        tty.setReadCallback(new StreamCallback() {
            @Override
            public void call(Object[] args) throws Exception {
                if (args != null && args.length > 0) {
                    final ByteBuffer buffer = (ByteBuffer) args[0];
                    System.out.print(new String(buffer.array()));
                    System.out.print(prompt);
                } else {
                    System.out.print("\n");
                }
            }
        });
        System.out.print(prompt);
        tty.readStart();
        loop.run();
    }

    public static void main(final String[] args) throws Exception {
        final TTYHandleTest test = new TTYHandleTest();
        final Object[][] ofds = test.stdOutErrProvider();
        for (Object[] fd : ofds) {
            test.testWrite((String) fd[0], (Integer) fd[1]);
            test.testStdOutErrWindowSize((String) fd[0], (Integer) fd[1]);
        }
        final Object[][] ifds = test.stdInProvider();
        for (Object[] fd : ifds) {
            test.testRead((String) fd[0], (Integer) fd[1]);
            test.testStdinWindowSize((String) fd[0], (Integer) fd[1]);
        }
    }
}
