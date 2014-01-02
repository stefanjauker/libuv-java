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

import java.nio.ByteBuffer;

import com.oracle.libuv.Address;
import com.oracle.libuv.Stats;
import com.oracle.libuv.cb.AsyncCallback;
import com.oracle.libuv.cb.CallbackExceptionHandler;
import com.oracle.libuv.cb.CallbackHandler;
import com.oracle.libuv.cb.CheckCallback;
import com.oracle.libuv.cb.FileCallback;
import com.oracle.libuv.cb.FileCloseCallback;
import com.oracle.libuv.cb.FileEventCallback;
import com.oracle.libuv.cb.FileOpenCallback;
import com.oracle.libuv.cb.FilePollCallback;
import com.oracle.libuv.cb.FilePollStopCallback;
import com.oracle.libuv.cb.FileReadCallback;
import com.oracle.libuv.cb.FileReadDirCallback;
import com.oracle.libuv.cb.FileReadLinkCallback;
import com.oracle.libuv.cb.FileStatsCallback;
import com.oracle.libuv.cb.FileUTimeCallback;
import com.oracle.libuv.cb.FileWriteCallback;
import com.oracle.libuv.cb.IdleCallback;
import com.oracle.libuv.cb.ProcessCloseCallback;
import com.oracle.libuv.cb.ProcessExitCallback;
import com.oracle.libuv.cb.SignalCallback;
import com.oracle.libuv.cb.StreamCloseCallback;
import com.oracle.libuv.cb.StreamConnectCallback;
import com.oracle.libuv.cb.StreamConnectionCallback;
import com.oracle.libuv.cb.StreamRead2Callback;
import com.oracle.libuv.cb.StreamReadCallback;
import com.oracle.libuv.cb.StreamShutdownCallback;
import com.oracle.libuv.cb.StreamWriteCallback;
import com.oracle.libuv.cb.TimerCallback;
import com.oracle.libuv.cb.UDPCloseCallback;
import com.oracle.libuv.cb.UDPRecvCallback;
import com.oracle.libuv.cb.UDPSendCallback;

public final class LoopCallbackHandler implements CallbackHandler {

    private final CallbackExceptionHandler exceptionHandler;

    public LoopCallbackHandler(final CallbackExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void handleAsyncCallback(final AsyncCallback cb, final int status) {
        try {
            cb.onSend(status);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleCheckCallback(final CheckCallback cb, final int status) {
        try {
            cb.onCheck(status);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleSignalCallback(final SignalCallback cb, final int signum) {
        try {
            cb.onSignal(signum);
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
    public void handleStreamConnectCallback(final StreamConnectCallback cb, final int status, final Exception error) {
        try {
            cb.onConnect(status, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleStreamConnectionCallback(final StreamConnectionCallback cb, final int status, final Exception error) {
        try {
            cb.onConnection(status, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleStreamCloseCallback(final StreamCloseCallback cb) {
        try {
            cb.onClose();
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleStreamShutdownCallback(final StreamShutdownCallback cb, final int status, final Exception error) {
        try {
            cb.onShutdown(status, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileCallback(final FileCallback cb, final Object context, final Exception error) {
        try {
            cb.onDone(context, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileCloseCallback(final FileCloseCallback cb, final Object context, final int fd, final Exception error) {
        try {
            cb.onClose(context, fd, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileOpenCallback(final FileOpenCallback cb, final Object context, final int fd, final Exception error) {
        try {
            cb.onOpen(context, fd, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileReadCallback(final FileReadCallback cb, final Object context, final int bytesRead, final ByteBuffer data, final Exception error) {
        try {
            cb.onRead(context, bytesRead, data, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileReadDirCallback(final FileReadDirCallback cb, final Object context, final String[] names, final Exception error) {
        try {
            cb.onReadDir(context, names, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileReadLinkCallback(final FileReadLinkCallback cb, final Object context, final String name, final Exception error) {
        try {
            cb.onReadLink(context, name, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileStatsCallback(final FileStatsCallback cb, final Object context, final Stats stats, final Exception error) {
        try {
            cb.onStats(context, stats, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileUTimeCallback(final FileUTimeCallback cb, final Object context, final long time, final Exception error) {
        try {
            cb.onUTime(context, time, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileWriteCallback(final FileWriteCallback cb, final Object context, final int bytesWritten, final Exception error) {
        try {
            cb.onWrite(context, bytesWritten, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleFileEventCallback(final FileEventCallback cb, final int status, final String event, final String filename) {
        try {
            cb.onEvent(status, event, filename);
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
    public void handleProcessCloseCallback(ProcessCloseCallback cb) {
        try {
            cb.onClose();
        } catch (Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleProcessExitCallback(ProcessExitCallback cb, int status, int signal, Exception error) {
        try {
            cb.onExit(status, signal, error);
        } catch (Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleTimerCallback(final TimerCallback cb, final int status) {
        try {
            cb.onTimer(status);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleUDPRecvCallback(final UDPRecvCallback cb, final int nread, final ByteBuffer data, final Address address) {
        try {
            cb.onRecv(nread, data, address);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleUDPSendCallback(final UDPSendCallback cb, final int status, final Exception error) {
        try {
            cb.onSend(status, error);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleUDPCloseCallback(final UDPCloseCallback cb) {
        try {
            cb.onClose();
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }

    @Override
    public void handleIdleCallback(final IdleCallback cb, final int status) {
        try {
            cb.onIdle(status);
        } catch (final Exception ex) {
            exceptionHandler.handle(ex);
        }
    }
}
