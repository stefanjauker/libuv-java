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

import net.java.libuv.Constants;
import net.java.libuv.FileCallback;
import net.java.libuv.NativeException;
import net.java.libuv.Files;
import net.java.libuv.handles.LoopHandle;
import net.java.libuv.Stats;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class FilesTest extends TestBase {

    private String testName;
    private static final int CALLBACK_ID = 1;

    @BeforeMethod
    protected void startSession(Method method) throws Exception {
        final String tmp = System.getProperty("java.io.tmpdir");
        testName = (tmp.endsWith(File.separator) ? tmp : tmp + File.separator) + method.getName();
    }

    @AfterMethod
    public void endSession(Method method) {
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);

        cleanupFiles(handle, testName);
        cleanupFiles(handle, testName + ".txt");
        cleanupFiles(handle, testName + "-new.txt");
        cleanupFiles(handle, testName + "2.txt");
    }

    @Test
    public void testGetPath() {
        final String filename = testName + ".txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);

        final int fd = handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        final String path = handle.getPath(fd);
        Assert.assertEquals(path, filename);
        cleanupFiles(handle, filename);
    }

    @Test
    public void testOpenWriteReadAndCloseSync() {
        final String filename = testName + ".txt";
        final byte[] b = "some data".getBytes();
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);

        final int fd = handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        Assert.assertTrue(fd >= 0);
        handle.write(fd, b, 0, b.length, 0);
        final byte[] bb = new byte[b.length];
        handle.read(fd, bb, 0, bb.length, 0);
        final int status = handle.close(fd);
        Assert.assertTrue(status == 0);
        Assert.assertEquals(b, bb);
        cleanupFiles(handle, filename);
    }

    @Test
    public void testOpenWriteReadAndCloseAsync() throws Exception {
        final String filename = testName + ".txt";
        final String data = "some data";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final byte[] writeBuffer = data.getBytes();
        final byte[] readBuffer = new byte[writeBuffer.length];
        final AtomicInteger fd = new AtomicInteger();
        final AtomicBoolean openCallbackCalled = new AtomicBoolean(false);
        final AtomicBoolean writeCallbackCalled = new AtomicBoolean(false);
        final AtomicBoolean readCallbackCalled = new AtomicBoolean(false);
        final AtomicBoolean closeCallbackCalled = new AtomicBoolean(false);

        handle.setOpenCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                openCallbackCalled.set(true);
                checkCallbackArgs(args);
                fd.set((Integer)args[0]);
                Assert.assertTrue(fd.get() > 0);
                handle.write(fd.get(), writeBuffer, 0, writeBuffer.length, 0, CALLBACK_ID);
            }
        });

        handle.setWriteCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                writeCallbackCalled.set(true);
                checkCallbackArgs(args);
                long written = (Long) args[0];
                Assert.assertTrue(written == data.getBytes().length);
                handle.read(fd.get(), readBuffer, 0, readBuffer.length, 0, CALLBACK_ID);
            }
        });

        handle.setReadCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                readCallbackCalled.set(true);
                Assert.assertEquals(writeBuffer, readBuffer);
                handle.close(fd.get(), CALLBACK_ID);
            }
        });

        handle.setCloseCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                closeCallbackCalled.set(true);
                checkCallbackArgs(args);
                Assert.assertEquals(args[0], fd.get());
                cleanupFiles(handle, filename);
            }
        });

        handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO, CALLBACK_ID);
        loop.run();
        Assert.assertTrue(openCallbackCalled.get());
        Assert.assertTrue(writeCallbackCalled.get());
        Assert.assertTrue(readCallbackCalled.get());
        Assert.assertTrue(closeCallbackCalled.get());
    }

    @Test
    public void testUnlinkSync() {
        final String filename = testName + ".txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);

        final int fd = handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        final int status = handle.unlink(filename);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void testUnlinkAsync() throws Exception {
        final String filename = testName + ".txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final AtomicBoolean unlinkCallbackCalled = new AtomicBoolean(false);

        handle.setUnlinkCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                unlinkCallbackCalled.set(true);
                checkCallbackArgs(args);
                Assert.assertTrue((Integer)args[0] == 0);
            }
        });

        handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        handle.unlink(filename, CALLBACK_ID);
        loop.run();
        Assert.assertTrue(unlinkCallbackCalled.get());
    }

    @Test
    public void testMkdirRmdirSync() {
        final String dirname = testName;
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);

        int status = handle.mkdir(dirname, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        Assert.assertTrue(status == 0);
        status = handle.rmdir(dirname);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void testMkdirRmdirAsync() throws Exception {
        final String dirname = testName;
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final AtomicBoolean mkdirCallbackCalled = new AtomicBoolean(false);
        final AtomicBoolean rmdirCallbackCalled = new AtomicBoolean(false);

        handle.setMkdirCallback( new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                mkdirCallbackCalled.set(true);
                checkCallbackArgs(args);
                handle.rmdir(dirname, CALLBACK_ID);
            }
        });

        handle.setRmdirCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                rmdirCallbackCalled.set(true);
                checkCallbackArgs(args);
            }
        });

        final int status = handle.mkdir(dirname, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO, CALLBACK_ID);
        Assert.assertTrue(status == 0);
        loop.run();
        Assert.assertTrue(mkdirCallbackCalled.get());
        Assert.assertTrue(rmdirCallbackCalled.get());

    }

    @Test
    public void testReaddirSync() {
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final String filename = "src";
        String[] names = handle.readdir(filename, Constants.O_RDONLY);
        Assert.assertEquals(names.length, 2);
    }

    @Test
    public void testReaddirAsync() throws Exception {
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final String filename = "src";
        final AtomicBoolean readdirCallbackCalled = new AtomicBoolean(false);

        handle.setReaddirCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                readdirCallbackCalled.set(true);
                Assert.assertEquals(args.length, 2);
            }
        });

        String[] names = handle.readdir(filename, Constants.O_RDONLY, CALLBACK_ID);
        Assert.assertEquals(names, null);
        loop.run();
        Assert.assertTrue(readdirCallbackCalled.get());
    }

    @Test
    public void testRenameSync() {
        final String filename = testName + ".txt";
        final String newName = testName + "-new" + ".txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);

        final int fd = handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        handle.close(fd);
        handle.rename(filename, newName);
        Assert.assertTrue (handle.open(newName, Constants.O_RDONLY, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO) > 0);
        cleanupFiles(handle, newName);
    }


    @Test
    public void testRenameAsync() throws Exception {
        final String filename = testName + ".txt";
        final String newName = testName + "-new" + ".txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final AtomicBoolean renameCallbackCalled = new AtomicBoolean(false);

        handle.setRenameCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                renameCallbackCalled.set(true);
                checkCallbackArgs(args);
                Assert.assertTrue (handle.open(newName, Constants.O_RDONLY, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO) > 0);
                cleanupFiles(handle, newName);
            }
        });

        final int fd = handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        handle.close(fd);
        handle.rename(filename, newName, CALLBACK_ID);
        loop.run();
        Assert.assertTrue(renameCallbackCalled.get());
    }

    @Test
    public void testFtruncateSync() {
        final String filename = testName + ".txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);

        final int fd = handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        handle.ftruncate(fd, 1000);
        final Stats stats = handle.fstat(fd);
        Assert.assertEquals(stats.getSize(), 1000);
        cleanupFiles(handle, filename);
    }

    @Test
    public void testFtruncateAsync() throws Exception {
        final String filename = testName + ".txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final AtomicInteger fd = new AtomicInteger();
        final AtomicBoolean ftruncateCallbackCalled = new AtomicBoolean(false);

        handle.setFTruncateCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                ftruncateCallbackCalled.set(true);
                checkCallbackArgs(args);
                final Stats stats = handle.fstat(fd.get());
                Assert.assertEquals(stats.getSize(), 1000);
                cleanupFiles(handle, filename);
            }
        });

        fd.set(handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO));
        handle.ftruncate(fd.get(), 1000, CALLBACK_ID);
        loop.run();
        Assert.assertTrue(ftruncateCallbackCalled.get());
    }

    @Test
    public void testLinkSync() {
        final String filename = testName + ".txt";
        final String filename2 = testName + "2.txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);

        final int fd = handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        final byte[] b = "some data".getBytes();
        handle.write(fd, b, 0, b.length, 0);
        handle.close(fd);
        handle.link(filename, filename2);
        final Stats stats = handle.stat(filename2);
        Assert.assertEquals(stats.getSize(), b.length);
        cleanupFiles(handle, filename, filename2);;
    }

    @Test
    public void testLinkAsync() throws Exception {
        final String filename = testName + ".txt";
        final String filename2 = testName + "2.txt";
        final LoopHandle loop = new LoopHandle();
        final Files handle = new Files(loop);
        final AtomicBoolean linkCallbackCalled = new AtomicBoolean();
        final byte[] b = "some data".getBytes();

        handle.setLinkCallback(new FileCallback() {
            @Override
            public void call(int id, Object[] args) throws Exception {
                linkCallbackCalled.set(true);
                final Stats stats = handle.stat(filename2);
                Assert.assertEquals(stats.getSize(), b.length);
                cleanupFiles(handle, filename, filename2);
            }
        });

        final int fd = handle.open(filename, Constants.O_RDWR | Constants.O_CREAT, Constants.S_IRWXU | Constants.S_IRWXG | Constants.S_IRWXO);
        handle.write(fd, b, 0, b.length, 0);
        handle.close(fd);
        handle.link(filename, filename2, CALLBACK_ID);
        loop.run();
        Assert.assertTrue(linkCallbackCalled.get());
    }

    private void cleanupFiles(final Files handle, final String... files) {
        for (int i = 0; i < files.length; i++) {
            try {
                String test = files[i];
                Stats stat = handle.stat(test);
                if ((stat.getMode() & Constants.S_IFMT) == Constants.S_IFDIR) {
                    handle.rmdir(test);
                } else if ((stat.getMode() & Constants.S_IFMT) == Constants.S_IFREG) {
                    handle.unlink(test);
                }
            } catch (Exception e) {
            }
        }
    }

    private void checkCallbackArgs(final Object[] args) {
        if (args.length == 2) {
            if ((Integer)args[0] == -1 && args[1] instanceof NativeException) {
                NativeException exception = (NativeException) args[1];
                Assert.fail(exception.getMessage());
            }
        }
    }

}
