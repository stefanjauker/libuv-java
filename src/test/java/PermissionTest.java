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

import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import net.java.libuv.LibUV;
import net.java.libuv.LibUVPermission;
import net.java.libuv.StreamCallback;
import net.java.libuv.handles.LoopHandle;
import net.java.libuv.handles.PipeHandle;
import net.java.libuv.handles.ProcessHandle;
import net.java.libuv.handles.SignalHandle;
import net.java.libuv.handles.StdioOptions;
import net.java.libuv.handles.TCPHandle;
import net.java.libuv.handles.UDPHandle;

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
    public void testPipeNoAuth() {

        init(new Permissions());

        final LoopHandle lh = new LoopHandle();
        final PipeHandle ph = new PipeHandle(lh, false);

        testFailure(new Runnable() {
            @Override
            public void run() {
                ph.bind("toto");
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                ph.connect("toto");
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                ph.open(2);
            }
        });

        System.out.println("Security pipe NoAuth test passed");
    }

    @Test
    public void testPipeAuth() {
        final String PIPE_NAME;

        if (OS.startsWith("Windows")) {
            PIPE_NAME = "\\\\.\\pipe\\uv-java-test-pipe";
        } else {
            PIPE_NAME = "/tmp/uv-java-test-pipe";
        }

        Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.pipe.*"));
        permissions.add(new LibUVPermission("libuv.stream.*"));

        init(permissions);

        final LoopHandle lh = new LoopHandle();
        final PipeHandle client = new PipeHandle(lh, false);
        final PipeHandle server = new PipeHandle(lh, false);
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

        testSuccess(new Runnable() {
            @Override
            public void run() {
                server2.open(2);
            }
        });

        System.out.println("Security pipe Auth test passed");
    }

    @Test
    public void testChildProcessNoAuth() {

        init(new Permissions());

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


        testFailure(new Runnable() {
            @Override
            public void run() {
                process.spawn(args[0], args, null, ".", processFlags, stdio, -1, -1);
            }
        });

        System.out.println("Security child process NoAuth test passed");
    }

    @Test
    public void testChildProcessAuth() {

        Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.spawn"));
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

    @Test
    public void testConnectionNoAuth() {

        init(new Permissions());

        final LoopHandle lh = new LoopHandle();
        final TCPHandle server = new TCPHandle(lh);
        final TCPHandle client = new TCPHandle(lh);

        final int port = getPort();

        testFailure(new Runnable() {
            @Override
            public void run() {
                server.bind(ADDRESS, port);
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                server.listen(1);
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                client.connect(ADDRESS, port);
            }
        });

        final TCPHandle server2 = new TCPHandle(lh);
        testFailure(new Runnable() {
            @Override
            public void run() {
                server2.bind("toto", port);
            }
        });

        final UDPHandle udpserver = new UDPHandle(lh);
        final UDPHandle udpclient = new UDPHandle(lh);
        final int port2 = getPort();

        testFailure(new Runnable() {
            @Override
            public void run() {
                udpserver.bind(port2, ADDRESS);
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                udpclient.send("toto", port2, ADDRESS);
            }
        });

        System.out.println("Security connection NoAuth test passed");
    }

    @Test
    public void testConnection6NoAuth() {

        init(new Permissions());

        final LoopHandle lh = new LoopHandle();
        final TCPHandle server = new TCPHandle(lh);
        final TCPHandle client = new TCPHandle(lh);

        final int port = getPort();
        testFailure(new Runnable() {
            @Override
            public void run() {
                server.bind6(ADDRESS6, port);
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                server.listen(1);
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                client.connect6(ADDRESS6, port);
            }
        });

        final TCPHandle server2 = new TCPHandle(lh);
        testFailure(new Runnable() {
            @Override
            public void run() {
                server2.bind6("toto", port);
            }
        });

        final UDPHandle udpserver = new UDPHandle(lh);
        final UDPHandle udpclient = new UDPHandle(lh);
        final int port2 = getPort();
        testFailure(new Runnable() {
            @Override
            public void run() {
                udpserver.bind6(port2, ADDRESS6);
            }
        });
        testFailure(new Runnable() {
            @Override
            public void run() {
                udpclient.send6("toto", port2, ADDRESS6);
            }
        });

        System.out.println("Security connection IPV6 NoAuth test passed");
    }

    @Test
    public void testConnectionAuth() throws Exception {
        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final int port = getPort();
        final int port2 = getPort();
        Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.tcp"));
        permissions.add(new LibUVPermission("libuv.udp"));
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
        final AtomicBoolean serverDone = new AtomicBoolean(false);
        final int port = getPort();
        final int port2 = getPort();
        Permissions permissions = new Permissions();
        permissions.add(new LibUVPermission("libuv.tcp"));
        permissions.add(new LibUVPermission("libuv.udp"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:" + port, "listen"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:" + port, "connect"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:*", "accept"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:" + port2, "listen"));
        permissions.add(new SocketPermission("[" + ADDRESS6 + "]:" + port2, "connect"));

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
    public void testSignalNoAuth() throws Exception {
        
        init(new Permissions());

        final LoopHandle lh = new LoopHandle();
        final SignalHandle sh = new SignalHandle(lh);
        
        testFailure(new Runnable() {
            @Override
            public void run() {
                sh.start(28);
            }
        });
        
        System.out.println("Security signal No Auth test passed");
    }
    
    @Test
    public void testSignalAuth() throws Exception {
        Permissions permissions = new Permissions();
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
