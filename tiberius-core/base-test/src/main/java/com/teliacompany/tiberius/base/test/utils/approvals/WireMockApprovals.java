package com.teliacompany.tiberius.base.test.utils.approvals;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.namer.ApprovalNamer;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.teliacompany.tiberius.base.test.utils.TestUtils.TEST_OBJECT_MAPPER;

public final class WireMockApprovals {
    private WireMockApprovals() {
        //Not to be instantiated
    }

    public static void verifyRequestJsonBody(RequestPatternBuilder requestPattern, String fileTag) {
        ApprovalNamer approvalNamer = Approvals.createApprovalNamer();
        final String[] split = approvalNamer.getSourceFilePath().split("src.test.resources.approvals");
        final String basePath = split[split.length - 1];
        verifyRequestJsonBody(requestPattern, basePath, approvalNamer.getApprovalName() + "." + fileTag);
    }

    public static void verifyRequestJsonBody(RequestPatternBuilder requestPattern, String basePath, String filename) {
        verify(requestMadeFor(r -> {
            MatchResult match = requestPattern.build().match(r);
            if(match.isExactMatch()) {
                ApprovalsUtils.verifyJson(prettyJson(r), basePath, filename);
            }
            return match;
        }));
    }

    public static void verifyRequestXmlBody(RequestPatternBuilder requestPattern, String fileTag) {
        ApprovalNamer approvalNamer = Approvals.createApprovalNamer();
        verifyRequestXmlBody(requestPattern, approvalNamer.getSourceFilePath(), approvalNamer.getApprovalName() + "." + fileTag);
    }

    public static void verifyRequestXmlBody(RequestPatternBuilder requestPattern, String basePath, String filename) {
        verify(requestMadeFor(r -> {
            MatchResult match = requestPattern.build().match(r);
            if(match.isExactMatch()) {
                ApprovalsConfigurer.configure("src/test/resources/approvals");
                Approvals.verifyXml(r.getBodyAsString(), new Options()
                        .forFile()
                        .withBaseName("/" + basePath + "/" + filename));
            }
            return match;
        }));
    }

    private static Object prettyJson(Request r) {
        try {
            return TEST_OBJECT_MAPPER.readValue(r.getBody(), Object.class);
        } catch(IOException e) {
            return r.getBodyAsString();
        }
    }
}
