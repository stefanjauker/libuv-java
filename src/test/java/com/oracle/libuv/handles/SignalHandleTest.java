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

import com.oracle.libuv.cb.SignalCallback;
import com.oracle.libuv.TestBase;

public class SignalHandleTest extends TestBase {

    // this test needs to be run manually, so no @Test annotation
    public static void main(final String[] args) throws Throwable {
        if (TestBase.IS_WINDOWS) {
            System.err.println("Sorry this test does not work on windows");
            return;
        }
        final LoopHandle loop = new LoopHandle();
        final SignalHandle handle = new SignalHandle(loop);
        handle.setSignalCallback(new SignalCallback() {
            @Override
            public void onSignal(final int signum) throws Exception {
                assert signum == 28;
                System.out.println("received signal " + signum);
            }
        });
        handle.start(28);
        System.out.println("waiting for signals... ");
        System.out.println("  (try kill -WINCH <pid>, kill -28 <pid>, or just resize the console)");
        loop.run();
    }

}
