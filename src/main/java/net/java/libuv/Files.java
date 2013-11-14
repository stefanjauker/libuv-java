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

import java.util.HashMap;
import java.util.Map;

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
        final int r = _close(pointer, fd, SYNC_MODE);
        if (r != -1) {
            paths.remove(fd);
        }
        return r;
    }

    public int close(final int fd, final Object context) {
        final int r = _close(pointer, fd, context);
        if (r != -1) {
            paths.remove(fd);
        }
        return r;
    }

    public int open(final String path, final int flags, final int mode) {
        LibUVPermission.checkOpenFile(path, flags);
        final int fd = _open(pointer, path, flags, mode, SYNC_MODE);
        if (fd != -1) {
            paths.put(fd, path);
        }
        return fd;
    }

    public int open(final String path, final int flags, final int mode, final Object context) {
        LibUVPermission.checkOpenFile(path, flags);
        return _open(pointer, path, flags, mode, context);
    }

    public int read(final int fd, final byte[] buffer, final long offset, final long length, final long position) {
        // Open has checked that fd is readable.
        return _read(pointer, fd, buffer, length, offset, position, SYNC_MODE);
    }

    public int read(final int fd, final byte[] buffer, final long offset, final long length, final long position, final Object context) {
        // Open has checked that fd is readable.
        return _read(pointer, fd, buffer, length, offset, position, context);
    }

    public int unlink(final String path) {
        LibUVPermission.checkDeleteFile(path);
        return _unlink(pointer, path, SYNC_MODE);
    }

    public int unlink(final String path, final Object context) {
        LibUVPermission.checkDeleteFile(path);
        return _unlink(pointer, path, context);
    }

    public int write(final int fd, final byte[] buffer, final long offset, final long length, final long position) {
        // Open has checked that fd is writable.
        assert(offset < buffer.length);
        assert(offset + length <= buffer.length);
        return _write(pointer, fd, buffer, length, offset, position, SYNC_MODE);
    }

    public int write(final int fd, final byte[] buffer, final long offset, final long length, final long position, final Object context) {
        // Open has checked that fd is writable.
        assert(offset < buffer.length);
        assert(offset + length <= buffer.length);
        return _write(pointer, fd, buffer, length, offset, position, context);
    }

    public int mkdir(final String path, final int mode) {
        LibUVPermission.checkWriteFile(path);
        return _mkdir(pointer, path, mode, SYNC_MODE);
    }

    public int mkdir(final String path, final int mode, final Object context) {
        LibUVPermission.checkWriteFile(path);
        return _mkdir(pointer, path, mode, context);
    }

    public int rmdir(final String path) {
        LibUVPermission.checkDeleteFile(path);
        return _rmdir(pointer, path, SYNC_MODE);
    }

    public int rmdir(final String path, final Object context) {
        LibUVPermission.checkDeleteFile(path);
        return _rmdir(pointer, path, context);
    }

    public String[] readdir(final String path, final int flags) {
        LibUVPermission.checkReadFile(path);
        return _readdir(pointer, path, flags, SYNC_MODE);
    }

    public String[] readdir(final String path, final int flags, final Object context) {
        LibUVPermission.checkReadFile(path);
        return _readdir(pointer, path, flags, context);
    }

    public Stats stat(final String path) {
        LibUVPermission.checkReadFile(path);
        return _stat(pointer, path, SYNC_MODE);
    }

    public Stats stat(final String path, final Object context) {
        LibUVPermission.checkReadFile(path);
        return _stat(pointer, path, context);
    }

    public Stats fstat(final int fd) {
        LibUVPermission.checkReadFile(fd, getPath(fd));
        return _fstat(pointer, fd, SYNC_MODE);
    }

    public Stats fstat(final int fd, final Object context) {
        LibUVPermission.checkReadFile(fd, getPath(fd));
        return _fstat(pointer, fd, context);
    }

    public int rename(final String path, final String newPath) {
        LibUVPermission.checkWriteFile(path);
        LibUVPermission.checkWriteFile(newPath);
        return _rename(pointer, path, newPath, SYNC_MODE);
    }

    public int rename(final String path, final String newPath, final Object context) {
        LibUVPermission.checkWriteFile(path);
        LibUVPermission.checkWriteFile(newPath);
        return _rename(pointer, path, newPath, context);
    }

    public int fsync(final int fd) {
        // If a file is open, it can be synced, no security check.
        return _fsync(pointer, fd, SYNC_MODE);
    }

    public int fsync(final int fd, final Object context) {
        // If a file is open, it can be synced, no security check.
        return _fsync(pointer, fd, context);
    }

    public int fdatasync(final int fd) {
        // If a file is open, it can be synced, no security check.
        return _fdatasync(pointer, fd, SYNC_MODE);
    }

    public int fdatasync(final int fd, final Object context) {
        // If a file is open, it can be synced, no security check.
        return _fdatasync(pointer, fd, context);
    }

    public int ftruncate(final int fd, final long offset) {
        // Open has checked that fd is writable.
        return _ftruncate(pointer, fd, offset, SYNC_MODE);
    }

    public int ftruncate(final int fd, final long offset, final Object context) {
        // Open has checked that fd is writable.
        return _ftruncate(pointer, fd, offset, context);
    }

    public int sendfile(final int outFd, final int inFd, final long offset, final long length) {
        // No security check required.
        return _sendfile(pointer, outFd, inFd, offset, length, SYNC_MODE);
    }

    public int sendfile(final int outFd, final int inFd, final long offset, final long length, final Object context) {
        // No security check required.
        return _sendfile(pointer, outFd, inFd, offset, length, context);
    }

    public int chmod(final String path, final int mode) {
        LibUVPermission.checkWriteFile(path);
        return _chmod(pointer, path, mode, SYNC_MODE);
    }

    public int chmod(final String path, final int mode, final Object context) {
        LibUVPermission.checkWriteFile(path);
        return _chmod(pointer, path, mode, context);
    }

    public int utime(final String path, final double atime, final double mtime) {
        LibUVPermission.checkWriteFile(path);
        return _utime(pointer, path, atime, mtime, SYNC_MODE);
    }

    public int utime(final String path, final double atime, final double mtime, final Object context) {
        LibUVPermission.checkWriteFile(path);
        return _utime(pointer, path, atime, mtime, context);
    }

    public int futime(final int fd, final double atime, final double mtime) {
        LibUVPermission.checkWriteFile(fd, getPath(fd));
        return _futime(pointer, fd, atime, mtime, SYNC_MODE);
    }

    public int futime(final int fd, final double atime, final double mtime, final Object context) {
        LibUVPermission.checkWriteFile(fd, getPath(fd));
        return _futime(pointer, fd, atime, mtime, context);
    }

    public Stats lstat(final String path) {
        LibUVPermission.checkReadFile(path);
        return _lstat(pointer, path, SYNC_MODE);
    }

    public Stats lstat(final String path, final Object context) {
        LibUVPermission.checkReadFile(path);
        return _lstat(pointer, path, context);
    }

    public int link(final String path, final String newPath) {
        LibUVPermission.checkHardLink(path, newPath);
        return _link(pointer, path, newPath, SYNC_MODE);
    }

    public int link(final String path, final String newPath, final Object context) {
        LibUVPermission.checkHardLink(path, newPath);
        return _link(pointer, path, newPath, context);
    }

    public int symlink(final String path, final String newPath, final int flags) {
        LibUVPermission.checkSymbolicLink(path, newPath);
        return _symlink(pointer, path, newPath, flags, SYNC_MODE);
    }

    public int symlink(final String path, final String newPath, final int flags, final Object context) {
        LibUVPermission.checkSymbolicLink(path, newPath);
        return _symlink(pointer, path, newPath, flags, context);
    }

    public String readlink(final String path) {
        LibUVPermission.checkReadFile(path);
        return _readlink(pointer, path, SYNC_MODE);
    }

    public String readlink(final String path, final Object context) {
        LibUVPermission.checkReadFile(path);
        return _readlink(pointer, path, context);
    }

    public int fchmod(final int fd, final int mode) {
        LibUVPermission.checkWriteFile(fd, getPath(fd));
        return _fchmod(pointer, fd, mode, SYNC_MODE);
    }

    public int fchmod(final int fd, final int mode, final Object context) {
        LibUVPermission.checkWriteFile(fd, getPath(fd));
        return _fchmod(pointer, fd, mode, context);
    }

    public int chown(final String path, final int uid, final int gid) {
        LibUVPermission.checkWriteFile(path);
        return _chown(pointer, path, uid, gid, SYNC_MODE);
    }

    public int chown(final String path, final int uid, final int gid, final Object context) {
        LibUVPermission.checkWriteFile(path);
        return _chown(pointer, path, uid, gid, context);
    }

    public int fchown(final int fd, final int uid, final int gid) {
        LibUVPermission.checkWriteFile(fd, getPath(fd));
        return _fchown(pointer, fd, uid, gid, SYNC_MODE);
    }

    public int fchown(final int fd, final int uid, final int gid, final Object context) {
        LibUVPermission.checkWriteFile(fd, getPath(fd));
        return _fchown(pointer, fd, uid, gid, context);
    }

    public String getPath(final int fd) {
        // No security check, can retrieve path of an opened fd.
        return paths.get(fd);
    }

    private void callback(final int type, final Object context, final Exception error) {
        Integer fd;
        switch (type) {
            case UV_FS_CUSTOM:
                if (onCustom != null) {
                    loop.getCallbackHandler().handleFileCallback(onCustom, context, error);
                }
                break;
            case UV_FS_SENDFILE:
                if (onSendfile != null) {
                    loop.getCallbackHandler().handleFileCallback(onSendfile, context, error);
                }
                break;
            case UV_FS_FTRUNCATE:
                if (onFTruncate != null) {
                    loop.getCallbackHandler().handleFileCallback(onFTruncate, context, error);
                }
                break;
            case UV_FS_CHMOD:
                if (onChmod != null) {
                    loop.getCallbackHandler().handleFileCallback(onChmod, context, error);
                }
                break;
            case UV_FS_FCHMOD:
                if (onFChmod != null) {
                    loop.getCallbackHandler().handleFileCallback(onFChmod, context, error);
                }
                break;
            case UV_FS_FSYNC:
                if (onFSync != null) {
                    loop.getCallbackHandler().handleFileCallback(onFSync, context, error);
                }
                break;
            case UV_FS_FDATASYNC:
                if (onFDatasync != null) {
                    loop.getCallbackHandler().handleFileCallback(onFDatasync, context, error);
                }
                break;
            case UV_FS_UNLINK:
                if (onUnlink != null) {
                    loop.getCallbackHandler().handleFileCallback(onUnlink, context, error);
                }
                break;
            case UV_FS_RMDIR:
                if (onRmDir != null) {
                    loop.getCallbackHandler().handleFileCallback(onRmDir, context, error);
                }
                break;
            case UV_FS_MKDIR:
                if (onMkDir != null) {
                    loop.getCallbackHandler().handleFileCallback(onMkDir, context, error);
                }
                break;
            case UV_FS_RENAME:
                if (onRename != null) {
                    loop.getCallbackHandler().handleFileCallback(onRename, context, error);
                }
                break;
            case UV_FS_LINK:
                if (onLink != null) {
                    loop.getCallbackHandler().handleFileCallback(onLink, context, error);
                }
                break;
            case UV_FS_SYMLINK:
                if (onSymLink != null) {
                    loop.getCallbackHandler().handleFileCallback(onSymLink, context, error);
                }
                break;
            case UV_FS_CHOWN:
                if (onChown != null) {
                    loop.getCallbackHandler().handleFileCallback(onChown, context, error);
                }
                break;
            case UV_FS_FCHOWN:
                if (onFChown != null) {
                    loop.getCallbackHandler().handleFileCallback(onFChown, context, error);
                }
                break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void callClose(final Object context, final int fd, final Exception error) {
        if (fd != -1) {
            paths.remove(fd);
        }
        if (onClose != null) {
            loop.getCallbackHandler().handleFileCloseCallback(onClose, context, fd, error);
        }
    }

    private void callOpen(final Object context, final int fd, final String path, final Exception error) {
        if (fd != -1) {
            paths.put(fd, path);
        }
        if (onOpen != null) {
            loop.getCallbackHandler().handleFileOpenCallback(onOpen, context, fd, error);
        }
    }

    private void callRead(final Object context, final int bytesRead, final byte[] data, final Exception error) {
        if (onRead != null) {
            loop.getCallbackHandler().handleFileReadCallback(onRead, context, bytesRead, data, error);
        }
    }

    private void callReadDir(final Object context, final String[] names, final Exception error) {
        if (onReadDir != null) {
            loop.getCallbackHandler().handleFileReadDirCallback(onReadDir, context, names, error);
        }
    }

    private void callReadLink(final Object context, final String name, final Exception error) {
        if (onReadLink != null) {
            loop.getCallbackHandler().handleFileReadLinkCallback(onReadLink, context, name, error);
        }
    }

    private void callStats(final int type, final Object context, final Stats stats, final Exception error) {
        switch(type) {
            case UV_FS_FSTAT:
                if (onFStat != null) {
                    loop.getCallbackHandler().handleFileStatsCallback(onFStat, context, stats, error);
                }
                break;
            case UV_FS_LSTAT:
                if (onLStat != null) {
                    loop.getCallbackHandler().handleFileStatsCallback(onLStat, context, stats, error);
                }
                break;
            case UV_FS_STAT:
                if (onStat != null) {
                    loop.getCallbackHandler().handleFileStatsCallback(onStat, context, stats, error);
                }
                break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void callUTime(final int type, final Object context, final long time, final Exception error) {
        switch(type) {
            case UV_FS_UTIME:
                if (onUTime != null) {
                    loop.getCallbackHandler().handleFileUTimeCallback(onUTime, context, time, error);
                }
                break;
            case UV_FS_FUTIME:
                if (onFUTime != null) {
                    loop.getCallbackHandler().handleFileUTimeCallback(onFUTime, context, time, error);
                }
                break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void callWrite(final Object context, final int bytesWritten, final Exception error) {
        if (onWrite != null) {
            loop.getCallbackHandler().handleFileWriteCallback(onWrite, context, bytesWritten, error);
        }
    }

    private static native void _static_initialize();

    private static native long _new();

    private native void _initialize(final long ptr, final long loop);

    private native int _close(final long ptr);

    private native int _close(final long ptr, final int fd, final Object context);

    private native int _open(final long ptr, final String path, final int flags, final int mode, final Object context);

    private native int _read(final long ptr, final int fd, final byte[] data, final long length, final long offset, final long position, final Object context);

    private native int _unlink(final long ptr, final String path, final Object context);

    private native int _write(final long ptr, final int fd, final byte[] data, final long length, final long offset, final long position, final Object context);

    private native int _mkdir(final long ptr, final String path, final int mode, final Object context);

    private native int _rmdir(final long ptr, final String path, final Object context);

    private native String[] _readdir(final long ptr, final String path, final int flags, final Object context);

    private native Stats _stat(final long ptr, final String path, final Object context);

    private native Stats _fstat(final long ptr, final int fd, final Object context);

    private native int _rename(final long ptr, final String path, final String newPath, final Object context);

    private native int _fsync(final long ptr, final int fd, final Object context);

    private native int _fdatasync(final long ptr, final int fd, final Object context);

    private native int _ftruncate(final long ptr, final int fd, final long offset, final Object context);

    private native int _sendfile(final long ptr, final int outFd, final int inFd, final long offset, final long length, final Object context);

    private native int _chmod(final long ptr, final String path, final int mode, final Object context);

    private native int _utime(final long ptr, final String path, final double atime, final double mtime, final Object context);

    private native int _futime(final long ptr, final int fd, final double atime, final double mtime, final Object context);

    private native Stats _lstat(final long ptr, final String path, final Object context);

    private native int _link(final long ptr, final String path, final String newPath, final Object context);

    private native int _symlink(final long ptr, final String path, final String newPath, final int flags, final Object context);

    private native String _readlink(final long ptr, final String path, final Object context);

    private native int _fchmod(final long ptr, final int fd, final int mode, final Object context);

    private native int _chown(final long ptr, final String path, final int uid, final int gid, final Object context);

    private native int _fchown(final long ptr, final int fd, final int uid, final int gid, final Object context);

}
