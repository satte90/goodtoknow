package com.teliacompany.tiberius.base.server.api.smoketest;

import java.util.ArrayList;
import java.util.List;

public class SmokeTestResponse {
    private SmokeTestStatus status;
    private boolean subServicesChecked;
    private List<SmokeTestSubServiceResult> subServiceResults = new ArrayList<>();

    public SmokeTestStatus getStatus() {
        return status;
    }

    public SmokeTestResponse setStatus(SmokeTestStatus status) {
        this.status = status;
        return this;
    }

    public boolean isSubServicesChecked() {
        return subServicesChecked;
    }

    public SmokeTestResponse setSubServicesChecked(boolean subServicesChecked) {
        this.subServicesChecked = subServicesChecked;
        return this;
    }

    public SmokeTestResponse addSubServiceResult(SmokeTestSubServiceResult subResults) {
        subServiceResults.add(subResults);
        return this;
    }

    public SmokeTestResponse setSubServiceResults(List<SmokeTestSubServiceResult> subResults) {
        subServiceResults = subResults;
        return this;
    }

    public List<SmokeTestSubServiceResult> getSubServiceResults() {
        if(subServiceResults == null) {
            subServiceResults = new ArrayList<>();
        }
        return subServiceResults;
    }
}
