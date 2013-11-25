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
#include <time.h>
#include <string.h>

#include "uv.h"
#include "stats.h"
#include "exception.h"
#include "net_java_libuv_Files.h"

#ifdef __MACOS__
#include <sys/fcntl.h>
#endif

#ifdef _WIN32
#include <io.h>
#include <Shlwapi.h>
#include <tchar.h>
#endif

class FileCallbacks;

class FileRequest {
private:
  FileCallbacks* _callback;
  jobject _buffer;
  jbyteArray _data;
  jbyte* _bytes;
  jsize _offset;
  jobject _context;
  jint _fd;
  jstring _path;

public:
  FileRequest(FileCallbacks* ptr, jobject context, jint fd, jstring path);
  ~FileRequest();

  jbyte* get_bytes(jobject buffer, jbyteArray data, jsize offset, jsize length);
  void set_bytes(jint length);

  FileCallbacks* callback() { return _callback; }

  jobject context() { return _context; }

  jint fd() { return _fd; }

  jstring path() { return _path; }

  jobject buffer() { return _buffer; }
};

class FileCallbacks {
private:
  static jclass _files_cid;
  static jclass _stats_cid;

  static jmethodID _close_callback_mid;
  static jmethodID _file_callback_mid;
  static jmethodID _open_callback_mid;
  static jmethodID _read_callback_mid;
  static jmethodID _readdir_callback_mid;
  static jmethodID _readlink_callback_mid;
  static jmethodID _stats_callback_mid;
  static jmethodID _utime_callback_mid;
  static jmethodID _write_callback_mid;
  static jmethodID _stats_init_mid;

  static JNIEnv* _env;

  jobject _instance;
  uv_loop_t* _loop;

public:
  static jclass _string_cid;

  static void static_initialize(JNIEnv* env, jclass cls);
  static JNIEnv* env() { return _env; }

  FileCallbacks();
  ~FileCallbacks();

  uv_loop_t* loop() { return _loop; }

  void initialize(jobject instance, uv_loop_t* loop);
  void fs_cb(FileRequest* request, uv_fs_type fs_type, ssize_t result, void* ptr);
  void fs_cb(FileRequest* request, uv_fs_type fs_type, int errorno);
};

jclass FileCallbacks::_files_cid = NULL;
jclass FileCallbacks::_stats_cid = NULL;
jclass FileCallbacks::_string_cid = NULL;

jmethodID FileCallbacks::_close_callback_mid = NULL;
jmethodID FileCallbacks::_file_callback_mid = NULL;
jmethodID FileCallbacks::_open_callback_mid = NULL;
jmethodID FileCallbacks::_read_callback_mid = NULL;
jmethodID FileCallbacks::_readdir_callback_mid = NULL;
jmethodID FileCallbacks::_readlink_callback_mid = NULL;
jmethodID FileCallbacks::_stats_callback_mid = NULL;
jmethodID FileCallbacks::_utime_callback_mid = NULL;
jmethodID FileCallbacks::_write_callback_mid = NULL;
jmethodID FileCallbacks::_stats_init_mid = NULL;

JNIEnv* FileCallbacks::_env = NULL;

FileRequest::FileRequest(FileCallbacks* ptr, jobject context, jint fd, jstring path) {
  _callback = ptr;
  _context = context ? _callback->env()->NewGlobalRef(context) : NULL;
  _fd = fd;
  _path = path ? (jstring) _callback->env()->NewGlobalRef(path) : NULL;
  _bytes = NULL;
  _buffer = NULL;
  _data = NULL;
}

FileRequest::~FileRequest() {
  if (_context) {
    _callback->env()->DeleteGlobalRef(_context);
  }

  if (_buffer) {
    _callback->env()->DeleteGlobalRef(_buffer);
  }

  if (_data) {
    _callback->env()->DeleteGlobalRef(_data);
    if (_bytes) {
      delete[] _bytes;
    }
  }

  if (_path) {
    _callback->env()->DeleteGlobalRef(_path);
  }
}

jbyte* FileRequest::get_bytes(jobject buffer, jbyteArray data, jsize offset, jsize length) {
  assert(!_bytes);
  assert(!_buffer);
  assert(buffer);

  _offset = offset;
  _buffer = _callback->env()->NewGlobalRef(buffer);
  if (data) {
    _data = (jbyteArray) _callback->env()->NewGlobalRef(data);
    _bytes = new jbyte[length];
  } else {
    _data = NULL;
    _bytes = (jbyte*) _callback->env()->GetDirectBufferAddress(buffer);
  }
  return _bytes;
}

