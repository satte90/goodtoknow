package com.teliacompany.tiberius.user.converter.v1;

import com.teliacompany.tiberius.user.api.v1.UserRequest;
import com.teliacompany.tiberius.user.model.RoleType;
import com.teliacompany.tiberius.user.model.UserEntity;

public final class UserConverter {
    private UserConverter() {
        //Util class
    }

    public static UserEntity convertRequest(UserRequest user) {
        if(user == null) {
            return null;
        }

        final RoleType roleType = RoleConverter.convert(user.getRole());
        return new UserEntity(user.getTcad(), roleType, user.getRetailerId());
    }
}
