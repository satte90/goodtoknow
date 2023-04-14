package com.teliacompany.tiberius.user.cache;

import com.teliacompany.ldap.model.LdapGroup;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastCache;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastComponent;
import reactor.core.publisher.Mono;

@TiberiusHazelcastComponent(
        name = LdapGroupCache.NAME,
        timeToLiveSeconds = "86400",
        maxIdleSeconds = "80000",
        maxSize = "10000",
        version = 1
)
public class LdapGroupCache extends TiberiusHazelcastCache<String, LdapGroupCacheItem> {
    public static final String NAME = "ldapGroups";
    @Override
    protected String keyExtractor(LdapGroupCacheItem value) {
        return value.getLdapGroupKey();
    }

    public Mono<LdapGroup> putLdapGroup(String key, LdapGroup ldapGroup) {
        return super.put(new LdapGroupCacheItem(key, ldapGroup))
                .then(Mono.just(ldapGroup));
    }

    public Mono<LdapGroup> getLdapGroup(String key) {
        return super.get(key)
                .map(LdapGroupCacheItem::getLdapGroup);
    }
}
