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

package net.java.libuv.pummel;

import net.java.libuv.Constants;
import net.java.libuv.Files;
import net.java.libuv.TestBase;
import net.java.libuv.cb.FileReadCallback;
import net.java.libuv.handles.LoopHandle;

import java.io.File;

public class FileReadTest extends TestBase {

    private int count = 0;
    private final String filename = (TestBase.TMPDIR.endsWith(File.separator) ? TestBase.TMPDIR : TestBase.TMPDIR + File.separator) + "FileReadTest.txt";
    private final LoopHandle loop;
    private final Files handle;
    private long startTime;

    private final static int ITERATIONS = 100000;
    private final static long DURATION = 300000;  // DURATION after 5 minutes
    private final static int BUFFER_SIZE = 16 * 1024 * 1024;

    private byte[] readBuffer = new byte[BUFFER_SIZE];
    private int fd;

    public FileReadTest() {
        loop = new LoopHandle();
        handle = new Files(loop);

        handle.setReadCallback(new FileReadCallback() {
            @Override
            public void onRead(int callbackId, int bytesRead, byte[] data, Exception error) throws Exception {
                if ((count % 1000) == 0) {
                    System.out.print(count + " ");
                }
                if (count > ITERATIONS) {
                    System.out.println("Max number of ITERATIONS reached");
                    handle.close(fd);
                    handle.unlink(filename);
                    System.exit(0);
                }
                if (System.currentTimeMillis() - startTime > DURATION) {
                    System.out.println("Test complete total ITERATIONS: " + count);
                    handle.close(fd);
                    handle.unlink(filename);
                    System.exit(0);
                }

                if (bytesRead != BUFFER_SIZE) {
                    System.out.println("wrong number of bytes returned " + bytesRead + " ITERATIONS " + count);
                    handle.close(fd);
                    handle.unlink(filename);
                    System.exit(0);
                }

                count++;
                for (int i = 0; i < BUFFER_SIZE; i++) {
                    readBuffer[i] = 0;
                }
                handle.read(fd, readBuffer, 0, BUFFER_SIZE, 0, this.hashCode());
            }
        });
    }

    public void readFile() throws Exception {
        startTime = System.currentTimeMillis();
        fd = handle.open(filename, Constants.O_WRONLY | Constants.O_CREAT, Constants.S_IRWXU);
        byte[] b = new byte[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++) {
            b[i] = (byte)'x';
        }
        handle.write(fd, b, 0, BUFFER_SIZE, 0);
        handle.close(fd);
        fd = handle.open(filename, Constants.O_RDONLY, Constants.S_IRWXU);
        handle.read(fd, readBuffer, 0, BUFFER_SIZE, 0, this.hashCode());
        loop.run();
    }

    public static void main(final String[] args) throws Exception {
        FileReadTest f = new FileReadTest();
        f.readFile();
    }
}
