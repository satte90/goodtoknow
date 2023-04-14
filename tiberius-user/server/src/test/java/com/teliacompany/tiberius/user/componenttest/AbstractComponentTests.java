package com.teliacompany.tiberius.user.componenttest;

import com.teliacompany.ldap.mock.LdapMockRequest;
import com.teliacompany.ldap.model.LdapGroup;
import com.teliacompany.ldap.model.LdapUser;
import com.teliacompany.tiberius.base.test.TiberiusTestConfig;
import com.teliacompany.tiberius.base.test.TiberiusWebTestClient;
import com.teliacompany.tiberius.base.test.client.ComponentTestClient;
import com.teliacompany.tiberius.base.test.runner.TiberiusComponentTestExtension;
import com.teliacompany.tiberius.user.api.v1.Retailer;
import com.teliacompany.tiberius.user.api.v1.Role;
import com.teliacompany.tiberius.user.api.v1.UserRequest;
import com.teliacompany.tiberius.user.cache.LdapGroupCache;
import com.teliacompany.tiberius.user.model.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(TiberiusComponentTestExtension.class)
@TiberiusTestConfig(port = 8080, wiremockPort = 8089, mongodbEnabled = true, mongodbPort = 27018, testDate = AbstractComponentTests.TEST_DATE)
public abstract class AbstractComponentTests {
    public static final String TEST_DATE = "2020-12-08T11:43:00Z";
    public static final String TEST_DATE_PLUS_ONE_MIN = "2020-12-08T11:44:00Z";

    @TiberiusWebTestClient
    protected ComponentTestClient webClient;

    @BeforeEach
    public void clearCaches() {
        webClient.clearHazelcastCache(LdapGroupCache.NAME);
    }

    void createRetailer(String id, String name, Role role) {
        Retailer retailer = new Retailer();
        retailer.setId(id);
        retailer.setName(name);
        retailer.setRole(role);

        webClient.put()
                .uri("/devops/retailers")
                .body(BodyInserters.fromValue(retailer))
                .exchange()
                .expectStatus()
                .isOk();
    }

    void createUser(String tcad, String retailerId, Role role) {
        UserRequest user = new UserRequest();

        user.setRetailerId(retailerId);
        user.setRole(role);

        webClient.put()
                .uri("/tcad/" + tcad)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus()
                .isOk();
    }

    void mockLdapUser(String tcad, String firstName, String lastName, String department, RoleType... idmRoleGroups) {
        // Id is null since it can not be easily serialized to json
        List<String> authorityGroups = new ArrayList<>();

        authorityGroups.add("CN=WEB_Augustus_Test_Access_Role,OU=WebRole,OU=ApplicationIntranet,OU=Admin,OU=Hosting,DC=tcad,DC=telia,DC=se");

        Arrays.stream(idmRoleGroups)
                .map(group -> "CN=OURTELIA_TEST_ROLE_" + group + ",OU=WebRole,OU=ApplicationIntranet,OU=Admin,OU=Hosting,DC=tcad,DC=telia,DC=se")
                .forEach(authorityGroups::add);

        byte[] thumbnailPhoto = "some bytes instead of an image".getBytes(StandardCharsets.UTF_8);
        LdapUser mockUser = new LdapUser(null, authorityGroups, department, thumbnailPhoto, firstName, lastName, tcad, "email", "phone");


        for (String authorityGroup : authorityGroups) {
            LdapGroup mockGroup = new LdapGroup(null, authorityGroup, Collections.singletonList("memberof"));

            webClient.put()
                    .uri("/ldap/mock/findByDn/" + authorityGroup)
                    .body(BodyInserters.fromValue(new LdapMockRequest(mockGroup)))
                    .exchange()
                    .expectStatus()
                    .isNoContent();
        }

        webClient.put()
                .uri("/ldap/mock/findOne/" + tcad)
                .body(BodyInserters.fromValue(new LdapMockRequest(mockUser)))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    void mockLdapGroupError(String tcad, String firstName, String lastName, String department, RoleType... idmRoleGroups) {
        // Id is null since it can not be easily serialized to json
        List<String> authorityGroups = new ArrayList<>();

        authorityGroups.add("CN=WEB_Augustus_Test_Access_Role,OU=WebRole,OU=ApplicationIntranet,OU=Admin,OU=Hosting,DC=tcad,DC=telia,DC=se");

        Arrays.stream(idmRoleGroups)
                .map(group -> "CN=OURTELIA_TEST_ROLE_" + group + ",OU=WebRole,OU=ApplicationIntranet,OU=Admin,OU=Hosting,DC=tcad,DC=telia,DC=se")
                .forEach(authorityGroups::add);

        for (String authorityGroup : authorityGroups) {
            RuntimeException errorResponse = new RuntimeException("mock error response");

            webClient.put()
                    .uri("/ldap/mock/findByDn/" + authorityGroup)
                    .body(BodyInserters.fromValue(new LdapMockRequest(errorResponse)))
                    .exchange()
                    .expectStatus()
                    .isNoContent();
        }

        LdapUser mockUser = new LdapUser(null, authorityGroups, department, null, firstName, lastName, tcad, "email", "phone");

        webClient.put()
                .uri("/ldap/mock/findOne/" + tcad)
                .body(BodyInserters.fromValue(new LdapMockRequest(mockUser)))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    void resetLdapMock() {
        webClient.delete()
                .uri("/ldap/mock")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    void setTestTime(Instant i) {
        webClient.post()
                .uri("/testsupport/time")
                .body(BodyInserters.fromValue(i))
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
