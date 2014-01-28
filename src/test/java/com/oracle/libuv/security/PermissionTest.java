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

package com.oracle.libuv.security;

import java.io.File;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.LinkPermission;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.PropertyPermission;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.oracle.libuv.Constants;
import com.oracle.libuv.Files;
import com.oracle.libuv.cb.StreamConnectionCallback;
import com.oracle.libuv.FilesTest;
import com.oracle.libuv.LibUV;
import com.oracle.libuv.LibUVPermission;
import com.oracle.libuv.NativeException;
import com.oracle.libuv.Stats;
import com.oracle.libuv.TestBase;
import com.oracle.libuv.handles.LoopHandle;
import com.oracle.libuv.handles.PipeHandle;
import com.oracle.libuv.handles.ProcessHandle;
import com.oracle.libuv.handles.SignalHandle;
import com.oracle.libuv.handles.StdioOptions;
import com.oracle.libuv.handles.TCPHandle;
import com.oracle.libuv.handles.TTYHandle;
import com.oracle.libuv.handles.UDPHandle;
import com.oracle.libuv.handles.UDPHandleTest;
import com.oracle.libuv.runner.TestRunner;

/**
 * Test LibUV permissions. This test doesn't rely on a policy file. Permissions
 * are configured in each test.
 *
 */
public class PermissionTest extends TestBase {

    private static final String OS = System.getProperty("os.name");
    private static final String ADDRESS = "127.0.0.1";
    private static final String ADDRESS6 = "::1";
    private static int p = 49152;

    private static int getPort() {
        return ++p;
    }

    /**
     * In memory policy configuration, no need for a policy file.
     */
    public static class SimplePolicy extends Policy {

        final Permissions permissions;

        public SimplePolicy(final Permissions perms) {
            permissions = new Permissions();
            final Enumeration<Permission> enumeration = perms.elements();
            while (enumeration.hasMoreElements()) {
                permissions.add(enumeration.nextElement());
            }
            // required for calling System.setSecurityManager(null);
            permissions.add(new RuntimePermission("setSecurityManager"));
            // Default java.policy grants listen to localhost
            permissions.add(new SocketPermission("localhost:1024-", "listen"));
            // Tests create multiple loops
            permissions.add(new LibUVPermission("libuv.loop.multi"));
        }

        @Override
        public boolean implies(final ProtectionDomain domain,
                final Permission permission) {
            return permissions.implies(permission);
        }
    }

    @BeforeMethod
    public void before() {
        if (System.getSecurityManager() != null) {
            throw new Error("Security manager is already set");
        }
    }

    @AfterMethod
    public void after() {
        System.setSecurityManager(null);
    }

    @Test
    public void testProcessNoAuth() {

        init(new Permissions());

        testFailure(new Runnable() {
            @Override
            public void run() {
                LibUV.exePath();
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                LibUV.chdir("toto");
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                LibUV.cwd();
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                LibUV.setTitle("toto");
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                LibUV.getTitle();
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                LibUV.kill(999, -9);
            }
        });

        System.out.println("Security process NoAuth test passed");
    }

    @Test
    public void testProcessAuth() {

        final Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.process.*"));
        init(permissions);

        testSuccess(new Runnable() {
            @Override
            public void run() {
                LibUV.exePath();
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                LibUV.cwd();
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                LibUV.setTitle("toto");
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                LibUV.getTitle();
            }
        });

        System.out.println("Security process Auth test passed");
    }

    @Test
    public void testHandlesNoAuth() {
        final LoopHandle lh = new LoopHandle();

        init(new Permissions());

        testFailure(new Runnable() {
            @Override
            public void run() {
                new LoopHandle();
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                new PipeHandle(lh, false);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                new ProcessHandle(lh);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                new UDPHandle(lh);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                new TCPHandle(lh);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                new Files(lh);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                new TTYHandle(lh, 0, true);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                new SignalHandle(lh);
            }
        });

        System.out.println("Security handles NoAuth test passed");
    }

    @Test
    public void testPipeAuth() throws Throwable {
        final String PIPE_NAME;

        if (OS.startsWith("Windows")) {
            PIPE_NAME = "\\\\.\\pipe\\uv-java-test-pipe-auth";
        } else {
            PIPE_NAME = "/tmp/uv-java-test-pipe-auth";
        }
        java.nio.file.Files.deleteIfExists(FileSystems.getDefault().getPath(PIPE_NAME));

        final AtomicInteger serverRecvCount = new AtomicInteger(0);

        final Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.pipe.connect"));
        permissions.add(new LibUVPermission("libuv.pipe.bind"));
        permissions.add(new LibUVPermission("libuv.pipe.open"));
        permissions.add(new LibUVPermission("libuv.pipe.accept"));
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new FilePermission(PIPE_NAME, "delete"));

