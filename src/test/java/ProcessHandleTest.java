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
import net.java.libuv.ProcessCallback;
import net.java.libuv.handles.LoopHandle;
import net.java.libuv.handles.ProcessHandle;
import net.java.libuv.handles.StdioOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessHandleTest {

    static {
        // call a LibUV method just to ensure that the native lib is loaded
        System.out.println(ProcessHandleTest.class.getSimpleName() + " in " + LibUV.cwd());
    }

    @Test
    public void testExitCode() throws Exception {
        final AtomicBoolean exitCalled = new AtomicBoolean(false);
        final AtomicBoolean closeCalled = new AtomicBoolean(false);
        final LoopHandle loop = new LoopHandle();
        final ProcessHandle process = new ProcessHandle(loop);

        process.setExitCallback(new ProcessCallback() {
            @Override
            public void call(Object[] args) throws Exception {
                System.out.println("status " + args[0] + ", signal " + args[1]);
                exitCalled.set(true);
            }
        });

        process.setCloseCallback(new ProcessCallback() {
            @Override
            public void call(Object[] args) throws Exception {
                closeCalled.set(true);
            }
        });

        String[] args = new String[2];
        args[0] = "java";
        args[1] = "-version";

        EnumSet<ProcessHandle.ProcessFlags> processFlags = EnumSet.noneOf(ProcessHandle.ProcessFlags.class);
        processFlags.add(ProcessHandle.ProcessFlags.NONE);

        StdioOptions[] stdio = new StdioOptions[3];
        stdio[0] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 0);
        stdio[1] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 1);
        stdio[2] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 2);

        process.spawn(args[0], args, null, ".", processFlags, stdio, -1, -1);
        loop.run();
        Assert.assertTrue(exitCalled.get());

        process.close();
        loop.run();
        Assert.assertTrue(closeCalled.get());
    }

    public static void main(final String[] args) throws Exception {
        final ProcessHandleTest test = new ProcessHandleTest();
        test.testExitCode();
    }
}
