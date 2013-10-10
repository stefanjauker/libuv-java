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
#include "throw.h"
#include "net_java_libuv_handles_FileHandle.h"

class FileCallbacks {
private:
  static jclass _int_cid;
  static jclass _long_cid;
  static jclass _file_handle_cid;
  static jclass _stats_cid;

  static jmethodID _int_valueof_mid;
  static jmethodID _long_valueof_mid;
  static jmethodID _callback_1arg_mid;
  static jmethodID _callback_narg_mid;
  static jmethodID _stats_init_mid;

  static JNIEnv* _env;

  jobject _instance;

public:

  static jclass _object_cid;

  static void static_initialize(JNIEnv *env, jclass cls);
  static jobject build_stats(uv_statbuf_t *ptr);

  FileCallbacks();
  ~FileCallbacks();

  int _callback_id;
  jlong _read_buffer_offset;
  jobject _read_buffer;
  jbyte* _read_bytes_array;

  void initialize(jobject instance);
  void fs_cb(uv_fs_type fs_type, ssize_t result, void *ptr);
  void fs_cb(uv_fs_type fs_type, int errorno, const char *path);
};

jclass FileCallbacks::_int_cid = NULL;
jclass FileCallbacks::_long_cid = NULL;
jclass FileCallbacks::_file_handle_cid = NULL;
jclass FileCallbacks::_object_cid = NULL;
jclass FileCallbacks::_stats_cid = NULL;

jmethodID FileCallbacks::_int_valueof_mid = NULL;
jmethodID FileCallbacks::_long_valueof_mid = NULL;
jmethodID FileCallbacks::_callback_1arg_mid = NULL;
jmethodID FileCallbacks::_callback_narg_mid = NULL;
jmethodID FileCallbacks::_stats_init_mid = NULL;

JNIEnv* FileCallbacks::_env = NULL;

void FileCallbacks::static_initialize(JNIEnv* env, jclass cls) {
  _env = env;
  assert(_env);

  _int_cid = env->FindClass("java/lang/Integer");
  assert(_int_cid);
  _int_cid = (jclass) env->NewGlobalRef(_int_cid);
  assert(_int_cid);

  _long_cid = env->FindClass("java/lang/Long");
  assert(_long_cid);
  _long_cid = (jclass) env->NewGlobalRef(_long_cid);
  assert(_long_cid);

  _object_cid = env->FindClass("java/lang/Object");
  assert(_object_cid);
  _object_cid = (jclass) env->NewGlobalRef(_object_cid);
  assert(_object_cid);

  _stats_cid = env->FindClass("net/java/libuv/handles/Stats");
  assert(_stats_cid);
  _stats_cid = (jclass) env->NewGlobalRef(_stats_cid);
  assert(_stats_cid);

  _int_valueof_mid = env->GetStaticMethodID(_int_cid, "valueOf", "(I)Ljava/lang/Integer;");
  assert(_int_valueof_mid);

  _long_valueof_mid = env->GetStaticMethodID(_long_cid, "valueOf", "(J)Ljava/lang/Long;");
  assert(_long_valueof_mid);

  _file_handle_cid = (jclass) env->NewGlobalRef(cls);
  assert(_file_handle_cid);

  _callback_1arg_mid = env->GetMethodID(_file_handle_cid, "callback", "(IILjava/lang/Object;)V");
  assert(_callback_1arg_mid);
  _callback_narg_mid = env->GetMethodID(_file_handle_cid, "callback", "(II[Ljava/lang/Object;)V");
  assert(_callback_narg_mid);

  _stats_init_mid = env->GetMethodID(_stats_cid, "<init>", "(IIIIIIIJIJJJJ)V");
  assert(_stats_init_mid);
}

jobject FileCallbacks::build_stats(uv_statbuf_t *ptr) {
  if (ptr) {
    int blksize = 0;
    double blocks = 0;
#ifdef __POSIX__
    blksize = ptr->st_blksize;
    blocks = ptr->st_blocks;
#endif

    return _env->NewObject(_stats_cid,
      _stats_init_mid,
      ptr->st_dev,
      ptr->st_ino,
      ptr->st_mode,
      ptr->st_nlink,
      ptr->st_uid,
      ptr->st_gid,
      ptr->st_rdev,
      ptr->st_size,
      blksize,
      blocks,
      ptr->st_atime * 1000,     // Convert seconds to milliseconds
      ptr->st_mtime * 1000,     // Convert seconds to milliseconds
      ptr->st_ctime * 1000);    // Convert seconds to milliseconds
  }
  return NULL;
}

