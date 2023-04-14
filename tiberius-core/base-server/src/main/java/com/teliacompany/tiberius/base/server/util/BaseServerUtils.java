package com.teliacompany.tiberius.base.server.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class BaseServerUtils {
    private BaseServerUtils() {
        //Not to be instantiated
    }

    public static String getIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch(UnknownHostException e) {
            return null;
        }
    }
}
