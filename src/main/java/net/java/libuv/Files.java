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

package net.java.libuv;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.java.libuv.cb.FileCallback;
import net.java.libuv.cb.FileCloseCallback;
import net.java.libuv.cb.FileOpenCallback;
import net.java.libuv.cb.FileReadCallback;
import net.java.libuv.cb.FileReadDirCallback;
import net.java.libuv.cb.FileReadLinkCallback;
import net.java.libuv.cb.FileStatsCallback;
import net.java.libuv.cb.FileUTimeCallback;
import net.java.libuv.cb.FileWriteCallback;
import net.java.libuv.handles.LoopHandle;

public final class Files {

    static {
        _static_initialize();
    }

    private static final Object SYNC_MODE = null;

    // must be equal to values in uv.h
    private static final int UV_FS_UNKNOWN   = -1;
    private static final int UV_FS_CUSTOM    = 0;
    private static final int UV_FS_OPEN      = 1;
    private static final int UV_FS_CLOSE     = 2;
    private static final int UV_FS_READ      = 3;
    private static final int UV_FS_WRITE     = 4;
    private static final int UV_FS_SENDFILE  = 5;
    private static final int UV_FS_STAT      = 6;
    private static final int UV_FS_LSTAT     = 7;
    private static final int UV_FS_FSTAT     = 8;
    private static final int UV_FS_FTRUNCATE = 9;
    private static final int UV_FS_UTIME     = 10;
    private static final int UV_FS_FUTIME    = 11;
    private static final int UV_FS_CHMOD     = 12;
    private static final int UV_FS_FCHMOD    = 13;
    private static final int UV_FS_FSYNC     = 14;
    private static final int UV_FS_FDATASYNC = 15;
    private static final int UV_FS_UNLINK    = 16;
    private static final int UV_FS_RMDIR     = 17;
    private static final int UV_FS_MKDIR     = 18;
    private static final int UV_FS_RENAME    = 19;
    private static final int UV_FS_READDIR   = 20;
    private static final int UV_FS_LINK      = 21;
    private static final int UV_FS_SYMLINK   = 22;
    private static final int UV_FS_READLINK  = 23;
    private static final int UV_FS_CHOWN     = 24;
    private static final int UV_FS_FCHOWN    = 25;

    private FileCallback onCustom = null;
    private FileOpenCallback onOpen = null;
    private FileCloseCallback onClose = null;
    private FileReadCallback onRead = null;
    private FileWriteCallback onWrite = null;
    private FileCallback onSendfile = null;
    private FileStatsCallback onStat = null;
    private FileStatsCallback onLStat = null;
    private FileStatsCallback onFStat = null;
    private FileCallback onFTruncate = null;
    private FileUTimeCallback onUTime = null;
    private FileUTimeCallback onFUTime = null;
    private FileCallback onChmod = null;
    private FileCallback onFChmod = null;
    private FileCallback onFSync = null;
    private FileCallback onFDatasync = null;
    private FileCallback onUnlink = null;
    private FileCallback onRmDir = null;
    private FileCallback onMkDir = null;
    private FileCallback onRename = null;
    private FileReadDirCallback onReadDir = null;
    private FileCallback onLink = null;
    private FileCallback onSymLink = null;
    private FileReadLinkCallback onReadLink = null;
    private FileCallback onChown = null;
    private FileCallback onFChown = null;

    private final long pointer;
    private final LoopHandle loop;
    private final Map<Integer, String> paths = new HashMap<>();

    private boolean closed;

    public Files(final LoopHandle loop) {
        LibUVPermission.checkHandle();
        this.pointer = _new();
        assert pointer != 0;
        this.loop = loop;
        _initialize(pointer, loop.pointer());
    }

    public void setCustomCallback(final FileCallback callback) {
        onCustom = callback;
    }

    public void setOpenCallback(final FileOpenCallback callback) {
        onOpen = callback;
    }

    public void setCloseCallback(final FileCloseCallback callback) {
        onClose = callback;
    }

    public void setReadCallback(final FileReadCallback callback) {
        onRead = callback;
    }

    public void setWriteCallback(final FileWriteCallback callback) {
        onWrite = callback;
    }

    public void setSendfileCallback(final FileCallback callback) {
        onSendfile = callback;
    }

