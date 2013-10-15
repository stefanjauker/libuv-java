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

import net.java.libuv.FileCallback;
import net.java.libuv.LibUVPermission;

public final class FileHandle extends Handle {

    static {
        _static_initialize();
    }

    private static final int SYNC_MODE = 0;

    // must be equal to values in uv.h
    public static final int UV_FS_UNKNOWN   = -1;
    public static final int UV_FS_CUSTOM    = 0;
    public static final int UV_FS_OPEN      = 1;
    public static final int UV_FS_CLOSE     = 2;
    public static final int UV_FS_READ      = 3;
    public static final int UV_FS_WRITE     = 4;
    public static final int UV_FS_SENDFILE  = 5;
    public static final int UV_FS_STAT      = 6;
    public static final int UV_FS_LSTAT     = 7;
    public static final int UV_FS_FSTAT     = 8;
    public static final int UV_FS_FTRUNCATE = 9;
    public static final int UV_FS_UTIME     = 10;
    public static final int UV_FS_FUTIME    = 11;
    public static final int UV_FS_CHMOD     = 12;
    public static final int UV_FS_FCHMOD    = 13;
    public static final int UV_FS_FSYNC     = 14;
    public static final int UV_FS_FDATASYNC = 15;
    public static final int UV_FS_UNLINK    = 16;
    public static final int UV_FS_RMDIR     = 17;
    public static final int UV_FS_MKDIR     = 18;
    public static final int UV_FS_RENAME    = 19;
    public static final int UV_FS_READDIR   = 20;
    public static final int UV_FS_LINK      = 21;
    public static final int UV_FS_SYMLINK   = 22;
    public static final int UV_FS_READLINK  = 23;
    public static final int UV_FS_CHOWN     = 24;
    public static final int UV_FS_FCHOWN    = 25;

    private FileCallback onCustom = null;
    private FileCallback onOpen = null;
    private FileCallback onClose = null;
    private FileCallback onRead = null;
    private FileCallback onWrite = null;
    private FileCallback onSendfile = null;
    private FileCallback onStat = null;
    private FileCallback onLStat = null;
    private FileCallback onFStat = null;
    private FileCallback onFTruncate = null;
    private FileCallback onUtime = null;
    private FileCallback onFUtime = null;
    private FileCallback onChmod = null;
    private FileCallback onFChmod = null;
    private FileCallback onFSync = null;
    private FileCallback onFDatasync = null;
    private FileCallback onUnlink = null;
    private FileCallback onRmdir = null;
    private FileCallback onMkdir = null;
    private FileCallback onRename = null;
    private FileCallback onReaddir = null;
    private FileCallback onLink = null;
    private FileCallback onSymlink = null;
    private FileCallback onReadlink = null;
    private FileCallback onChown = null;
    private FileCallback onFChown = null;

    public FileHandle(final LoopHandle loop) {
        super(loop.pointer(), loop);
    }

    public void setCustomCallback(final FileCallback callback) {
        this.onCustom = callback;
    }

    public void setOpenCallback(final FileCallback callback) {
        this.onOpen = callback;
    }

    public void setCloseCallback(final FileCallback callback) {
        this.onClose = callback;
    }

    public void setReadCallback(final FileCallback callback) {
        this.onRead = callback;
    }

    public void setWriteCallback(final FileCallback callback) {
        this.onWrite = callback;
    }

    public void setSendfileCallback(final FileCallback callback) {
        this.onSendfile = callback;
    }

    public void setStatCallback(final FileCallback callback) {
        this.onStat = callback;
    }

    public void setLStatCallback(final FileCallback callback) {
        this.onLStat = callback;
    }

    public void setFStatCallback(final FileCallback callback) {
        this.onFStat = callback;
    }

    public void setFTruncateCallback(final FileCallback callback) {
        this.onFTruncate = callback;
    }

    public void setUtimeCallback(final FileCallback callback) {
        this.onUtime = callback;
    }

    public void setFUtimeCallback(final FileCallback callback) {
        this.onFUtime = callback;
    }

