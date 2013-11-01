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

import java.io.UnsupportedEncodingException;

import net.java.libuv.Address;
import net.java.libuv.LibUVPermission;
import net.java.libuv.cb.UDPCallback;

public final class UDPHandle extends Handle {

    private boolean closed;

    private UDPCallback onRecv = null;
    private UDPCallback onSend = null;
    private UDPCallback onClose = null;

    public enum Membership {
        // must be equal to uv_membership values in uv.h
        LEAVE_GROUP(0),
        JOIN_GROUP(1);

        final int value;

        Membership(final int value) {
            this.value = value;
        }
    }

    static {
        _static_initialize();
    }

    public void setRecvCallback(final UDPCallback callback) {
        onRecv = callback;
    }

    public void setSendCallback(final UDPCallback callback) {
        onSend = callback;
    }

    public void setCloseCallback(final UDPCallback callback) {
        onClose = callback;
    }

    public UDPHandle(final LoopHandle loop) {
        super(_new(loop.pointer()), loop);
        this.closed = false;
        _initialize(pointer);
    }

    public void close() {
        if (!closed) {
            _close(pointer);
        }
        closed = true;
    }

    public Address address() {
        return _address(pointer);
    }

    public int bind(final int port, final String address) {
        LibUVPermission.checkUDPBind(address, port);
        return _bind(pointer, port, address);
    }

    public int bind6(final int port, final String address) {
        LibUVPermission.checkUDPBind(address, port);
        return _bind6(pointer, port, address);
    }

    public int send(final String str,
                    final int port,
                    final String host) {
        final byte[] data;
        try {
            data = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e); // "utf-8" is always supported
        }
        return send(data, 0, data.length, port, host);
    }

    public int send6(final String str,
                     final int port,
                     final String host) {
        final byte[] data;
        try {
            data = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e); // "utf-8" is always supported
        }
        return send6(data, 0, data.length, port, host);
    }

    public int send(final String str,
                    final String encoding,
                    final int port,
                    final String host) throws UnsupportedEncodingException {
        final byte[] data = str.getBytes(encoding);
        return send(data, 0, data.length, port, host);
    }

    public int send6(final String str,
                     final String encoding,
                     final int port,
                     final String host) throws UnsupportedEncodingException {
        final byte[] data = str.getBytes(encoding);
        return send6(data, 0, data.length, port, host);
    }

    public int send(final byte[] data,
                    final int port,
                    final String host) {
        LibUVPermission.checkUDPSend(host, port);
        return _send(pointer, data, 0, data.length, port, host);
    }

    public int send6(final byte[] data,
                     final int port,
                     final String host) {
        LibUVPermission.checkUDPSend(host, port);
        return _send6(pointer, data, 0, data.length, port, host);
    }

    public int send(final byte[] data,
                    final int offset,
                    final int length,
                    final int port,
                    final String host) {
        LibUVPermission.checkUDPSend(host, port);
        return _send(pointer, data, offset, length, port, host);
    }

    public int send6(final byte[] data,
                     final int offset,
                     final int length,
                     final int port,
                     final String host) {
        LibUVPermission.checkUDPSend(host, port);
        return _send6(pointer, data, offset, length, port, host);
    }

    public int recvStart() {
        return _recv_start(pointer);
    }

    public int recvStop() {
        return _recv_stop(pointer);
    }

    public int setTTL(final int ttl) {
        return _set_ttl(pointer, ttl);
    }

    public int setMembership(final String multicastAddress,
                             final String interfaceAddress,
                             final Membership membership) {
        return _set_membership(pointer, multicastAddress, interfaceAddress, membership.value);
    }

    public int setMulticastLoop(final boolean on) {
        return _set_multicast_loop(pointer, on ? 1 : 0);
    }

    public int setMulticastTTL(final int ttl) {
        return _set_multicast_ttl(pointer, ttl);
    }

    public int setBroadcast(final boolean on) {
        return _set_broadcast(pointer, on ? 1 : 0);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    private void callback(final int type, final Object arg) {
        final Object[] args = {arg};
        callback(type, args);
    }

    private void callback(final int type, final Object... args) {
        switch (type) {
            case 1: if (onRecv != null) {call(onRecv, args);} break;
            case 2: if (onSend != null) {call(onSend, args);} break;
            case 3: if (onClose != null) {call(onClose, args);} break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void call(final UDPCallback callback, final Object... args) {
       loop.callbackHandler.handleUDPCallback(callback, args);
    }

    private static native long _new(final long loop);

    private static native void _static_initialize();

    private native void _initialize(final long ptr);

    private native Address _address(final long ptr);

    private native int _bind(final long ptr,
                             final int port,
                             final String host);

    private native int _bind6(final long ptr,
                              final int port,
                              final String host);

    private native int _send(final long ptr,
                             final byte[] data,
                             final int offset,
                             final int length,
                             final int port,
                             final String host);

    private native int _send6(final long ptr,
                              final byte[] data,
                              final int offset,
                              final int length,
                              final int port,
                              final String host);

    private native int _recv_start(final long ptr);

    private native int _recv_stop(final long ptr);

    private native int _set_ttl(long ptr,
                                int ttl);

    private native int _set_membership(final long ptr,
                                       final String multicastAddress,
                                       final String interfaceAddress,
                                       final int membership);

    private native int _set_multicast_loop(long ptr,
                                           int on);

    private native int _set_multicast_ttl(long ptr,
                                          int ttl);

    private native int _set_broadcast(long ptr,
                                      int on);

    private native void _close(final long ptr);

}
