package com.teliacompany.tiberius.user.componenttest;


import com.teliacompany.tiberius.base.test.utils.approvals.ApprovalsUtils;
import com.teliacompany.tiberius.user.api.v1.Role;
import com.teliacompany.tiberius.user.api.v1.UserResponse;
import com.teliacompany.tiberius.user.api.v1.customerhistory.InsertCustomerHistory;
import com.teliacompany.tiberius.user.api.v1.customerhistory.UserCustomerHistory;
import com.teliacompany.tiberius.user.model.RoleType;
import io.netty.channel.group.ChannelMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


class CustomerHistoryComponentTest extends AbstractComponentTests {

    public static final int MAX_HISTORY_LENGTH = 10;
    Instant testStartTime = Instant.parse(TEST_DATE);

    @Test
    void testGetEmptyCustomerHistoryList() {
        String tcad = "obz674";

        webClient.get()
                .uri("/customer/history/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(0, response.getResponseBody().length);
                });
    }

    @Test
    void testInsertAndGetCustomerHistoryList() {
        String tcad = "obz674";

        InsertCustomerHistory body = new InsertCustomerHistory();
        body.setName("James Bond");
        body.setTscId("007");
        setTestTime(testStartTime);

        webClient.put()
                .uri("/customer/history/" + tcad)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus()
                .isNoContent();

        webClient.get()
                .uri("/customer/history/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(response -> {
                    final UserCustomerHistory[] responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertEquals(1, responseBody.length);
                    Assertions.assertEquals("James Bond", responseBody[0].getName());
                    Assertions.assertEquals("007", responseBody[0].getTscId());
                    Assertions.assertEquals(Instant.parse(TEST_DATE), responseBody[0].getTimestamp());
                });
    }

    @Test
    void testInsertMoreThanLimitAndGetCustomerHistoryList() {
        String tcad = "obz674";

        List<InsertCustomerHistory> cases = IntStream.rangeClosed(0, MAX_HISTORY_LENGTH).mapToObj(i -> {
            InsertCustomerHistory body = new InsertCustomerHistory();
            body.setName("Agent " + i);
            body.setTscId("00" + i);
            return body;
        }).collect(Collectors.toList());


        cases.forEach(c -> {
            testStartTime = testStartTime.plusSeconds(50);
            setTestTime(testStartTime);

            webClient.put()
                    .uri("/customer/history/" + tcad)
                    .body(BodyInserters.fromValue(c))
                    .exchange()
                    .expectStatus()
                    .isNoContent()
                    .returnResult(Void.class);
        });

        webClient.get()
                .uri("/customer/history/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(response -> {
                    ApprovalsUtils.verifyJsonResponse(response);
                    final UserCustomerHistory[] responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertEquals(MAX_HISTORY_LENGTH, responseBody.length);

                    for(int i = 0; i < 10; i++) {
                        InsertCustomerHistory customerHistory = cases.get(MAX_HISTORY_LENGTH - i);
                        Assertions.assertEquals(customerHistory.getName(), responseBody[i].getName());
                        Assertions.assertEquals(customerHistory.getTscId(), responseBody[i].getTscId());
                    }
                });
    }

    @Test
    void testInsertSameCustomerMultipleTimesGetCustomerHistoryListWithTimestampLatestInserted() {
        String tcad = "obz674";

        List<InsertCustomerHistory> cases = IntStream.rangeClosed(0, 4).mapToObj(i -> {
            InsertCustomerHistory body = new InsertCustomerHistory();
            body.setName("Agent1");
            body.setTscId("007");
            return body;

        }).collect(Collectors.toList());


        cases.forEach(c -> {
            testStartTime = testStartTime.plusSeconds(60);
            setTestTime(testStartTime);

            webClient.put()
                    .uri("/customer/history/" + tcad)
                    .body(BodyInserters.fromValue(c))
                    .exchange()
                    .expectStatus()
                    .isNoContent()
                    .returnResult(Void.class);
        });

        webClient.get()
                .uri("/customer/history/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(response -> {
                    ApprovalsUtils.verifyJsonResponse(response);
                    final UserCustomerHistory[] responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertEquals(testStartTime, responseBody[0].getTimestamp());
                });
    }

    @Test
    void testGetUserHistoryListByTSCID() {
        String tscid = "070000000";
        String tcad = "ABC00";
        int expectedMaxHistory = MAX_HISTORY_LENGTH +1;

        InsertCustomerHistory body = new InsertCustomerHistory();
        body.setName("Agent Test" );
        body.setTscId("070000000");

        IntStream.rangeClosed(0, MAX_HISTORY_LENGTH)
                .forEach(i -> {
                    testStartTime = testStartTime.plusSeconds(60);
                    setTestTime(testStartTime);
                    webClient.put()
                            .uri("/customer/history/" + tcad + i)
                            .body(BodyInserters.fromValue(body))
                            .exchange()
                            .expectStatus()
                            .isNoContent()
                            .returnResult(Void.class);
                });

        webClient.get()
                .uri("/history/" + tscid)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(response -> {
                    ApprovalsUtils.verifyJsonResponse(response);
                    final UserCustomerHistory[] responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertEquals(expectedMaxHistory, responseBody.length);
                });
    }

    @Test
    void testInsertSameUserMultipleTimesAndGetCustomerHistoryList() {
        String tscid = "070000000";
        String tcad = "ABC00";
        String expectedRole = "OURTELIA";

        InsertCustomerHistory body = new InsertCustomerHistory();
        body.setName("Agent Tester" );
        body.setTscId(tscid);
        addUser(tcad.toLowerCase(Locale.ROOT), Role.TELEMARKETING, RoleType.TELEMARKETING);
        IntStream.rangeClosed(0, 5)
                .forEach(i -> {
                    if (i == 5){
                        addUser(tcad.toLowerCase(Locale.ROOT), Role.OURTELIA, RoleType.OURTELIA);
                    }
                            testStartTime = testStartTime.plusSeconds(60);
                            setTestTime(testStartTime);
                            webClient.put()
                                    .uri("/customer/history/" + tcad)
                                    .body(BodyInserters.fromValue(body))
                                    .exchange()
                                    .expectStatus()
                                    .isNoContent()
                                    .returnResult(Void.class);
                        });


        webClient.get()
                .uri("/history/" + tscid)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(response -> {
                    ApprovalsUtils.verifyJsonResponse(response);
                    final UserCustomerHistory[] responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertEquals(6, responseBody.length);

                    //last inserted role should be the latest one
                    Assertions.assertEquals(responseBody[0].getRole(), expectedRole);
                });
    }

    @Test
    void testInsertUserInfoWihtoutNoUserRoleInformation() {
        String tscid = "070000000";
        String tcad = "ABC007";

        InsertCustomerHistory body = new InsertCustomerHistory();
        body.setName("Agent Tester");
        body.setTscId(tscid);
        Instant expectedTime = testStartTime.plusSeconds(300);

        webClient.put()
                .uri("/customer/history/" + tcad)
                .body(BodyInserters.fromValue(body))
                .exchange()
                .expectStatus()
                .isNoContent()
                .returnResult(Void.class);


        webClient.get()
                .uri("/history/" + tscid)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(response -> {
                    ApprovalsUtils.verifyJsonResponse(response);
                    final UserCustomerHistory[] responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertEquals(1, responseBody.length);
                    Assertions.assertEquals(tcad.toLowerCase(), responseBody[0].getTcad());
                    //last inserted role should be the latest one
                    Assertions.assertEquals(responseBody[0].getRole(), "UNKNOWN");
                });
    }

    @Test
    void testGetEmptyUserHistoryList() {
        String tscid = "070000000";

        webClient.get()
                .uri("/history/" + tscid)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(0, response.getResponseBody().length);
                });
    }

    void addUser(String tcad, Role role, RoleType roleType) {

        String testRetailer = "retailer123";

        mockLdapUser(tcad, "fname1", "lname1", "dep", roleType);
        createRetailer(testRetailer, "Retailer 123", role);
        createUser(tcad, testRetailer, role);


        webClient.get()
                .uri("/tcad/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class);

    }

    @Test
    void testInsertDifferentUserMultipleTimesAndGetCustomerHistoryList() {
        String tscid = "070000000";
        List<String> tcadList = List.of("ABC00","ABC01", "ABC00", "ABC03", "ABC01") ;

        InsertCustomerHistory body = new InsertCustomerHistory();
        body.setName("Agent Tester" );
        body.setTscId(tscid);
        IntStream.rangeClosed(0, 4)
                .forEach(i -> {
                    testStartTime = testStartTime.plusSeconds(60);
                    setTestTime(testStartTime);
                    webClient.put()
                            .uri("/customer/history/" + tcadList.get(i))
                            .body(BodyInserters.fromValue(body))
                            .exchange()
                            .expectStatus()
                            .isNoContent()
                            .returnResult(Void.class);
                });


        webClient.get()
                .uri("/history/" + tscid)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserCustomerHistory[].class)
                .consumeWith(ApprovalsUtils::verifyJsonResponse);
    }
}
