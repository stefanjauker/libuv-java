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

import net.java.libuv.CallbackExceptionHandler;
import net.java.libuv.CallbackHandler;
import net.java.libuv.CheckCallback;
import net.java.libuv.FileCallback;
import net.java.libuv.IdleCallback;
import net.java.libuv.LibUVPermission;
import net.java.libuv.NativeException;
import net.java.libuv.ProcessCallback;
import net.java.libuv.SignalCallback;
import net.java.libuv.StreamCallback;
import net.java.libuv.TimerCallback;
import net.java.libuv.UDPCallback;

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
            public void handleCheckCallback(CheckCallback cb, int status) {
                try {
                    cb.call(status);
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleProcessCallback(ProcessCallback cb, Object[] args) {
                try {
                    cb.call(args);
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleSignalCallback(SignalCallback cb, int signum) {
                try {
                    cb.call(signum);
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleStreamCallback(StreamCallback cb, Object[] args) {
                try {
                    cb.call(args);
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleFileCallback(FileCallback cb, int id, Object[] args) {
                try {
                    cb.call(id, args);
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleTimerCallback(TimerCallback cb, int status) {
                try {
                    cb.call(status);
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleUDPCallback(UDPCallback cb, Object[] args) {
                try {
                    cb.call(args);
                } catch (Exception ex) {
                    exceptionHandler.handle(ex);
                }
            }

            @Override
            public void handleIdleCallback(IdleCallback cb, int status) {
                try {
                    cb.call(status);
                } catch (Exception ex) {
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
