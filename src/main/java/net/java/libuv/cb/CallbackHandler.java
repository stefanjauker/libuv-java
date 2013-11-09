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

package net.java.libuv.cb;

import java.nio.ByteBuffer;

import net.java.libuv.Address;
import net.java.libuv.Stats;

public interface CallbackHandler {
    public void handleCheckCallback(CheckCallback cb, int status);
    public void handleIdleCallback(IdleCallback cb, int status);
    public void handleProcessCallback(ProcessCallback cb, Object[] args);
    public void handleSignalCallback(SignalCallback cb, int signum);
    public void handleStreamCallback(StreamCallback cb, Object[] args);
    public void handleStreamReadCallback(StreamReadCallback cb, ByteBuffer data);
    public void handleStreamRead2Callback(StreamRead2Callback cb, ByteBuffer data, long handle, int type);
    public void handleStreamWriteCallback(StreamWriteCallback cb, int status, Exception error);
    public void handleFileCallback(FileCallback cb, int id, Exception error);
    public void handleFileCloseCallback(FileCloseCallback cb, int callbackId, int fd, Exception error);
    public void handleFileOpenCallback(FileOpenCallback cb, int callbackId, int fd, Exception error);
    public void handleFileReadCallback(FileReadCallback cb, int callbackId, int bytesRead, byte[] data, Exception error);
    public void handleFileReadDirCallback(FileReadDirCallback cb, int callbackId, String[] names, Exception error);
    public void handleFileReadLinkCallback(FileReadLinkCallback cb, int callbackId, String name, Exception error);
    public void handleFileStatsCallback(FileStatsCallback cb, int callbackId, Stats stats, Exception error);
    public void handleFileUTimeCallback(FileUTimeCallback cb, int callbackId, long time, Exception error);
    public void handleFileWriteCallback(FileWriteCallback cb, int callbackId, int bytesWritten, Exception error);
    public void handleFileEventCallback(FileEventCallback cb, int status, String event, String filename);
    public void handleFilePollCallback(FilePollCallback cb, int status, Stats previous, Stats current);
    public void handleFilePollStopCallback(FilePollStopCallback cb);
    public void handleTimerCallback(TimerCallback cb, int status);
    public void handleUDPRecvCallback(UDPRecvCallback cb, int nread, ByteBuffer data, Address address);
    public void handleUDPSendCallback(UDPSendCallback cb, int status, Exception error);
    public void handleUDPCloseCallback(UDPCloseCallback cb);
}
