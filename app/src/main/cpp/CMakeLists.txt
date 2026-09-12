cmake_minimum_required(VERSION 3.28.0)
project(native-lib)

# Padrão C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Flags de compilação gerais
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall")

# Suporte solicitado pelo Gradle para page size flexível
set(ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES ON CACHE BOOL "" FORCE)

# Força alinhamento para 16KB em todas as bibliotecas compartilhadas geradas
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

# Biblioteca nativa principal
add_library(
    native-lib
    SHARED
    native-lib.cpp
)

# Biblioteca padrão do Android (logcat)
find_library(
    log-lib
    log
)

# Linkagem da biblioteca
target_link_libraries(
    native-lib
    ${log-lib}
)