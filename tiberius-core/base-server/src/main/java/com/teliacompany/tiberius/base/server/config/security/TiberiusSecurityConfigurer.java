package com.teliacompany.tiberius.base.server.config.security;

import java.util.Collections;
import java.util.List;

/**
 * Implement this for custom security rules
 *
 * To specify a specific role requirement annotate method with either (note: case sensitive, should be capital letters)
 * - @PreAuthorize("hasAuthority('ROLE_NAME')")
 * - @PreAuthorize("hasRole('ROLE_ROLE_NAME')") (note ROLE_ prefix needed!)
 *
 */
public interface TiberiusSecurityConfigurer {

    List<String> additionalUnsecuredEndpoints();

    /**
     * Implement this to specify endpoints that requires a JWT but no roles are required to access it. Otherwise at least one role is required.
     * Default: emptyList
     */
    default List<String> noRolesRequiredEndpoints() {
        return Collections.emptyList();
    }
}
