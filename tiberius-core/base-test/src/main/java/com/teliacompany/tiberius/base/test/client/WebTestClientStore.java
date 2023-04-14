package com.teliacompany.tiberius.base.test.client;

public final class WebTestClientStore {
    private static ComponentTestClient WEB_TEST_CLIENT;

    private WebTestClientStore() {

    }

    public static void registerWebTestClient(ComponentTestClient webTestClient) {
        if(WEB_TEST_CLIENT == null) {
            WEB_TEST_CLIENT = webTestClient;
        }
    }

    public static ComponentTestClient getWebTestClient() {
        if(WEB_TEST_CLIENT == null) {
            throw new NullPointerException("No WebTestClient has been registered");
        }
        return WEB_TEST_CLIENT;
    }
}
