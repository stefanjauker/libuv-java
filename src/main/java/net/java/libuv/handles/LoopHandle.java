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

import net.java.libuv.LibUVPermission;
import net.java.libuv.NativeException;
import net.java.libuv.Stats;

import net.java.libuv.cb.CallbackExceptionHandler;
import net.java.libuv.cb.CallbackHandler;
import net.java.libuv.cb.CheckCallback;
import net.java.libuv.cb.FileCallback;
import net.java.libuv.cb.FileEventCallback;
import net.java.libuv.cb.FilePollCallback;
import net.java.libuv.cb.FilePollStopCallback;
import net.java.libuv.cb.IdleCallback;
import net.java.libuv.cb.ProcessCallback;
import net.java.libuv.cb.SignalCallback;
import net.java.libuv.cb.StreamCallback;
import net.java.libuv.cb.StreamRead2Callback;
import net.java.libuv.cb.StreamReadCallback;
import net.java.libuv.cb.StreamWriteCallback;
import net.java.libuv.cb.TimerCallback;
import net.java.libuv.cb.UDPCallback;

public final class LoopHandle {

    // Track the number of created LoopHandles.
    private static int createdLoopCount = 0;

    protected final CallbackExceptionHandler exceptionHandler;
    protected final CallbackHandler callbackHandler;
    private final long pointer;
    private Exception pendingException;

    private enum RunMode {

        // must be equal to uv_run_mode values in uv.h
        DEFAULT(0),
        ONCE(1),
        NOWAIT(2);

        final int value;

        RunMode(final int value) {
            this.value = value;
        }
    }

    private static synchronized void newLoop() {
        LibUVPermission.checkHandle();
        createdLoopCount += 1;
        LibUVPermission.checkNewLoop(createdLoopCount);
    }

    public LoopHandle(final CallbackExceptionHandler exceptionHandler,
            final CallbackHandler callbackHandler) {
        newLoop();
        this.pointer = _new();
        assert pointer != 0;
        assert exceptionHandler != null;
        this.exceptionHandler = exceptionHandler;
        this.callbackHandler = callbackHandler;
    }

    public LoopHandle() {
        newLoop();
        this.pointer = _new();
        assert pointer != 0;

        this.exceptionHandler = new CallbackExceptionHandler() {
            @Override
            public void handle(final Exception ex) {
                if (pendingException == null) {
                    pendingException = ex;
                } else {
                    pendingException.addSuppressed(ex);
                }
            }
        };

        this.callbackHandler = new CallbackHandler() {
            @Override
            public void handleCheckCallback(final CheckCallback cb, final int status) {
                try {
                    cb.call(status);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleProcessCallback(final ProcessCallback cb, final Object[] args) {
                try {
                    cb.call(args);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleSignalCallback(final SignalCallback cb, final int signum) {
                try {
                    cb.call(signum);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleStreamCallback(final StreamCallback cb, final Object[] args) {
                try {
                    cb.call(args);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleStreamReadCallback(final StreamReadCallback cb, final ByteBuffer data) {
                try {
                    cb.onRead(data);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleStreamRead2Callback(final StreamRead2Callback cb, final ByteBuffer data, final long handle, final int type) {
                try {
                    cb.onRead2(data, handle, type);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleStreamWriteCallback(final StreamWriteCallback cb, final int status, final Exception error) {
                try {
                    cb.onWrite(status, error);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleFileCallback(final FileCallback cb, final int id, final Object[] args) {
                try {
                    cb.call(id, args);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleFileEventCallback(final FileEventCallback cb, final int status, final String event, final String filename) {
                try {
                    cb.call(status, event, filename);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleFilePollCallback(FilePollCallback cb, int status, Stats previous, Stats current) {
                try {
                    cb.onPoll(status, previous, current);
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleFilePollStopCallback(FilePollStopCallback cb) {
                try {
                    cb.onStop();
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleTimerCallback(final TimerCallback cb, final int status) {
                try {
                    cb.call(status);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleUDPCallback(final UDPCallback cb, final Object[] args) {
                try {
                    cb.call(args);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleIdleCallback(final IdleCallback cb, final int status) {
                try {
                    cb.call(status);
                } catch (final Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }
        };
    }

    public CallbackExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public boolean runNoWait() throws Exception {
        throwPendingException();
        return _run(pointer, RunMode.NOWAIT.value) != 0;
    }

    public boolean runOnce() throws Exception {
        throwPendingException();
        return _run(pointer, RunMode.ONCE.value) != 0;
    }

    public boolean run() throws Exception {
        throwPendingException();
        return _run(pointer, RunMode.DEFAULT.value) != 0;
    }

    public void stop() {
        _stop(pointer);
    }

    public void destroy() {
        _destroy(pointer);
    }

    public void closeAll() {
        _close_all(pointer);
    }

    public NativeException getLastError() {
        return _get_last_error(pointer);
    }

    public String[] list() {
        return _list(pointer);
    }

    public long pointer() {
        return pointer;
    }

    private void throwPendingException() throws Exception {
        if (pendingException != null) {
            final Exception pex = pendingException;
            pendingException = null;
            throw pex;
        }
    }

    private static native long _new();

    private native int _run(final long ptr, final int mode);

    private native void _stop(final long ptr);

    private native void _destroy(final long ptr);

    private native void _close_all(final long ptr);

    private native String[] _list(final long ptr);

    private native NativeException _get_last_error(final long ptr);

}
