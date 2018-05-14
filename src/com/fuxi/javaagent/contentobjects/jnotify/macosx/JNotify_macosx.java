package com.fuxi.javaagent.contentobjects.jnotify.macosx;

import com.fuxi.javaagent.contentobjects.jnotify.JNotifyException;
import com.fuxi.javaagent.contentobjects.jnotify.Observer;

import java.util.ArrayList;
import java.util.List;

public class JNotify_macosx {
    private static Object initCondition = new Object();
    private static Object countLock = new Object();
    private static int watches = 0;

    public static List<Observer> list=new ArrayList<>();
    public static String  msg;

    public static void registerObserver(Observer o){
        list.add(o);
    }
    public static void notifyObserver(){
        for (Observer o: list) {
            o.update(msg);
        }
    }

    public static void setInfomation(String s) {
        JNotify_macosx.msg = s;
        notifyObserver();
    }

    static {
//        String path = null;
//        try {
//            path = URLDecoder.decode(JNotify_linux.class.getResource("/" + "libjnotify.jnilib").getFile().replace("+", "%2B")
//                    , "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        System.load(path);
        Thread thread = new Thread("FSEvent thread") //$NON-NLS-1$
        {

            public void run() {
                try {
                    nativeInit();
                    synchronized (initCondition) {
                        initCondition.notifyAll();
                        initCondition = null;
                    }
                    while (true) {
                        synchronized (countLock) {
                            while (watches == 0) {
                                try {
                                    countLock.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                        nativeNotifyLoop();
                    }
                } catch (Throwable e) {
                    String exception=e.getMessage();
                    setInfomation(exception);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private static native void nativeInit();

    private static native int nativeAddWatch(String path) throws JNotifyException;

    private static native boolean nativeRemoveWatch(int wd);

    private static native void nativeNotifyLoop();

    private static FSEventListener _eventListener;

    public static int addWatch(String path) throws JNotifyException {
        Object myCondition = initCondition;
        if (myCondition != null) {
            synchronized (myCondition) {
                while (initCondition != null) {
                    try {
                        initCondition.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        int wd = nativeAddWatch(path);
        synchronized (countLock) {
            watches++;
            countLock.notifyAll();
        }
        return wd;
    }

    public static boolean removeWatch(int wd) {
        boolean removed = nativeRemoveWatch(wd);
        if (removed) {
            synchronized (countLock) {
                watches--;
            }
        }
        return removed;
    }

    public static void callbackProcessEvent(int wd, String rootPath, String filePath, boolean recurse) {
        if (_eventListener != null) {
            _eventListener.notifyChange(wd, rootPath, filePath, recurse);
        }
    }

    public static void callbackInBatch(int wd, boolean state) {
        if (_eventListener != null) {
            if (state) {
                _eventListener.batchStart(wd);
            } else {
                _eventListener.batchEnd(wd);
            }
        }
    }

    public static void setNotifyListener(FSEventListener eventListener) {
        if (_eventListener == null) {
            _eventListener = eventListener;
        } else {
            throw new RuntimeException("Notify listener is already set. multiple notify listeners are not supported.");
        }
    }
}
