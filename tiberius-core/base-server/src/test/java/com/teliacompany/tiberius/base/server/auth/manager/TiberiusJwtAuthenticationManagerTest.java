package com.teliacompany.tiberius.base.server.auth.manager;

import com.teliacompany.webflux.error.exception.WebException;
import com.teliacompany.tiberius.base.server.auth.TiberiusJwtAuthenticationToken;
import com.teliacompany.tiberius.base.server.config.security.TiberiusSecurityConfigurer;
import com.teliacompany.tiberius.user.api.v1.Role;
import com.teliacompany.tiberius.user.auth.TiberiusAuthJwtsUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TiberiusJwtAuthenticationManagerTest extends AbstractAuthenticationManagerTest {
    private static final String TCAD = "agent007";

    @Override
    TiberiusJwtAuthenticationManager createManager() {
        return new TiberiusJwtAuthenticationManager(testEnv, securityConfigurers, publicKeyProviders, "Test-App", "");
    }

    @Test
    void testValidJwtWithRole() {
        //Create JWT & PreAuthentication
        String validAdminJwt = TiberiusAuthJwtsUtils.createSignedJwt(mockedKeyProvider.getLatestPrivateKey(), mockedKeyProvider.getLatestKeyPairId(), TCAD, Role.SUPERUSER.name(), "123456", true);
        Authentication preAuth = TiberiusJwtAuthenticationToken.unverified("test/one", validAdminJwt, "test123");

        //Setup manager
        TiberiusJwtAuthenticationManager manager = createManager();

        // Execute
        final Authentication finalAuth = manager.authenticate(preAuth).block();

        // Verify
        assertNotNull(finalAuth);
        assertTiberiusJwtTokenAuthentication(finalAuth, TCAD);
        assertTiberiusJwtTokenAuthenticationRole(finalAuth, Role.SUPERUSER);
    }

    @Test
    void testValidJwtWithoutRole() {
        //On endpoints requiring no role, a JWT without role should work

        //Create JWT & PreAuthentication
        String validAdminJwt = TiberiusAuthJwtsUtils.createSignedJwt(mockedKeyProvider.getLatestPrivateKey(), mockedKeyProvider.getLatestKeyPairId(), TCAD);
        Authentication preAuth1 = TiberiusJwtAuthenticationToken.unverified("no/role/heyho", validAdminJwt, null);
        Authentication preAuth2 = TiberiusJwtAuthenticationToken.unverified("/no/role2/heyho/deep", validAdminJwt, null);

        // Add no role required paths
        securityConfigurers.add(getNoRoleRequiredConfigurer("/no/role/*", "/no/role2/**"));

        //Setup manager
        TiberiusJwtAuthenticationManager manager = createManager();

        // Execute
        try {
            final Authentication finalAuth1 = manager.authenticate(preAuth1).block();
            final Authentication finalAuth2 = manager.authenticate(preAuth2).block();

            // Assert
            assertNotNull(finalAuth1);
            assertNotNull(finalAuth2);
            assertTiberiusJwtTokenAuthentication(finalAuth1, TCAD);
            assertTiberiusJwtTokenAuthenticationNoRole(finalAuth1);
            assertTiberiusJwtTokenAuthentication(finalAuth2, TCAD);
            assertTiberiusJwtTokenAuthenticationNoRole(finalAuth2);
        } catch(WebException webException) {
            fail("Should not get exception " + webException.getMessage());
        }
    }

    private static void assertTiberiusJwtTokenAuthenticationNoRole(Authentication finalAuth) {
        assertEquals(0, finalAuth.getAuthorities().size());
    }

    private static TiberiusSecurityConfigurer getNoRoleRequiredConfigurer(final String... noRolePath) {
        return new TiberiusSecurityConfigurer() {
            @Override
            public List<String> additionalUnsecuredEndpoints() {
                return new ArrayList<>();
            }

            @Override
            public List<String> noRolesRequiredEndpoints() {
                return Arrays.asList(noRolePath);
            }
        };
    }
}
