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

#include <assert.h>

#include "uv.h"
#include "throw.h"
#include "net_java_libuv_handles_LoopHandle.h"

static void _close_cb(uv_handle_t* handle) {
}

static void _walk_cb(uv_handle_t* handle, void* arg) {
  if (!uv_is_closing(handle)) {
    uv_close(handle, _close_cb);
  }
}

/*
 * Class:     net_java_libuv_handles_LoopHandle
 * Method:    _new
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_net_java_libuv_handles_LoopHandle__1new
  (JNIEnv *env, jclass cls) {

  uv_loop_t* ptr = uv_loop_new();
  assert(ptr);
  return reinterpret_cast<jlong>(ptr);
}

/*
 * Class:     net_java_libuv_handles_LoopHandle
 * Method:    _run
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_LoopHandle__1run
  (JNIEnv *env, jobject that, jlong ptr, jint mode) {

  assert(ptr);
  return uv_run(reinterpret_cast<uv_loop_t*>(ptr), (uv_run_mode) mode);
}

/*
 * Class:     net_java_libuv_handles_LoopHandle
 * Method:    _stop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_LoopHandle__1stop
  (JNIEnv *env, jobject that, jlong ptr) {

  assert(ptr);
  uv_stop(reinterpret_cast<uv_loop_t*>(ptr));
}

/*
 * Class:     net_java_libuv_handles_LoopHandle
 * Method:    _destroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_LoopHandle__1destroy
  (JNIEnv *env, jobject that, jlong ptr) {

  assert(ptr);
  uv_loop_t* handle = reinterpret_cast<uv_loop_t*>(ptr);
  uv_loop_delete(handle);
}

/*
 * Class:     net_java_libuv_handles_LoopHandle
 * Method:    _close_all
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_LoopHandle__1close_1all
  (JNIEnv *env, jobject that, jlong ptr) {

  assert(ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(ptr);
  uv_walk(loop, _walk_cb, NULL);
}

/*
 * Class:     net_java_libuv_handles_LoopHandle
 * Method:    _get_last_error
 * Signature: (J)Lnet/java/libuv/NativeException;
 */
JNIEXPORT jthrowable JNICALL Java_net_java_libuv_handles_LoopHandle__1get_1last_1error
  (JNIEnv * env, jobject that, jlong ptr) {

  assert(ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(ptr);
  int code = uv_last_error(loop).code;

  return NewException(env, code);
}
