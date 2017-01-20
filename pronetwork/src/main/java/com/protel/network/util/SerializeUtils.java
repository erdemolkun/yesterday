package com.protel.network.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

/**
 * Created by eolkun on 2.2.2015.
 */
public class SerializeUtils {

    public static boolean deleteFile(String completePath) {
        try {
            File file = new File(completePath);
            return file.delete();

        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean writeToFile(Object result, String completePath) {
        if (result != null && result instanceof Serializable) {
            // Cache response to file.
            FileOutputStream fos = null;
            try {
//                fos = context.openFileOutput(completePath, Context.MODE_PRIVATE);
                File file = new File(completePath);
                fos = new FileOutputStream(file);

                SecurityUtils.encrypt((Serializable) result, fos);
            } catch (Exception ex) {
                LogUtils.ex(ex);
                return false;
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception ex) {
                    LogUtils.ex(ex);
                }
            }
        }
        return true;
    }

    public static Object readCacheResponse(String completePath) {
        FileInputStream fis = null;
        Serializable cacheResponse = null;
        try {
            fis = new FileInputStream(new File(completePath));
//            fis = context.openFileInput(completePath);
            cacheResponse = (Serializable) SecurityUtils.decrypt(fis);
            fis.close();

        } catch (Exception ex) {
            LogUtils.ex(ex);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ex) {
                LogUtils.ex(ex);
            }
        }
        return cacheResponse;
    }
}
