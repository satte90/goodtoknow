package com.teliacompany.tiberius.user.config;

import com.teliacompany.ldap.mock.MockableReactiveLdapService;
import com.teliacompany.ldap.mock.ReactiveLdapTemplateMock;
import com.teliacompany.ldap.service.LdapService;
import com.teliacompany.tiberius.base.server.api.TestModeData;
import com.teliacompany.tiberius.base.server.testsupport.testmode.listener.TiberiusTestModeEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AppConfig {

    @Bean
    @Profile({"componenttest", "local"})
    public TiberiusTestModeEventListener ldapTestModeEventListener(LdapService ldapService) {
        if(ldapService instanceof MockableReactiveLdapService) {
            MockableReactiveLdapService mockableReactiveLdapService = (MockableReactiveLdapService) ldapService;
            return new TiberiusTestModeEventListener() {
                @Override
                public void enableTestMode(TestModeData testModeData) {
                    mockableReactiveLdapService.enableMock();
                }

                @Override
                public void disableTestMode() {
                    mockableReactiveLdapService.disableMock();
                }
            };
        }
        return null;
    }
}
