package com.teliacompany.tiberius.base.server.auth.manager;

import com.teliacompany.tiberius.base.server.auth.TiberiusJwtAuthenticationToken;
import com.teliacompany.tiberius.user.api.v1.Role;
import com.teliacompany.tiberius.user.auth.TiberiusAuthJwtsUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TiberiusDisabledAuthenticationManagerTest extends AbstractAuthenticationManagerTest {

    @Override
    TiberiusDisabledAuthenticationManager createManager() {
        return new TiberiusDisabledAuthenticationManager();
    }

    @Test
    void testValidJwtWithRole() {
        //Create JWT & PreAuthentication
        String validAdminJwt = TiberiusAuthJwtsUtils.createSignedJwt(mockedKeyProvider.getLatestPrivateKey(), mockedKeyProvider.getLatestKeyPairId(), "local007", Role.SUPERUSER.name(), "123456", true);
        Authentication preAuth = TiberiusJwtAuthenticationToken.unverified("test/one", validAdminJwt, "test123");

        //Setup manager
        TiberiusDisabledAuthenticationManager manager = createManager();

        // Execute
        final Authentication finalAuth = manager.authenticate(preAuth).block();

        // Verify
        assertNotNull(finalAuth);
        assertTiberiusJwtTokenAuthentication(finalAuth, "test123");
        assertTiberiusJwtTokenAuthenticationRole(finalAuth, Role.values());
    }
}
