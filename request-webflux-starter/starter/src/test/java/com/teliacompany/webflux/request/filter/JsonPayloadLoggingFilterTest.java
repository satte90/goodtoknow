package com.teliacompany.webflux.request.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JsonPayloadLoggingFilterTest {

    public static Stream<? extends Arguments> provideArguments() {
        return Stream.of(
                new TestCase()
                        .setJson("{\"applicationId\":\"telia\",\"domesticTopupTypes\":[\"COMPENSATION\"],\"loginPassword\":\"hämligt_lösenORD123#Q!!!\",\"loginUser\":\"telia\",\"msisdn\":\"23213\",\"portalId\":\"vart\"}")
                        .addPasswordValue("hämligt_löenORD123#Q!!!"),
                new TestCase()
                        .setJson("{\"applicationId\":\"telia\",\"domesticTopupTypes\":[\"COMPENSATION\"],\"pass\" : \"bok\",\"loginUser\":\"telia\",\"msisdn\":\"23213\",\"portalId\":\"vart\"}")
                        .addPasswordValue("bok"),
                new TestCase()
                        .setJson("{\"applicationId\":\"telia\",\"domesticTopupTypes\":[\"COMPENSATION\"],\"loginPassword\":\"hämligt_lösenORD123#Q!!!\",\"loginUser\":\"telia\",\"pass\": \"bananasplit2\",\"msisdn\":\"23213\",\"portalId\":\"vart\"}")
                        .addPasswordValue("hämligt_löenORD123#Q!!!")
                        .addPasswordValue("bananasplit2"),
                new TestCase()
                        .setJson("{\"superSecret\":\"doNotLogThisPlz\",\"user\":\"megaMan\",\"psw\": \"#yolo\"}")
                        .addPasswordValue("doNotLogThisPlz")
                        .addPasswordValue("#yolo"),
                new TestCase()
                        .setJson("{\"secretStuffz\":\"z*zz\",\"user\":\"megaMan\",\"lösenord\": \"#yolo\"}")
                        .addPasswordValue("z*zz")
                        .addPasswordValue("#yolo")
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void test(TestCase testCase) {
        JsonPayloadLoggingFilter filter = JsonPayloadLoggingFilter.defaultFilter();

        String body = filter.filterPayload(testCase.getJson());

        Assertions.assertTrue(body.contains("************"));
        testCase.getPasswordValues()
                .forEach(pv -> Assertions.assertFalse(body.contains(pv), "Found password value in json: " + pv));
    }

    private static class TestCase {
        private String json;
        private List<String> passwordValues = new ArrayList<>();

        public String getJson() {
            return json;
        }

        public TestCase setJson(String json) {
            this.json = json;
            return this;
        }

        public List<String> getPasswordValues() {
            return passwordValues;
        }

        public TestCase addPasswordValue(String passwordValue) {
            this.passwordValues.add(passwordValue);
            return this;
        }

        @Override
        public String toString() {
            return passwordValues.toString();
        }
    }
}

