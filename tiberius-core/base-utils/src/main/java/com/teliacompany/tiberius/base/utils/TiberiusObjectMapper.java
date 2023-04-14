package com.teliacompany.tiberius.base.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Provides static access to read and write json strings via the object mapper configured for Tiberius.
 */
public final class TiberiusObjectMapper {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusObjectMapper.class);
    private static final String COULD_NOT_READ_JSON = "Could not read json";
    private static final String TIBERIUS = "Tiberius";
    private static final String COULD_NOT_READ_BYTES = "Could not read bytes";

    private TiberiusObjectMapper() {
        //Util method. Not to be initialized
    }

    public static <T> T read(String json, Class<T> expectedClass) {
        try {
            return TeliaObjectMapper.get().readValue(json, expectedClass);
        } catch(IOException e) {
            LOG.error(COULD_NOT_READ_JSON + ": ", e);
            throw new InternalServerErrorException(COULD_NOT_READ_JSON, TIBERIUS, e);
        }
    }

    public static <T> T read(String json, TypeReference<T> typeReference) {
        try {
            return TeliaObjectMapper.get().readValue(json, typeReference);
        } catch(IOException e) {
            LOG.error(COULD_NOT_READ_JSON + ": ", e);
            throw new InternalServerErrorException(COULD_NOT_READ_JSON, TIBERIUS, e);
        }
    }

    public static <T> List<T> readList(String json, Class<T[]> expectedClass) {
        //Arrays.asList does not return a normal arrayList, it does not support add operation for example... So return a new arrayList
        return new ArrayList<>(Arrays.asList(read(json, expectedClass)));
    }

    /**
     * Will convert bytearray to string utf8 and then parse string as json to expected object
     */
    public static <T> T read(byte[] bytes, Class<T> expectedClass) {
        try {
            return TeliaObjectMapper.get().readValue(bytes, expectedClass);
        } catch(IOException e) {
            LOG.error(COULD_NOT_READ_BYTES + ": ", e);
            throw new InternalServerErrorException(COULD_NOT_READ_BYTES, TIBERIUS, e);
        }
    }

    public static <T> T read(byte[] bytes, TypeReference<T> typeReference) {
        try {
            return TeliaObjectMapper.get().readValue(bytes, typeReference);
        } catch(IOException e) {
            LOG.error(COULD_NOT_READ_BYTES + ": ", e);
            throw new InternalServerErrorException(COULD_NOT_READ_BYTES, TIBERIUS, e);
        }
    }

    public static <T> List<T> readList(byte[] bytes, Class<T[]> expectedClass) {
        return new ArrayList<>(Arrays.asList(read(bytes, expectedClass)));
    }

    public static <T> String write(T object) {
        try {
            return TeliaObjectMapper.get().writeValueAsString(object);
        } catch(IOException e) {
            LOG.error("Could not write json: ", e);
            throw new InternalServerErrorException("Could not write json", TIBERIUS, e);
        }
    }
}
