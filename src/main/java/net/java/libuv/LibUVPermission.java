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
package net.java.libuv;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.BasicPermission;

import net.java.libuv.handles.Address;

/**
 * Permissions specific to LibUV. 
 * Permission examples: 
 * permission net.java.libuv.LibUVPermission "libuv.process.*"; 
 * permission net.java.libuv.LibUVPermission "libuv.process.chdir";
 * permission net.java.libuv.LibUVPermission "libuv.pipe.*";
 * permission net.java.libuv.LibUVPermission "libuv.signal.9";
 * 
 * - Child process spawning is authorized thanks to SecurityManager.checkExec. 
 * libuv.spawn permission is also required. 
 * - TCP/UDP are authorized thanks to calls to SecurityManager.checkConnect/checkListen/checkAccept
 * libuv.udp or libuv.tcp permission are also required. 
 */
public final class LibUVPermission extends BasicPermission {

    static final long serialVersionUID = 8529091307897434802L;

    public interface AddressResolver {

        public Address resolve();
    }
    private static final String LIBUV = "libuv";
    private static final String PREFIX = LIBUV + ".";
    // process
    private static final String PROCESS = PREFIX + "process.";
    public static final String PROCESS_CHDIR = PROCESS + "chdir";
    public static final String PROCESS_CWD = PROCESS + "cwd";
    public static final String PROCESS_EXE_PATH = PROCESS + "exePath";
    public static final String PROCESS_GET_TITLE = PROCESS + "getTitle";
    public static final String PROCESS_KILL = PROCESS + "kill";
    public static final String PROCESS_SET_TITLE = PROCESS + "setTitle";
    // pipe
    private static final String PIPE = PREFIX + "pipe.";
    public static final String PIPE_BIND = PIPE + "bind";
    public static final String PIPE_CONNECT = PIPE + "connect";
    public static final String PIPE_OPEN = PIPE + "open";
    // tcp
    private static final LibUVPermission TCP = new LibUVPermission(PREFIX + "tcp");
    // udp
    private static final LibUVPermission UDP = new LibUVPermission(PREFIX + "udp");
    // spawn
    private static final LibUVPermission SPAWN = new LibUVPermission(PREFIX + "spawn");
    
    // signal
    public static final String SIGNAL = PREFIX + "signal.";
    
    public LibUVPermission(String name) {
        super(name);
    }

    public static void checkPermission(String name) {
        SecurityManager sm = System.getSecurityManager();
        if (System.getSecurityManager() != null) {
            LibUVPermission perm = new LibUVPermission(name);
            sm.checkPermission(perm);
        }
    }

    public static void checkSpawn(String cmd) {
        SecurityManager sm = System.getSecurityManager();
        if (System.getSecurityManager() != null) {
            sm.checkExec(cmd);
            sm.checkPermission(SPAWN);
        }
    }

    public static void checkBind(String host, int port) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // Side effect is to check permission to resolve host.
            new InetSocketAddress(host, port);
            sm.checkPermission(TCP);
        }
    }

    public static void checkConnect(String host, int port) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkConnect(host, port);
            sm.checkPermission(TCP);
        }
    }

    public static void checkListen(int port) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkListen(port);
            sm.checkPermission(TCP);
        }
    }

    public static void checkAccept(AddressResolver resolver) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Address addr = resolver.resolve();

            sm.checkAccept(addr.getIp(), addr.getPort());
            sm.checkPermission(TCP);
        }
    }

    public static void checkUDPBind(String host, int port) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkBind(host, port);
            sm.checkListen(port);
            sm.checkPermission(UDP);
        }
    }

    public static void checkUDPSend(String host, int port) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                InetAddress addr = InetAddress.getByName(host);
                if (addr.isMulticastAddress()) {
                    sm.checkMulticast(addr);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            sm.checkConnect(host, port);
            sm.checkPermission(UDP);
        }
    }
}
