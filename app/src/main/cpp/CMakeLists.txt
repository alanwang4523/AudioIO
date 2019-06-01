# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CPP_DIR_PATH ${CMAKE_CURRENT_SOURCE_DIR})
set(OBOE_LIB_PATH ${CPP_DIR_PATH}/oboe/libs/${ANDROID_ABI}/liboboe.a)

set(CPP_FILES_IN_JNI ${CPP_DIR_PATH}/jni/*.cpp)
set(CPP_FILES_IN_AUDIOIO ${CPP_DIR_PATH}/audio_io/*.cpp)

include_directories(${CPP_DIR_PATH}/oboe/include/)
include_directories(${CPP_DIR_PATH}/jni/)

add_library(
        audio_io
        SHARED

        ${CPP_FILES_IN_JNI}
        ${CPP_FILES_IN_AUDIOIO})

target_link_libraries(
        audio_io

        ${OBOE_LIB_PATH}
        OpenSLES
        log)