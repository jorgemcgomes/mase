CFLAGS = -I../include -fPIC -static

OS = $(shell uname -s)
ifeq ($(OS), Linux)
	CFLAGS += -D__linux
	EXT = so
else
	CFLAGS += -D__APPLE__
	EXT = dylib
endif

all: 
	@rm -f lib/*.$(EXT)
	@rm -f *.o 
	g++ $(CFLAGS) -I jdk/include -I jdk/include/linux -c v_repExtPluginJBot.cpp -o v_repExtPluginJBot.o
	g++ $(CFLAGS) -c ../common/luaFunctionData.cpp -o luaFunctionData.o
	g++ $(CFLAGS) -c ../common/luaFunctionDataItem.cpp -o luaFunctionDataItem.o
	g++ $(CFLAGS) -c ../common/v_repLib.cpp -o v_repLib.o
	@mkdir -p lib
	g++ -D_JNI_IMPLEMENTATION_ luaFunctionData.o luaFunctionDataItem.o v_repExtPluginJBot.o v_repLib.o -shared -L jdk/jre/lib/amd64/server/ -ljvm -o lib/libv_repExtPluginJBot.$(EXT)
	cp lib/libv_repExtPluginJBot.so ../../libv_repExtPluginJBot.so
