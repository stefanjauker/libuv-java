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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.java.libuv.LibUV;
import net.java.libuv.UDPCallback;
import net.java.libuv.handles.LoopHandle;
import net.java.libuv.handles.UDPHandle;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UDPHandleTest {

    static {
        // call a LibUV method just to ensure that the native lib is loaded
        System.out.println(UDPHandleTest.class.getSimpleName() + " in " + LibUV.cwd());
    }

    private static final String HOST = "127.0.0.1";
    private static final String HOST6 = "::1";
    private static final int PORT = 34567;
    private static final int PORT6 = 45678;
    private static final int TIMES = 10;

    @Test
    public void testConnection() throws Exception {
        @SuppressWarnings("unused")
        final AtomicInteger serverSendCount = new AtomicInteger(0);
        final AtomicInteger clientSendCount = new AtomicInteger(0);

        final AtomicInteger serverRecvCount = new AtomicInteger(0);
        @SuppressWarnings("unused")
        final AtomicInteger clientRecvCount = new AtomicInteger(0);

        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final AtomicBoolean clientDone = new AtomicBoolean(false);

        final LoopHandle loop = new LoopHandle();
        final UDPHandle server = new UDPHandle(loop);
        final UDPHandle client = new UDPHandle(loop);

        final UDPCallback serverLoggingCallback = new LoggingCallback("s: ");
        final UDPCallback clientLoggingCallback = new LoggingCallback("c: ");

        server.setRecvCallback(new UDPCallback() {
            @Override
            public void call(Object[] args) throws Exception {
                serverRecvCount.incrementAndGet();
                serverLoggingCallback.call(args);
            }
        });

        client.setRecvCallback(new UDPCallback() {
            @Override
            public void call(final Object[] args) throws Exception {
                clientLoggingCallback.call(args);
            }
        });

        client.setSendCallback(new UDPCallback() {
            @Override
            public void call(final Object[] args) throws Exception {
                if (clientSendCount.get() < TIMES) {
                    client.send("PING." + clientSendCount.incrementAndGet(), PORT, HOST);
                } else {
                    client.close();
                    clientDone.set(true);
                }
            }
        });

        server.bind(PORT, HOST);
        server.recvStart();

        client.recvStart();
        client.send("INIT." + clientSendCount.incrementAndGet(), PORT, HOST);

        while (!serverDone.get() && !clientDone.get()) {
            loop.runNoWait();
        }

        Assert.assertEquals(clientSendCount.get(), TIMES);
        // 9 or 10 are valid values -
        // we receive 9 messages on a desktop, all 10 on a fast server
        Assert.assertTrue(serverRecvCount.get() >= TIMES - 1);
        Assert.assertTrue(serverRecvCount.get() < TIMES + 1);
    }

    @Test
    public void testConnection6() throws Exception {
        if (!isIPv6Enabled()) {
            return;
        }
        @SuppressWarnings("unused")
        final AtomicInteger serverSendCount = new AtomicInteger(0);
        final AtomicInteger clientSendCount = new AtomicInteger(0);

        final AtomicInteger serverRecvCount = new AtomicInteger(0);
        @SuppressWarnings("unused")
        final AtomicInteger clientRecvCount = new AtomicInteger(0);

        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final AtomicBoolean clientDone = new AtomicBoolean(false);

        final LoopHandle loop = new LoopHandle();
        final UDPHandle server = new UDPHandle(loop);
        final UDPHandle client = new UDPHandle(loop);

        final UDPCallback serverLoggingCallback = new LoggingCallback("s: ");
        final UDPCallback clientLoggingCallback = new LoggingCallback("c: ");

        server.setRecvCallback(new UDPCallback() {
            @Override
            public void call(Object[] args) throws Exception {
                serverRecvCount.incrementAndGet();
                serverLoggingCallback.call(args);
            }
        });

        client.setRecvCallback(new UDPCallback() {
            @Override
            public void call(Object[] args) throws Exception {
                clientLoggingCallback.call(args);
            }
        });

        client.setSendCallback(new UDPCallback() {
            @Override
            public void call(Object[] args) throws Exception {
                if (clientSendCount.get() < TIMES) {
                    client.send("PING." + clientSendCount.incrementAndGet(), PORT6, HOST6);
                } else {
                    client.close();
                    clientDone.set(true);
                }
            }
        });

        server.bind6(PORT6, HOST6);
        server.recvStart();

        client.recvStart();
        client.send6("INIT." + clientSendCount.incrementAndGet(), PORT6, HOST6);

        while (!serverDone.get() && !clientDone.get()) {
            loop.runNoWait();
        }

        Assert.assertEquals(clientSendCount.get(), TIMES);
        // 9 or 10 are valid values -
        // we receive 9 messages on a desktop, all 10 on a fast server
        Assert.assertTrue(serverRecvCount.get() >= TIMES - 1);
        Assert.assertTrue(serverRecvCount.get() < TIMES + 1);
    }

    public static void main(final String[] args) throws Exception {
        final UDPHandleTest test = new UDPHandleTest();
        test.testConnection();
        if (isIPv6Enabled()) {
            test.testConnection6();
        }
    }

    public static boolean isIPv6Enabled() {
        try {
            final UDPHandle socket = new UDPHandle(new LoopHandle());
            socket.recvStart();
            socket.send6("", PORT6, HOST6);
        } catch(net.java.libuv.NativeException e) {
            if ("EAFNOSUPPORT".equals(e.errnoString())) {
                return false;
            }
        }
        return true;
    }
}