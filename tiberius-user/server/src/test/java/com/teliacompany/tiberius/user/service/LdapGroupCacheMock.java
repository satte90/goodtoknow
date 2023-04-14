package com.teliacompany.tiberius.user.service;

import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastTestCacheMap;
import com.teliacompany.tiberius.user.cache.LdapGroupCache;

public class LdapGroupCacheMock extends LdapGroupCache {
    public LdapGroupCacheMock() {
        super.initialize(new TiberiusHazelcastTestCacheMap<>("Tiberius-User", "ldapGroups"));
    }
}
