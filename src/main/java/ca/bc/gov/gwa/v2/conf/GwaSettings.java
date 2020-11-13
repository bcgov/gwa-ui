/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.LoggerFactory;


public class GwaSettings {

    private final Map<String, Object> config = new HashMap<>();

    private String gitHubAccessToken;

    private String gitHubClientId;

    private String gitHubClientSecret;

    private String gitHubOrganizationName;

    private String gitHubOrganizationRole;

    private String kongAdminPassword = null;

    private String kongAdminUrl = "http://localhost:8001";

    private String kongAdminUsername = null;

    private int apiKeyExpiryDays = 90;

    private boolean useEndpoints = true;
    
    private String oidcBaseUri;
    
    private String oidcRealm;
    
    private String oidcClientId;
    
    private String oidcClientSecret;
    
    private String oidcCallbackUrl;
    
    private String apiKey;
    
    private String keycloakUrl;
    
    private String keycloakRealm;
    
    private String keycloakUsername;
    
    private String keycloakPassword;
    
    private String grafanaUrl;
    
    private String gwaApiUrl;

    public Map<String, Object> getConfig() {
        return config;
    }

    public String getGitHubAccessToken() {
        return gitHubAccessToken;
    }

    public String getGitHubClientId() {
        return gitHubClientId;
    }

    public String getGitHubClientSecret() {
        return gitHubClientSecret;
    }

    public String getGitHubOrganizationName() {
        return gitHubOrganizationName;
    }

    public String getGitHubOrganizationRole() {
        return gitHubOrganizationRole;
    }

    public String getKongAdminPassword() {
        return kongAdminPassword;
    }

    public String getKongAdminUrl() {
        return kongAdminUrl;
    }

    public String getKongAdminUsername() {
        return kongAdminUsername;
    }

    public int getApiKeyExpiryDays() {
        return apiKeyExpiryDays;
    }

    public boolean isUseEndpoints() {
        return useEndpoints;
    }

    public String getOidcBaseUri() {
        return oidcBaseUri;
    }

    public String getOidcRealm() {
        return oidcRealm;
    }

    public String getOidcClientId() {
        return oidcClientId;
    }

    public String getOidcClientSecret() {
        return oidcClientSecret;
    }

    public String getOidcCallbackUrl() {
        return oidcCallbackUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getKeycloakUrl() {
        return keycloakUrl;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public String getKeycloakUsername() {
        return keycloakUsername;
    }

    public String getKeycloakPassword() {
        return keycloakPassword;
    }

    public String getGwaApiUrl() {
        return gwaApiUrl;
    }

    public String getGrafanaUrl() {
        return grafanaUrl;
    }
    
    
    
    public GwaSettings() {
        try {
            readProperties();
            this.kongAdminUrl = getConfig("gwaKongAdminUrl", this.kongAdminUrl);
            this.kongAdminUsername = getConfig("gwaKongAdminUsername", this.kongAdminUsername);
            this.kongAdminPassword = getConfig("gwaKongAdminPassword", this.kongAdminPassword);
            this.gitHubOrganizationName = getConfig("gwaGitHubOrganization", "gwa-qa");
            this.gitHubOrganizationRole = "github_" + this.gitHubOrganizationName.toLowerCase();
            this.gitHubAccessToken = getConfig("gwaGitHubAccessToken");
            this.gitHubClientId = getConfig("gwaGitHubClientId");
            this.gitHubClientSecret = getConfig("gwaGitHubClientSecret");
            this.apiKeyExpiryDays = Integer.parseInt(getConfig("gwaApiKeyExpiryDays", "90"));
            this.useEndpoints = !"false".equals(getConfig("gwaUseEndpoints", "true"));

            this.oidcBaseUri = getConfig("oidcBaseUri");
            this.oidcRealm = getConfig("oidcRealm");
            this.oidcClientId = getConfig("oidcClientId");
            this.oidcClientSecret = getConfig("oidcClientSecret");
            this.oidcCallbackUrl = getConfig("oidcCallbackUrl");

            this.apiKey = getConfig("apiKey");

            this.keycloakUrl = getConfig("keycloakUrl");
            this.keycloakRealm = getConfig("keycloakRealm");
            this.keycloakUsername = getConfig("keycloakUsername");
            this.keycloakPassword = getConfig("keycloakPassword");

            this.gwaApiUrl = getConfig("gwaApiUrl");

            this.grafanaUrl = getConfig("grafanaUrl");
            
            if (this.oidcClientId == null || this.oidcClientSecret == null) {
                LoggerFactory.getLogger(getClass())
                        .error("Missing oidc client configuration");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Something is wrong with your configuration!", ex);
        }
    }

    private void readProperties() throws IOException {
        final String catalinaBase = System.getProperty("catalina.base");
        for (final String dir : Arrays.asList(catalinaBase, ".", "..", "/apps")) {
            final String fileName = dir + "/config/gwa.properties";
            final File propertiesFile = new File(fileName);
            if (propertiesFile.exists()) {
                final Properties properties = new Properties();

                try (FileInputStream in = new FileInputStream(propertiesFile)) {
                    properties.load(in);
                    final Enumeration<?> propertyNames = properties.propertyNames();
                    while (propertyNames.hasMoreElements()) {
                        final String propertyName = (String) propertyNames.nextElement();
                        final String value = properties.getProperty(propertyName);
                        this.config.put(propertyName, value);
                    }
                }
                return;
            }
        }
    }

    public String getConfig(final String name) {
        return getConfig(name, null);
    }

    public String getConfig(final String name, final String defaultValue) {
        String value = null;
        value = System.getProperty(name);
        if (value == null) {
            value = (String) this.config.get(name);
        }
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

}
