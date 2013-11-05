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
package net.java.libuv.handles;

import net.java.libuv.Constants;
import net.java.libuv.Files;
import net.java.libuv.Stats;
import net.java.libuv.TestBase;
import net.java.libuv.cb.FileCallback;
import net.java.libuv.cb.FilePollCallback;
import net.java.libuv.cb.FilePollStopCallback;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FilePollHandleTest extends TestBase {

    private String testName;

    @BeforeMethod
    public void startSession(final Method method) throws Exception {
        testName = (TestBase.TMPDIR.endsWith(File.separator) ? TestBase.TMPDIR : TestBase.TMPDIR + File.separator) + method.getName();
    }

    @Test
    public void testFilePoll() throws Exception {
        final AtomicBoolean gotCallback = new AtomicBoolean(false);
        final AtomicBoolean gotStop = new AtomicBoolean(false);
        final AtomicInteger times = new AtomicInteger(0);

        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final FilePollHandle pollHandle = new FilePollHandle(loop);

        handle.setOpenCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                int fd = (Integer)args[0];
                handle.ftruncate(fd, 1000);
                handle.close(fd);
            }
        });

        pollHandle.setStopCallback(new FilePollStopCallback() {
            @Override
            public void onStop() throws Exception {
                System.out.println("poll stop");
                handle.unlink(testName);
                gotStop.set(true);
            }
        });

        pollHandle.setFilePollCallback(new FilePollCallback() {
            @Override
            public void onPoll(int status, Stats prev, Stats curr) throws Exception {
                Assert.assertEquals(status, 0);
                Assert.assertEquals(prev.getSize(), 0);
                Assert.assertEquals(curr.getSize(), 1000);
                gotCallback.set(true);
                System.out.println("poll");
                times.incrementAndGet();
                pollHandle.close();
            }
        });

        final int fd = handle.open(testName, Constants.O_WRONLY | Constants.O_CREAT, Constants.S_IRWXU);
        handle.close(fd);

        pollHandle.start(testName, true, 1);

        handle.open(testName, Constants.O_WRONLY | Constants.O_CREAT, Constants.S_IRWXU, this.hashCode());

        final long start = System.currentTimeMillis();
        while (!gotCallback.get() || !gotStop.get()) {
            if (System.currentTimeMillis() - start > TIMEOUT) {
                Assert.fail("timeout waiting for file poll");
            }
            loop.runNoWait();
        }

        Assert.assertTrue(gotCallback.get());
        Assert.assertTrue(gotStop.get());
        Assert.assertEquals(times.get(), 1);
    }

    public static void main(final String[] args) throws Exception {
        final FilePollHandleTest test = new FilePollHandleTest();
        test.testFilePoll();
    }
}

