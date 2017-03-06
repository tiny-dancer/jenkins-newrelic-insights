package org.jenkinsci.plugins.plugin.newrelic;

import java.io.IOException;

public interface NewRelicInsights {
    boolean sendCustomEvent(String insertKey, String accountId, String data) throws IOException;
    String getApiEndpoint();
}
