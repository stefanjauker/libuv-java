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
#include <stdlib.h>
#include <jni.h>

#include "context.h"

ContextHolder::ContextHolder(JNIEnv* env, jobject data, jobject context, jobject callback) {
  _env = env;
  _data = data ? (jobject) env->NewGlobalRef(data) : NULL;
  _context = context ? (jobject) env->NewGlobalRef(context) : NULL;
  _callback = callback ? (jobject) env->NewGlobalRef(callback) : NULL;
}

ContextHolder::ContextHolder(JNIEnv* env, jobject data, jobject context) {
  _env = env;
  _data = data ? (jobject) env->NewGlobalRef(data) : NULL;
  _context = context ? (jobject) env->NewGlobalRef(context) : NULL;
  _callback = NULL;
}

ContextHolder::ContextHolder(JNIEnv* env, jobject context) {
  _env = env;
  _data = NULL;
  _context = context ? (jobject) env->NewGlobalRef(context) : NULL;
  _callback = NULL;
}

ContextHolder::~ContextHolder() {
  if (_context) {
    _env->DeleteGlobalRef(_context);
  }
  if (_callback) {
    _env->DeleteGlobalRef(_callback);
  }
  if (_data) {
    _env->DeleteGlobalRef(_data);
  }
}
