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

import com.oracle.libuv.LibUVPermission;
import com.oracle.libuv.NativeException;

import com.oracle.libuv.cb.CallbackExceptionHandler;
import com.oracle.libuv.cb.ContextProvider;
import com.oracle.libuv.cb.CallbackHandler;
import com.oracle.libuv.cb.CallbackHandlerFactory;

public final class LoopHandle {

    static {
        NativeException.static_initialize();
        _static_initialize();
    }

    // Track the number of created LoopHandles.
    private static int createdLoopCount = 0;

    protected final CallbackExceptionHandler exceptionHandler;
    protected final CallbackHandlerFactory callbackHandlerFactory;
    protected final ContextProvider contextProvider;
    private final long pointer;
    private Throwable pendingException;

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
            final CallbackHandlerFactory callbackHandler,
            final ContextProvider contextProvider) {
        newLoop();
        this.pointer = _new();
        assert pointer != 0;
        assert exceptionHandler != null;
        this.exceptionHandler = exceptionHandler;
        this.callbackHandlerFactory = callbackHandler;
        this.contextProvider = contextProvider;
    }

    public LoopHandle() {
        newLoop();
        this.pointer = _new();
        assert pointer != 0;

        this.exceptionHandler = new CallbackExceptionHandler() {
            @Override
            public void handle(final Throwable ex) {
                if (pendingException == null) {
                    pendingException = ex;
                } else {
                    pendingException.addSuppressed(ex);
                }
            }
        };

        this.callbackHandlerFactory = new LoopCallbackHandlerFactory(this.exceptionHandler);

        this.contextProvider = new ContextProvider() {
            @Override
            public Object getContext() {
                return null;
            }
        };
    }

    public CallbackHandler getCallbackHandler(final Object context) {
        return callbackHandlerFactory.newCallbackHandler(context);
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandlerFactory.newCallbackHandler();
    }

    public Object getContext() {
        return contextProvider.getContext();
    }

    public CallbackExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public boolean runNoWait() throws Throwable {
        throwPendingException();
        return _run(pointer, RunMode.NOWAIT.value) != 0;
    }

    public boolean runOnce() throws Throwable {
        throwPendingException();
        return _run(pointer, RunMode.ONCE.value) != 0;
    }

    public boolean run() throws Throwable {
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

    private void throwPendingException() throws Throwable {
        if (pendingException != null) {
            final Throwable pex = pendingException;
            pendingException = null;
            throw pex;
        }
    }

    private static native long _new();

    private static native void _static_initialize();

    private native int _run(final long ptr, final int mode);

    private native void _stop(final long ptr);

    private native void _destroy(final long ptr);

    private native void _close_all(final long ptr);

    private native String[] _list(final long ptr);

    private native NativeException _get_last_error(final long ptr);

}
