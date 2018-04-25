/*******************************************************************************
 * JNotify - Allow java applications to register to File system events.
 *
 * Copyright (C) 2005 - Content Objects
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 ******************************************************************************
 *
 * You may also redistribute and/or modify this library under the terms of the
 * Eclipse Public License. See epl.html.
 *
 ******************************************************************************
 *
 * Content Objects, Inc., hereby disclaims all copyright interest in the
 * library `JNotify' (a Java library for file system events).
 *
 * Yahali Sherman, 21 November 2005
 *    Content Objects, VP R&D.
 *
 ******************************************************************************
 * Author : Omry Yadan
 ******************************************************************************/

package com.fuxi.javaagent.contentobjects.jnotify;


public class JNotify {
    public static final int FILE_CREATED = 0x1;
    public static final int FILE_DELETED = 0x2;
    public static final int FILE_MODIFIED = 0x4;
    public static final int FILE_RENAMED = 0x8;
    public static final int FILE_ANY = FILE_CREATED | FILE_DELETED | FILE_MODIFIED | FILE_RENAMED;

    private static IJNotify _instance;
    private static boolean nativeLibraryLoaded;
    private static Object lock = new Object();
    private static Error nativeLoadError = null;
    private static Exception nativeLoadException = null;

    static {

    }

    public static void init(String releasePath) {

        String osName = System.getProperty("os.name").toLowerCase();
        String libFullName;
        if (osName.equals("linux")) {
            try {
                if (System.getProperty("os.arch").contains("64")) {
                    libFullName = "libjnotify_64bit.so";
                } else {
                    libFullName = "libjnotify_32bit.so";
                }
                load(releasePath, libFullName);
                _instance = (IJNotify) Class.forName("com.fuxi.javaagent.contentobjects.jnotify.linux.JNotifyAdapterLinux").newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (osName.startsWith("windows")) {
            try {
                if (System.getProperty("os.arch").contains("64")) {
                    libFullName = "jnotify_64bit.dll";
                } else {
                    libFullName = "jnotify.dll";
                }
                load(releasePath, libFullName);
                _instance = (IJNotify) Class.forName("com.fuxi.javaagent.contentobjects.jnotify.win32.JNotifyAdapterWin32").newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (osName.startsWith("mac os x")) {
            try {
                load(releasePath, "libjnotify.dylib");
                _instance = (IJNotify) Class.forName("com.fuxi.javaagent.contentobjects.jnotify.macosx.JNotifyAdapterMacOSX").newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Unsupported OS : " + osName);
        }

    }

    private synchronized static void load(final String tmpDirectory, String libFullName) {
        if (!nativeLibraryLoaded) {
            synchronized (lock) {
                if (!nativeLibraryLoaded) {
                    try {
                        LibraryLoader.loadLibrary(tmpDirectory, libFullName);
                        nativeLibraryLoaded = true;
                        checkNativeLibraryLoaded();
                    } catch (Error e) {
                        nativeLoadError = e;
                    } catch (Exception e) {
                        nativeLoadException = e;
                    }
                }
            }
        }
    }

    private static void checkNativeLibraryLoaded() {
        if (!nativeLibraryLoaded) {
            if (nativeLoadError != null) {
                throw new IllegalStateException("Jnotify native library not loaded", nativeLoadError);
            } else if (nativeLoadException != null) {
                throw new IllegalStateException("Jnotify native library not loaded", nativeLoadException);
            } else {
                throw new IllegalStateException("Jnotify native library not loaded");
            }
        }
    }

    public static int addWatch(String path, int mask, boolean watchSubtree, JNotifyListener listener) throws JNotifyException {
        return _instance.addWatch(path, mask, watchSubtree, listener);
    }

    public static boolean removeWatch(int watchId) throws JNotifyException {
        return _instance.removeWatch(watchId);
    }
}
