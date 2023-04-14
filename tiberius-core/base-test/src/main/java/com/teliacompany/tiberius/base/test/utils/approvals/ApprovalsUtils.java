package com.teliacompany.tiberius.base.test.utils.approvals;

import com.teliacompany.tiberius.base.test.runner.TiberiusComponentTestExtension;
import com.teliacompany.tiberius.base.test.runner.TiberiusTestAppBootstrapper;
import com.teliacompany.tiberius.base.test.utils.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.namer.ApprovalNamer;
import org.junit.jupiter.api.Assertions;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.Objects;

import static com.teliacompany.tiberius.base.test.TiberiusTestConfig.DEFAULT_APPROVALS_BASE_PATH;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Approvals really likes to push new major versions with breaking changes and they can really change things up and upgrades have been quite messy...
 * Documentation can be found here: https://github.com/approvals/ApprovalTests.Java/blob/master/approvaltests/docs/README.md
 */
public final class ApprovalsUtils {
    private ApprovalsUtils() {
        //Not to be instantiated
    }

    public static void verifyJsonResponse(EntityExchangeResult<?> response) {
        assertNotNull(response);
        verifyJson(response.getResponseBody());
    }

    public static void verifyJsonResponse(EntityExchangeResult<?> response, boolean sorted) {
        assertNotNull(response);
        verifyJson(response.getResponseBody(), sorted);
    }

    /**
     * filenameOrBasePath is treated like a path if it contains any /
     */
    public static void verifyJsonResponse(EntityExchangeResult<?> response, String filenameOrBasePath) {
        assertNotNull(response);
        verifyJson(response.getResponseBody(), null, filenameOrBasePath);
    }

    public static void verifyJsonResponse(EntityExchangeResult<?> response, String basePath, String filename) {
        assertNotNull(response);
        verifyJson(response.getResponseBody(), basePath, filename);
    }

    public static void verifyJsonResponse(EntityExchangeResult<?> response, String basePath, String filename, boolean sorted) {
        assertNotNull(response);
        verifyJson(response.getResponseBody(), basePath, filename, sorted);
    }

    public static void verifyJson(Object object) {
        verifyJson(object, true);
    }

    public static void verifyJson(Object object, boolean sorted) {
        verifyJson(object, null, "automatic", sorted);
    }

    /**
     * filenameOrBasePath is treated like a path if it contains any /
     */
    public static void verifyJson(Object object, String filenameOrBasePath) {
        verifyJson(object, null, filenameOrBasePath, true);
    }

    /**
     * filenameOrBasePath is treated like a path if it contains any /
     */
    public static void verifyJson(Object object, String filenameOrBasePath, boolean sorted) {
        verifyJson(object, null, filenameOrBasePath, sorted);
    }

    public static void verifyJson(Object object, String basePath, String filename) {
        verifyJson(object, basePath, filename, true);
    }

    public static void verifyJson(Object object, String basePath, String filename, boolean sorted) {
        assertNotNull(object);
        Objects.requireNonNull(filename);

        // If fileName ends with a slash, treat it as a base path instead and let the filename be set automatically
        if(basePath == null && filename.contains("/")) {
            basePath = filename;
            filename = "automatic";
        } else if(basePath == null) {
            basePath = "";
        }
        filename = filename.equals("automatic") ? new ResourceNamer().getApprovalName() : filename;

        ApprovalsConfigurer.configure("src/test/resources/approvals");
        final String json = TestUtils.asPrettyJson(object, sorted);
        basePath = StringUtils.removeStart(basePath, "/");
        basePath = StringUtils.removeStart(basePath, DEFAULT_APPROVALS_BASE_PATH);
        basePath = StringUtils.removeStart(basePath, "/");
        filename = StringUtils.removeStart(filename, "/");
        Approvals.verify(json, new Options()
                .forFile()
                .withName("/" + basePath + "/" + filename, ".json"));
    }
}
