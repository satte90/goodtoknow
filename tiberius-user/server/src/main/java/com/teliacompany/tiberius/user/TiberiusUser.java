package com.teliacompany.tiberius.user;

import com.teliacompany.ldap.service.LdapService;
import com.teliacompany.tiberius.base.server.TiberiusApplication;
import com.teliacompany.tiberius.base.server.TiberiusRunner;
import org.springframework.context.annotation.ComponentScan;

@TiberiusApplication
@ComponentScan(basePackageClasses = {TiberiusUser.class, LdapService.class})
public class TiberiusUser {
    public static void main(String[] args) {
        TiberiusRunner.run(TiberiusUser.class, args);
    }

}
