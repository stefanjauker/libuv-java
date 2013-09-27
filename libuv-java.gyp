{
    'includes': {
        'config.gypi',
    },
    'variables': {
        'LIBUV_HOME%': '<(SOURCE_HOME%)/deps/uv',
        'LIBUV_PATH%': 'out/<(target)/lib.target',
    },
    'target_defaults': {
        'default_configuration': '<(target)',
        'configurations': {
            'Debug': {
                'defines': [ 'DEBUG', '_DEBUG' ],
                'cflags': [ '-g', '-O0' ],
                'conditions': [
                    ['OS == "win"', {
                        'msvs_settings': {
                            'VCLinkerTool': {
                                'GenerateDebugInformation': 'true',
                            },
                        },
                    }],
                    [ 'OS=="linux"', {
                    }],
                    [ 'OS=="mac"', {
                    }],
                ],
            },
            'Release': {
                'defines': [ 'NDEBUG' ],
                'conditions': [
                    ['OS == "win"', {
                    }],
                    [ 'OS=="linux"', {
                    }],
                    [ 'OS=="mac"', {
                    }],
                ],
            },
        }
    },
    'targets': [
        {
            'target_name': 'libuv-java',
            'type': 'shared_library',
            'defines': [
            ],
            'dependencies': [
            ],
            'include_dirs': [
                '<(JAVA_HOME)/include',
                '<(LIBUV_HOME)/include',
                '<(LIBUV_PATH)/../obj.target/libuv-java',
            ],
            'conditions': [
                ['OS == "linux"', {
                    'sources': [
                        'child.cpp',
                        'handle.cpp',
                        'loop.cpp',
                        'misc.cpp',
                        'os.cpp',
                        'pipe.cpp',
                        'process.cpp',
                        'stream.cpp',
                        'tcp.cpp',
                        'throw.cpp',
                        'tty.cpp',
                        'udp.cpp',
                    ],
                    'libraries': [
                        '-Wl,-soname,libuv.so.0.5',
                        '-L<(LIBUV_PATH)',
                        '-luv',
                    ],
                    'defines': [
                        '__POSIX__',
                        # 'NDEBUG',
                    ],
                    'cflags': [
                        '-fPIC',
                    ],
                    'ldflags': [
                    ],
                    'include_dirs': [
                        '<(JAVA_HOME)/include/linux',
                    ],
                }],
                ['OS == "mac"', {
                    'sources': [
                        '<(LIBUV_PATH)/../obj.target/libuv-java/child.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/handle.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/loop.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/misc.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/os.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/pipe.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/process.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/stream.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/tcp.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/throw.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/tty.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/udp.cpp',
                    ],
                    'libraries': [
                        '-luv',
                    ],
                    'defines': [
                        '__POSIX__',
                        # 'NDEBUG',
                    ],
                    'include_dirs': [
                        '<(JAVA_HOME)/include/darwin',
                    ],
                    'dependencies': [
                        '<(LIBUV_HOME)/uv.gyp:libuv',
                    ],
                }],
                ['OS == "win"', {
                    'dependencies': [
                        '<(LIBUV_HOME)/uv.gyp:libuv',
                    ],
                    'sources': [
                        '<(LIBUV_PATH)/../obj.target/libuv-java/child.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/handle.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/loop.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/misc.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/os.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/pipe.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/process.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/stream.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/tcp.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/throw.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/tty.cpp',
                        '<(LIBUV_PATH)/../obj.target/libuv-java/udp.cpp',
                    ],
                    'defines': [
                        '_WIN32',
                        # 'NDEBUG',
                    ],
                    'cflags': [
                    ],
                    'ldflags': [
                    ],
                    'include_dirs': [
                        '<(JAVA_HOME)/include/win32',
                    ],
                    'libraries': [
                    ],
                }]
            ],
            'actions': [
            ],
        },

    ],

}