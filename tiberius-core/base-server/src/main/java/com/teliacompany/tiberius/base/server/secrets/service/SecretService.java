package com.teliacompany.tiberius.base.server.secrets.service;

import java.util.List;
import java.util.Map;

public interface SecretService {

    List<String> requestSecretNames();

    void onSecretsReceived(Map<String, String> secrets);
}
