CC     = clang
SOURCE = com_fuxi_javaagent_contentobjects_jnotify_macosx_JNotify_macosx.c com_fuxi_javaagent_contentobjects_jnotify_macosx_JNotifyAdapterMacOSX_JNFile.c
TARGET = libjnotify.dylib
CFLAGS = -framework CoreFoundation -framework CoreServices \
		 -shared \
		 -I /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include \
		 -I /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include/darwin

default:
	$(CC) $(SOURCE) -o $(TARGET) $(CFLAGS)

clean:
	rm -f *.o $(TARGET)
