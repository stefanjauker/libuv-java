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

import net.java.libuv.StreamCallback;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class StreamHandle extends Handle {

    protected boolean closed;
    private boolean readStarted;

    private StreamCallback onRead = null;
    private StreamCallback onWrite = null;
    private StreamCallback onConnect = null;
    private StreamCallback onConnection = null;
    private StreamCallback onClose = null;
    private StreamCallback onShutdown = null;

    static {
        _static_initialize();
    }

    public void setReadCallback(final StreamCallback callback) {
        onRead = callback;
    }

    public void setWriteCallback(final StreamCallback callback) {
        onWrite = callback;
    }

    public void setConnectCallback(final StreamCallback callback) {
        onConnect = callback;
    }

    public void setConnectionCallback(final StreamCallback callback) {
        onConnection = callback;
    }

    public void setCloseCallback(final StreamCallback callback) {
        onClose = callback;
    }

    public void setShutdownCallback(final StreamCallback callback) {
        onShutdown = callback;
    }

    public void readStart() {
        if (!readStarted) {
            _read_start(pointer);
        }
        readStarted = true;
    }

    public void read2Start() {
        if (!readStarted) {
            _read2_start(pointer);
        }
        readStarted = true;
    }

    public void readStop() {
        _read_stop(pointer);
        readStarted = false;
    }

    public int write2(final String str, final StreamHandle handle) {
        assert handle != null;
        final byte[] data;
        try {
            data = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e); // "utf-8" is always supported
        }
        return _write2(pointer, data, 0, data.length, handle.pointer);
    }

    public int write(final String str) {
        final byte[] data;
        try {
            data = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e); // "utf-8" is always supported
        }
        return write(data, 0, data.length);
    }

    public int write(final String str, final String encoding) throws UnsupportedEncodingException {
        final byte[] data = str.getBytes(encoding);
        return write(data, 0, data.length);
    }

    public int write(final ByteBuffer data) {
        return write(data.array(), data.position(), data.remaining());
    }

    public int write(final byte[] data, final int offset, final int length) {
        return _write(pointer, data, offset, length);
    }

    public int write(final byte[] data) {
        return write(data, 0, data.length);
    }

    public int closeWrite() {
        return _close_write(pointer);
    }

    public void close() {
        if (!closed) {
            _close(pointer);
        }
        closed = true;
    }

    public int listen(final int backlog) {
        return _listen(pointer, backlog);
    }

    public int accept(final StreamHandle client) {
        return _accept(pointer, client.pointer);
    }

    public boolean isReadable() {
        return _readable(pointer);
    }

    public boolean isWritable() {
        return _writable(pointer);
    }

    public long writeQueueSize() {
        return _write_queue_size(pointer);
    }

    protected StreamHandle(final long pointer, final LoopHandle loop) {
        super(pointer, loop);
        this.closed = false;
        this.readStarted = false;
        _initialize(pointer);
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
            case 1: if (onRead != null) {call(onRead, args);} break;
            case 2: if (onWrite != null) {call(onWrite, args);} break;
            case 3: if (onConnect != null) {call(onConnect, args);} break;
            case 4: if (onConnection != null) {call(onConnection, args);} break;
            case 5: if (onClose != null) {call(onClose, args);} break;
            case 6: if (onShutdown != null) {call(onShutdown, args);} break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void call(final StreamCallback callback, final Object... args) {
        try {
            callback.call(args);
        } catch (final Exception ex) {
            loop.exceptionHandler.handle(ex);
        }
    }

    private static native void _static_initialize();

    private native void _initialize(final long ptr);

    private native void _read_start(final long ptr);

    private native void _read2_start(final long ptr);

    private native void _read_stop(final long ptr);

    private native boolean _readable(final long ptr);

    private native boolean _writable(final long ptr);

    private native int _write(final long ptr,
                              final byte[] data,
                              final int offset,
                              final int length);

    private native int _write2(final long ptr,
                               final byte[] data,
                               final int offset,
                               final int length,
                               final long handlePointer);

    private native long _write_queue_size(final long ptr);

    private native void _close(final long ptr);

    private native int _close_write(final long ptr);

    private native int _listen(final long ptr, final int backlog);

    private native int _accept(final long ptr, final long client);

}