    public void setStatCallback(final FileStatsCallback callback) {
        onStat = callback;
    }

    public void setLStatCallback(final FileStatsCallback callback) {
        onLStat = callback;
    }

    public void setFStatCallback(final FileStatsCallback callback) {
        onFStat = callback;
    }

    public void setFTruncateCallback(final FileCallback callback) {
        onFTruncate = callback;
    }

    public void setUTimeCallback(final FileUTimeCallback callback) {
        onUTime = callback;
    }

    public void setFUTimeCallback(final FileUTimeCallback callback) {
        onFUTime = callback;
    }

    public void setChmodCallback(final FileCallback callback) {
        onChmod = callback;
    }

    public void setFChmodCallback(final FileCallback callback) {
        onFChmod = callback;
    }

    public void setFSyncCallback(final FileCallback callback) {
        onFSync = callback;
    }

    public void setFDatasyncCallback(final FileCallback callback) {
        onFDatasync = callback;
    }

    public void setUnlinkCallback(final FileCallback callback) {
        onUnlink = callback;
    }

    public void setRmDirCallback(final FileCallback callback) {
        onRmDir = callback;
    }

    public void setMkDirCallback(final FileCallback callback) {
        onMkDir = callback;
    }

    public void setRenameCallback(final FileCallback callback) {
        onRename = callback;
    }

    public void setReadDirCallback(final FileReadDirCallback callback) {
        onReadDir = callback;
    }

    public void setLinkCallback(final FileCallback callback) {
        onLink = callback;
    }

    public void setSymLinkCallback(final FileCallback callback) {
        onSymLink = callback;
    }

    public void setReadLinkCallback(final FileReadLinkCallback callback) {
        onReadLink = callback;
    }

    public void setChownCallback(final FileCallback callback) {
        onChown = callback;
    }

    public void setFChownCallback(final FileCallback callback) {
        onFChown = callback;
    }

