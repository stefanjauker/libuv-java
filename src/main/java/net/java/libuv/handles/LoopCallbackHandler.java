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

import net.java.libuv.Address;
import net.java.libuv.Stats;
import net.java.libuv.cb.CallbackHandler;
import net.java.libuv.cb.CheckCallback;
import net.java.libuv.cb.FileCallback;
import net.java.libuv.cb.FileEventCallback;
import net.java.libuv.cb.FilePollCallback;
import net.java.libuv.cb.FilePollStopCallback;
import net.java.libuv.cb.FileReadCallback;
import net.java.libuv.cb.FileWriteCallback;
import net.java.libuv.cb.IdleCallback;
import net.java.libuv.cb.ProcessCallback;
import net.java.libuv.cb.SignalCallback;
import net.java.libuv.cb.StreamCallback;
import net.java.libuv.cb.StreamRead2Callback;
import net.java.libuv.cb.StreamReadCallback;
import net.java.libuv.cb.StreamWriteCallback;
import net.java.libuv.cb.TimerCallback;
import net.java.libuv.cb.UDPCloseCallback;
import net.java.libuv.cb.UDPRecvCallback;
import net.java.libuv.cb.UDPSendCallback;

final class LoopCallbackHandler implements CallbackHandler {

    private final LoopHandle loopHandle;

    public LoopCallbackHandler(final LoopHandle loopHandle) {
        this.loopHandle = loopHandle;
    }

    @Override
    public void handleCheckCallback(final CheckCallback cb, final int status) {
        try {
            cb.call(status);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleProcessCallback(final ProcessCallback cb, final Object[] args) {
        try {
            cb.call(args);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleSignalCallback(final SignalCallback cb, final int signum) {
        try {
            cb.call(signum);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleStreamCallback(final StreamCallback cb, final Object[] args) {
        try {
            cb.call(args);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleStreamReadCallback(final StreamReadCallback cb, final ByteBuffer data) {
        try {
            cb.onRead(data);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleStreamRead2Callback(final StreamRead2Callback cb, final ByteBuffer data, final long handle, final int type) {
        try {
            cb.onRead2(data, handle, type);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleStreamWriteCallback(final StreamWriteCallback cb, final int status, final Exception error) {
        try {
            cb.onWrite(status, error);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileCallback(final FileCallback cb, final int id, final Object[] args) {
        try {
            cb.call(id, args);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileReadCallback(final FileReadCallback cb, final int callbackId, final int bytesRead, final byte[] data, final Exception error) {
        try {
            cb.onRead(callbackId, bytesRead, data, error);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileWriteCallback(final FileWriteCallback cb, final int callbackId, final int bytesWritten, final Exception error) {
        try {
            cb.onWrite(callbackId, bytesWritten, error);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileEventCallback(final FileEventCallback cb, final int status, final String event, final String filename) {
        try {
            cb.call(status, event, filename);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFilePollCallback(FilePollCallback cb, int status, Stats previous, Stats current) {
        try {
            cb.onPoll(status, previous, current);
        } catch (Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFilePollStopCallback(FilePollStopCallback cb) {
        try {
            cb.onStop();
        } catch (Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleTimerCallback(final TimerCallback cb, final int status) {
        try {
            cb.call(status);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleUDPRecvCallback(final UDPRecvCallback cb, final int nread, final ByteBuffer data, final Address address) {
        try {
            cb.onRecv(nread, data, address);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleUDPSendCallback(final UDPSendCallback cb, final int status, final Exception error) {
        try {
            cb.onSend(status, error);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleUDPCloseCallback(final UDPCloseCallback cb) {
        try {
            cb.onClose();
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleIdleCallback(final IdleCallback cb, final int status) {
        try {
            cb.call(status);
        } catch (final Exception ex) {
            loopHandle.exceptionHandler.handle(ex);
        }
    }
}
