package com.teliacompany.tiberius.base.test.exception;

/**
 * Wrapper for runtime exception for tests, basically to avoid linting complaints that you should not throw raw RuntimeExceptions, which makes sense in real code but for
 * tests, not so much...
 */
public class TestRuntimeException extends RuntimeException {
    public TestRuntimeException(Throwable e) {
        super(e);
    }

    public TestRuntimeException(String s, Exception e) {
        super(s, e);
    }

    public TestRuntimeException(String s) {
        super(s);
    }
}
