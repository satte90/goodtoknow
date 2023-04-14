package com.teliacompany.tiberius.user.componenttest;

import com.spun.util.ObjectUtils;
import com.spun.util.io.FileUtils;
import com.teliacompany.tiberius.base.test.utils.approvals.ApprovalsUtils;
import com.teliacompany.webflux.error.api.ErrorResponse;
import com.teliacompany.webflux.request.utils.Constants;
import com.teliacompany.tiberius.base.test.mock.ApiMarketMock;
import com.teliacompany.tiberius.user.api.v1.Role;
import com.teliacompany.tiberius.user.api.v1.UserRequest;
import com.teliacompany.tiberius.user.api.v1.UserResponse;
import com.teliacompany.tiberius.user.model.RoleType;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.ClipboardReporter;
import org.approvaltests.reporters.EnvironmentAwareReporter;
import org.approvaltests.reporters.GenericDiffReporter;
import org.approvaltests.reporters.JunitReporter;
import org.approvaltests.reporters.PitReporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.File;
import java.nio.charset.StandardCharsets;

class UserComponentTest extends AbstractComponentTests {

    @BeforeEach
    void setUp() {
        ApiMarketMock.mockAuthentication();
        resetLdapMock();
    }

    @Test
    void createAndGetUser() {
        String tcad = "tcad1";
        String testRetailer = "retailer123";

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.TELEMARKETING);
        createRetailer(testRetailer, "Retailer 123", Role.TELEMARKETING);
        createUser(tcad, testRetailer, Role.TELEMARKETING);

