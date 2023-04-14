package com.teliacompany.tiberius.base.server.secrets.agent;

import com.teliacompany.tiberius.base.server.secrets.service.SecretService;
import com.teliacompany.tiberius.crypto.CryptoUtils;
import com.teliacompany.tiberius.crypto.exception.TiberiusCryptoException;
import com.teliacompany.tiberius.user.auth.api.v1.secrets.SecretResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public final class SecretAgentHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SecretAgentHelper.class);
    private final Map<Class<? extends SecretService>, List<String>> secretsToRequest = new HashMap<>();
    private final Map<Class<? extends SecretService>, Map<String, String>> secretsFound = new HashMap<>();
    private final Set<String> secretsRequest;
    private final List<SecretService> tiberiusSecretServices;
    private final DecryptionKeyOrSecret decryptionKeyOrSecret;

    public SecretAgentHelper(List<SecretService> tiberiusSecretServices) {
        this(tiberiusSecretServices, new DecryptionKeyOrSecret());
    }

    public SecretAgentHelper(List<SecretService> tiberiusSecretServices, Key key) {
        this(tiberiusSecretServices, new DecryptionKeyOrSecret(key));
    }

    public SecretAgentHelper(List<SecretService> tiberiusSecretServices, String secret) {
        this(tiberiusSecretServices, new DecryptionKeyOrSecret(secret));
    }

    public SecretAgentHelper(List<SecretService> tiberiusSecretServices, DecryptionKeyOrSecret decryptionKeyOrSecret) {
        this.tiberiusSecretServices = tiberiusSecretServices;
        this.decryptionKeyOrSecret = decryptionKeyOrSecret;

        tiberiusSecretServices.forEach(ss -> secretsToRequest.put(ss.getClass(), ss.requestSecretNames()));

        final String secretServiceNames = tiberiusSecretServices.stream().map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(", "));
        LOG.info("Found {} secretServices: [{}]", tiberiusSecretServices.size(), secretServiceNames);

        this.secretsRequest = secretsToRequest.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        secretsToRequest.keySet().forEach(clazz -> secretsFound.put(clazz, new HashMap<>()));
    }

    public Set<String> getSecretsRequest() {
        return secretsRequest;
    }

    public String updateSecretsAndNotifySecretServices(List<SecretResponse> secrets) {
        LOG.info("Found {} secrets...", secrets.size());
        secrets.forEach(secret -> {
            updateSecretsAndNotifySecretServices(secret, decryptSecret(secret, this.decryptionKeyOrSecret));
        });

        LOG.info("Sending onSecretsReceived notifications");
        tiberiusSecretServices.forEach(secretService -> {
            final Map<String, String> secretsForService = secretsFound.get(secretService.getClass());
            secretService.onSecretsReceived(secretsForService);
        });

        LOG.info("Secret notifications sent successfully");
        return "OK";
    }

    private static String decryptSecret(SecretResponse secret, DecryptionKeyOrSecret keyOrSecret) {
        try {
            if(keyOrSecret.isKey()) {
                LOG.info("Decrypting secret: {}", secret.getName());
                return CryptoUtils.decrypt(secret.getEncryptedSecret(), keyOrSecret.key);
            } else if(keyOrSecret.isSecret()) {
                LOG.info("Decrypting secret: {}", secret.getName());
                return CryptoUtils.decrypt(secret.getEncryptedSecret(), keyOrSecret.secret);
            }
        } catch(TiberiusCryptoException | IllegalArgumentException e) {
            // Probably not encrypted, use as is.
            LOG.warn("Found unencrypted secret. Use for example Tiberius Crypto Utils to encrypt it and store it (requires tiberius secret encryption key)");
            return secret.getEncryptedSecret();
        }
        //Not encrypted
        return secret.getEncryptedSecret();
    }

    private void updateSecretsAndNotifySecretServices(SecretResponse secret, String secretValue) {
        secretsToRequest.entrySet().stream()
                .filter(e -> e.getValue().contains(secret.getName()))
                .map(Entry::getKey)
                .forEach(clazz -> putSecret(clazz, secret.getName(), secretValue));
    }

    private void putSecret(Class<? extends SecretService> clazz, String secretName, String decryptedSecretValue) {
        secretsFound.get(clazz)
                .put(secretName, decryptedSecretValue);
    }

    private static class DecryptionKeyOrSecret {
        private final String secret;
        private final Key key;

        private DecryptionKeyOrSecret() {
            this.key = null;
            this.secret = null;
        }

        private DecryptionKeyOrSecret(Key key) {
            this.key = key;
            this.secret = null;
        }

        private DecryptionKeyOrSecret(String secret) {
            this.secret = secret;
            this.key = null;
        }

        private boolean isKey() {
            return key != null;
        }

        private boolean isSecret() {
            return secret != null;
        }
    }
}
