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

package com.oracle.libuv.handles;

import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.oracle.libuv.cb.ProcessCloseCallback;
import com.oracle.libuv.cb.ProcessExitCallback;
import com.oracle.libuv.TestBase;
import com.oracle.libuv.cb.StreamConnectCallback;
import com.oracle.libuv.cb.StreamConnectionCallback;
import com.oracle.libuv.cb.StreamReadCallback;

public class ProcessHandleTest extends TestBase {

    private static final String OS = System.getProperty("os.name");

    @Test
    public void testExitCode() throws Throwable {
        final String MESSAGE = "TEST";
        final String PIPE_NAME;
        if (OS.startsWith("Windows")) {
            PIPE_NAME = "\\\\.\\pipe\\libuv-java-process-handle-test-pipe";
        } else {
            PIPE_NAME = "/tmp/libuv-java-process-handle-test-pipe";
            Files.deleteIfExists(FileSystems.getDefault().getPath(PIPE_NAME));
        }

        final AtomicBoolean exitCalled = new AtomicBoolean(false);
        final AtomicBoolean closeCalled = new AtomicBoolean(false);
        final LoopHandle loop = new LoopHandle();
        final ProcessHandle process = new ProcessHandle(loop);
        final PipeHandle parent = new PipeHandle(loop, false);
        final PipeHandle peer = new PipeHandle(loop, false);
        final PipeHandle child = new PipeHandle(loop, false);

        peer.setReadCallback(new StreamReadCallback() {
            @Override
            public void onRead(final ByteBuffer data) throws Exception {
                final byte[] bytes = data.array();
                final String s = new String(bytes, "utf-8");
                Assert.assertEquals(s, MESSAGE);
                peer.close();
                process.close();
            }
        });

        parent.setConnectionCallback(new StreamConnectionCallback() {
            @Override
            public void onConnection(int status, Exception error) throws Exception {
                parent.accept(peer);
                peer.readStart();
                parent.close();
            }
        });

        child.setConnectCallback(new StreamConnectCallback() {
            @Override
            public void onConnect(int status, Exception error) throws Exception {
                child.write(MESSAGE);
                child.close();
            }
        });

        process.setExitCallback(new ProcessExitCallback() {
            @Override
            public void onExit(final int status, final int signal, final Exception error) throws Exception {
                System.out.println("status " + status + ", signal " + signal);
                child.connect(PIPE_NAME);
                exitCalled.set(true);
            }
        });

        process.setCloseCallback(new ProcessCloseCallback() {
            @Override
            public void onClose() throws Exception {
                closeCalled.set(true);
            }
        });

        final String[] args = new String[2];
        args[0] = "java";
        args[1] = "-version";

        final EnumSet<ProcessHandle.ProcessFlags> processFlags = EnumSet.noneOf(ProcessHandle.ProcessFlags.class);
        processFlags.add(ProcessHandle.ProcessFlags.NONE);

        final StdioOptions[] stdio = new StdioOptions[3];
        stdio[0] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 0);
        stdio[1] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 1);
        stdio[2] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 2);

        parent.bind(PIPE_NAME);
        parent.listen(0);

        process.spawn(args[0], args, null, ".", processFlags, stdio, -1, -1);

        while (!exitCalled.get() && !closeCalled.get()) {
            loop.run();
        }
    }

    public static void main(final String[] args) throws Throwable {
        final ProcessHandleTest test = new ProcessHandleTest();
        test.testExitCode();
    }
}
