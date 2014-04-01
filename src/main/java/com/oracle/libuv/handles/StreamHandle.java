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

package com.oracle.libuv.handles;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Objects;

import com.oracle.libuv.cb.StreamCloseCallback;
import com.oracle.libuv.cb.StreamConnectCallback;
import com.oracle.libuv.cb.StreamConnectionCallback;
import com.oracle.libuv.cb.StreamRead2Callback;
import com.oracle.libuv.cb.StreamReadCallback;
import com.oracle.libuv.cb.StreamShutdownCallback;
import com.oracle.libuv.cb.StreamWriteCallback;

public class StreamHandle extends Handle {

    protected boolean closed;
    private boolean readStarted;

    private StreamReadCallback onRead = null;
    private StreamRead2Callback onRead2 = null;
    private StreamWriteCallback onWrite = null;
    private StreamConnectCallback onConnect = null;
    private StreamConnectionCallback onConnection = null;
    private StreamCloseCallback onClose = null;
    private StreamShutdownCallback onShutdown = null;

    static {
        _static_initialize();
    }

    public void setReadCallback(final StreamReadCallback callback) {
        onRead = callback;
    }

    public void setRead2Callback(final StreamRead2Callback callback) {
        onRead2 = callback;
    }

    public void setWriteCallback(final StreamWriteCallback callback) {
        onWrite = callback;
    }

    public void setConnectCallback(final StreamConnectCallback callback) {
        onConnect = callback;
    }

    public void setConnectionCallback(final StreamConnectionCallback callback) {
        onConnection = callback;
    }

    public void setCloseCallback(final StreamCloseCallback callback) {
        onClose = callback;
    }

    public void setShutdownCallback(final StreamShutdownCallback callback) {
        onShutdown = callback;
    }

    public int readStart() {
        int r = 0;
        if (!readStarted) {
            r = _read_start(pointer);
        }
        readStarted = true;
        return r;
    }

    public void readStop() {
        _read_stop(pointer);
        readStarted = false;
    }

    public int write2(final String str, final StreamHandle handle) {
        return _write2(str, handle, null);
    }

    public int write2(final String str, final UDPHandle handle) {
        return _write2(str, handle, null);
    }

    public int write2(final String str, final StreamHandle handle, final Object callback) {
        return _write2(str, handle, callback);
    }

    public int write2(final String str, final UDPHandle handle, final Object callback) {
        return _write2(str, handle, callback);
    }

    private int _write2(final String str, final Handle handle, final Object callback) {
        Objects.requireNonNull(str);
        assert handle != null;
        final byte[] data;
        try {
            data = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e); // "utf-8" is always supported
        }
        return _write2(pointer, ByteBuffer.wrap(data), data, 0, data.length, handle.pointer, callback, loop.getContext());
    }

    public int write(final String str) {
        return write(str, (Object) null);
    }

    public int write(final String str, final Object callback) {
        Objects.requireNonNull(str);
        final byte[] data;
        try {
            data = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e); // "utf-8" is always supported
        }
        return write(ByteBuffer.wrap(data), 0, data.length, callback);
    }

    public int write(final String str, final String encoding) throws UnsupportedEncodingException {
        return write(str, encoding, null);
    }

    public int write(final String str, final String encoding, final Object callback) throws UnsupportedEncodingException {
        Objects.requireNonNull(str);
        final byte[] data = str.getBytes(encoding);
        return write(ByteBuffer.wrap(data), 0, data.length, callback);
    }

    public int write(final ByteBuffer buffer, final int offset, final int length) {
        return write(buffer, offset, length, null);
    }

    public int write(final ByteBuffer buffer, final int offset, final int length, final Object callback) {
        Objects.requireNonNull(buffer);
        return buffer.hasArray() ?
                _write(pointer, buffer, buffer.array(), offset, length, callback, loop.getContext()) :
                _write(pointer, buffer, null, offset, length, callback, loop.getContext());
    }

    public int write(final ByteBuffer buffer, final Object callback) {
        Objects.requireNonNull(buffer);
        return write(buffer, 0, buffer.capacity(), callback);
    }

    public int write(final ByteBuffer buffer) {
        return write(buffer, null);
    }

    public int shutdown(final Object callback) {
        return _shutdown(pointer, callback, loop.getContext());
    }

    public int setBlocking(final boolean blocking) {
        return _set_blocking(pointer, blocking ? 1 : 0);
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
        return _readable(pointer) == 0;
    }

    public boolean isWritable() {
        return _writable(pointer) == 0;
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

    private void callRead(final int status, final Exception error, final ByteBuffer data) {
        if (onRead != null) {
            loop.getCallbackHandler().handleStreamReadCallback(onRead, status, error, data);
        }
    }

    private void callRead2(int status, Exception error, final ByteBuffer data, final long handle, final int type) {
        if (onRead2 != null) {
            loop.getCallbackHandler().handleStreamRead2Callback(onRead2, status, error, data, handle, type);
        }
    }

    private void callWrite(final int status, final Exception error, final Object callback, final Object context) {
        if (onWrite != null) {
            loop.getCallbackHandler(context).handleStreamWriteCallback(onWrite, callback, status, error);
        }
    }

    private void callConnect(final int status, final Exception error, final Object callback, final Object context) {
        if (onConnect != null) {
            loop.getCallbackHandler(context).handleStreamConnectCallback(onConnect, callback, status, error);
        }
    }

    private void callConnection(final int status, final Exception error) {
        if (onConnection != null) {
            loop.getCallbackHandler().handleStreamConnectionCallback(onConnection, status, error);
        }
    }

    private void callClose() {
        if (onClose != null) {
            loop.getCallbackHandler().handleStreamCloseCallback(onClose);
        }
    }

    private void callShutdown(final int status, final Exception error, final Object callback, final Object context) {
        if (onShutdown != null) {
            loop.getCallbackHandler(context).handleStreamShutdownCallback(onShutdown, callback, status, error);
        }
    }

    private static native void _static_initialize();

    private native void _initialize(final long ptr);

    private native int _read_start(final long ptr);

    private native int _read_stop(final long ptr);

    private native int _readable(final long ptr);

    private native int _writable(final long ptr);

    private native int _write(final long ptr,
                              final ByteBuffer buffer,
                              final byte[] data,
                              final int offset,
                              final int length,
                              final Object callback,
                              final Object context);

    private native int _write2(final long ptr,
                               final ByteBuffer buffer,
                               final byte[] data,
                               final int offset,
                               final int length,
                               final long handlePointer,
                               final Object callback,
                               final Object context);

    private native long _write_queue_size(final long ptr);

    private native void _close(final long ptr);

    private native int _shutdown(final long ptr, final Object callback, final Object context);

    private native int _listen(final long ptr, final int backlog);

    private native int _accept(final long ptr, final long client);

    private native int _set_blocking(final long ptr, final int blocking);
}
