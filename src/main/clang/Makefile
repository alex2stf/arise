#debuggers: ASTX_DBG_ECMA, ASTX_DBG_SSH2
PLATFORM=$(shell uname)

COMPILER=g++
CCOMPILER=gcc

BUILD_DIR=build/
ASTOX_SRC=src/
CODE_SRC=./
TESTS_SRC=test/src/
BIN_DIR=bin/

#final linker flags
LIBS=-lpthread -lm
LIB_PATHS=
INCLUDES=-Iinclude
TEST_INCLUDES=-Itest/include
DEFINES=
OTHER_FLAGS=







all: rfcommserver



rfcommserver:
	gcc rfcomm-server.c -o server -lbluetooth