void FileRequest::set_bytes(jsize length) {
  if (_data) {
    _callback->env()->SetByteArrayRegion(_data, _offset, length, _bytes);
  }
}

void FileCallbacks::static_initialize(JNIEnv* env, jclass cls) {
  _env = env;
  assert(_env);

  _stats_cid = env->FindClass("net/java/libuv/Stats");
  assert(_stats_cid);
  _stats_cid = (jclass) env->NewGlobalRef(_stats_cid);
  assert(_stats_cid);

  _string_cid = env->FindClass("java/lang/String");
  assert(_string_cid);
  _string_cid = (jclass) env->NewGlobalRef(_string_cid);
  assert(_string_cid);

  _files_cid = (jclass) env->NewGlobalRef(cls);
  assert(_files_cid);

  _file_callback_mid = env->GetMethodID(_files_cid, "callback", "(ILjava/lang/Object;Ljava/lang/Exception;)V");
  assert(_file_callback_mid);

  _close_callback_mid = env->GetMethodID(_files_cid, "callClose", "(Ljava/lang/Object;ILjava/lang/Exception;)V");
  assert(_close_callback_mid);

  _open_callback_mid = env->GetMethodID(_files_cid, "callOpen", "(Ljava/lang/Object;ILjava/lang/String;Ljava/lang/Exception;)V");
  assert(_open_callback_mid);

  _read_callback_mid = env->GetMethodID(_files_cid, "callRead", "(Ljava/lang/Object;ILjava/nio/ByteBuffer;Ljava/lang/Exception;)V");
  assert(_read_callback_mid);

  _readdir_callback_mid = env->GetMethodID(_files_cid, "callReadDir", "(Ljava/lang/Object;[Ljava/lang/String;Ljava/lang/Exception;)V");
  assert(_readdir_callback_mid);

  _readlink_callback_mid = env->GetMethodID(_files_cid, "callReadLink", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Exception;)V");
  assert(_readlink_callback_mid);

  _stats_callback_mid = env->GetMethodID(_files_cid, "callStats", "(ILjava/lang/Object;Lnet/java/libuv/Stats;Ljava/lang/Exception;)V");
  assert(_stats_callback_mid);

  _utime_callback_mid = env->GetMethodID(_files_cid, "callUTime", "(ILjava/lang/Object;JLjava/lang/Exception;)V");
  assert(_utime_callback_mid);

  _write_callback_mid = env->GetMethodID(_files_cid, "callWrite", "(Ljava/lang/Object;ILjava/lang/Exception;)V");
  assert(_write_callback_mid);

  _stats_init_mid = env->GetMethodID(_stats_cid, "<init>", "(IIIIIIIJIJJJJ)V");
  assert(_stats_init_mid);

}

FileCallbacks::FileCallbacks() {
}

FileCallbacks::~FileCallbacks() {
  _env->DeleteGlobalRef(_instance);
}

void FileCallbacks::initialize(jobject instance, uv_loop_t* loop) {
  assert(_env);
  assert(instance);
  assert(loop);

  _instance = _env->NewGlobalRef(instance);
  _loop = loop;
}

