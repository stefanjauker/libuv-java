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

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.java.libuv.TestBase;
import net.java.libuv.cb.AsyncCallback;

public class AsyncHandleTest extends TestBase {

    @Test
    public void testAsync() throws Exception {
        final AtomicBoolean gotCallback = new AtomicBoolean(false);
        final AtomicInteger times = new AtomicInteger(0);

        final LoopHandle loop = new LoopHandle();
        final AsyncHandle asyncHandle = new AsyncHandle(loop);
        final ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1);

        asyncHandle.setAsyncCallback(new AsyncCallback() {
            @Override
            public void onSend(final int status) throws Exception {
                gotCallback.set(true);
                System.out.println("onSend!");
                times.incrementAndGet();
                asyncHandle.close();
            }
        });

        timer.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("calling asyncHandle.send...");
                asyncHandle.send();
            }
        }, 100, TimeUnit.MILLISECONDS);

        loop.run();

        Assert.assertTrue(gotCallback.get());
        Assert.assertEquals(times.get(), 1);
    }

    public static void main(final String[] args) throws Exception {
        final AsyncHandleTest test = new AsyncHandleTest();
        test.testAsync();
    }

}
