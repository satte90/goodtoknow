package com.teliacompany.tiberius.base.server.service.smoketest;

import com.teliacompany.tiberius.base.server.api.smoketest.SmokeTestResponse;
import com.teliacompany.tiberius.base.server.api.smoketest.SmokeTestStatus;
import com.teliacompany.tiberius.base.server.api.smoketest.SmokeTestSubServiceResult;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Example usage:
 * Implement getTests() method and provide a list of calls to subSystems.
 *
 * Optionally you can override the test() method if you for example don't have any subsystems to call.
 */
public interface SmokeTestService {
    List<SmokeTest<?>> getTests();

    default Mono<SmokeTestResponse> test(SmokeTestRequest smokeTestRequest) {
        List<Mono<SmokeTestSubServiceResult>> monos = getTests().stream()
                .map(st -> st.setSmokeTestRequest(smokeTestRequest))
                .map(SmokeTest::run)
                .collect(Collectors.toList());
        return combineSubServiceResponses(monos);
    }

    default Mono<SmokeTestResponse> combineSubServiceResponses(Iterable<Mono<SmokeTestSubServiceResult>> subMonos) {
        return Mono.zip(subMonos, this::combineSubServiceResults)
                .switchIfEmpty(Mono.just(new ArrayList<>()))
                .map(responses -> new SmokeTestResponse()
                        .setStatus(getWorstStatus(responses))
                        .setSubServicesChecked(!responses.isEmpty())
                        .setSubServiceResults(responses));
    }

    default List<SmokeTestSubServiceResult> combineSubServiceResults(Object[] results) {
        return Arrays.stream(results)
                .filter(res -> res instanceof SmokeTestSubServiceResult)
                .map(res -> (SmokeTestSubServiceResult) res)
                .collect(Collectors.toList());
    }

    default SmokeTestStatus getWorstStatus(List<SmokeTestSubServiceResult> subServiceResults) {
        return subServiceResults.stream()
                .map(SmokeTestSubServiceResult::getStatus)
                .max(Comparator.comparing(SmokeTestStatus::getCode))
                .orElse(SmokeTestStatus.NOT_CHECKED);
    }
}
