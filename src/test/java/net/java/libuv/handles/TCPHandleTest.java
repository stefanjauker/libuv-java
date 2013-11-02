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

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.java.libuv.LoggingCallback;
import net.java.libuv.cb.StreamCallback;
import net.java.libuv.TestBase;
import net.java.libuv.cb.StreamReadCallback;

public class TCPHandleTest extends TestBase {

    private static final String ADDRESS = "127.0.0.1";
    private static final String ADDRESS6 = "::1";
    private static final int PORT = 23456;
    private static final int PORT6 = 34567;
    private static final int TIMES = 10;

    @Test
    public void testConnection() throws Exception {
        final AtomicInteger serverSendCount = new AtomicInteger(0);
        final AtomicInteger clientSendCount = new AtomicInteger(0);

        final AtomicInteger serverRecvCount = new AtomicInteger(0);
        final AtomicInteger clientRecvCount = new AtomicInteger(0);

        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final AtomicBoolean clientDone = new AtomicBoolean(false);

        final LoopHandle loop = new LoopHandle();
        final TCPHandle server = new TCPHandle(loop);
        final TCPHandle peer = new TCPHandle(loop);
        final TCPHandle client = new TCPHandle(loop);

        final StreamCallback serverLoggingCallback = new LoggingCallback("s: ");
        final StreamCallback clientLoggingCallback = new LoggingCallback("c: ");

        final Random random = new Random();

        server.setConnectionCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // connection
                serverLoggingCallback.call(args);
                server.accept(peer);
                peer.readStart();
                System.out.println("s: " + server.getSocketName() + " connected to " + peer.getPeerName());
                peer.write("message " + serverSendCount.getAndIncrement() + " from server");
                server.close(); // not expecting any more connections
            }
        });

        peer.setReadCallback(new StreamReadCallback() {
            @Override
            public void onRead(final ByteBuffer data) throws Exception {
                serverRecvCount.incrementAndGet();
                if (data == null) {
                    peer.close();
                } else {
                    final Object[] args = {data};
                    serverLoggingCallback.call(args);
                    if (serverRecvCount.get() == TIMES) {
                        peer.close();
                    } else {
                        peer.write("message " + serverSendCount.getAndIncrement() + " from server");
                    }
                }
            }
        });

        peer.setCloseCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // close
                serverDone.set(true);
            }
        });

        client.setReadCallback(new StreamReadCallback() {
            @Override
            public void onRead(final ByteBuffer data) throws Exception {
                clientRecvCount.incrementAndGet();
                if (data == null) {
                    client.close();
                } else {
                    final Object[] args = {data};
                    clientLoggingCallback.call(args);
                    if (clientRecvCount.get() == TIMES) {
                        client.close();
                    } else {
                        client.write("message " + clientSendCount.getAndIncrement() + " from client");
                    }
                }
            }
        });

        client.setConnectCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // connect
                clientLoggingCallback.call(args);
                System.out.println("c: " + client.getSocketName() + " connected to " + client.getPeerName());
                client.readStart();
                client.write("message " + clientSendCount.getAndIncrement() + " from client");
            }
        });

        client.setCloseCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // close
                clientDone.set(true);
            }
        });

        server.bind(ADDRESS, PORT);
        server.listen(1);

        Thread.sleep((long) (random.nextDouble() * 100));
        client.connect(ADDRESS, PORT);

        while (!serverDone.get() || !clientDone.get()) {
            loop.run();
        }

        Assert.assertEquals(serverSendCount.get(), TIMES);
        Assert.assertEquals(clientSendCount.get(), TIMES);
        Assert.assertEquals(serverRecvCount.get(), TIMES);
        Assert.assertEquals(clientRecvCount.get(), TIMES);
    }

    @Test
    public void testConnection6() throws Exception {
        final LoopHandle loop = new LoopHandle();
        if (!UDPHandleTest.isIPv6Enabled(loop)) {
            return;
        }

        final AtomicInteger serverSendCount = new AtomicInteger(0);
        final AtomicInteger clientSendCount = new AtomicInteger(0);

        final AtomicInteger serverRecvCount = new AtomicInteger(0);
        final AtomicInteger clientRecvCount = new AtomicInteger(0);

        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final AtomicBoolean clientDone = new AtomicBoolean(false);

        final TCPHandle server = new TCPHandle(loop);
        final TCPHandle peer = new TCPHandle(loop);
        final TCPHandle client = new TCPHandle(loop);

        final StreamCallback serverLoggingCallback = new LoggingCallback("s: ");
        final StreamCallback clientLoggingCallback = new LoggingCallback("c: ");

        final Random random = new Random();

        server.setConnectionCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // connection
                serverLoggingCallback.call(args);
                server.accept(peer);
                peer.readStart();
                System.out.println("s: " + server.getSocketName() + " connected to " + peer.getPeerName());
                peer.write("message " + serverSendCount.getAndIncrement() + " from server");
                server.close(); // not expecting any more connections
            }
        });

        peer.setReadCallback(new StreamReadCallback() {
            @Override
            public void onRead(final ByteBuffer data) throws Exception {
                serverRecvCount.incrementAndGet();
                if (data == null) {
                    peer.close();
                } else {
                    final Object[] args = {data};
                    serverLoggingCallback.call(args);
                    if (serverRecvCount.get() == TIMES) {
                        peer.close();
                    } else {
                        peer.write("message " + serverSendCount.getAndIncrement() + " from server");
                    }
                }
            }
        });

        peer.setCloseCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // close
                serverDone.set(true);
            }
        });

        client.setReadCallback(new StreamReadCallback() {
            @Override
            public void onRead(final ByteBuffer data) throws Exception {
                clientRecvCount.incrementAndGet();
                if (data == null) {
                    client.close();
                } else {
                    final Object[] args = {data};
                    clientLoggingCallback.call(args);
                    if (clientRecvCount.get() == TIMES) {
                        client.close();
                    } else {
                        client.write("message " + clientSendCount.getAndIncrement() + " from client");
                    }
                }
            }
        });

        client.setConnectCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // connect
                clientLoggingCallback.call(args);
                System.out.println("c: " + client.getSocketName() + " connected to " + client.getPeerName());
                client.readStart();
                client.write("message " + clientSendCount.getAndIncrement() + " from client");
            }
        });

        client.setCloseCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // close
                clientDone.set(true);
            }
        });

        server.bind6(ADDRESS6, PORT6);
        server.listen(1);

        Thread.sleep((long) (random.nextDouble() * 100));
        client.connect6(ADDRESS6, PORT6);

        while (!serverDone.get() || !clientDone.get()) {
            loop.run();
        }

        Assert.assertEquals(serverSendCount.get(), TIMES);
        Assert.assertEquals(clientSendCount.get(), TIMES);
        Assert.assertEquals(serverRecvCount.get(), TIMES);
        Assert.assertEquals(clientRecvCount.get(), TIMES);
    }

    public static void main(final String[] args) throws Exception {
        final TCPHandleTest test = new TCPHandleTest();
        test.testConnection();
        test.testConnection6();
    }

}
