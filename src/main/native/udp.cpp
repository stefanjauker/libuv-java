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

#include "uv.h"
#include "exception.h"
#include "stream.h"
#include "udp.h"
#include "net_java_libuv_handles_UDPHandle.h"

uv_buf_t _alloc_cb(uv_handle_t* handle, size_t suggested_size) {
  // override - 64k buffers are too large for udp
  if (suggested_size >= 64 * 1024) suggested_size = 4 * 1024;
  return uv_buf_init(new char[suggested_size], static_cast<unsigned int>(suggested_size));
}

jclass UDPCallbacks::_object_cid = NULL;
jclass UDPCallbacks::_integer_cid = NULL;
jclass UDPCallbacks::_udp_handle_cid = NULL;
jclass UDPCallbacks::_buffer_cid = NULL;

jmethodID UDPCallbacks::_integer_valueof_mid = NULL;
jmethodID UDPCallbacks::_callback_1arg_mid = NULL;
jmethodID UDPCallbacks::_callback_narg_mid = NULL;
jmethodID UDPCallbacks::_buffer_wrap_mid = NULL;

JNIEnv* UDPCallbacks::_env = NULL;

void UDPCallbacks::static_initialize(JNIEnv* env, jclass cls) {
  _env = env;
  assert(_env);

  _object_cid = env->FindClass("java/lang/Object");
  assert(_object_cid);
  _object_cid = (jclass) env->NewGlobalRef(_object_cid);
  assert(_object_cid);

  _integer_cid = env->FindClass("java/lang/Integer");
  assert(_integer_cid);
  _integer_cid = (jclass) env->NewGlobalRef(_integer_cid);
  assert(_integer_cid);

  _buffer_cid = env->FindClass("java/nio/ByteBuffer");
  assert(_buffer_cid);
  _buffer_cid = (jclass) env->NewGlobalRef(_buffer_cid);
  assert(_buffer_cid);

  _integer_valueof_mid = env->GetStaticMethodID(_integer_cid, "valueOf", "(I)Ljava/lang/Integer;");
  assert(_integer_valueof_mid);

  _buffer_wrap_mid = env->GetStaticMethodID(_buffer_cid, "wrap", "([B)Ljava/nio/ByteBuffer;");
  assert(_buffer_wrap_mid);

  _udp_handle_cid = (jclass) env->NewGlobalRef(cls);
  assert(_udp_handle_cid);

  _callback_1arg_mid = env->GetMethodID(_udp_handle_cid, "callback", "(ILjava/lang/Object;)V");
  assert(_callback_1arg_mid);
  _callback_narg_mid = env->GetMethodID(_udp_handle_cid, "callback", "(I[Ljava/lang/Object;)V");
  assert(_callback_narg_mid);

  // ensure JNI ids used by StreamCallbacks::_address_to_js are initialized
  StreamCallbacks::static_initialize_address(env);
}

void UDPCallbacks::initialize(jobject instance) {
  assert(_env);
  assert(instance);
  _instance = _env->NewGlobalRef(instance);
}

UDPCallbacks::UDPCallbacks() {
}

UDPCallbacks::~UDPCallbacks() {
  _env->DeleteGlobalRef(_instance);
}

void UDPCallbacks::on_recv(ssize_t nread, uv_buf_t buf, struct sockaddr* addr, unsigned flags) {
  if (nread == 0) return;
  jobject nread_arg = _env->CallStaticObjectMethod(_integer_cid, _integer_valueof_mid, nread);
  assert(nread_arg);
  jobject buffer_arg = NULL;
  if (nread > 0) {
    jsize size = static_cast<jsize>(nread);
    jbyteArray bytes = _env->NewByteArray(size);
    _env->SetByteArrayRegion(bytes, 0, size, reinterpret_cast<signed char const*>(buf.base));
    buffer_arg = _env->CallStaticObjectMethod(_buffer_cid, _buffer_wrap_mid, bytes);
    free(buf.base);
  }
  jobject rinfo_arg = addr ? StreamCallbacks::_address_to_js(_env, addr) : NULL;
  jobjectArray args = _env->NewObjectArray(3, _object_cid, 0);
  OOM(_env, args);
  _env->SetObjectArrayElement(args, 0, nread_arg);
  _env->SetObjectArrayElement(args, 1, buffer_arg);
  _env->SetObjectArrayElement(args, 2, rinfo_arg);
  _env->CallVoidMethod(
      _instance,
      _callback_narg_mid,
      UDP_RECV_CALLBACK,
      args);
}

