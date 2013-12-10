{
    'includes': {
        'config.gypi',
    },
    'variables': {
        'LIBUV_HOME%': '<(SOURCE_HOME%)/deps/uv',
        'LIBUV_JAVA_HOME%': '<(LIBUV_JAVA_HOME%)',
        'SRC%': './out/<(target)/obj.target',
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
                            'VCCLCompilerTool': {
                                'ObjectFile': 'out\<(target)\obj.target\libuv-java\\',
                            },
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
                '<(SRC)/libuv-java',
            ],
            'conditions': [
                ['OS == "linux"', {
                    'sources': [
                        'async.cpp',
                        'check.cpp',
                        'child_process.cpp',
                        'constants.cpp',
                        'context.cpp',
                        'exception.cpp',
                        'file.cpp',
                        'file_event.cpp',
                        'file_poll.cpp',
                        'handle.cpp',
                        'idle.cpp',
                        'loop.cpp',
                        'misc.cpp',
                        'os.cpp',
                        'pipe.cpp',
                        'process.cpp',
                        'signal.cpp',
                        'stats.cpp',
                        'stream.cpp',
                        'timer.cpp',
                        'tcp.cpp',
                        'tty.cpp',
                        'udp.cpp',
                    ],
                    'libraries': [
                    ],
                    'defines': [
                        '__POSIX__',
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
                        '<(SRC)/libuv-java/async.cpp',
                        '<(SRC)/libuv-java/check.cpp',
                        '<(SRC)/libuv-java/child_process.cpp',
                        '<(SRC)/libuv-java/constants.cpp',
                        '<(SRC)/libuv-java/context.cpp',
                        '<(SRC)/libuv-java/exception.cpp',
                        '<(SRC)/libuv-java/file.cpp',
                        '<(SRC)/libuv-java/file_event.cpp',
                        '<(SRC)/libuv-java/file_poll.cpp',
                        '<(SRC)/libuv-java/handle.cpp',
                        '<(SRC)/libuv-java/idle.cpp',
                        '<(SRC)/libuv-java/loop.cpp',
                        '<(SRC)/libuv-java/misc.cpp',
                        '<(SRC)/libuv-java/os.cpp',
                        '<(SRC)/libuv-java/pipe.cpp',
                        '<(SRC)/libuv-java/process.cpp',
                        '<(SRC)/libuv-java/signal.cpp',
                        '<(SRC)/libuv-java/stats.cpp',
                        '<(SRC)/libuv-java/stream.cpp',
                        '<(SRC)/libuv-java/timer.cpp',
                        '<(SRC)/libuv-java/tcp.cpp',
                        '<(SRC)/libuv-java/tty.cpp',
                        '<(SRC)/libuv-java/udp.cpp',
                    ],
                    'libraries': [
                        '-luv',
                    ],
                    'defines': [
                        '__POSIX__',
                        '__MACOS__'
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
                        '<(SRC)/libuv-java/async.cpp',
                        '<(SRC)/libuv-java/check.cpp',
                        '<(SRC)/libuv-java/child_process.cpp',
                        '<(SRC)/libuv-java/constants.cpp',
                        '<(SRC)/libuv-java/context.cpp',
                        '<(SRC)/libuv-java/exception.cpp',
                        '<(SRC)/libuv-java/file.cpp',
                        '<(SRC)/libuv-java/file_event.cpp',
                        '<(SRC)/libuv-java/file_poll.cpp',
                        '<(SRC)/libuv-java/handle.cpp',
                        '<(SRC)/libuv-java/idle.cpp',
                        '<(SRC)/libuv-java/loop.cpp',
                        '<(SRC)/libuv-java/misc.cpp',
                        '<(SRC)/libuv-java/os.cpp',
                        '<(SRC)/libuv-java/pipe.cpp',
                        '<(SRC)/libuv-java/process.cpp',
                        '<(SRC)/libuv-java/signal.cpp',
                        '<(SRC)/libuv-java/stats.cpp',
                        '<(SRC)/libuv-java/stream.cpp',
                        '<(SRC)/libuv-java/timer.cpp',
                        '<(SRC)/libuv-java/tcp.cpp',
                        '<(SRC)/libuv-java/tty.cpp',
                        '<(SRC)/libuv-java/udp.cpp',
                    ],
                    'defines': [
                        '_UNICODE',
                        'UNICODE',
                        '_WIN32_WINNT=0x0600',
                        '_WIN32',
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
