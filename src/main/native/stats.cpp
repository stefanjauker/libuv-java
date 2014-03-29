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

#include "stats.h"

#include <assert.h>

jclass Stats::_stats_cid = NULL;

jmethodID Stats::_stats_init_mid = NULL;
jmethodID Stats::_stats_set_mid = NULL;

Stats::Stats() {
}

Stats::~Stats() {
}

void Stats::static_initialize(JNIEnv* env) {
  if (!_stats_cid) {
      _stats_cid = env->FindClass("com/oracle/libuv/Stats");
      assert(_stats_cid);
      _stats_cid = (jclass) env->NewGlobalRef(_stats_cid);
      assert(_stats_cid);
  }

  if (!_stats_init_mid) {
    _stats_init_mid = env->GetMethodID(_stats_cid, "<init>", "()V");
    assert(_stats_init_mid);
  }

  if (!_stats_set_mid) {
    _stats_set_mid = env->GetMethodID(_stats_cid, "set", "(JJJJJJJJJJDDDD)V");
    assert(_stats_set_mid);
  }
}

inline static double to_millis(const uv_timespec_t& t) {
    return static_cast<double>(t.tv_nsec) / 1000000.0 + (static_cast<double>(t.tv_sec) * 1000.0);
}

jobject Stats::create(JNIEnv* env, const uv_stat_t* ptr) {
    if (!ptr) return NULL;
    jobject stats = env->NewObject(_stats_cid, _stats_init_mid);
    update(env, stats, ptr);
    env->DeleteLocalRef(stats);
    return stats;
}

void Stats::update(JNIEnv* env, jobject stats, const uv_stat_t* ptr) {
    if (!ptr) return;

    env->CallVoidMethod(
        stats,
        _stats_set_mid,
        ptr->st_dev,
        ptr->st_ino,
        ptr->st_mode,
        ptr->st_nlink,
        ptr->st_uid,
        ptr->st_gid,
        ptr->st_rdev,
        ptr->st_size,
        ptr->st_blksize,
        ptr->st_blocks,
        to_millis(ptr->st_atim),
        to_millis(ptr->st_mtim),
        to_millis(ptr->st_ctim),
        to_millis(ptr->st_birthtim));
}
