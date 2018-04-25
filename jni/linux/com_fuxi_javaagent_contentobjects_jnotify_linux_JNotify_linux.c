 
#include "com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux.h"
#include <sys/time.h>
#include <sys/select.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>
#include <linux/inotify.h>
#include "inotify-syscalls.h"

int runLoop(JNIEnv *env, jclass clazz);
void __attribute__ ((destructor)) 	cleanup(void);
void dispatch(JNIEnv *env, jclass clazz, struct inotify_event *event);

int init();
int add_watch(char *path, __u32 mask);
int remove_watch(int wd);

/*
 * Class:     com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux
 * Method:    nativeInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_1linux_nativeInit
  (JNIEnv *env, jclass clazz)
{
	return (jint)init();
}

/*
 * Class:     com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux
 * Method:    nativeAddWatch
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_1linux_nativeAddWatch
  (JNIEnv *env, jclass clazz, jstring path, jint mask)
{
	const char *str;
    str = (*env)->GetStringUTFChars(env, path, NULL);
    if (str == NULL) 
    {
    	return -1; /* OutOfMemoryError already thrown */
    }
    // todo : ERROR HADNLING!
    int wd = add_watch((char*)str, mask);
    (*env)->ReleaseStringUTFChars(env, path, str);
    
	return wd;
}  

/*
 * Class:     com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux
 * Method:    nativeRemoveWatch
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_1linux_nativeRemoveWatch
  (JNIEnv *jni, jclass clazz, jint wd)
{
	return remove_watch(wd);
}
  
/*
 * Class:     com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux
 * Method:    nativeNotifyLoop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_1linux_nativeNotifyLoop
  (JNIEnv *env, jclass clazz)
{
	return runLoop(env, clazz);
}

/**
 * inotify fd.
 */
int fd = -1;


/**
 * initialize the inotify hook.
 * returns:
 * 	0 if all ok.
 * 	errno if initialization failed. (see inotify_init() documentation).
 */
int init()
{
	if (fd != -1)
	{
		return 0;
	}
	
    fd = inotify_init ();
    if (fd < 0)
    {
        return errno;
    }
    else
    {
    	return 0;
    }
}

/**
 * Adds a watch with the specified mask.
 * see inotify_add_watch for ddocumentation.
 * returns -1 on error, or the watch descriptor otherwise.
 */
int add_watch(char *path, __u32 mask)
{
    int wd = inotify_add_watch (fd, path, mask);
    int lastErr = errno;
	if (wd == -1)
	{
		return -lastErr;
	}
	return wd;
}

/**
 * removes a watch descriptor.
 */
int remove_watch(int wd)
{
	int ret = inotify_rm_watch (fd, wd);
    return ret;
}

void cleanup()
{
	if (fd != -1)
	{
		if (close(fd) < 0)
	        perror ("close");
	}
}

int runLoop(JNIEnv *env, jclass clazz)
{
	if (fd == -1)
	{
		return 1;
	}
	
	static int BUF_LEN = 4096;
    char buf[BUF_LEN];    
    int len, i = 0;
    
	while (fd != -1)
	{
	    len = read (fd, buf, BUF_LEN);
	
	    while (i < len) 
	    {
	        struct inotify_event *event = (struct inotify_event *) &buf[i];
	       	dispatch(env, clazz, event);
	        i += sizeof (struct inotify_event) + event->len;
	    }
	    i=0;
	}
	

	return 0;
}

void dispatch(JNIEnv *env, jclass clazz, struct inotify_event *event)
{
    jstring name;
	if (event->len)
	{
		name = (*env)->NewStringUTF(env, event->name);
	}
	else
	{
		char nostr[] = {0};
		name = (*env)->NewStringUTF(env, nostr);
	}
	
     jmethodID mid =   (*env)->GetStaticMethodID(env, clazz, "callbackProcessEvent", "(Ljava/lang/String;III)V");
     if (mid == NULL) 
     {
		 printf("callbackProcessEvent not found! \n");
		 fflush(stdout);
         return;  /* method not found */
     }
     
     (*env)->CallStaticVoidMethod(env, clazz, mid, name, event->wd, event->mask, event->cookie);
	//callbackProcessEvent(String name, int wd, int mask, int cookie)        	
     // we need to delete this or Java will hold it until the thread exits
     (*env)->DeleteLocalRef(env, name);
}


/*
 * Class:     com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux
 * Method:    getErrorDesc
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_1linux_getErrorDesc
  (JNIEnv *env, jclass clazz, jlong errorCode)
{
	const char* err;
	if (errorCode < sys_nerr)
	{
		err = sys_errlist[errorCode];
	}
	else
	{
		err = "Unknown error\0";
	}
	return (*env)->NewStringUTF(env, err);
}
