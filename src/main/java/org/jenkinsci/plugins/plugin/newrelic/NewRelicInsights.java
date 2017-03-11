package org.jenkinsci.plugins.plugin.newrelic;

import org.jenkinsci.plugins.plugin.KeyValue;

import java.io.IOException;
import java.util.List;

public interface NewRelicInsights {
    boolean sendCustomEvent(String insertKey, String accountId, String data, List<KeyValue> keyValues) throws IOException;
    String getApiEndpoint();
}
