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

import java.io.File;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkPermission;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.java.libuv.Constants;

import net.java.libuv.LibUV;
import net.java.libuv.LibUVPermission;
import net.java.libuv.NativeException;
import net.java.libuv.StreamCallback;
import net.java.libuv.handles.FileHandle;
import net.java.libuv.handles.LoopHandle;
import net.java.libuv.handles.PipeHandle;
import net.java.libuv.handles.ProcessHandle;
import net.java.libuv.handles.SignalHandle;
import net.java.libuv.handles.Stats;
import net.java.libuv.handles.StdioOptions;
import net.java.libuv.handles.TCPHandle;
import net.java.libuv.handles.TTYHandle;
import net.java.libuv.handles.UDPHandle;
import org.testng.Assert;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Test LibUV permissions. This test doesn't rely on a policy file. Permissions
 * are configured in each test.
 *
 */
public class PermissionTest {

    private static final String OS = System.getProperty("os.name");
    private static final String ADDRESS = "127.0.0.1";
    private static final String ADDRESS6 = "::1";
    private static int p = 49152;

    static {
        // call a LibUV method just to ensure that the native lib is loaded
        System.out.println(PermissionTest.class.getSimpleName() + " in " + LibUV.cwd());
    }

    private static int getPort() {
        return ++p;
    }

    /**
     * In memory policy configuration, no need for a policy file.
     */
    public static class SimplePolicy extends Policy {

        final Permissions permissions;