        init(permissions);

        final LoopHandle lh = new LoopHandle();
        final PipeHandle client = new PipeHandle(lh, false);
        final PipeHandle server = new PipeHandle(lh, false);
        final PipeHandle peer = new PipeHandle(lh, false);
        final PipeHandle server2 = new PipeHandle(lh, false);

        testSuccess(new Runnable() {
            @Override
            public void run() {
                server.bind(PIPE_NAME);
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                server.listen(1);
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                client.connect(PIPE_NAME);
            }
        });

        server.setConnectionCallback(new StreamConnectionCallback() {
            @Override
            public void onConnection(int status, Exception error) throws Exception {
                testSuccess(new Runnable() {
                    @Override
                    public void run() {
                        server.accept(peer);
                    }
                });
                System.out.println("New Pipe connection");
                serverRecvCount.incrementAndGet();
                server.close();
            }
        });

        while (serverRecvCount.get() == 0) {
            lh.runNoWait();
        }

        try {
            java.nio.file.Files.deleteIfExists(FileSystems.getDefault()
                    .getPath(PIPE_NAME));
        } catch (final Exception ignore) {
        }

        Assert.assertEquals(serverRecvCount.get(), 1);

        testSuccess(new Runnable() {
            @Override
            public void run() {
                server2.open(2);
            }
        });