        webClient.get()
                .uri("/tcad/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(tcad, response.getResponseBody().getTcad());
                    Assertions.assertEquals("fname1", response.getResponseBody().getFirstName());
                });
    }

    @Test
    void createAndGetUserWithRoleDifferentFromRetailer() {
        String tcad = "tcad2";

        mockLdapUser(tcad, "fname2", "lname2", "dep", RoleType.TELEMARKETING, RoleType.OURTELIA);
        createRetailer("retailer123", "Retailer 123", Role.TELEMARKETING);
        createUser(tcad, "retailer123", Role.OURTELIA);

        webClient.get()
                .uri("/tcad/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(tcad, response.getResponseBody().getTcad());
                    Assertions.assertEquals(Role.OURTELIA, response.getResponseBody().getRole());
                    Assertions.assertEquals(Role.TELEMARKETING, response.getResponseBody().getRetailer().getRole());
                });
    }


    @Test
    void getUserNotInMongo() {
        String tcad = "tcad4";
        mockLdapUser(tcad, "fname2", "lname2", "dep", RoleType.TELEMARKETING);

        webClient.get()
                .uri("/tcad/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .value(ApprovalsUtils::verifyJson);
    }

    @Test
    void createUserWithRoleNotAuthorized() {
        String tcad = "tcad1";
        String testRetailer = "retailer123";

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.TELEMARKETING);
        createRetailer(testRetailer, "Retailer 123", Role.TELEMARKETING);
        createUser(tcad, testRetailer, Role.TELEMARKETING);

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.ORDERMANAGEMENT);

        webClient.get()
                .uri("/tcad/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .value(ApprovalsUtils::verifyJson);
    }

    @Test
    void createUserWithoutExistingRetailer() {
        UserRequest user = new UserRequest();

        final String tcad = "tcad3";
        user.setRetailerId("missingRetailer");
        user.setRole(Role.TELEMARKETING);

        webClient.put()
                .uri("/tcad/" + tcad)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void createUserWithMissingLdapEntry() {
        String testRetailer = "retailer123";

        createRetailer(testRetailer, "Retailer 123", Role.TELEMARKETING);

        UserRequest user = new UserRequest();

        final String tcad = "tcad4";
        user.setRetailerId(testRetailer);
        user.setRole(Role.TELEMARKETING);

        webClient.put()
                .uri("/tcad/" + tcad)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }


    @Test
    void createAndUpdateUser() {
        String tcad = "tcad123";

        mockLdapUser(tcad, "fname2", "lname2", "dep", RoleType.TELEMARKETING, RoleType.RETAILER);
        createRetailer("retailer1", "Retailer 1", Role.TELEMARKETING);
        createRetailer("retailer2", "Retailer 2", Role.RETAILER);
        createUser(tcad, "retailer1", Role.TELEMARKETING);

        webClient.get()
                .uri("/tcad/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(tcad, response.getResponseBody().getTcad());
                    Assertions.assertEquals("retailer1", response.getResponseBody().getRetailer().getId());
                    Assertions.assertEquals(Role.TELEMARKETING, response.getResponseBody().getRole());
                });

        // Updates the same user
        createUser(tcad, "retailer2", Role.RETAILER);

        webClient.get()
                .uri("/tcad/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(tcad, response.getResponseBody().getTcad());
                    Assertions.assertEquals("retailer2", response.getResponseBody().getRetailer().getId());
                    Assertions.assertEquals(Role.RETAILER, response.getResponseBody().getRole());
                });
    }

    @Test
    void createAndGetUserWithLdapGroupErrorNotCached() {
        String tcad = "tcad456";
        String testRetailer = "retailer123";

        mockLdapGroupError(tcad, "fname1", "lname1", "dep", RoleType.SUPERUSER);
        createRetailer(testRetailer, "Retailer 123", Role.SUPERUSER);
        UserRequest user = new UserRequest();

        user.setRetailerId(testRetailer);
        user.setRole(Role.SUPERUSER);

        // Try to create when ldap has error
        webClient.put()
                .uri("/tcad/" + tcad)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus()
                .isBadRequest();

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.SUPERUSER);
        // Try to create again when ldap does not respond with error, which makes sure that the error has not been cached
        createUser(tcad, testRetailer, Role.SUPERUSER);

        webClient.get()
                .uri("/tcad/" + tcad)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(tcad, response.getResponseBody().getTcad());
                    Assertions.assertEquals("fname1", response.getResponseBody().getFirstName());
                });
    }

    @Test
    void getPhotoTest() {
        String tcad = "photo12";

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.TELEMARKETING);

        webClient.get()
                .uri("/photo/" + tcad)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals("some bytes instead of an image",new String(response.getResponseBody()));
                });
    }

    @Test
    void createUserWithMissingRole() {
        String tcad = "tcadMissingRole";
        String testRetailer = "retailer123";

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.TELEMARKETING);
        createRetailer(testRetailer, "Retailer 123", Role.TELEMARKETING);
        UserRequest user = new UserRequest();

        user.setRetailerId(testRetailer);
        user.setRole(null);

        webClient.put()
                .uri("/tcad/" + tcad)
                .header(Constants.HTTP_TRANSACTION_ID_HEADER, "julgran7000")
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    //  Scrub for approval
                    response.getResponseBody().setTimestamp(0);
                    response.getResponseBody().setErrorLink("");
                    response.getResponseBody().setAttributes(null);
                    ApprovalsUtils.verifyJson(response.getResponseBody());
                });
    }

    @Test
    void createUserWithMissingRoleButPresentInDb() {
        String tcad = "tcadMissingRole";
        String testRetailer = "retailer123";

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.TELEMARKETING);
        createRetailer(testRetailer, "Retailer 123", Role.TELEMARKETING);

        UserRequest user = new UserRequest();
        user.setRetailerId(testRetailer);
        user.setRole(Role.TELEMARKETING);

        createUser(tcad, user.getRetailerId(), user.getRole());

        webClient.put()
                .uri("/tcad/" + tcad)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    ApprovalsUtils.verifyJson(response.getResponseBody());
                });
    }

    @Test
    void createUserWithMissingRetailerId() {
        String tcad = "tcadMissingRetailerId";
        String testRetailer = "retailer123";

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.TELEMARKETING);
        createRetailer(testRetailer, "Retailer 123", Role.TELEMARKETING);

        UserRequest user = new UserRequest();
        user.setRetailerId(null);
        user.setRole(Role.TELEMARKETING);

        webClient.put()
                .uri("/tcad/" + tcad)
                .body(BodyInserters.fromValue(user))
                .header(Constants.HTTP_TRANSACTION_ID_HEADER, "tomtenisse2000000")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(ErrorResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    // Remove for approval
                    response.getResponseBody().setTimestamp(0);
                    response.getResponseBody().setErrorLink("");
                    response.getResponseBody().setAttributes(null);
                    ApprovalsUtils.verifyJson(response.getResponseBody());
                });
    }

    @Test
    void createUserWithMissingRetailerIdButPresnetInDb() {
        String tcad = "tcadMissingRetailerId";
        String testRetailer = "retailer123";

        mockLdapUser(tcad, "fname1", "lname1", "dep", RoleType.TELEMARKETING);
        createRetailer(testRetailer, "Retailer 123", Role.TELEMARKETING);

        UserRequest user = new UserRequest();
        user.setRetailerId(testRetailer);
        user.setRole(Role.TELEMARKETING);

        createUser(tcad, user.getRetailerId(), user.getRole());

        webClient.put()
                .uri("/tcad/" + tcad)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserResponse.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    ApprovalsUtils.verifyJson(response.getResponseBody());
                });
    }

    @Test
    void getRolesTest() {
        webClient.get()
                .uri("/roles")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Role[].class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(Role.values().length, response.getResponseBody().length);
                    // Verify that RoleType and Role (api) is same length, better test would be to check that they are mapped 1:1 but better than nothing...
                    Assertions.assertEquals(RoleType.values().length, response.getResponseBody().length);
                });
    }
}