    public void setChmodCallback(final FileCallback callback) {
        this.onChmod = callback;
    }

    public void setFChmodCallback(final FileCallback callback) {
        this.onFChmod = callback;
    }

    public void setFSyncCallback(final FileCallback callback) {
        this.onFSync = callback;
    }

    public void setFDatasyncCallback(final FileCallback callback) {
        this.onFDatasync = callback;
    }

    public void setUnlinkCallback(final FileCallback callback) {
        this.onUnlink = callback;
    }

    public void setRmdirCallback(final FileCallback callback) {
        this.onRmdir = callback;
    }

    public void setMkdirCallback(final FileCallback callback) {
        this.onMkdir = callback;
    }

    public void setRenameCallback(final FileCallback callback) {
        this.onRename = callback;
    }

    public void setReaddirCallback(final FileCallback callback) {
        this.onReaddir = callback;
    }

    public void setLinkCallback(final FileCallback callback) {
        this.onLink = callback;
    }

    public void setSymlinkCallback(final FileCallback callback) {
        this.onSymlink = callback;
    }

    public void setReadlinkCallback(final FileCallback callback) {
        this.onReadlink = callback;
    }

    public void setChownCallback(final FileCallback callback) {
        this.onChown = callback;
    }

    public void setFChownCallback(final FileCallback callback) {
        this.onFChown = callback;
    }

    public int close(final int fd) {
        return _close(pointer, fd, SYNC_MODE);
    }

    public int close(final int fd, final int callbackId) {
        return _close(pointer, fd, callbackId);
    }

    public int open(final String path, final int flags, final int mode) {
        LibUVPermission.checkOpenFile(path, flags);
        return _open(pointer, path, flags, mode, SYNC_MODE);
    }

    public int open(final String path, final int flags, final int mode, final int callbackId) {
        LibUVPermission.checkOpenFile(path, flags);
        return _open(pointer, path, flags, mode, callbackId); 
    }

    public int read(final int fd, final byte[] buffer, final long offset, final long length, final long position) {
        // Open has checked that fd is readable.
        return _read(pointer, fd, buffer, length, offset, position, SYNC_MODE);
    }

    public int read(final int fd, final byte[] buffer, final long offset, final long length, final long position, final int callbackId) {
        // Open has checked that fd is readable.
        return _read(pointer, fd, buffer, length, offset, position, callbackId);
    }

    public int unlink(final String path) {
        LibUVPermission.checkDeleteFile(path);
        return _unlink(pointer, path, SYNC_MODE);
    }

    public int unlink(final String path, final int callbackId) {
        LibUVPermission.checkDeleteFile(path);
        return _unlink(pointer, path, callbackId);
    }

    public int write(final int fd, final byte[] buffer, final long offset, final long length, final long position) {
        // Open has checked that fd is writable.
        return _write(pointer, fd, buffer, length, offset, position, SYNC_MODE);
    }

    public int write(final int fd, final byte[] buffer, final long offset, final long length, final long position, final int callbackId) {
        // Open has checked that fd is writable.
        return _write(pointer, fd, buffer, length, offset, position, callbackId);
    }

    public int mkdir(final String path, int mode) {
        LibUVPermission.checkWriteFile(path);
        return _mkdir(pointer, path, mode, SYNC_MODE);
    }

    public int mkdir(final String path, int mode, final int callbackId) {
        LibUVPermission.checkWriteFile(path);
        return _mkdir(pointer, path, mode, callbackId);
    }

    public int rmdir(final String path) {
        LibUVPermission.checkDeleteFile(path);
        return _rmdir(pointer, path, SYNC_MODE);
    }

    public int rmdir(final String path, final int callbackId) {
        LibUVPermission.checkDeleteFile(path);
        return _rmdir(pointer, path, callbackId);
    }

    public String[] readdir(final String path, int flags) {
        LibUVPermission.checkReadFile(path);
        return _readdir(pointer, path, flags, SYNC_MODE);
    }

