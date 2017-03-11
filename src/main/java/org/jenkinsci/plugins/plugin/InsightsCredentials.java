package org.jenkinsci.plugins.plugin;

import com.cloudbees.plugins.credentials.Credentials;
import hudson.util.Secret;

public interface InsightsCredentials extends Credentials {

    String getName();

    String getDescription();

    Secret getApiKey();

}