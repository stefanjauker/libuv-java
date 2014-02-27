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

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.oracle.libuv.TestBase;

public class LoopHandleTest extends TestBase {

    private static final String DOT_SPLIT_REGEX = "\\.";

    @Test
    public void testList() throws Throwable {
        final LoopHandle loop = new LoopHandle();
        final String[] handles = loop.list();
        Assert.assertNotNull(handles);
        Assert.assertEquals(handles.length, 0);

        final AsyncHandle async = new AsyncHandle(loop);
        final CheckHandle check = new CheckHandle(loop);
        final IdleHandle idle = new IdleHandle(loop);
        final SignalHandle signal = new SignalHandle(loop);
        final PipeHandle pipe = new PipeHandle(loop, false);
        final TCPHandle tcp = new TCPHandle(loop);
        final UDPHandle udp = new UDPHandle(loop);

        System.out.println(async);
        System.out.println(check);
        System.out.println(idle);
        System.out.println(signal);
        System.out.println(pipe);
        System.out.println(tcp);
        System.out.println(udp);

        final Set<String> pointers = new HashSet<>();
        pointers.add(async.toString().split(DOT_SPLIT_REGEX)[1]);
        pointers.add(check.toString().split(DOT_SPLIT_REGEX)[1]);
        pointers.add(idle.toString().split(DOT_SPLIT_REGEX)[1]);
        pointers.add(signal.toString().split(DOT_SPLIT_REGEX)[1]);
        pointers.add(pipe.toString().split(DOT_SPLIT_REGEX)[1]);
        pointers.add(tcp.toString().split(DOT_SPLIT_REGEX)[1]);
        pointers.add(udp.toString().split(DOT_SPLIT_REGEX)[1]);

        final String[] handles1 = loop.list();
        Assert.assertNotNull(handles1);
        Assert.assertEquals(handles1.length, 7);
        for (final String handle : handles1) {
            System.out.println(handle);
            Assert.assertNotNull(handle);
            final String pointer = handle.toString().split(DOT_SPLIT_REGEX)[1];
            Assert.assertTrue(pointers.remove(pointer));
        }
        Assert.assertTrue(pointers.isEmpty());
    }

    public static void main(final String[] args) throws Throwable {
        final LoopHandleTest test = new LoopHandleTest();
        test.testList();
    }

}