    public String[] readdir(final String path, int flags, final int callbackId) {
        LibUVPermission.checkReadFile(path);
        return _readdir(pointer, path, flags, callbackId);
    }

    public Stats stat(final String path) {
        LibUVPermission.checkReadFile(path);
        return _stat(pointer, path, SYNC_MODE);
    }

    public Stats stat(final String path, final int callbackId) {
        LibUVPermission.checkReadFile(path);
        return _stat(pointer, path, callbackId);
    }

    public Stats fstat(final int fd) {
        LibUVPermission.checkReadFile(fd, this);
        return _fstat(pointer, fd, SYNC_MODE);
    }

    public Stats fstat(final int fd, final int callbackId) {
        LibUVPermission.checkReadFile(fd, this);
        return _fstat(pointer, fd, callbackId);
    }

    public int rename(final String path, final String newPath) {
        LibUVPermission.checkWriteFile(path);
        LibUVPermission.checkWriteFile(newPath);
        return _rename(pointer, path, newPath, SYNC_MODE);
    }

    public int rename(final String path, final String newPath, final int callbackId) {
        LibUVPermission.checkWriteFile(path);
        LibUVPermission.checkWriteFile(newPath);
        return _rename(pointer, path, newPath, callbackId);
    }

    public int fsync(final int fd) {
        // If a file is open, it can be synced, no security check.
        return _fsync(pointer, fd, SYNC_MODE);
    }

    public int fsync(final int fd, final int callbackId) {
        // If a file is open, it can be synced, no security check.
        return _fsync(pointer, fd, callbackId);
    }

    public int fdatasync(final int fd) {
        // If a file is open, it can be synced, no security check.
        return _fdatasync(pointer, fd, SYNC_MODE);
    }

    public int fdatasync(final int fd, final int callbackId) {
        // If a file is open, it can be synced, no security check.
        return _fdatasync(pointer, fd, callbackId);
    }

    public int ftruncate(final int fd, final long offset) {
        // Open has checked that fd is writable.
        return _ftruncate(pointer, fd, offset, SYNC_MODE);
    }

    public int ftruncate(final int fd, final long offset, final int callbackId) {
        // Open has checked that fd is writable.
        return _ftruncate(pointer, fd, offset, callbackId);
    }

    public int sendfile(final int outFd, final int inFd, long offset, long length) {
        // No security check required.
        return _sendfile(pointer, outFd, inFd, offset, length, SYNC_MODE);
    }

    public int sendfile(final int outFd, final int inFd, long offset, long length, final int callbackId) {
        // No security check required.
        return _sendfile(pointer, outFd, inFd, offset, length, callbackId); 
    }

    public int chmod(final String path, int mode) {
        LibUVPermission.checkWriteFile(path);
        return _chmod(pointer, path, mode, SYNC_MODE);
    }

    public int chmod(final String path, int mode, final int callbackId) {
        LibUVPermission.checkWriteFile(path);
        return _chmod(pointer, path, mode, callbackId);
    }

    public int utime(final String path, double atime, double mtime) {
        LibUVPermission.checkWriteFile(path);
        return _utime(pointer, path, atime, mtime, SYNC_MODE);
    }

    public int utime(final String path, double atime, double mtime, final int callbackId) {
        LibUVPermission.checkWriteFile(path);
        return _utime(pointer, path, atime, mtime, callbackId);
    }

    public int futime(final int fd, double atime, double mtime) {
        LibUVPermission.checkWriteFile(fd, this);
        return _futime(pointer, fd, atime, mtime, SYNC_MODE);
    }

    public int futime(final int fd, double atime, double mtime, final int callbackId) {
        LibUVPermission.checkWriteFile(fd, this);
        return _futime(pointer, fd, atime, mtime, callbackId); 
    }

    public Stats lstat(final String path) {
        LibUVPermission.checkReadFile(path);
        return _lstat(pointer, path, SYNC_MODE);
    }

    public Stats lstat(final String path, final int callbackId) {
        LibUVPermission.checkReadFile(path);
        return _lstat(pointer, path, callbackId);
    }