        public SimplePolicy(Permissions perms) {
            permissions = new Permissions();
            Enumeration<Permission> enumeration = perms.elements();
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
        public boolean implies(ProtectionDomain domain, Permission permission) {
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
        // testng broken a security manager, would have to grant all testng required
        // permissions. Better to set it back to null. We are in a test....
        // So do unsafe stuff!
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

        Permissions permissions = new Permissions();
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
                new FileHandle(lh);
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
    public void testPipeAuth() throws Exception {
        final String PIPE_NAME;

        if (OS.startsWith("Windows")) {
            PIPE_NAME = "\\\\.\\pipe\\uv-java-test-pipe";
        } else {
            PIPE_NAME = "/tmp/uv-java-test-pipe";
        }
        Files.deleteIfExists(FileSystems.getDefault().getPath(PIPE_NAME));

        final AtomicInteger serverRecvCount = new AtomicInteger(0);

        Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.pipe.connect"));
        permissions.add(new LibUVPermission("libuv.pipe.bind"));
        permissions.add(new LibUVPermission("libuv.pipe.open"));
        permissions.add(new LibUVPermission("libuv.pipe.accept"));
        permissions.add(new LibUVPermission("libuv.handle"));

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

        server.setConnectionCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception {
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
         while(serverRecvCount.get() == 0) {
            lh.runNoWait();
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

        Permissions permissions = new Permissions();
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

    @Test(enabled = false) // TODO: test hangs on windows
    public void testConnectionAuth() throws Exception {
        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final int port = getPort();
        final int port2 = getPort();
        Permissions permissions = new Permissions();
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

        server.setConnectionCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // connection

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
    public void testConnection6Auth() throws Exception {
        final LoopHandle lh = new LoopHandle();
        if (!UDPHandleTest.isIPv6Enabled(lh)) {
            return;
        }

        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final int port = getPort();
        final int port2 = getPort();
        Permissions permissions = new Permissions();
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

        server.setConnectionCallback(new StreamCallback() {
            @Override
            public void call(final Object[] args) throws Exception { // connection

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
            Thread.sleep((long) (100));
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
        Permissions permissions = new Permissions();
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
        Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new FilePermission("testOpenWriteReadAndCloseSync.txt", "write"));
        permissions.add(new FilePermission("testOpenWriteReadAndCloseSync.txt", "read"));
        permissions.add(new FilePermission("testOpenWriteReadAndCloseSync.txt", "delete"));
        permissions.add(new FilePermission("testOpenWriteReadAndCloseAsync.txt", "write"));
        permissions.add(new FilePermission("testOpenWriteReadAndCloseAsync.txt", "read"));
        permissions.add(new FilePermission("testUnlinkSync.txt", "write"));
        permissions.add(new FilePermission("testUnlinkSync.txt", "read"));
        permissions.add(new FilePermission("testUnlinkSync.txt", "delete"));
        permissions.add(new FilePermission("testUnlinkAsync.txt", "write"));
        permissions.add(new FilePermission("testUnlinkAsync.txt", "read"));
        permissions.add(new FilePermission("testUnlinkAsync.txt", "delete"));
        permissions.add(new FilePermission("testMkdirRmdirSync", "write"));
        permissions.add(new FilePermission("testMkdirRmdirSync", "delete"));
        permissions.add(new FilePermission("testMkdirRmdirAsync", "write"));
        permissions.add(new FilePermission("testMkdirRmdirAsync", "delete"));
        permissions.add(new FilePermission("src", "read"));
        permissions.add(new FilePermission("testRenameSync.txt", "write"));
        permissions.add(new FilePermission("testRenameSync.txt", "read"));
        permissions.add(new FilePermission("testRenameSynctestRenameSync.txt", "write"));
        permissions.add(new FilePermission("testRenameSynctestRenameSync.txt", "read"));
        permissions.add(new FilePermission("testRenameSynctestRenameSync.txt", "delete"));
        permissions.add(new FilePermission("testRenameAsync.txt", "write"));
        permissions.add(new FilePermission("testRenameAsync.txt", "read"));
        permissions.add(new FilePermission("testRenameAsynctestRenameAsync.txt", "write"));
        permissions.add(new FilePermission("testFtruncateSync.txt", "write"));
        permissions.add(new FilePermission("testFtruncateSync.txt", "read"));
        permissions.add(new FilePermission("testFtruncateSync.txt", "delete"));
        permissions.add(new FilePermission("testFtruncateAsync.txt", "write"));
        permissions.add(new FilePermission("testFtruncateAsync.txt", "read"));
        permissions.add(new FilePermission("testLinkSync.txt", "write"));
        permissions.add(new FilePermission("testLinkSync.txt", "read"));
        permissions.add(new LinkPermission("hard"));
        permissions.add(new FilePermission("testLinkSync2.txt", "write"));
        permissions.add(new FilePermission("testLinkSync2.txt", "read"));
        permissions.add(new FilePermission("testLinkSync.txt", "delete"));
        permissions.add(new FilePermission("testLinkSync2.txt", "delete"));
        permissions.add(new FilePermission("testLinkAsync.txt", "write"));
        permissions.add(new FilePermission("testLinkAsync.txt", "read"));
        permissions.add(new FilePermission("testLinkAsync2.txt", "write"));
        
        init(permissions);
        FileHandleTest.main(null);

        System.out.println("Security File Auth test passed");
    }
    
    @Test
    public void testFileReaOnly() throws Exception {
        Permissions permissions = new Permissions();
        final String fileName = "testFileReaOnly.txt";
        permissions.add(new LibUVPermission("libuv.handle"));
        permissions.add(new FilePermission(fileName, "read"));
        permissions.add(new FilePermission(fileName, "delete"));
        File f = new File(fileName);
        f.createNewFile();
        
        init(permissions);
        final LoopHandle loop = new LoopHandle();
        final FileHandle handle = new FileHandle(loop);
        final int fd = handle.open(fileName, Constants.O_RDONLY, Constants.S_IRWXU);
        
        handle.read(fd, new byte[5], 0, 0, 0);
        Stats s = handle.fstat(fd);
        if (s == null) {
           throw new Exception("Stats is null"); 
        }
        handle.fdatasync(fd);
        handle.fsync(fd);
        
        try {
            handle.write(fd, "Hello".getBytes(), 0, 2, 0);
            throw new Exception("Write should have failed");
        }catch(NativeException ex){
            // XXX OK.
        }
        
        try {
            handle.ftruncate(fd, 1);
            throw new Exception("Write should have failed");
        }catch(NativeException ex){
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
    public void testFileNoAuth() throws Exception {
        Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.handle"));
        init(permissions);
        final LoopHandle loop = new LoopHandle();
        final FileHandle handle = new FileHandle(loop);
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

    private static void testFailure(Runnable r) {
        try {
            r.run();
            throw new RuntimeException("Should have failed");
        } catch (Throwable ex) {
            if (!(ex instanceof AccessControlException)) {
                System.out.println("UNEXPECTED EXCEPTION " + ex);
                ex.printStackTrace();
                throw ex;
            } else {
                System.out.println("Catch expected exception " + ex);
            }
        }
    }

    private static void testSuccess(Runnable r) {
        try {
            r.run();
        } catch (Exception ex) {
            if (ex instanceof AccessControlException) {
                System.out.println("UNEXPECTED SECURITY EXCEPTION " + ex);
                ex.printStackTrace();
                throw ex;
            }
        }
    }

    private static void init(Permissions permissions) {
        Policy.setPolicy(new SimplePolicy(permissions));
        System.setSecurityManager(new SecurityManager());
    }
}