    public void close() {
        if (!closed) {
            paths.clear();
            _close(pointer);
        }
        closed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public int close(final int fd) {
        final String path = getPathAssertNonNull(fd, "closeSync");
        Objects.requireNonNull(path);
        final int r = _close(pointer, fd, SYNC_MODE, loop.getDomain());
        if (r != -1) {
            paths.remove(fd);
        }
        return r;
    }

    public int close(final int fd, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callClose(context, -1, newEBADF("close", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        final int r = _close(pointer, fd, context, loop.getDomain());
        if (r != -1) {
            paths.remove(fd);
        }
        return r;
    }

    public int open(final String path, final int flags, final int mode) {
        Objects.requireNonNull(path);
        LibUVPermission.checkOpenFile(path, flags);
        final int fd = _open(pointer, path, flags, mode, SYNC_MODE, loop.getDomain());
        if (fd != -1) {
            paths.put(fd, path);
        }
        return fd;
    }

    public int open(final String path, final int flags, final int mode, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkOpenFile(path, flags);
        return _open(pointer, path, flags, mode, context, loop.getDomain());
    }

    public int read(final int fd, final ByteBuffer buffer, final long offset, final long length, final long position) {
        final String path = getPathAssertNonNull(fd, "readSync");
        Objects.requireNonNull(path);
        Objects.requireNonNull(buffer);
        LibUVPermission.checkReadFile(fd, path);
        return buffer.hasArray() ?
                _read(pointer, fd, buffer, buffer.array(), length, offset, position, SYNC_MODE, loop.getDomain()) :
                _read(pointer, fd, buffer, null, length, offset, position, SYNC_MODE, loop.getDomain());
    }

    public int read(final int fd, final ByteBuffer buffer, final long offset, final long length, final long position, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callRead(context, -1, buffer, newEBADF("read", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        Objects.requireNonNull(buffer);
        LibUVPermission.checkReadFile(fd, path);
        return buffer.hasArray() ?
                _read(pointer, fd, buffer, buffer.array(), length, offset, position, context, loop.getDomain()) :
                _read(pointer, fd, buffer, null, length, offset, position, context, loop.getDomain());
    }

    public int unlink(final String path) {
        Objects.requireNonNull(path);
        LibUVPermission.checkDeleteFile(path);
        return _unlink(pointer, path, SYNC_MODE, loop.getDomain());
    }

    public int unlink(final String path, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkDeleteFile(path);
        return _unlink(pointer, path, context, loop.getDomain());
    }

    public int write(final int fd, final ByteBuffer buffer, final long offset, final long length, final long position) {
        final String path = getPathAssertNonNull(fd, "writeSync");
        Objects.requireNonNull(path);
        Objects.requireNonNull(buffer);
        LibUVPermission.checkWriteFile(fd, path);
        assert(offset < buffer.limit());
        assert(offset + length <= buffer.limit());
        return buffer.hasArray() ?
                _write(pointer, fd, buffer, buffer.array(), length, offset, position, SYNC_MODE, loop.getDomain()) :
                _write(pointer, fd, buffer, null, length, offset, position, SYNC_MODE, loop.getDomain());
    }

    public int write(final int fd, final ByteBuffer buffer, final long offset, final long length, final long position, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callWrite(context, -1, newEBADF("write", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        Objects.requireNonNull(buffer);
        LibUVPermission.checkWriteFile(fd, getPath(fd));
        assert(offset < buffer.limit());
        assert(offset + length <= buffer.limit());
        return buffer.hasArray() ?
                _write(pointer, fd, buffer, buffer.array(), length, offset, position, context, loop.getDomain()) :
                _write(pointer, fd, buffer, null, length, offset, position, context, loop.getDomain());
    }

    public int mkdir(final String path, final int mode) {
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(path);
        return _mkdir(pointer, path, mode, SYNC_MODE, loop.getDomain());
    }

    public int mkdir(final String path, final int mode, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(path);
        return _mkdir(pointer, path, mode, context, loop.getDomain());
    }

    public int rmdir(final String path) {
        Objects.requireNonNull(path);
        LibUVPermission.checkDeleteFile(path);
        return _rmdir(pointer, path, SYNC_MODE, loop.getDomain());
    }

    public int rmdir(final String path, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkDeleteFile(path);
        return _rmdir(pointer, path, context, loop.getDomain());
    }

    public String[] readdir(final String path, final int flags) {
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(path);
        return _readdir(pointer, path, flags, SYNC_MODE, loop.getDomain());
    }

    public String[] readdir(final String path, final int flags, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(path);
        return _readdir(pointer, path, flags, context, loop.getDomain());
    }

    public Stats stat(final String path) {
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(path);
        return _stat(pointer, path, SYNC_MODE, loop.getDomain());
    }

    public Stats stat(final String path, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(path);
        return _stat(pointer, path, context, loop.getDomain());
    }

    public Stats fstat(final int fd) {
        final String path = getPathAssertNonNull(fd, "fstatSync");
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(fd, path);
        return _fstat(pointer, fd, SYNC_MODE, loop.getDomain());
    }

    public Stats fstat(final int fd, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callStats(UV_FS_FSTAT, context, null, newEBADF("fstat", fd), loop.getDomain());
            return null;
        }
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(fd, path);
        return _fstat(pointer, fd, context, loop.getDomain());
    }

    public int rename(final String path, final String newPath) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(newPath);
        LibUVPermission.checkWriteFile(path);
        LibUVPermission.checkWriteFile(newPath);
        return _rename(pointer, path, newPath, SYNC_MODE, loop.getDomain());
    }

    public int rename(final String path, final String newPath, final Object context) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(newPath);
        LibUVPermission.checkWriteFile(path);
        LibUVPermission.checkWriteFile(newPath);
        return _rename(pointer, path, newPath, context, loop.getDomain());
    }

    public int fsync(final int fd) {
        final String path = getPathAssertNonNull(fd, "fsyncSync");
        Objects.requireNonNull(path);
        // If a file is open, it can be synced, no security check.
        return _fsync(pointer, fd, SYNC_MODE, loop.getDomain());
    }

    public int fsync(final int fd, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callback(UV_FS_FSYNC, context, newEBADF("fsync", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        // If a file is open, it can be synced, no security check.
        return _fsync(pointer, fd, context, loop.getDomain());
    }

    public int fdatasync(final int fd) {
        final String path = getPathAssertNonNull(fd, "fdatasyncSync");
        Objects.requireNonNull(path);
        // If a file is open, it can be synced, no security check.
        return _fdatasync(pointer, fd, SYNC_MODE, loop.getDomain());
    }

    public int fdatasync(final int fd, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callback(UV_FS_FDATASYNC, context, newEBADF("fdatasync", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        // If a file is open, it can be synced, no security check.
        return _fdatasync(pointer, fd, context, loop.getDomain());
    }

    public int ftruncate(final int fd, final long offset) {
        final String path = getPathAssertNonNull(fd, "ftruncateSync");
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(fd, path);
        return _ftruncate(pointer, fd, offset, SYNC_MODE, loop.getDomain());
    }

    public int ftruncate(final int fd, final long offset, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callback(UV_FS_FTRUNCATE, context, newEBADF("ftruncate", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(fd, path);
        return _ftruncate(pointer, fd, offset, context, loop.getDomain());
    }

    public int sendfile(final int outFd, final int inFd, final long offset, final long length) {
        Objects.requireNonNull(getPath(outFd));
        Objects.requireNonNull(getPath(inFd));
        // No security check required.
        return _sendfile(pointer, outFd, inFd, offset, length, SYNC_MODE, loop.getDomain());
    }

    public int sendfile(final int outFd, final int inFd, final long offset, final long length, final Object context) {
        Objects.requireNonNull(getPath(outFd));
        Objects.requireNonNull(getPath(inFd));
        // No security check required.
        return _sendfile(pointer, outFd, inFd, offset, length, context, loop.getDomain());
    }

    public int chmod(final String path, final int mode) {
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(path);
        return _chmod(pointer, path, mode, SYNC_MODE, loop.getDomain());
    }

    public int chmod(final String path, final int mode, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(path);
        return _chmod(pointer, path, mode, context, loop.getDomain());
    }

    public int utime(final String path, final double atime, final double mtime) {
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(path);
        return _utime(pointer, path, atime, mtime, SYNC_MODE, loop.getDomain());
    }

    public int utime(final String path, final double atime, final double mtime, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(path);
        return _utime(pointer, path, atime, mtime, context, loop.getDomain());
    }

    public int futime(final int fd, final double atime, final double mtime) {
        final String path = getPathAssertNonNull(fd, "futimeSync");
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(fd, path);
        return _futime(pointer, fd, atime, mtime, SYNC_MODE, loop.getDomain());
    }

    public int futime(final int fd, final double atime, final double mtime, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callUTime(UV_FS_FUTIME, context, -1, newEBADF("futime", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(fd, path);
        return _futime(pointer, fd, atime, mtime, context, loop.getDomain());
    }

    public Stats lstat(final String path) {
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(path);
        return _lstat(pointer, path, SYNC_MODE, loop.getDomain());
    }

    public Stats lstat(final String path, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(path);
        return _lstat(pointer, path, context, loop.getDomain());
    }

    public int link(final String path, final String newPath) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(newPath);
        LibUVPermission.checkHardLink(path, newPath);
        return _link(pointer, path, newPath, SYNC_MODE, loop.getDomain());
    }

    public int link(final String path, final String newPath, final Object context) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(newPath);
        LibUVPermission.checkHardLink(path, newPath);
        return _link(pointer, path, newPath, context, loop.getDomain());
    }

    public int symlink(final String path, final String newPath, final int flags) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(newPath);
        LibUVPermission.checkSymbolicLink(path, newPath);
        return _symlink(pointer, path, newPath, flags, SYNC_MODE, loop.getDomain());
    }

    public int symlink(final String path, final String newPath, final int flags, final Object context) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(newPath);
        LibUVPermission.checkSymbolicLink(path, newPath);
        return _symlink(pointer, path, newPath, flags, context, loop.getDomain());
    }

    public String readlink(final String path) {
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(path);
        return _readlink(pointer, path, SYNC_MODE, loop.getDomain());
    }

    public String readlink(final String path, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkReadFile(path);
        return _readlink(pointer, path, context, loop.getDomain());
    }

    public int fchmod(final int fd, final int mode) {
        final String path = getPathAssertNonNull(fd, "fchmodSync");
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(fd, path);
        return _fchmod(pointer, fd, mode, SYNC_MODE, loop.getDomain());
    }

    public int fchmod(final int fd, final int mode, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callback(UV_FS_FCHMOD, context, newEBADF("fchmod", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(fd, path);
        return _fchmod(pointer, fd, mode, context, loop.getDomain());
    }

    public int chown(final String path, final int uid, final int gid) {
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(path);
        return _chown(pointer, path, uid, gid, SYNC_MODE, loop.getDomain());
    }

    public int chown(final String path, final int uid, final int gid, final Object context) {
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(path);
        return _chown(pointer, path, uid, gid, context, loop.getDomain());
    }

    public int fchown(final int fd, final int uid, final int gid) {
        final String path = getPathAssertNonNull(fd, "fchown");
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(fd, path);
        return _fchown(pointer, fd, uid, gid, SYNC_MODE, loop.getDomain());
    }

    public int fchown(final int fd, final int uid, final int gid, final Object context) {
        final String path = getPath(fd);
        if (path == null) {
            callback(UV_FS_FCHOWN, context, newEBADF("fchown", fd), loop.getDomain());
            return -1;
        }
        Objects.requireNonNull(path);
        LibUVPermission.checkWriteFile(fd, path);
        return _fchown(pointer, fd, uid, gid, context, loop.getDomain());
    }

    public String getPath(final int fd) {
        // No security check, can retrieve path of an opened fd.
        return paths.get(fd);
    }

    private String getPathAssertNonNull(final int fd, final String method) {
        final String path = paths.get(fd);
        if (path == null) {
            throw newEBADF(method, fd);
        }
        return path;
    }

    private NativeException newEBADF(final String method, final int fd) {
        return new NativeException(9, "EBADF", "Bad file number: " + fd, method, null, null);
    }

    private void callback(final int type, final Object context, final Exception error,final Object domain) {
        Integer fd;
        switch (type) {
            case UV_FS_CUSTOM:
                if (onCustom != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onCustom, context, error);
                }
                break;
            case UV_FS_SENDFILE:
                if (onSendfile != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onSendfile, context, error);
                }
                break;
            case UV_FS_FTRUNCATE:
                if (onFTruncate != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onFTruncate, context, error);
                }
                break;
            case UV_FS_CHMOD:
                if (onChmod != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onChmod, context, error);
                }
                break;
            case UV_FS_FCHMOD:
                if (onFChmod != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onFChmod, context, error);
                }
                break;
            case UV_FS_FSYNC:
                if (onFSync != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onFSync, context, error);
                }
                break;
            case UV_FS_FDATASYNC:
                if (onFDatasync != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onFDatasync, context, error);
                }
                break;
            case UV_FS_UNLINK:
                if (onUnlink != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onUnlink, context, error);
                }
                break;
            case UV_FS_RMDIR:
                if (onRmDir != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onRmDir, context, error);
                }
                break;
            case UV_FS_MKDIR:
                if (onMkDir != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onMkDir, context, error);
                }
                break;
            case UV_FS_RENAME:
                if (onRename != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onRename, context, error);
                }
                break;
            case UV_FS_LINK:
                if (onLink != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onLink, context, error);
                }
                break;
            case UV_FS_SYMLINK:
                if (onSymLink != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onSymLink, context, error);
                }
                break;
            case UV_FS_CHOWN:
                if (onChown != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onChown, context, error);
                }
                break;
            case UV_FS_FCHOWN:
                if (onFChown != null) {
                    loop.getCallbackHandler(domain).handleFileCallback(onFChown, context, error);
                }
                break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void callClose(final Object context, final int fd, final Exception error, final Object domain) {
        if (onClose != null) {
            loop.getCallbackHandler(domain).handleFileCloseCallback(onClose, context, fd, error);
        }
    }

    private void callOpen(final Object context, final int fd, final String path, final Exception error, final Object domain) {
        if (fd != -1) {
            paths.put(fd, path);
        }
        if (onOpen != null) {
            loop.getCallbackHandler(domain).handleFileOpenCallback(onOpen, context, fd, error);
        }
    }

    private void callRead(final Object context, final int bytesRead, final ByteBuffer data, final Exception error, final Object domain) {
        if (onRead != null) {
            loop.getCallbackHandler(domain).handleFileReadCallback(onRead, context, bytesRead, data, error);
        }
    }

    private void callReadDir(final Object context, final String[] names, final Exception error, final Object domain) {
        if (onReadDir != null) {
            loop.getCallbackHandler(domain).handleFileReadDirCallback(onReadDir, context, names, error);
        }
    }

    private void callReadLink(final Object context, final String name, final Exception error, final Object domain) {
        if (onReadLink != null) {
            loop.getCallbackHandler(domain).handleFileReadLinkCallback(onReadLink, context, name, error);
        }
    }

    private void callStats(final int type, final Object context, final Stats stats, final Exception error, final Object domain) {
        switch(type) {
            case UV_FS_FSTAT:
                if (onFStat != null) {
                    loop.getCallbackHandler(domain).handleFileStatsCallback(onFStat, context, stats, error);
                }
                break;
            case UV_FS_LSTAT:
                if (onLStat != null) {
                    loop.getCallbackHandler(domain).handleFileStatsCallback(onLStat, context, stats, error);
                }
                break;
            case UV_FS_STAT:
                if (onStat != null) {
                    loop.getCallbackHandler(domain).handleFileStatsCallback(onStat, context, stats, error);
                }
                break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void callUTime(final int type, final Object context, final long time, final Exception error, final Object domain) {
        switch(type) {
            case UV_FS_UTIME:
                if (onUTime != null) {
                    loop.getCallbackHandler(domain).handleFileUTimeCallback(onUTime, context, time, error);
                }
                break;
            case UV_FS_FUTIME:
                if (onFUTime != null) {
                    loop.getCallbackHandler(domain).handleFileUTimeCallback(onFUTime, context, time, error);
                }
                break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void callWrite(final Object context, final int bytesWritten, final Exception error, final Object domain) {
        if (onWrite != null) {
            loop.getCallbackHandler(domain).handleFileWriteCallback(onWrite, context, bytesWritten, error);
        }
    }

    private static native void _static_initialize();

    private static native long _new();

    private native void _initialize(final long ptr, final long loop);

    private native int _close(final long ptr);

    private native int _close(final long ptr, final int fd, final Object context, final Object domain);

    private native int _open(final long ptr, final String path, final int flags, final int mode, final Object context, final Object domain);

    private native int _read(final long ptr, final int fd, final ByteBuffer buffer, final byte[] data, final long length, final long offset, final long position, final Object context, final Object domain);

    private native int _unlink(final long ptr, final String path, final Object context, final Object domain);

    private native int _write(final long ptr, final int fd, final ByteBuffer buffer, final byte[] data, final long length, final long offset, final long position, final Object context, final Object domain);

    private native int _mkdir(final long ptr, final String path, final int mode, final Object context, final Object domain);

    private native int _rmdir(final long ptr, final String path, final Object context, final Object domain);

    private native String[] _readdir(final long ptr, final String path, final int flags, final Object context, final Object domain);

    private native Stats _stat(final long ptr, final String path, final Object context, final Object domain);

    private native Stats _fstat(final long ptr, final int fd, final Object context, final Object domain);

    private native int _rename(final long ptr, final String path, final String newPath, final Object context, final Object domain);

    private native int _fsync(final long ptr, final int fd, final Object context, final Object domain);

    private native int _fdatasync(final long ptr, final int fd, final Object context, final Object domain);

    private native int _ftruncate(final long ptr, final int fd, final long offset, final Object context, final Object domain);

    private native int _sendfile(final long ptr, final int outFd, final int inFd, final long offset, final long length, final Object context, final Object domain);

    private native int _chmod(final long ptr, final String path, final int mode, final Object context, final Object domain);

    private native int _utime(final long ptr, final String path, final double atime, final double mtime, final Object context, final Object domain);

    private native int _futime(final long ptr, final int fd, final double atime, final double mtime, final Object context, final Object domain);

    private native Stats _lstat(final long ptr, final String path, final Object context, final Object domain);

    private native int _link(final long ptr, final String path, final String newPath, final Object context, final Object domain);

    private native int _symlink(final long ptr, final String path, final String newPath, final int flags, final Object context, final Object domain);

    private native String _readlink(final long ptr, final String path, final Object context, final Object domain);

    private native int _fchmod(final long ptr, final int fd, final int mode, final Object context, final Object domain);

    private native int _chown(final long ptr, final String path, final int uid, final int gid, final Object context, final Object domain);

    private native int _fchown(final long ptr, final int fd, final int uid, final int gid, final Object context, final Object domain);

}
