package com.teliacompany.webflux.request.utils;

import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.request.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ByteStreamUtil {

    public static ByteArrayOutputStream writeBytesToStream(ByteArrayOutputStream os, byte[] bytes) {
        try {
            os.write(bytes);
            return os;
        } catch(IOException e) {
            throw new InternalServerErrorException("Could not write byte buffer to teh byteArrayOutputStream", WebClient.REQ_WF_STARTER, e);
        }
    }
}
