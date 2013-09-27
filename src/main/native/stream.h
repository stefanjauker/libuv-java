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

#ifndef _libuv_java_stream_h_
#define _libuv_java_stream_h_

#include <jni.h>

#include "uv.h"

class StreamCallbacks {
private:
  static jclass _integer_cid;
  static jclass _long_cid;
  static jclass _object_cid;
  static jclass _buffer_cid;
  static jclass _address_cid;
  static jclass _stream_handle_cid;

  static jmethodID _integer_valueof_mid;
  static jmethodID _long_valueof_mid;
  static jmethodID _buffer_wrap_mid;
  static jmethodID _address_init_mid;
  static jmethodID _callback_1arg_mid;
  static jmethodID _callback_narg_mid;

  static JNIEnv* _env;

  jobject _instance;

public:
  static void static_initialize(JNIEnv *env, jclass cls);
  static void static_initialize_address(JNIEnv* env);
  static jobject _address_to_js(JNIEnv* env, const sockaddr* addr);

  StreamCallbacks();
  ~StreamCallbacks();

  void initialize(jobject instance);
  void throw_exception(int code, const char* message);

  void on_read(uv_buf_t* buf, jsize nread);
  void on_read2(uv_buf_t* buf, jsize nread, long ptr, uv_handle_type pending);
  void on_write(int status);
  void on_write(int status, int error_code);
  void on_shutdown(int status);
  void on_shutdown(int status, int error_code);
  void on_connect(int status);
  void on_connect(int status, int error_code);
  void on_connection(int status);
  void on_connection(int status, int error_code);
  void on_close();
};

typedef enum {
  STREAM_READ_CALLBACK = 1,
  STREAM_WRITE_CALLBACK,
  STREAM_CONNECT_CALLBACK,
  STREAM_CONNECTION_CALLBACK,
  STREAM_CLOSE_CALLBACK,
  STREAM_SHUTDOWN_CALLBACK
} StreamHandleCallbackType;

#endif // _libuv_java_stream_h_
