/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package com.fuxi.javaagent.contentobjects.jnotify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class LibraryLoader {

    static final String SEPARATOR;
    static final String DELIMITER;

    static {
        DELIMITER = System.getProperty("line.separator"); //$NON-NLS-1$
        SEPARATOR = System.getProperty("file.separator"); //$NON-NLS-1$
    }

    static void loadLibrary(final String tempDirectory, String libFullName) {

        StringBuffer message = new StringBuffer();
        String path = null;

        if (tempDirectory != null) {
            path = tempDirectory;
        } else {
            path = System.getProperty("user.home"); //$NON-NLS-1$
        }

        if (extract(path + SEPARATOR + libFullName, libFullName, message)) {
            return;
        }

        /* Failed to find the library */
        throw new UnsatisfiedLinkError("Could not load Jnotify library. Reasons: " + message.toString()); //$NON-NLS-1$
    }

    static boolean load(final String libName, final StringBuffer message) {
        try {
            if (libName.indexOf(SEPARATOR) != -1) {
                System.load(libName);
            } else {
                System.loadLibrary(libName);
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            if (message.length() == 0) {
                message.append(DELIMITER);
            }
            message.append('\t');
            message.append(e.getMessage());
            message.append(DELIMITER);
            e.printStackTrace();
        }
        return false;
    }

    static boolean extract(final String fileName, final String mappedName, final StringBuffer message) {
        FileOutputStream os = null;
        InputStream is = null;
        File file = new File(fileName);
        boolean extracted = false;
        try {
            if (file.exists()) {
                file.delete();
            }
            is = LibraryLoader.class.getResourceAsStream("/" + mappedName); //$NON-NLS-1$
            if (is != null) {
                extracted = true;
                int read;
                byte[] buffer = new byte[4096];
                os = new FileOutputStream(fileName);
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.close();
                is.close();
                chmod("755", fileName);
                if (load(fileName, message)) {
                    return true;
                }
            }
        } catch (Throwable e) {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e1) {
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e1) {
            }
            if (extracted && file.exists()) {
                file.delete();
            }
            System.out.println("Unable to extract jnotify library (" + fileName + "):");
            System.out.println(e.getMessage());
        }
        return false;
    }

    static void chmod(final String permision, final String path) throws Throwable {
        if (isWindows()) {
            return;
        }
        try {
            Runtime.getRuntime().exec(new String[]{"chmod", permision, path}).waitFor(); //$NON-NLS-1$
        } catch (Throwable e) {
            throw e;
        }
    }

    static String getOsName() {
        return System.getProperty("os.name") + System.getProperty("java.specification.vendor");
    }

    static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }
}
