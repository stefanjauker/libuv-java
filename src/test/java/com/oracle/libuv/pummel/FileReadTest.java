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

package com.oracle.libuv.pummel;

import com.oracle.libuv.Constants;
import com.oracle.libuv.Files;
import com.oracle.libuv.TestBase;
import com.oracle.libuv.cb.FileReadCallback;
import com.oracle.libuv.handles.LoopHandle;

import java.io.File;
import java.nio.ByteBuffer;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;

public class FileReadTest extends TestBase {

    private int count = 0;
    private final String filename = (TestBase.TMPDIR.endsWith(File.separator) ? TestBase.TMPDIR : TestBase.TMPDIR + File.separator) + "FileReadTest.txt";
    private final LoopHandle loop;
    private final Files files;
    private long startTime;

    private final static int ITERATIONS = 10000;
    private final static long DURATION = 300000;  // DURATION after 5 minutes
    private final static int BUFFER_SIZE = 16 * 1024 * 1024;

    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private int fd;

    @AfterMethod
    public void cleanup() {
        if (files != null) {
            files.close(fd);
            files.unlink(filename);
        }
    }

    public FileReadTest() {
        loop = new LoopHandle();
        files = new Files(loop);

        files.setReadCallback(new FileReadCallback() {
            @Override
            public void onRead(Object context, int bytesRead, ByteBuffer data, Exception error) throws Exception {
                Assert.assertEquals(context, FileReadTest.this);
                Assert.assertEquals(data, readBuffer);
                for (int i = 0; i < bytesRead; i++) {
                    Assert.assertEquals(data.get(i), (byte) i);
                }
                Assert.assertEquals(bytesRead, BUFFER_SIZE);

                if ((count % 1000) == 0) {
                    System.out.print(count + " ");
                }
                if (count > ITERATIONS) {
                    System.out.println("Max number of ITERATIONS reached");
                    return;
                }
                if (System.currentTimeMillis() - startTime > DURATION) {
                    System.out.println("Test complete total ITERATIONS: " + count);
                    return;
                }

                count++;
                fill(readBuffer, (byte) 0);
                files.read(fd, readBuffer, 0, BUFFER_SIZE, 0, FileReadTest.this);
            }
        });
    }

    public void readFile() throws Throwable {
        startTime = System.currentTimeMillis();
        fill(readBuffer, (byte) 0);
        fd = files.open(filename, Constants.O_WRONLY | Constants.O_CREAT, Constants.S_IRWXU);
        ByteBuffer b = ByteBuffer.allocateDirect(BUFFER_SIZE);
        for (int i = 0; i < BUFFER_SIZE; i++) {
            b.put(i, (byte) i);
        }
        files.write(fd, b, 0, BUFFER_SIZE, 0);
        files.close(fd);
        fd = files.open(filename, Constants.O_RDONLY, Constants.S_IRWXU);
        files.read(fd, readBuffer, 0, BUFFER_SIZE, 0, FileReadTest.this);
        loop.run();
    }

    public static void main(final String[] args) throws Throwable {
        FileReadTest f = new FileReadTest();
        try {
            f.readFile();
        } finally {
            f.cleanup();
        }
    }
}
