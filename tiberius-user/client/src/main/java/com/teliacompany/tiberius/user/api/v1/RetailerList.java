package com.teliacompany.tiberius.user.api.v1;

import java.util.List;

public class RetailerList {

    private List<Retailer> retailers;

    public RetailerList() {
        this.retailers = null;
    }

    public List<Retailer> getRetailers() {
        return retailers;
    }

    public void setRetailers(List<Retailer> retailers) {
        this.retailers = retailers;
    }
}