void FileCallbacks::fs_cb(FileRequest* request, uv_fs_type fs_type, ssize_t result, void* ptr) {
  assert(_env);
  assert(request);

  switch (fs_type) {
    case UV_FS_CLOSE:
      _env->CallVoidMethod(
          _instance,
          _close_callback_mid,
          request->context(),
          request->fd(),
          NULL);
      return;

    case UV_FS_RENAME:
    case UV_FS_UNLINK:
    case UV_FS_RMDIR:
    case UV_FS_MKDIR:
    case UV_FS_FTRUNCATE:
    case UV_FS_FSYNC:
    case UV_FS_FDATASYNC:
    case UV_FS_LINK:
    case UV_FS_SYMLINK:
    case UV_FS_CHMOD:
    case UV_FS_FCHMOD:
    case UV_FS_CHOWN:
    case UV_FS_FCHOWN:
      _env->CallVoidMethod(
          _instance,
          _file_callback_mid,
          fs_type,
          request->context(),
          NULL);
      return;

    case UV_FS_OPEN:
      _env->CallVoidMethod(
          _instance,
          _open_callback_mid,
          request->context(),
          result,
          request->path(),
          NULL);
      return;

    case UV_FS_UTIME:
    case UV_FS_FUTIME:
      _env->CallVoidMethod(
          _instance,
          _utime_callback_mid,
          fs_type,
          request->context(),
          result,
          NULL);
      return;

    case UV_FS_STAT:
    case UV_FS_LSTAT:
    case UV_FS_FSTAT:
      _env->CallVoidMethod(
          _instance,
          _stats_callback_mid,
          fs_type,
          request->context(),
          Stats::create(static_cast<uv_statbuf_t*>(ptr)),
          NULL);
      return;

    case UV_FS_READLINK:
      _env->CallVoidMethod(
          _instance,
          _readlink_callback_mid,
          request->context(),
          _env->NewStringUTF(static_cast<char*>(ptr)),
          NULL);
      return;

    case UV_FS_READDIR: {
      char* namebuf = static_cast<char*>(ptr);
      int nnames = static_cast<int>(result);

      jobjectArray names = _env->NewObjectArray(nnames, _string_cid, 0);
      OOM(_env, names);
      for (int i = 0; i < nnames; i++) {
        jstring name = _env->NewStringUTF(namebuf);
        OOM(_env, name);
        _env->SetObjectArrayElement(names, i, name);
#ifndef NDEBUG
        namebuf += strlen(namebuf);
        assert(*namebuf == '\0');
        namebuf += 1;
#else
        namebuf += strlen(namebuf) + 1;
#endif
      }

      _env->CallVoidMethod(
          _instance,
          _readdir_callback_mid,
          request->context(),
          names,
          NULL);
      return;
    }

    case UV_FS_READ:
      request->set_bytes(static_cast<jsize>(result));
      _env->CallVoidMethod(
          _instance,
          _read_callback_mid,
          request->context(),
          result,
          request->buffer(),
          NULL);
      return;

    case UV_FS_WRITE:
      _env->CallVoidMethod(
          _instance,
          _write_callback_mid,
          request->context(),
          result,
          NULL);
    return;

    default:
      assert(0 && "Unhandled eio response");
  }
}

void FileCallbacks::fs_cb(FileRequest* request, uv_fs_type fs_type, int errorno) {
  assert(_env);
  assert(request);

  jstring path = request->path();
  const char* cpath = NULL;
  if (path) {
    cpath = _env->GetStringUTFChars(path, 0);
  }

  jthrowable exception = NewException(_env, errorno, NULL, NULL, cpath);

  switch (fs_type) {
    case UV_FS_CLOSE:
      _env->CallVoidMethod(
          _instance,
          _close_callback_mid,
          request->context(),
          -1,
          exception);
      break;

    case UV_FS_RENAME:
    case UV_FS_UNLINK:
    case UV_FS_RMDIR:
    case UV_FS_MKDIR:
    case UV_FS_FTRUNCATE:
    case UV_FS_FSYNC:
    case UV_FS_FDATASYNC:
    case UV_FS_LINK:
    case UV_FS_SYMLINK:
    case UV_FS_CHMOD:
    case UV_FS_FCHMOD:
    case UV_FS_CHOWN:
    case UV_FS_FCHOWN:
      _env->CallVoidMethod(
          _instance,
          _file_callback_mid,
          fs_type,
          request->context(),
          exception);
      break;

    case UV_FS_OPEN:
      _env->CallVoidMethod(
          _instance,
          _open_callback_mid,
          request->context(),
          -1,
          NULL,
          exception);
      break;

    case UV_FS_UTIME:
    case UV_FS_FUTIME:
      _env->CallVoidMethod(
          _instance,
          _utime_callback_mid,
          fs_type,
          request->context(),
          -1,
          exception);
      break;

    case UV_FS_STAT:
    case UV_FS_LSTAT:
    case UV_FS_FSTAT:
      _env->CallVoidMethod(
          _instance,
          _stats_callback_mid,
          fs_type,
          request->context(),
          NULL,
          exception);
      break;

    case UV_FS_READLINK:
      _env->CallVoidMethod(
          _instance,
          _readlink_callback_mid,
          request->context(),
          NULL,
          exception);
      break;

    case UV_FS_READDIR:
      _env->CallVoidMethod(
          _instance,
          _readdir_callback_mid,
          request->context(),
          NULL,
          exception);
      break;

    case UV_FS_READ:
      _env->CallVoidMethod(
          _instance,
          _read_callback_mid,
          request->context(),
          -1,
          request->buffer(),
          exception);
      break;

    case UV_FS_WRITE:
      _env->CallVoidMethod(
          _instance,
          _write_callback_mid,
          request->context(),
          -1,
          exception);
      break;

    default:
      assert(0 && "Unhandled eio response");
  }

  if (path) {
    _env->ReleaseStringUTFChars(path, cpath);
  }
}