void UDPCallbacks::on_send(int status) {
  jobject arg = _env->CallStaticObjectMethod(_integer_cid, _integer_valueof_mid, status);
  assert(arg);
  _env->CallVoidMethod(
      _instance,
      _callback_1arg_mid,
      UDP_SEND_CALLBACK,
      arg);
}

void UDPCallbacks::on_send(int status, int error_code) {
  assert(_env);
  assert(status < 0);

  jobject status_value = _env->CallStaticObjectMethod(_integer_cid, _integer_valueof_mid, status);
  jthrowable exception = NewException(_env, error_code);
  jobjectArray args = _env->NewObjectArray(2, _object_cid, 0);
  OOM(_env, args);
  _env->SetObjectArrayElement(args, 0, status_value);
  _env->SetObjectArrayElement(args, 1, exception);
  _env->CallVoidMethod(
      _instance,
      _callback_narg_mid,
      UDP_SEND_CALLBACK,
      args);
}

void UDPCallbacks::on_close() {
  _env->CallVoidMethod(
      _instance,
      _callback_1arg_mid,
      UDP_CLOSE_CALLBACK,
      NULL);
}

static void _close_cb(uv_handle_t* handle) {
  assert(handle);
  assert(handle->data);
  UDPCallbacks* cb = reinterpret_cast<UDPCallbacks*>(handle->data);
  cb->on_close();
  delete cb;
  delete handle;
}

static void _recv_cb(uv_udp_t* udp, ssize_t nread, uv_buf_t buf, struct sockaddr* addr, unsigned flags) {
  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  assert(handle->data);
  UDPCallbacks* cb = reinterpret_cast<UDPCallbacks*>(handle->data);
  cb->on_recv(nread, buf, addr, flags);
}

