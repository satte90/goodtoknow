package com.teliacompany.tiberius.user.cache;

import com.teliacompany.ldap.model.LdapGroup;

import java.io.Serializable;

public class LdapGroupCacheItem implements Serializable {
    private final String ldapGroupKey;
    private final LdapGroup ldapGroup;

    public LdapGroupCacheItem(String ldapGroupKey, LdapGroup ldapGroup) {
        this.ldapGroupKey = ldapGroupKey;
        this.ldapGroup = ldapGroup;
    }

    public String getLdapGroupKey() {
        return ldapGroupKey;
    }

    public LdapGroup getLdapGroup() {
        return ldapGroup;
    }
}
