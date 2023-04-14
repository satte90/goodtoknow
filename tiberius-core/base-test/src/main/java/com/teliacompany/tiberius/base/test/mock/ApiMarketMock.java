package com.teliacompany.tiberius.base.test.mock;

import com.teliacompany.tiberius.base.test.utils.TestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public final class ApiMarketMock {
    private ApiMarketMock() {
        //Util
    }

    public static void mockAuthentication() {
        Oauth2MockResponse oAuth2Response = new Oauth2MockResponse();
        oAuth2Response.setAccess_token("faketoken");
        oAuth2Response.setExpires_in(100000L);

        String fakeResponseJson = TestUtils.asJson(oAuth2Response);
        stubFor(post(urlPathEqualTo("/token"))
                .withQueryParam("grant_type", equalTo("client_credentials"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(fakeResponseJson)));
    }
}