static void _send_cb(uv_udp_send_t* req, int status) {
  assert(req->handle);
  assert(req->handle->data);
  assert(req->data);
  UDPCallbacks* cb = reinterpret_cast<UDPCallbacks*>(req->handle->data);
  if (status < 0) {
    int error_code = uv_last_error(req->handle->loop).code;
    cb->on_send(status, error_code);
  } else {
    cb->on_send(status);
  }
  delete[] reinterpret_cast<jbyte*>(req->data);
  delete req;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _new
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_java_libuv_handles_UDPHandle__1new
  (JNIEnv *env, jclass cls, jlong loop) {

  assert(loop);
  uv_loop_t* lp = reinterpret_cast<uv_loop_t*>(loop);
  uv_udp_t* udp = new uv_udp_t();
  int r = uv_udp_init(lp, udp);
  if (r) {
    ThrowException(env, udp->loop, "uv_udp_init");
  } else {
    udp->data = new UDPCallbacks();
  }
  return reinterpret_cast<jlong>(udp);
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _static_initialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_UDPHandle__1static_1initialize
  (JNIEnv *env, jclass cls) {

  UDPCallbacks::static_initialize(env, cls);
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _initialize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_UDPHandle__1initialize
  (JNIEnv *env, jobject that, jlong udp) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  assert(handle->data);
  UDPCallbacks* cb = reinterpret_cast<UDPCallbacks*>(handle->data);
  cb->initialize(that);
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _address
 * Signature: (J)Lnet/java/libuv/Address;
 */
JNIEXPORT jobject JNICALL Java_net_java_libuv_handles_UDPHandle__1address
  (JNIEnv *env, jobject that, jlong udp) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  struct sockaddr_storage address;
  sockaddr* sock = reinterpret_cast<sockaddr*>(&address);
  int addrlen = sizeof(address);
  int r = uv_udp_getsockname(handle, sock, &addrlen);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_getsockname");
    return NULL;
  }
  const sockaddr* addr = reinterpret_cast<const sockaddr*>(&address);
  return StreamCallbacks::_address_to_js(env, addr);
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _bind
 * Signature: (JILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1bind
  (JNIEnv *env, jobject that, jlong udp, jint port, jstring host) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  const char* h = env->GetStringUTFChars(host, 0);
  sockaddr_in addr = uv_ip4_addr(h, port);
  unsigned flags = 0;
  int r = uv_udp_bind(handle, addr, flags);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_bind", h);
  }
  env->ReleaseStringUTFChars(host, h);
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _bind6
 * Signature: (JILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1bind6
  (JNIEnv *env, jobject that, jlong udp, jint port, jstring host) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  const char* h = env->GetStringUTFChars(host, 0);
  sockaddr_in6 addr = uv_ip6_addr(h, port);
  unsigned flags = 0;
  int r = uv_udp_bind6(handle, addr, flags);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_bind6", h);
  }
  env->ReleaseStringUTFChars(host, h);
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _send
 * Signature: (J[B;IIILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1send
  (JNIEnv *env, jobject that, jlong udp, jbyteArray data, jint offset, jint length, jint port, jstring host) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  const char* h = env->GetStringUTFChars(host, 0);
  sockaddr_in addr = uv_ip4_addr(h, port);

  jbyte* base = new jbyte[length - offset];
  env->GetByteArrayRegion(data, offset, length, base);
  uv_buf_t buf;
  buf.base = reinterpret_cast<char*>(base);
  buf.len = length - offset;

  uv_udp_send_t* req = new uv_udp_send_t();
  req->handle = handle;
  req->data = base;
  int r = uv_udp_send(req, handle, &buf, 1, addr, _send_cb);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_send", h);
  }
  env->ReleaseStringUTFChars(host, h);
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _send6
 * Signature: (J[B;IIILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1send6
  (JNIEnv *env, jobject that, jlong udp, jbyteArray data, jint offset, jint length, jint port, jstring host) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  const char* h = env->GetStringUTFChars(host, 0);
  sockaddr_in6 addr = uv_ip6_addr(h, port);

  jbyte* base = new jbyte[length - offset];
  env->GetByteArrayRegion(data, offset, length, base);
  uv_buf_t buf;
  buf.base = reinterpret_cast<char*>(base);
  buf.len = length - offset;

  uv_udp_send_t* req = new uv_udp_send_t();
  req->handle = handle;
  req->data = base;
  int r = uv_udp_send6(req, handle, &buf, 1, addr, _send_cb);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_send6", h);
  }
  env->ReleaseStringUTFChars(host, h);
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _recv_start
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1recv_1start
  (JNIEnv *env, jobject that, jlong udp) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  int r = uv_udp_recv_start(handle, _alloc_cb, _recv_cb);
  // UV_EALREADY means that the socket is already bound but that's okay
  if (r && uv_last_error(handle->loop).code != UV_EALREADY) {
    ThrowException(env, handle->loop, "uv_udp_recv_start");
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _recv_stop
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1recv_1stop
  (JNIEnv *env, jobject that, jlong udp) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  int r = uv_udp_recv_stop(handle);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_recv_stop");
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _set_ttl
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1set_1ttl
  (JNIEnv *env, jobject that, jlong udp, jint ttl) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  int r = uv_udp_set_ttl(handle, ttl);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_set_ttl");
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _set_membership
 * Signature: (JLjava/lang/String;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1set_1membership
  (JNIEnv *env, jobject that, jlong udp, jstring multicastAddress, jstring interfaceAddress, jint membership) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  const char* maddr = env->GetStringUTFChars(multicastAddress, 0);
  const char* iaddr = env->GetStringUTFChars(interfaceAddress, 0);
  int r = uv_udp_set_membership(handle, maddr, iaddr, static_cast<uv_membership>(membership));
  env->ReleaseStringUTFChars(multicastAddress, maddr);
  env->ReleaseStringUTFChars(interfaceAddress, iaddr);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_set_membership");
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _set_multicast_loop
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1set_1multicast_1loop
  (JNIEnv *env, jobject that, jlong udp, jint on) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  int r = uv_udp_set_multicast_loop(handle, on);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_set_multicast_loop");
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _set_multicast_ttl
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1set_1multicast_1ttl
  (JNIEnv *env, jobject that, jlong udp, jint ttl) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  int r = uv_udp_set_multicast_ttl(handle, ttl);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_set_multicast_ttl");
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _set_broadcast
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_UDPHandle__1set_1broadcast
  (JNIEnv *env, jobject that, jlong udp, jint on) {

  assert(udp);
  uv_udp_t* handle = reinterpret_cast<uv_udp_t*>(udp);
  int r = uv_udp_set_broadcast(handle, on);
  if (r) {
    ThrowException(env, handle->loop, "uv_udp_set_broadcast");
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_UDPHandle
 * Method:    _close
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_UDPHandle__1close
  (JNIEnv *env, jobject that, jlong udp) {

  assert(udp);
  uv_handle_t* handle = reinterpret_cast<uv_handle_t*>(udp);
  uv_close(handle, _close_cb);
}
