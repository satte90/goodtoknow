package com.teliacompany.tiberius.user.componenttest;


import com.teliacompany.tiberius.base.test.mock.ApiMarketMock;
import com.teliacompany.tiberius.user.api.v1.Retailer;
import com.teliacompany.tiberius.user.api.v1.RetailerList;
import com.teliacompany.tiberius.user.api.v1.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RetailerComponentTest extends AbstractComponentTests {

    @BeforeEach
    public void setUp() {
        ApiMarketMock.mockAuthentication();
    }

    @Test
    public void createAndGetRetailerTest() {
        createRetailer("id", "name", Role.TELEMARKETING);

        webClient.get()
                .uri("/retailers")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(RetailerList.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(1, response.getResponseBody().getRetailers().size());
                });
    }

    @Test
    public void createAndGetRetailerMultipleTest() {
        createRetailer("id1", "name1", Role.TELEMARKETING);
        createRetailer("id2", "name2", Role.RETAILER);

        webClient.get()
                .uri("/retailers")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(RetailerList.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(2, response.getResponseBody().getRetailers().size());
                });
    }

    @Test
    public void createAndGetRetailerFilteredTest() {
        createRetailer("id1", "name1", Role.TELEMARKETING);
        createRetailer("id2", "name2", Role.RETAILER);

        webClient.get()
                .uri("/retailers?role=retailer")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(RetailerList.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    List<Retailer> retailers = response.getResponseBody().getRetailers();
                    Assertions.assertEquals(1, retailers.size());
                    Assertions.assertEquals(Role.RETAILER, retailers.get(0).getRole());
                });
    }

    @Test
    public void getRetailersEmpty() {
        webClient.get()
                .uri("/retailers")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(RetailerList.class)
                .consumeWith(response -> {
                    Assertions.assertNotNull(response.getResponseBody());
                    Assertions.assertEquals(0, response.getResponseBody().getRetailers().size());
                });
    }
}
