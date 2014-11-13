package org.sagebionetworks.audit;

import java.io.File;
import java.io.IOException;

import org.sagebionetworks.dashboard.config.Config;
import org.sagebionetworks.dashboard.config.DefaultConfig;
import org.sagebionetworks.dashboard.config.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("auditConfig")
public class AuditConfig implements Config {

    public AuditConfig() {
        try {
            String userHome = System.getProperty("user.home");
            File configFile = new File(userHome + "/.audit/audit.config");
            if (!configFile.exists()) {
                logger.warn("Missing config file " + configFile.getPath());
                // This file is needed as the source of properties
                // which should be overwritten by environment variables
                // or command-line arguments
                configFile = new File(getClass().getResource("/audit.config").getFile());
            }
            config = new DefaultConfig(configFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String key) {
        return config.get(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    @Override
    public int getInt(String key) {
        return config.getInt(key);
    }

    @Override
    public long getLong(String key) {
        return config.getLong(key);
    }

    @Override
    public Stack getStack() {
        return config.getStack();
    }

    public String getDbUrl() {
        return config.get("db.url");
    }

    public String getDbUsername() {
        return config.get("db.username");
    }

    public String getDbPassword() {
        return config.get("db.password");
    }

    private final Logger logger = LoggerFactory.getLogger(AuditConfig.class);
    private final Config config;
}
