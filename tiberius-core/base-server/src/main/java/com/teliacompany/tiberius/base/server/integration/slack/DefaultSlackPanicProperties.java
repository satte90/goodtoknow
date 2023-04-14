package com.teliacompany.tiberius.base.server.integration.slack;

final class DefaultSlackPanicProperties {
    static final String HOST = "https://hooks.slack.com";
    static final String BASE_ENDPOINT = "services/T03PATMPV/";
    static final String DEFAULT_ENDPOINT = "B020BUU9TPG/VDrJB5BlPRm2P3iPQhUpqgRh";
    static final String SERVICESTATUS_ENDPOINT = "B025QBDR649/nDTGNZormyPFw8nbkwTs3G4n";
    static final String TIBTEST_ENDPOINT = "B024XMQG5DY/IvaZoliUYUSGYB0zKfnvg14V";
    static final String VERSION = "1";
    static final boolean DEFAULT_PROXY_ENABLED = true;
    static final String DEFAULT_PROXY_HOST = "proxy-se.ddc.teliasonera.net";
    static final int DEFAULT_PROXY_PORT = 8080;

    private DefaultSlackPanicProperties() {
        //Not to be instantiated
    }
}
