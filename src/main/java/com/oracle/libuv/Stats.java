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

package com.oracle.libuv;

public final class Stats {

    private long dev = 0;
    private long ino = 0;
    private long mode = 0;
    private long nlink = 0;
    private long uid = 0;
    private long gid = 0;
    private long rdev = 0;
    private long size = 0;
    private long blksize = 0;
    private long blocks = 0;
    private double atime = 0;
    private double mtime = 0;
    private double ctime = 0;
    private double birthtime = 0;

    public Stats() {
    }
    
    public void set(final long dev, final long ino, final long mode,
                    final long nlink, final long uid, final long gid,
                    final long rdev, final long size, final long blksize,
                    final long blocks, final double atime, final double mtime,
                    final double ctime, final double birthtime) {
        this.dev = dev;
        this.ino = ino;
        this.mode = mode;
        this.nlink = nlink;
        this.uid = uid;
        this.gid = gid;
        this.rdev = rdev;
        this.size = size;
        this.blksize = blksize;
        this.blocks = blocks;
        this.atime = atime;
        this.mtime = mtime;
        this.ctime = ctime;
        this.birthtime = birthtime;
    }

    public long getDev() {
        return dev;
    }

    public long getIno() {
        return ino;
    }

    public long getMode() {
        return mode;
    }

    public long getNlink() {
        return nlink;
    }

    public long getUid() {
        return uid;
    }

    public long getGid() {
        return gid;
    }

    public long getRdev() {
        return rdev;
    }

    public long getSize() {
        return size;
    }

    public long getBlksize() {
        return blksize;
    }

    public long getBlocks() {
        return blocks;
    }

    public double getAtime() {
        return atime;
    }

    public double getMtime() {
        return mtime;
    }

    public double getCtime() {
        return ctime;
    }
    
    public double getBirthtime() {
        return birthtime;
    }

    @Override
    public String toString() {
        return "{ dev: " + dev +
                " ino: " + ino +
                " mode: " + mode +
                " nlink: " + nlink +
                " uid: " + uid +
                " gid: " + gid +
                " rdev: " + rdev +
                " size: " + size +
                " blksize: " + blksize +
                " blocks: " + blocks +
                " atime: " + atime +
                " mtime: " + mtime +
                " ctime: " + ctime +
                " birthtime: " + birthtime +
                " }";
    }
}
