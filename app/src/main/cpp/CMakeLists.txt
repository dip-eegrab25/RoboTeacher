cmake_minimum_required(VERSION 3.10.2)
project("whisper")

add_library(whisper SHARED
        whisper.cpp
        ggml.c
)

find_library(log-lib log)

target_link_libraries(
        whisper
        ${log-lib}
)
