package com.protel.network.util;

import com.protel.network.ProNetwork;
import com.protel.network.interfaces.ILogger;

/**
 * Created by eolkun on 26.1.2015.
 */
public class LogUtils {
    public static void l(String tag, String msg) {
        ILogger logger = ProNetwork.getSingleton().getLogger();
        logger.log(tag, msg);
    }

    public static void ex(Exception ex) {
        ILogger logger = ProNetwork.getSingleton().getLogger();
        logger.log(ex);
    }
}
