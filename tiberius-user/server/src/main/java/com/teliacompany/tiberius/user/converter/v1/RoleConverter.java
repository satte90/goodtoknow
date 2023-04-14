package com.teliacompany.tiberius.user.converter.v1;

import com.teliacompany.tiberius.user.api.v1.Role;
import com.teliacompany.tiberius.user.model.RoleType;

import java.util.List;

public final class RoleConverter {
    private RoleConverter() {
        //Util class
    }

    public static Role convert(RoleType roleType) {
        if(roleType == null) {
            return null;
        }

        return Role.valueOf(roleType.getRoleType());
    }

    public static Role[] convert(List<RoleType> roleTypeList) {
        if(roleTypeList == null) {
            return new Role[0];
        }

        return roleTypeList.stream()
                .map(RoleConverter::convert)
                .toArray(Role[]::new);
    }

    public static RoleType convert(Role role) {
        if(role == null) {
            return null;
        }

        return RoleType.valueOf(role.toString());
    }
}