        System.out.println("Security pipe Auth test passed");
    }

    @Test
    public void testChildProcessAuth() {

        final Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new FilePermission("<<ALL FILES>>", "execute"));

        init(permissions);

        final LoopHandle lh = new LoopHandle();
        final ProcessHandle process = new ProcessHandle(lh);

        final String[] args = new String[2];
        args[0] = "java";
        args[1] = "-version";

        final EnumSet<ProcessHandle.ProcessFlags> processFlags = EnumSet.noneOf(ProcessHandle.ProcessFlags.class);
        processFlags.add(ProcessHandle.ProcessFlags.NONE);

        final StdioOptions[] stdio = new StdioOptions[3];
        stdio[0] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 0);
        stdio[1] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 1);
        stdio[2] = new StdioOptions(StdioOptions.StdioType.INHERIT_FD, null, 2);

        testSuccess(new Runnable() {
            @Override
            public void run() {
                process.spawn(args[0], args, null, ".", processFlags, stdio, -1, -1);
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                process.close();
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                process.kill(-9);
            }
        });

        System.out.println("Security child process Auth test passed");
    }

    // @Test // TODO: test hangs on windows
    public void testConnectionAuth() throws Throwable {
        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final int port = getPort();
        final int port2 = getPort();
        final Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new SocketPermission(ADDRESS + ":" + port, "listen"));
        permissions.add(new SocketPermission(ADDRESS + ":" + port, "connect"));
        permissions.add(new SocketPermission(ADDRESS + ":*", "accept"));
        permissions.add(new SocketPermission(ADDRESS + ":" + port2, "listen"));
        permissions.add(new SocketPermission(ADDRESS + ":" + port2, "connect"));

        init(permissions);

        final LoopHandle lh = new LoopHandle();
        final TCPHandle server = new TCPHandle(lh);
        final TCPHandle client = new TCPHandle(lh);
        final TCPHandle peer = new TCPHandle(lh);

        server.setConnectionCallback(new StreamConnectionCallback() {
            @Override
            public void onConnection(int status, Exception error) throws Exception {
                testSuccess(new Runnable() {
                    @Override
                    public void run() {
                        server.accept(peer);
                    }
                });
                server.close();
                System.out.println("New Connection accepted " + peer.getPeerName());
                serverDone.set(true);
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                server.bind(ADDRESS, port);
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                server.listen(1);
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                client.connect(ADDRESS, port);
            }
        });

        while (!serverDone.get()) {
            lh.runNoWait();
            Thread.sleep(100);
        }

        final UDPHandle udpserver = new UDPHandle(lh);
        final UDPHandle udpclient = new UDPHandle(lh);

        testSuccess(new Runnable() {
            @Override
            public void run() {
                udpserver.bind(port2, ADDRESS);
            }
        });
        testSuccess(new Runnable() {
            @Override
            public void run() {
                udpclient.send("toto", port2, ADDRESS);
            }
        });

        System.out.println("Security connection Auth test passed");
    }

    @Test
    public void testConnection6Auth() throws Throwable {
        final LoopHandle lh = new LoopHandle();
        if (!UDPHandleTest.isIPv6Enabled(lh)) {
            return;
        }

        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final int port = getPort();
        final int port2 = getPort();
        final Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:" + port, "listen"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:" + port, "connect"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:*", "accept"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:" + port2, "listen"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:" + port2, "connect"));

        init(permissions);

        final TCPHandle server = new TCPHandle(lh);
        final TCPHandle client = new TCPHandle(lh);
        final TCPHandle peer = new TCPHandle(lh);

        server.setConnectionCallback(new StreamConnectionCallback() {
            @Override
            public void onConnection(int status, Exception error) throws Exception {
                testSuccess(new Runnable() {
                    @Override
                    public void run() {
                        server.accept(peer);
                    }
                });
                server.close();
                System.out.println("New Connection accepted " + peer.getPeerName());
                serverDone.set(true);
            }
        });

        testSuccess(new Runnable() {
            @Override
            public void run() {
                server.bind6(ADDRESS6, port);
            }
        });

        testSuccess(new Runnable() {
            @Override
            public void run() {
                server.listen(1);
            }
        });

        testSuccess(new Runnable() {
            @Override
            public void run() {
                client.connect6(ADDRESS6, port);
            }
        });

        while (!serverDone.get()) {
            lh.runNoWait();
            Thread.sleep(100);
        }

        final UDPHandle udpserver = new UDPHandle(lh);
        final UDPHandle udpclient = new UDPHandle(lh);

        testSuccess(new Runnable() {
            @Override
            public void run() {
                udpserver.bind6(port2, ADDRESS6);
            }
        });

        testSuccess(new Runnable() {
            @Override
            public void run() {
                udpclient.send6("toto", port2, ADDRESS6);
            }
        });

        System.out.println("Security connection IPV6 Auth test passed");
    }

    @Test
    public void testSignalAuth() throws Exception {
        final Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new LibUVPermission("libuv.signal.28"));

        init(permissions);

        final LoopHandle lh = new LoopHandle();
        final SignalHandle sh = new SignalHandle(lh);

        testSuccess(new Runnable() {
            @Override
            public void run() {
                sh.start(28);
            }
        });

        System.out.println("Security signal Auth test passed");
    }

    @Test
    public void testFileAuth() throws Exception {
        final String TMPDIR = TestBase.TMPDIR + File.separator;
        final Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new FilePermission(TMPDIR + "testGetPath.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testOpenWriteReadAndCloseSync.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testOpenWriteReadAndCloseAsync.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testUnlinkSync.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testUnlinkAsync.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testMkdirRmdirSync", "write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testMkdirRmdirAsync", "write, delete"));
        permissions.add(new FilePermission("src", "read"));
        permissions.add(new FilePermission(TMPDIR + "testRenameSync.txt", "read, write"));
        permissions.add(new FilePermission(TMPDIR + "testRenameSync-new.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testRenameAsync.txt", "read, write"));
        permissions.add(new FilePermission(TMPDIR + "testRenameAsync-new.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testFtruncateSync.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testFtruncateAsync.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testLinkSync.txt", "read, write, delete"));
        permissions.add(new LinkPermission("hard"));
        permissions.add(new FilePermission(TMPDIR + "testLinkSync2.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testLinkAsync.txt", "read, write, delete"));
        permissions.add(new FilePermission(TMPDIR + "testLinkAsync2.txt", "read, write, delete"));
        permissions.add(new FilePermission("test-reports/*", "write"));
        permissions.add(new RuntimePermission("setIO", "write"));
        permissions.add(new PropertyPermission("java.io.tmpdir", "read"));
        permissions.add(new FilePermission(TMPDIR, "read"));

        init(permissions);
        TestRunner.runATest(FilesTest.class.getName());

        System.out.println("Security File Auth test passed");
    }

    @Test
    public void testFileReadOnly() throws Exception {
        final Permissions permissions = new Permissions();
        final String fileName = "testFileReadOnly.txt";
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new FilePermission(fileName, "read"));
        permissions.add(new FilePermission(fileName, "delete"));
        final File f = new File(fileName);
        f.createNewFile();

        init(permissions);
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final int fd = handle.open(fileName, Constants.O_RDONLY, Constants.S_IRWXU);

        handle.read(fd, ByteBuffer.allocateDirect(5), 0, 0, 0);
        final Stats s = handle.fstat(fd);
        if (s == null) {
            throw new Exception("Stats is null");
        }

        try {
            handle.fdatasync(fd);
            if (OS.startsWith("Windows")) {
                throw new Exception("fdatasync should have failed");
            }
        } catch (final NativeException ex) {
            // XXX OK.
        }

        try {
            handle.fsync(fd);
            if (OS.startsWith("Windows")) {
                throw new Exception("fsync should have failed");
            }
        } catch (final NativeException ex) {
            // XXX OK.
        }

        try {
            handle.write(fd, ByteBuffer.wrap("Hello".getBytes()), 0, 2, 0);
            throw new Exception("Write should have failed");
        } catch (final AccessControlException ex) {
            // XXX OK.
        }

        try {
            handle.ftruncate(fd, 1);
            throw new Exception("Write should have failed");
        } catch (final AccessControlException ex) {
            // XXX OK.
        }

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.futime(fd, 999, 999999);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.fchmod(fd, Constants.S_IRWXU);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.fchown(fd, 01, 01);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.futime(fd, 999, 99999);
            }
        });

        handle.unlink(fileName);
    }

    @Test
    public void testFileFdRWT() throws Exception {
        final Permissions permissions = new Permissions();
        final String fileName = "testFileFdReadOnly.txt";
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new FilePermission(fileName, "delete"));
        final File f = new File(fileName);
        f.createNewFile();
        // Emulate codebase with the rights to open in R/W
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final int fd = handle.open(fileName, Constants.O_RDWR, Constants.S_IRWXU);

        // Allowed without security
        handle.read(fd, ByteBuffer.allocateDirect(5), 0, 0, 0);
        handle.write(fd, ByteBuffer.wrap("Hello".getBytes()), 0, 2, 0);
        handle.ftruncate(fd, 1);

        init(permissions);

        // At this point, emulate the brute-force retrieval of fd.

        try {
            handle.read(fd, ByteBuffer.allocateDirect(5), 0, 0, 0);
            throw new Exception("Read should have failed");
        } catch (final AccessControlException ex) {
            // XXX OK.
        }

        try {
            handle.write(fd, ByteBuffer.wrap("Hello".getBytes()), 0, 2, 0);
            throw new Exception("Write should have failed");
        } catch (final AccessControlException ex) {
            // XXX OK.
        }

        try {
            handle.ftruncate(fd, 1);
            throw new Exception("Truncate should have failed");
        } catch (final AccessControlException ex) {
            // XXX OK.
        }

        handle.unlink(fileName);
    }

    @Test
    public void testFileNoAuth() throws Exception {
        final Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.handle"));
        init(permissions);
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final String fileName = "testFileNoAuth.txt";
        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.open(fileName, Constants.O_CREAT, Constants.S_IRWXU);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.open(fileName, Constants.O_CREAT, Constants.S_IRWXU, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.open(fileName, Constants.O_RDONLY, Constants.S_IRWXU);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.open(fileName, Constants.O_RDONLY, Constants.S_IRWXU, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.open(fileName, Constants.O_RDWR, Constants.S_IRWXU);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.open(fileName, Constants.O_RDWR, Constants.S_IRWXU, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.open(fileName, Constants.O_WRONLY, Constants.S_IRWXU);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.open(fileName, Constants.O_WRONLY, Constants.S_IRWXU, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.chmod(fileName, Constants.S_IRWXU);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.chmod(fileName, Constants.S_IRWXU, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.chown(fileName, 1, 2);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.chown(fileName, 1, 2, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.readdir(fileName, Constants.O_RDWR);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.readdir(fileName, Constants.O_RDWR, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.link(fileName, fileName);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.link(fileName, fileName, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.symlink(fileName, fileName, Constants.O_RDWR);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.symlink(fileName, fileName, Constants.O_RDWR, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.lstat(fileName);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.lstat(fileName, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.mkdir(fileName, Constants.S_IRWXU);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.mkdir(fileName, Constants.S_IRWXU, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.readlink(fileName);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.readlink(fileName, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.rename(fileName, fileName);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.rename(fileName, fileName, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.rmdir(fileName);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.rmdir(fileName, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.stat(fileName);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.stat(fileName, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.symlink(fileName, fileName, Constants.O_RDWR);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.symlink(fileName, fileName, Constants.O_RDWR, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.unlink(fileName);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.unlink(fileName, 1);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.utime(fileName, 999, 99999);
            }
        });

        testFailure(new Runnable() {
            @Override
            public void run() {
                handle.utime(fileName, 999, 99999, 1);
            }
        });
        System.out.println("Security File No Auth test passed");
    }

    private static void testFailure(final Runnable r) {
        try {
            r.run();
            throw new RuntimeException("Should have failed");
        } catch (final Throwable ex) {
            if (!(ex instanceof AccessControlException)) {
                System.out.println("UNEXPECTED EXCEPTION " + ex);
                ex.printStackTrace();
                throw ex;
            } else {
                System.out.println("Catch expected exception " + ex);
            }
        }
    }

    private static void testSuccess(final Runnable r) {
        try {
            r.run();
        } catch (final Exception ex) {
            if (ex instanceof AccessControlException) {
                System.out.println("UNEXPECTED SECURITY EXCEPTION " + ex);
                ex.printStackTrace();
                throw ex;
            }
        }
    }

    private static void init(final Permissions permissions) {
        Policy.setPolicy(new SimplePolicy(permissions));
        System.setSecurityManager(new SecurityManager());
    }
}
