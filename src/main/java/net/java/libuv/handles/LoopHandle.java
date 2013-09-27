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
import net.java.libuv.NativeException;

public final class LoopHandle {

    protected final CallbackExceptionHandler exceptionHandler;
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

    public LoopHandle(final CallbackExceptionHandler exceptionHandler) {
        this.pointer = _new();
        assert pointer != 0;
        assert exceptionHandler != null;
        this.exceptionHandler = exceptionHandler;
    }

    public LoopHandle() {
        this.pointer = _new();
        assert pointer != 0;
        this.exceptionHandler = new CallbackExceptionHandler() {
            public void handle(final Exception ex) {
                if (pendingException == null) {
                    pendingException = ex;
                } else {
                    pendingException.addSuppressed(ex);
                }
            }
        };
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

    long pointer() {
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

    private native NativeException _get_last_error(final long ptr);

}