FileCallbacks::FileCallbacks() {
}

FileCallbacks::~FileCallbacks() {
  _env->DeleteGlobalRef(_instance);
}

void FileCallbacks::initialize(jobject instance) {

  assert(_env);
  assert(instance);

  _instance = _env->NewGlobalRef(instance);
}

void FileCallbacks::fs_cb(uv_fs_type fs_type, ssize_t result, void *ptr) {

  assert(_env);
  jobject arg;

  switch (fs_type) {
    case UV_FS_CLOSE:
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
      arg = NULL;
      break;

    case UV_FS_OPEN:
      arg = _env->CallStaticObjectMethod(_int_cid, _int_valueof_mid, result);
      break;

    case UV_FS_UTIME:
    case UV_FS_FUTIME:
    case UV_FS_WRITE:
      arg = _env->CallStaticObjectMethod(_long_cid, _long_valueof_mid, result);
      break;

    case UV_FS_READ: {
      jobjectArray args = _env->NewObjectArray(2, _object_cid, 0);
      assert(args);
      jobject bytesRead = _env->CallStaticObjectMethod(_long_cid, _long_valueof_mid, result);
      _env->SetByteArrayRegion(static_cast<jbyteArray>(_read_buffer), static_cast<jsize>(_read_buffer_offset), static_cast<jsize>(result), _read_bytes_array);
      _env->SetObjectArrayElement(args, 0, bytesRead);
      _env->SetObjectArrayElement(args, 1, _read_buffer);
      _env->CallVoidMethod(
          _instance,
          _callback_narg_mid,
          fs_type,
          _callback_id,
          args);
      _env->DeleteGlobalRef(_read_buffer);
      delete _read_bytes_array;
      _read_buffer_offset = 0;
      return;
    }

    case UV_FS_STAT:
    case UV_FS_LSTAT:
    case UV_FS_FSTAT:
      arg = build_stats(static_cast<uv_statbuf_t*>(ptr));
      break;

    case UV_FS_READLINK:
      arg = _env->NewStringUTF(static_cast<char*>(ptr));
      break;

    case UV_FS_READDIR: {
      char *namebuf = static_cast<char*>(ptr);
      int nnames = static_cast<int>(result);

      jobjectArray names = _env->NewObjectArray(nnames, _object_cid, 0);

      for (int i = 0; i < nnames; i++) {
        jstring name = _env->NewStringUTF(namebuf);
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
          _callback_narg_mid,
          fs_type,
          _callback_id,
          names);
      return;
    }

    default:
      assert(0 && "Unhandled eio response");
  }

  _env->CallVoidMethod(
      _instance,
      _callback_1arg_mid,
      fs_type,
      _callback_id,
      arg);
}

void FileCallbacks::fs_cb(uv_fs_type fs_type, int errorno, const char *path) {

  assert(_env);

  if (fs_type == UV_FS_READ) {
      _env->DeleteGlobalRef(_read_buffer);
      delete _read_bytes_array;
  }

  jobject error = _env->CallStaticObjectMethod(_int_cid, _int_valueof_mid, -1);
  jthrowable exception = NewException(_env, errorno, NULL, NULL, path);
  jobjectArray args = _env->NewObjectArray(2, _object_cid, 0);
  assert(args);
  _env->SetObjectArrayElement(args, 0, error);
  _env->SetObjectArrayElement(args, 1, exception);
  _env->CallVoidMethod(
      _instance,
      _callback_narg_mid,
      fs_type,
      _callback_id,
      args);
}

static void _fs_cb(uv_fs_t* req) {

  assert(req);
  assert(req->data);

  FileCallbacks* cb = reinterpret_cast<FileCallbacks*>(req->data);

  if (req->result == -1) {
    cb->fs_cb(req->fs_type, req->errorno, req->path);
  } else {
    cb->fs_cb(req->fs_type, req->result, req->ptr);
  }
  uv_fs_req_cleanup(req);
  delete(req);
}