    public int link(final String path, final String newPath) {
        LibUVPermission.checkHardLink(path, newPath);
        return _link(pointer, path, newPath, SYNC_MODE);
    }

    public int link(final String path, final String newPath, final int callbackId) {
        LibUVPermission.checkHardLink(path, newPath);
        return _link(pointer, path, newPath, callbackId);
    }

    public int symlink(final String path, final String newPath, final int flags) {
        LibUVPermission.checkSymbolicLink(path, newPath);
        return _symlink(pointer, path, newPath, flags, SYNC_MODE);
    }

    public int symlink(final String path, final String newPath, final int flags, final int callbackId) {
        LibUVPermission.checkSymbolicLink(path, newPath);
        return _symlink(pointer, path, newPath, flags, callbackId);
    }

    public String readlink(final String path) {
        LibUVPermission.checkReadFile(path);
        return _readlink(pointer, path, SYNC_MODE);
    }

    public String readlink(final String path, final int callbackId) {
        LibUVPermission.checkReadFile(path);
        return _readlink(pointer, path, callbackId);
    }

    public int fchmod(final int fd, final int mode) {
        LibUVPermission.checkWriteFile(fd, this);
        return _fchmod(pointer, fd, mode, SYNC_MODE);
    }

    public int fchmod(final int fd, final int mode, final int callbackId) {
        LibUVPermission.checkWriteFile(fd, this);
        return _fchmod(pointer, fd, mode, callbackId);
    }

    public int chown(final String path, final int uid, final int gid) {
        LibUVPermission.checkWriteFile(path);
        return _chown(pointer, path, uid, gid, SYNC_MODE);
    }

    public int chown(final String path, final int uid, final int gid, final int callbackId) {
        LibUVPermission.checkWriteFile(path);
        return _chown(pointer, path, uid, gid, callbackId);
    }

    public int fchown(final int fd, final int uid, final int gid) {
        LibUVPermission.checkWriteFile(fd, this);
        return _fchown(pointer, fd, uid, gid, SYNC_MODE);
    }

    public int fchown(final int fd, final int uid, final int gid, final int callbackId) {
        LibUVPermission.checkWriteFile(fd, this);
        return _fchown(pointer, fd, uid, gid, callbackId);
    }

    public String getPath(final int fd) {
        // No security check, can retrieve path of an opened fd.
        return _get_path(pointer, fd);
    }

    private void callback(final int type, final int callbackId, final Object arg) {
        final Object[] args = {arg};
        callback(type, callbackId, args);
    }

