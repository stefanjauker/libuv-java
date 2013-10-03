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

#include <string.h>
#include <assert.h>
#include <stdlib.h>
#include <jni.h>

#include "uv.h"
#include "throw.h"
#include "stream.h"
#include "net_java_libuv_handles_SignalHandle.h"

class SignalCallbacks {
private:
  static jclass _integer_cid;
  static jclass _signal_handle_cid;

  static jmethodID _integer_valueof_mid;
  static jmethodID _callback_1arg_mid;

  static JNIEnv* _env;

  jobject _instance;

public:
  static void static_initialize(JNIEnv* env, jclass cls);

  SignalCallbacks();
  ~SignalCallbacks();

  void initialize(jobject instance);

  void on_signal(int status);
};

jclass SignalCallbacks::_integer_cid = NULL;
jclass SignalCallbacks::_signal_handle_cid = NULL;

jmethodID SignalCallbacks::_integer_valueof_mid = NULL;
jmethodID SignalCallbacks::_callback_1arg_mid = NULL;

JNIEnv* SignalCallbacks::_env = NULL;

void SignalCallbacks::static_initialize(JNIEnv* env, jclass cls) {
  _env = env;
  assert(_env);

  _integer_cid = env->FindClass("java/lang/Integer");
  assert(_integer_cid);
  _integer_cid = (jclass) env->NewGlobalRef(_integer_cid);
  assert(_integer_cid);

  _integer_valueof_mid = env->GetStaticMethodID(_integer_cid, "valueOf", "(I)Ljava/lang/Integer;");
  assert(_integer_valueof_mid);

  _signal_handle_cid = (jclass) env->NewGlobalRef(cls);
  assert(_signal_handle_cid);

  _callback_1arg_mid = env->GetMethodID(_signal_handle_cid, "callback", "(Ljava/lang/Object;)V");
  assert(_callback_1arg_mid);
}

void SignalCallbacks::initialize(jobject instance) {
  assert(_env);
  assert(instance);
  _instance = _env->NewGlobalRef(instance);
}

SignalCallbacks::SignalCallbacks() {
}

SignalCallbacks::~SignalCallbacks() {
  _env->DeleteGlobalRef(_instance);
}

void SignalCallbacks::on_signal(int signum) {
  assert(_env);
  jobject arg = _env->CallStaticObjectMethod(_integer_cid, _integer_valueof_mid, signum);
  assert(arg);
  _env->CallVoidMethod(
      _instance,
      _callback_1arg_mid,
      arg);
}

static void _signal_cb(uv_signal_t* handle, int signum) {
  assert(handle);
  assert(handle->data);
  SignalCallbacks* cb = reinterpret_cast<SignalCallbacks*>(handle->data);
  cb->on_signal(signum);
}

/*
 * Class:     net_java_libuv_handles_SignalHandle
 * Method:    _new
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_java_libuv_handles_SignalHandle__1new
  (JNIEnv *env, jclass cls, jlong loop) {

  assert(loop);
  uv_loop_t* lp = reinterpret_cast<uv_loop_t*>(loop);
  uv_signal_t* signal = new uv_signal_t();
  int r = uv_signal_init(lp, signal);
  if (r) {
    ThrowException(env, signal->loop, "uv_signal_init");
  } else {
    signal->data = new SignalCallbacks();
  }
  return reinterpret_cast<jlong>(signal);
}

/*
 * Class:     net_java_libuv_handles_SignalHandle
 * Method:    _static_initialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_SignalHandle__1static_1initialize
  (JNIEnv *env, jclass cls) {

  SignalCallbacks::static_initialize(env, cls);
}

/*
 * Class:     net_java_libuv_handles_SignalHandle
 * Method:    _initialize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_SignalHandle__1initialize
  (JNIEnv *env, jobject that, jlong signal) {

  assert(signal);
  uv_signal_t* handle = reinterpret_cast<uv_signal_t*>(signal);
  assert(handle->data);
  SignalCallbacks* cb = reinterpret_cast<SignalCallbacks*>(handle->data);
  cb->initialize(that);
}

/*
 * Class:     net_java_libuv_handles_SignalHandle
 * Method:    _start
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_SignalHandle__1start
  (JNIEnv *env, jobject that, jlong signal, jint signum) {

  assert(signal);
  uv_signal_t* handle = reinterpret_cast<uv_signal_t*>(signal);
  int r = uv_signal_start(handle, _signal_cb, signum);
  if (r) {
    ThrowException(env, handle->loop, "uv_signal_start");
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_SignalHandle
 * Method:    _stop
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_SignalHandle__1stop
  (JNIEnv *env, jobject that, jlong signal) {

  assert(signal);
  uv_signal_t* handle = reinterpret_cast<uv_signal_t*>(signal);
  int r = uv_signal_stop(handle);
  if (r) {
    ThrowException(env, handle->loop, "uv_signal_stop");
  }
  SignalCallbacks* cb = reinterpret_cast<SignalCallbacks*>(handle->data);
  delete cb;
  delete handle;
  return r;
}