static FileCallbacks* initialize(jobject obj, int id) {

  assert(obj);

  FileCallbacks* cb = new FileCallbacks();
  cb->initialize(obj);
  cb->_callback_id = id;
  return cb;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _static_initialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_java_libuv_handles_FileHandle__1static_1initialize
  (JNIEnv *env, jclass cls) {

  FileCallbacks::static_initialize(env, cls);
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _close
* Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1close
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_close(loop, req, fd, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_close(loop, &req, fd, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_close");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _open
 * Signature: (JLjava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1open
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint flags, jint mode, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int fd;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    fd = uv_fs_open(loop, req, cpath, flags, mode, _fs_cb);
  } else {
    uv_fs_t req;
    fd = uv_fs_open(loop, &req, cpath, flags, mode, NULL);
    uv_fs_req_cleanup(&req);
    if (fd == -1) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_open", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return fd;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _read
 * Signature: (JI[BJJJI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1read
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jbyteArray buffer, jlong length, jlong offset, jlong position, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  jbyte* base = new jbyte[length - offset];
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    FileCallbacks* cb = initialize(that, callback);
    cb->_read_bytes_array = base;
    cb->_read_buffer = env->NewGlobalRef(buffer);
    cb->_read_buffer_offset = offset;
    req->data = cb;
    r = uv_fs_read(loop, req, fd, base, length, position, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_read(loop, &req, fd, base, length, position, NULL);
    env->SetByteArrayRegion(buffer, (jsize)offset, (jsize)length, base);
    uv_fs_req_cleanup(&req);
    delete base;
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_read");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _unlink
 * Signature: (JLjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1unlink
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_unlink(loop, req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_unlink(loop, &req, cpath, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_unlink", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _write
 * Signature: (JI[BJJJI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1write
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jbyteArray data, jlong length, jlong offset, jlong position, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);

  jbyte* base = new jbyte[length - offset];
  env->GetByteArrayRegion(data, (jsize)offset, (jsize)length, base);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_write(loop, req, fd, base, length, position, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_write(loop, &req, fd, base, length, position, NULL);
    uv_fs_req_cleanup(&req);
    delete base;
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_write");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _mkdir
 * Signature: (JLjava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1mkdir
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint mode, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_mkdir(loop, req, cpath, mode, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_mkdir(loop, &req, cpath, mode, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_mkdir", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _rmdir
 * Signature: (JLjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1rmdir
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_rmdir(loop, req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_rmdir(loop, &req, cpath, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_rmdir", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _readdir
 * Signature: (JLjava/lang/String;II)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_net_java_libuv_handles_FileHandle__1readdir
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint flags, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  jobjectArray names = NULL;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    uv_fs_readdir(loop, req, cpath, flags, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_readdir(loop, &req, cpath, flags, NULL);
    if (r == UV_OK) {
        char *namebuf = static_cast<char*>(req.ptr);
        int nnames = static_cast<int>(req.result);
        names = env->NewObjectArray(nnames, FileCallbacks::_object_cid, 0);

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
        ThrowException(env, uv_last_error(loop).code, "uv_fs_readdir", NULL, cpath);
    }
    uv_fs_req_cleanup(&req);
  }
  env->ReleaseStringUTFChars(path, cpath);
  return names;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _stat
 * Signature: (JLjava/lang/String;I)Lnet/java/libuv/handles/Stats;
 */
JNIEXPORT jobject JNICALL Java_net_java_libuv_handles_FileHandle__1stat
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  jobject stats = NULL;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    uv_fs_stat(loop, req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_stat(loop, &req, cpath, NULL);
    stats = FileCallbacks::build_stats(static_cast<uv_statbuf_t *>(req.ptr));
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_stat", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return stats;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _fstat
 * Signature: (JII)Lnet/java/libuv/handles/Stats;
 */
JNIEXPORT jobject JNICALL Java_net_java_libuv_handles_FileHandle__1fstat
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  jobject stats = NULL;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    uv_fs_fstat(loop, req, fd, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_fstat(loop, &req, fd, NULL);
    stats = FileCallbacks::build_stats(static_cast<uv_statbuf_t*>(req.ptr));
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_fstat");
    }
  }
  return stats;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _rename
 * Signature: (JLjava/lang/String;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1rename
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jstring new_path, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* src_path = env->GetStringUTFChars(path, 0);
  const char* dst_path = env->GetStringUTFChars(new_path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_rename(loop, req, src_path, dst_path, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_rename(loop, &req, src_path, dst_path, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_rename", NULL, src_path);
    }
  }
  env->ReleaseStringUTFChars(path, src_path);
  env->ReleaseStringUTFChars(new_path, dst_path);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _fsync
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1fsync
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_fsync(loop, req, fd, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_fsync(loop, &req, fd, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_fsync");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _fdatasync
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1fdatasync
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_fdatasync(loop, req, fd, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_fdatasync(loop, &req, fd, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_fdatasync");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _ftruncate
 * Signature: (JIJI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1ftruncate
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jlong offset, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_ftruncate(loop, req, fd, offset, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_ftruncate(loop, &req, fd, offset, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_ftruncate");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _sendfile
 * Signature: (JIIJJI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1sendfile
  (JNIEnv *env, jobject that, jlong loop_ptr, jint out_fd, jint in_fd, jlong offset, jlong length, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_sendfile(loop, req, out_fd, in_fd, offset, length, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_sendfile(loop, &req, out_fd, in_fd, offset, length, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_sendfile");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _chmod
 * Signature: (JLjava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1chmod
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint mode, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_chmod(loop, req, cpath, mode, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_chmod(loop, &req, cpath, mode, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_chmod", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _utime
 * Signature: (JLjava/lang/String;DDI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1utime
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jdouble atime, jdouble mtime, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_utime(loop, req, cpath, atime, mtime, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_utime(loop, &req, cpath, atime, mtime, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_utime", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _futime
 * Signature: (JIDDI)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1futime
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jdouble atime, jdouble mtime, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_futime(loop, req, fd, atime, mtime, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_futime(loop, &req, fd, atime, mtime, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_futime");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _lstat
 * Signature: (JLjava/lang/String;I)Lnet/java/libuv/handles/Stats;
 */
JNIEXPORT jobject JNICALL Java_net_java_libuv_handles_FileHandle__1lstat
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  jobject stats = NULL;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    uv_fs_lstat(loop, req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_lstat(loop, &req, cpath, NULL);
    stats = FileCallbacks::build_stats(static_cast<uv_statbuf_t*>(req.ptr));
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_lstat", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return stats;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _link
 * Signature: (JLjava/lang/String;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1link
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jstring new_path, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* src_path = env->GetStringUTFChars(path, 0);
  const char* dst_path = env->GetStringUTFChars(new_path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_link(loop, req, src_path, dst_path, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_link(loop, &req, src_path, dst_path, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_link", NULL, src_path);
    }
  }
  env->ReleaseStringUTFChars(path, src_path);
  env->ReleaseStringUTFChars(new_path, dst_path);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _symlink
 * Signature: (JLjava/lang/String;Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1symlink
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jstring new_path, jint flags, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* src_path = env->GetStringUTFChars(path, 0);
  const char* dst_path = env->GetStringUTFChars(new_path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_symlink(loop, req, src_path, dst_path, flags, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_symlink(loop, &req, src_path, dst_path, flags, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_symlink", NULL, src_path);
    }
  }
  env->ReleaseStringUTFChars(path, src_path);
  env->ReleaseStringUTFChars(new_path, dst_path);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _readlink
 * Signature: (JLjava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_java_libuv_handles_FileHandle__1readlink
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  jstring link = NULL;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    uv_fs_readlink(loop, req, cpath, _fs_cb);
  } else {
    uv_fs_t req;
    int r = uv_fs_readlink(loop, &req, cpath, NULL);
    link = env->NewStringUTF(static_cast<char*>(req.ptr));
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_readklink", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return link;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _fchmod
 * Signature: (JIII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1fchmod
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jint mode, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_fchmod(loop, req, fd, mode, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_fchmod(loop, &req, fd, mode, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_fchmod");
    }
  }
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _chown
 * Signature: (JLjava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1chown
  (JNIEnv *env, jobject that, jlong loop_ptr, jstring path, jint uid, jint gid, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  const char* cpath = env->GetStringUTFChars(path, 0);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_chown(loop, req, cpath, (uv_uid_t)uid, (uv_gid_t)gid, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_chown(loop, &req, cpath, (uv_uid_t)uid, (uv_gid_t)gid, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_chown", NULL, cpath);
    }
  }
  env->ReleaseStringUTFChars(path, cpath);
  return r;
}

/*
 * Class:     net_java_libuv_handles_FileHandle
 * Method:    _fchown
 * Signature: (JIIII)I
 */
JNIEXPORT jint JNICALL Java_net_java_libuv_handles_FileHandle__1fchown
  (JNIEnv *env, jobject that, jlong loop_ptr, jint fd, jint uid, jint gid, jint callback) {

  assert(loop_ptr);
  uv_loop_t* loop = reinterpret_cast<uv_loop_t*>(loop_ptr);
  int r;

  if (callback) {
    uv_fs_t* req = new uv_fs_t();
    req->data = initialize(that, callback);
    r = uv_fs_fchown(loop, req, fd, (uv_uid_t)uid, (uv_gid_t)gid, _fs_cb);
  } else {
    uv_fs_t req;
    r = uv_fs_fchown(loop, &req, fd, (uv_uid_t)uid, (uv_gid_t)gid, NULL);
    uv_fs_req_cleanup(&req);
    if (r != UV_OK) {
      ThrowException(env, uv_last_error(loop).code, "uv_fs_fchown");
    }
  }
  return r;
}
