package com.teliacompany.tiberius.user.componenttest;


import com.teliacompany.tiberius.base.test.mock.ApiMarketMock;
import com.teliacompany.tiberius.base.test.utils.approvals.ApprovalsUtils;
import com.teliacompany.tiberius.user.api.v1.elevate.AccessDelta;
import com.teliacompany.tiberius.user.api.v1.elevate.AccessModifyResult;
import com.teliacompany.tiberius.user.api.v1.elevate.UserTemporaryAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

class AccessComponentTest extends AbstractComponentTests {

    @BeforeEach
    void setUp() {
        ApiMarketMock.mockAuthentication();
        setTestTime(Instant.parse(TEST_DATE));
    }

    @Test
    void testGrantAccess() {
        String tcad = "tcad_testGrantAccess";

        postGrantAccess(tcad);

        getExpectedAccess(tcad, true);
    }

    @Test
    void testGrantExistingAccess() {
        String tcad = "tcad_testGrantExistingAccess";

        postGrantAccess(tcad);
        postGrantAccess(tcad);

        getExpectedAccess(tcad, true);
    }

    @Test
    void testAccessExpires() throws InterruptedException {
        String tcad = "tcad_testAccessExpires";

        postGrantAccess(tcad);

        getExpectedAccess(tcad, true);

        setTestTime(Instant.parse(TEST_DATE_PLUS_ONE_MIN));

        getExpectedAccess(tcad, false);

        setTestTime(Instant.parse(TEST_DATE));
    }

    @Test
    void testRemoveAccess() throws InterruptedException {
        String tcad = "tcad_testRemoveAccess";

        postGrantAccess(tcad);

        getExpectedAccess(tcad, true);

        deleteAccess(tcad, true);

        getExpectedAccess(tcad, false);
    }

    @Test
    void testRemoveMissingAccess() throws InterruptedException {
        String tcad = "tcad_testRemoveMissingAccess";

        getExpectedAccess(tcad, false);

        deleteAccess(tcad, false);

        getExpectedAccess(tcad, false);
    }

    @Test
    void testGetNoAccess() {
        String tcad = "tcad_testGetNoAccess";

        getExpectedAccess(tcad, false);
    }

    @Test
    public void testGetDeltaAccess() {
        String tcad = "tcad_testGetDeltaAccess";
        Instant testStartTime = Instant.parse(TEST_DATE);

        postGrantAccess(tcad);

        setTestTime(testStartTime.plusSeconds(3));

        webClient.get()
                .uri("/access/delta/" + 0)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccessDelta.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    ApprovalsUtils.verifyJson(response.getResponseBody());
                });
    }

    @Test
    public void testGetDeltaAccessExpired() {
        String tcad = "tcad_testGetDeltaAccessExpired";
        Instant testStartTime = Instant.parse(TEST_DATE);

        postGrantAccess(tcad);

        setTestTime(testStartTime.plusSeconds(3));

        EntityExchangeResult<AccessDelta> response = getDelta(0);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResponseBody());

        long timestamp = response.getResponseBody().getTimestamp();
        setTestTime(testStartTime.plusSeconds(6));

        EntityExchangeResult<AccessDelta> result = getDelta(timestamp);

        Assertions.assertNotNull(result.getResponseBody());
        ApprovalsUtils.verifyJson(result.getResponseBody());
    }

    @Test
    public void testGetDeltaAccessRemoved() {
        Map<String, AccessDelta> responses = new LinkedHashMap<>();
        String tcad = "tcad_testGetDeltaAccessRemoved";
        Instant testStartTime = Instant.parse(TEST_DATE);

        postGrantAccess(tcad);

        setTestTime(testStartTime.plusSeconds(1));

        EntityExchangeResult<AccessDelta> response = getDelta(0);
        Assertions.assertNotNull(response.getResponseBody());
        responses.put("Delta After Create", response.getResponseBody());

        setTestTime(testStartTime.plusSeconds(2));

        deleteAccess(tcad, true);

        setTestTime(testStartTime.plusSeconds(3));

        response = getDelta(0);
        Assertions.assertNotNull(response.getResponseBody());
        responses.put("Delta After Delete", response.getResponseBody());

        ApprovalsUtils.verifyJson(responses);
    }

    @Test
    public void testGetDeltaAccessNoUpdate() {
        // Assuming no other tests are putting data in at this time. :)
        Instant theDistantFuture = Instant.parse(TEST_DATE).plus(365_000, ChronoUnit.DAYS);
        EntityExchangeResult<AccessDelta> result = getDelta(theDistantFuture.toEpochMilli());

        Assertions.assertNotNull(result.getResponseBody());
        ApprovalsUtils.verifyJson(result.getResponseBody());
    }

    private void deleteAccess(String tcad, boolean expected) {
        webClient.delete()
                .uri("/access/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccessModifyResult.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(expected, response.getResponseBody().isSuccess());
                });
    }

    private void postGrantAccess(String tcad) {
        webClient.post()
                .uri("/access/grant/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccessModifyResult.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertTrue(response.getResponseBody().isSuccess());
                });
    }

    private void getExpectedAccess(String tcad, boolean expectedAccess) {
        webClient.get()
                .uri("/access/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserTemporaryAccess.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(expectedAccess, response.getResponseBody().isAccessGranted());
                });
    }

    private EntityExchangeResult<AccessDelta> getDelta(long from) {
        return webClient.get()
                .uri("/access/delta/" + from)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AccessDelta.class)
                .returnResult();
    }
}
