package org.folio.edge.rtac.security;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;

public class VaultStore extends SecureStore {

  public static final String TYPE = "Vault";

  public static final String PROP_VAULT_TOKEN = "token";
  public static final String PROP_VAULT_ADDRESS = "address";
  public static final String PROP_VAULT_USE_SSL = "enableSSL";
  public static final String PROP_SSL_PEM_FILE = "ssl.pem.path";
  public static final String PROP_TRUSTSTORE_JKS_FILE = "ssl.truststore.jks.path";
  public static final String PROP_KEYSTORE_JKS_FILE = "ssl.keystore.jks.path";
  public static final String PROP_KEYSTORE_PASSWORD = "ssl.keystore.password";

  public static final String DEFAULT_VAULT_ADDRESS = "http://127.0.0.1:8200";
  public static final String DEFAULT_VAULT_USER_SSL = "false";

  private static final Logger logger = Logger.getLogger(VaultStore.class);

  private Vault vault;

  public VaultStore(Properties properties) {
    super(properties);
    logger.info("Initializing...");

    final String token = properties.getProperty(PROP_VAULT_TOKEN);
    final String addr = properties.getProperty(PROP_VAULT_ADDRESS, DEFAULT_VAULT_ADDRESS);
    final boolean useSSL = Boolean.getBoolean(properties.getProperty(PROP_VAULT_USE_SSL, DEFAULT_VAULT_USER_SSL));

    try {
      VaultConfig config = new VaultConfig()
        .address(addr)
        .token(token);

      if (useSSL) {
        SslConfig sslConfig = new SslConfig();

        final String pemPath = properties.getProperty(PROP_SSL_PEM_FILE);
        if (pemPath != null) {
          sslConfig.clientKeyPemFile(new File(pemPath));
        }

        final String truststorePath = properties.getProperty(PROP_TRUSTSTORE_JKS_FILE);
        if (truststorePath != null) {
          sslConfig.trustStoreFile(new File(truststorePath));
        }

        final String keystorePass = properties.getProperty(PROP_KEYSTORE_PASSWORD);
        final String keystorePath = properties.getProperty(PROP_KEYSTORE_JKS_FILE);
        if (keystorePath != null) {
          sslConfig.keyStoreFile(new File(keystorePath), keystorePass);
        }

        config.sslConfig(sslConfig);
      }

      vault = new Vault(config.build());
    } catch (VaultException e) {
      logger.error("Failed to initialize: ", e);
    }
  }

  @Override
  public String get(String tenant, String username) {
    String ret = null;

    try {
      ret = vault.logical()
        .read("secret/" + tenant)
        .getData()
        .get(username);
    } catch (VaultException e) {
      logger.error(String.format("Exception retreiving password for secret/%s:%s: %s",
          tenant,
          username,
          e.getMessage()));
    }

    return ret;
  }
}