static void _fs_cb(uv_fs_t* req) {
  assert(req);
  assert(req->data);

  FileRequest* request = reinterpret_cast<FileRequest*>(req->data);
  assert(request);
  FileCallbacks* cb = request->callback();
  assert(cb);

  if (req->result == -1) {
    cb->fs_cb(request, req->fs_type, req->errorno);
  } else {
    cb->fs_cb(request, req->fs_type, req->result, req->ptr);
  }

  uv_fs_req_cleanup(req);
  delete(req);
  delete(request);
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _static_initialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_Files__1static_1initialize
  (JNIEnv *env, jclass cls) {

  FileCallbacks::static_initialize(env, cls);
  Stats::static_initialize(env);
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _new
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_net_java_libuv_Files__1new
  (JNIEnv *env, jclass cls) {

  FileCallbacks* cb = new FileCallbacks();
  return reinterpret_cast<jlong>(cb);
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _initialize
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_Files__1initialize
  (JNIEnv *env, jobject that, jlong ptr, jlong loop_ptr) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  cb->initialize(that, loop);
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _close
* Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1close__J
  (JNIEnv *env, jobject that, jlong ptr) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  delete cb;
  return 0;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _close
* Signature: (JILjava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1close__JILjava_lang_Object_2
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    r = uv_fs_close(cb->loop(), req, fd, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_close(cb->loop(), &req, fd, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_close");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _open
 * Signature: (JLjava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1open
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jint flags, jint mode, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int fd;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    fd = uv_fs_open(cb->loop(), req, cpath, flags, mode, _fs_cb);
  } else {
    uv_fs_t req;
    fd = uv_fs_open(cb->loop(), &req, cpath, flags, mode, NULL);
    uv_fs_req_cleanup(&req);
    if (fd == -1) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_open", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return fd;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _read
 * Signature: (JILjava/nio/ByteBuffer;[BJJJLjava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1read
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jobject buffer, jbyteArray data, jlong length, jlong offset, jlong position, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    FileRequest* request = new FileRequest(cb, context, fd, NULL);
    jbyte* bytes = request->get_bytes(buffer, data, static_cast<jsize>(offset), static_cast<jsize>(length));
    req->data = request;
    r = uv_fs_read(cb->loop(), req, fd, bytes + offset, length, position, _fs_cb);
  } else {
    uv_fs_t req;
    if (data) {
      jbyte* base = new jbyte[length];
      r = uv_fs_read(cb->loop(), &req, fd, base, length, position, NULL);
      env->SetByteArrayRegion(data, (jsize) offset, (jsize) length, base);
      delete[] base;
    } else {
      jbyte* base = (jbyte*) env->GetDirectBufferAddress(buffer);
      r = uv_fs_read(cb->loop(), &req, fd, base + offset, length, position, NULL);
    }
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_read");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _unlink
 * Signature: (JLjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1unlink
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_unlink(cb->loop(), req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_unlink(cb->loop(), &req, cpath, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_unlink", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _write
 * Signature: (JILjava/nio/ByteBuffer;[BJJJLjava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1write
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jobject buffer, jbyteArray data, jlong length, jlong offset, jlong position, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    if (data) {
      jbyte* base = (jbyte*) env->GetPrimitiveArrayCritical(data, NULL);
      OOME(env, base);
      r = uv_fs_write(cb->loop(), req, fd, base + offset, length, position, _fs_cb);
      env->ReleasePrimitiveArrayCritical(data, base, 0);
    } else {
      jbyte* base = (jbyte*) env->GetDirectBufferAddress(buffer);
      r = uv_fs_write(cb->loop(), req, fd, base + offset, length, position, _fs_cb);
    }
  } else {
    uv_fs_t req;
    if (data) {
      jbyte* base = (jbyte*) env->GetPrimitiveArrayCritical(data, NULL);
      OOME(env, base);
      r = uv_fs_write(cb->loop(), &req, fd, base + offset, length, position, NULL);
      env->ReleasePrimitiveArrayCritical(data, base, 0);
    } else {
      jbyte* base = (jbyte*) env->GetDirectBufferAddress(buffer);
      r = uv_fs_write(cb->loop(), &req, fd, base + offset, length, position, NULL);
    }
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_write");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _mkdir
 * Signature: (JLjava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1mkdir
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jint mode, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_mkdir(cb->loop(), req, cpath, mode, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_mkdir(cb->loop(), &req, cpath, mode, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_mkdir", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _rmdir
 * Signature: (JLjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1rmdir
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_rmdir(cb->loop(), req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_rmdir(cb->loop(), &req, cpath, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_rmdir", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _readdir
 * Signature: (JLjava/lang/String;II)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_net_java_libuv_Files__1readdir
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jint flags, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  jobjectArray names = NULL;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    uv_fs_readdir(cb->loop(), req, cpath, flags, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_readdir(cb->loop(), &req, cpath, flags, NULL);
    if (r >= 0) {
        char *namebuf = static_cast<char*>(req.ptr);
        int nnames = static_cast<int>(req.result);
        names = env->NewObjectArray(nnames, FileCallbacks::_string_cid, 0);

        for (int i = 0; i < nnames; i++) {
          jstring name = env->NewStringUTF(namebuf);
          env->SetObjectArrayElement(names, i, name);
#ifndef NDEBUG
          namebuf += strlen(namebuf);
          assert(*namebuf == '\0');
          namebuf += 1;
#else
          namebuf += strlen(namebuf) + 1;
#endif
        }
    } else {
        ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_readdir", NULL, cpath);
    }
    uv_fs_req_cleanup(&req);
  }
  env->ReleaseStringUTFChars(path, cpath);
  return names;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _stat
 * Signature: (JLjava/lang/String;I)Lnet/java/libuv/Stats;
 */
JNIEXPORT jobject JNICALL Java_net_java_libuv_Files__1stat
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  jobject stats = NULL;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    uv_fs_stat(cb->loop(), req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_stat(cb->loop(), &req, cpath, NULL);
    stats = Stats::create(static_cast<uv_statbuf_t *>(req.ptr));
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_stat", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return stats;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _fstat
 * Signature: (JII)Lnet/java/libuv/Stats;
 */
JNIEXPORT jobject JNICALL Java_net_java_libuv_Files__1fstat
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  jobject stats = NULL;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    uv_fs_fstat(cb->loop(), req, fd, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_fstat(cb->loop(), &req, fd, NULL);
    stats = Stats::create(static_cast<uv_statbuf_t*>(req.ptr));
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_fstat");
    }
  }
  return stats;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _rename
 * Signature: (JLjava/lang/String;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1rename
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jstring new_path, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* src_path = env->GetStringUTFChars(path, 0);
  const char* dst_path = env->GetStringUTFChars(new_path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_rename(cb->loop(), req, src_path, dst_path, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_rename(cb->loop(), &req, src_path, dst_path, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_rename", NULL, src_path);
    }
  }
  env->ReleaseStringUTFChars(path, src_path);
  env->ReleaseStringUTFChars(new_path, dst_path);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _fsync
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1fsync
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    r = uv_fs_fsync(cb->loop(), req, fd, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_fsync(cb->loop(), &req, fd, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_fsync");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _fdatasync
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1fdatasync
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    r = uv_fs_fdatasync(cb->loop(), req, fd, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_fdatasync(cb->loop(), &req, fd, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_fdatasync");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _ftruncate
 * Signature: (JIJI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1ftruncate
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jlong offset, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    r = uv_fs_ftruncate(cb->loop(), req, fd, offset, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_ftruncate(cb->loop(), &req, fd, offset, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_ftruncate");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _sendfile
 * Signature: (JIIJJI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1sendfile
  (JNIEnv *env, jobject that, jlong ptr, jint out_fd, jint in_fd, jlong offset, jlong length, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, in_fd, NULL);
    r = uv_fs_sendfile(cb->loop(), req, out_fd, in_fd, offset, length, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_sendfile(cb->loop(), &req, out_fd, in_fd, offset, length, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_sendfile");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _chmod
 * Signature: (JLjava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1chmod
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jint mode, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_chmod(cb->loop(), req, cpath, mode, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_chmod(cb->loop(), &req, cpath, mode, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_chmod", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _utime
 * Signature: (JLjava/lang/String;DDI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1utime
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jdouble atime, jdouble mtime, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_utime(cb->loop(), req, cpath, atime, mtime, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_utime(cb->loop(), &req, cpath, atime, mtime, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_utime", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _futime
 * Signature: (JIDDI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1futime
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jdouble atime, jdouble mtime, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    r = uv_fs_futime(cb->loop(), req, fd, atime, mtime, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_futime(cb->loop(), &req, fd, atime, mtime, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_futime");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _lstat
 * Signature: (JLjava/lang/String;I)Lnet/java/libuv/Stats;
 */
JNIEXPORT jobject JNICALL Java_net_java_libuv_Files__1lstat
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  jobject stats = NULL;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    uv_fs_lstat(cb->loop(), req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_lstat(cb->loop(), &req, cpath, NULL);
    stats = Stats::create(static_cast<uv_statbuf_t*>(req.ptr));
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_lstat", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return stats;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _link
 * Signature: (JLjava/lang/String;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1link
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jstring new_path, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* src_path = env->GetStringUTFChars(path, 0);
  const char* dst_path = env->GetStringUTFChars(new_path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_link(cb->loop(), req, src_path, dst_path, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_link(cb->loop(), &req, src_path, dst_path, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_link", NULL, src_path);
    }
  }
  env->ReleaseStringUTFChars(path, src_path);
  env->ReleaseStringUTFChars(new_path, dst_path);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _symlink
 * Signature: (JLjava/lang/String;Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1symlink
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jstring new_path, jint flags, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* src_path = env->GetStringUTFChars(path, 0);
  const char* dst_path = env->GetStringUTFChars(new_path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_symlink(cb->loop(), req, src_path, dst_path, flags, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_symlink(cb->loop(), &req, src_path, dst_path, flags, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_symlink", NULL, src_path);
    }
  }
  env->ReleaseStringUTFChars(path, src_path);
  env->ReleaseStringUTFChars(new_path, dst_path);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _readlink
 * Signature: (JLjava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_java_libuv_Files__1readlink
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  jstring link = NULL;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    uv_fs_readlink(cb->loop(), req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_readlink(cb->loop(), &req, cpath, NULL);
    link = env->NewStringUTF(static_cast<char*>(req.ptr));
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_readklink", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return link;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _fchmod
 * Signature: (JIII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1fchmod
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jint mode, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    r = uv_fs_fchmod(cb->loop(), req, fd, mode, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_fchmod(cb->loop(), &req, fd, mode, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_fchmod");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _chown
 * Signature: (JLjava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1chown
  (JNIEnv *env, jobject that, jlong ptr, jstring path, jint uid, jint gid, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, 0, path);
    r = uv_fs_chown(cb->loop(), req, cpath, (uv_uid_t) uid, (uv_gid_t) gid, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_chown(cb->loop(), &req, cpath, (uv_uid_t) uid, (uv_gid_t) gid, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_chown", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_Files
 * Method:    _fchown
 * Signature: (JIIII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_Files__1fchown
  (JNIEnv *env, jobject that, jlong ptr, jint fd, jint uid, jint gid, jobject context) {

  assert(ptr);
  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(ptr);
  int r;

  if (context) {
    uv_fs_t* req = new uv_fs_t();
    req->data = new FileRequest(cb, context, fd, NULL);
    r = uv_fs_fchown(cb->loop(), req, fd, (uv_uid_t) uid, (uv_gid_t) gid, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_fchown(cb->loop(), &req, fd, (uv_uid_t) uid, (uv_gid_t) gid, NULL);
    uv_fs_req_cleanup(&req);
    if (r < 0) {
      ThrowException(env, uv_last_error(cb->loop()).code, "uv_fs_fchown");
    }
  }
  return r;
}
