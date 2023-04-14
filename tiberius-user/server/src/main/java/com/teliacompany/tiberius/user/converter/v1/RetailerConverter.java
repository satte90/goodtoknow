package com.teliacompany.tiberius.user.converter.v1;

import com.teliacompany.tiberius.user.api.v1.Retailer;
import com.teliacompany.tiberius.user.api.v1.RetailerList;
import com.teliacompany.tiberius.user.model.RetailerEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public final class RetailerConverter {
    private RetailerConverter() {
        // Util class
    }

    public static Retailer convert(RetailerEntity entity) {
        if (entity == null) {
            return null;
        }

        Retailer retailer = new Retailer();

        retailer.setId(entity.getId());
        retailer.setName(entity.getName());
        retailer.setRole(RoleConverter.convert(entity.getRole()));

        return retailer;
    }

    public static RetailerList convert(List<RetailerEntity> entityList) {
        if (entityList == null) {
            return null;
        }

        RetailerList retailerList = new RetailerList();

        List<Retailer> convertedList = entityList.stream()
                .map(RetailerConverter::convert)
                .collect(Collectors.toList());

        retailerList.setRetailers(convertedList);

        return retailerList;
    }

    public static RetailerEntity convert(Retailer retailer) {
        if (retailer == null) {
            return null;
        }

        return new RetailerEntity(retailer.getId(), retailer.getName(), RoleConverter.convert(retailer.getRole()));
    }
}