    private void callback(final int type, final int callbackId, final Object... args) {
        switch (type) {
            case UV_FS_CUSTOM: if (onCustom != null) {call(onCustom, callbackId,  args);} break;
            case UV_FS_OPEN: if (onOpen != null) {call(onOpen, callbackId,  args);} break;
            case UV_FS_CLOSE: if (onClose != null) {call(onClose, callbackId,  args);} break;
            case UV_FS_READ: if (onRead != null) {call(onRead, callbackId, args);} break;
            case UV_FS_WRITE: if (onWrite != null) {call(onWrite, callbackId,  args);} break;
            case UV_FS_SENDFILE: if (onSendfile != null) {call(onSendfile, callbackId,  args);} break;
            case UV_FS_STAT: if (onStat != null) {call(onStat, callbackId,  args);} break;
            case UV_FS_LSTAT: if (onLStat != null) {call(onLStat, callbackId,  args);} break;
            case UV_FS_FSTAT: if (onFStat != null) {call(onFStat, callbackId,  args);} break;
            case UV_FS_FTRUNCATE: if (onFTruncate != null) {call(onFTruncate, callbackId,  args);} break;
            case UV_FS_UTIME: if (onUtime != null) {call(onUtime, callbackId,  args);} break;
            case UV_FS_FUTIME: if (onFUtime != null) {call(onFUtime, callbackId,  args);} break;
            case UV_FS_CHMOD: if (onChmod != null) {call(onChmod, callbackId,  args);} break;
            case UV_FS_FCHMOD: if (onFChmod != null) {call(onFChmod, callbackId,  args);} break;
            case UV_FS_FSYNC: if (onFSync != null) {call(onFSync, callbackId,  args);} break;
            case UV_FS_FDATASYNC: if (onFDatasync != null) {call(onFDatasync, callbackId,  args);} break;
            case UV_FS_UNLINK: if (onUnlink != null) {call(onUnlink, callbackId,  args);} break;
            case UV_FS_RMDIR: if (onRmdir != null) {call(onRmdir, callbackId,  args);} break;
            case UV_FS_MKDIR: if (onMkdir != null) { call(onMkdir, callbackId,  args);} break;
            case UV_FS_RENAME: if (onRename != null) {call(onRename, callbackId,  args);} break;
            case UV_FS_READDIR: if (onReaddir != null) {call(onReaddir, callbackId,  args);} break;
            case UV_FS_LINK: if (onLink != null) {call(onLink, callbackId,  args);} break;
            case UV_FS_SYMLINK: if (onSymlink != null) {call(onSymlink, callbackId,  args);} break;
            case UV_FS_READLINK: if (onReadlink != null) {call(onReadlink, callbackId,  args);} break;
            case UV_FS_CHOWN: if (onChown != null) {call(onChown, callbackId,  args);} break;
            case UV_FS_FCHOWN: if (onFChown != null) {call(onFChown, callbackId,  args);} break;
            default: assert false : "unsupported callback type " + type;
        }
    }

    private void call(final FileCallback callback, final int callbackId, final Object... args) {
        try {
            callback.call(callbackId, args);
        } catch (final Exception ex) {
            loop.exceptionHandler.handle(ex);
        }
    }

    private static native void _static_initialize();

    private native long _initialize();

    private native int _close(final long ptr, final int fd, final int callbackId); 

    private native int _open(final long ptr, final String path, final int flags, final int mode, final int callbackId); 

    private native int _read(final long ptr, final int fd, final byte[] data, final long length, final long offset, final long position, final int callbackId); 

    private native int _unlink(final long ptr, final String path, final int callbackId); 

    private native int _write(final long ptr, final int fd, final byte[] data, final long length, final long offset, final long position, final int callbackId); 

    private native int _mkdir(final long ptr, final String path, int mode, final int callbackId); 

    private native int _rmdir(final long ptr, final String path, final int callbackId); 

    private native String[] _readdir(final long ptr, final String path, int flags, final int callbackId); 

    private native Stats _stat(final long ptr, final String path, final int callbackId); 

    private native Stats _fstat(final long ptr, final int fd, final int callbackId); 

    private native int _rename(final long ptr, final String path, final String newPath, final int callbackId); 

    private native int _fsync(final long ptr, final int fd, final int callbackId); 

    private native int _fdatasync(final long ptr, final int fd, final int callbackId); 

    private native int _ftruncate(final long ptr, final int fd, final long offset, final int callbackId); 

    private native int _sendfile(final long ptr, final int outFd, final int inFd, final long offset, final long length, final int callbackId); 

    private native int _chmod(final long ptr, final String path, final int mode, final int callbackId); 

    private native int _utime(final long ptr, final String path, final double atime, final double mtime, final int callbackId); 

    private native int _futime(final long ptr, final int fd, final double atime, final double mtime, final int callbackId); 

    private native Stats _lstat(final long ptr, final String path, final int callbackId); 

    private native int _link(final long ptr, final String path, final String newPath, final int callbackId); 

    private native int _symlink(final long ptr, final String path, final String newPath, final int flags, final int callbackId); 

    private native String _readlink(final long ptr, final String path, final int callbackId); 

    private native int _fchmod(final long ptr, final int fd, final int mode, final int callbackId); 

    private native int _chown(final long ptr, final String path, final int uid, final int gid, final int callbackId); 

    private native int _fchown(final long ptr, final int fd, final int uid, final int gid, final int callbackId);

    private native String _get_path(final long ptr, final int fd);
}
