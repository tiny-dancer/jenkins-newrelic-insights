package org.jenkinsci.plugins.plugin.newrelic;

import com.google.gson.Gson;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.jenkinsci.plugins.plugin.KeyValue;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;


/**
 * REST client implementation for the New Relic API.
 */
public class NewRelicInsightsApacheClient implements NewRelicInsights {

    private static final String API_URL = "http://insights-collector.newrelic.com";

    private static final String INSIGHTS_PATH_PRE_ACCOUNT = "/v1/accounts/";

    private static final String INSIGHTS_PATH_POST_ACCOUNT = "/events";

    private URI getInsightsAccountUrl(String accountId) {
        try {
            return new URI(API_URL + INSIGHTS_PATH_PRE_ACCOUNT + accountId + INSIGHTS_PATH_POST_ACCOUNT);
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendCustomEvent(String insertKey, String accountId, Object data, List<KeyValue> keyValues) throws IOException {
        URI url = getInsightsAccountUrl(accountId);
        HttpPost request = new HttpPost(url);
        String json;

        if (keyValues != null && !keyValues.isEmpty()) {
            Map<String, String> objectMap = new HashedMap();

            for (KeyValue keyValue : keyValues) {
                objectMap.put(keyValue.getKey(), keyValue.getValue());
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll( objectMap );
            json = jsonObject.toString();
        } else {
            json = new Gson().toJson(data);
        }

        setHeaders(request, insertKey);
        StringEntity entity = new StringEntity(json);
        request.setEntity(entity);
        request.setHeader("Accept", "application/jsonObject");
        request.setHeader("Content-type", "application/jsonObject");

        CloseableHttpClient client = getHttpClient(url);
        boolean result;
        try {
            CloseableHttpResponse response = client.execute(request);
            result = HttpStatus.SC_OK == response.getStatusLine().getStatusCode();
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getApiEndpoint() {
        return API_URL;
    }

    protected CloseableHttpClient getHttpClient(URI url) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        Jenkins instance = Jenkins.getInstance();

        if (instance != null) {
            ProxyConfiguration proxyConfig = instance.proxy;
            if (proxyConfig != null) {
                Proxy proxy = proxyConfig.createProxy(url.getHost());
                if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
                    SocketAddress addr = proxy.address();
                    if (addr != null && addr instanceof InetSocketAddress) {
                        InetSocketAddress proxyAddr = (InetSocketAddress) addr;
                        HttpHost proxyHost = new HttpHost(proxyAddr.getAddress().getHostAddress(), proxyAddr.getPort());
                        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
                        builder = builder.setRoutePlanner(routePlanner);

                        String proxyUser = proxyConfig.getUserName();
                        if (proxyUser != null) {
                            String proxyPass = proxyConfig.getPassword();
                            CredentialsProvider cred = new BasicCredentialsProvider();
                            cred.setCredentials(new AuthScope(proxyHost),
                                    new UsernamePasswordCredentials(proxyUser, proxyPass));
                            builder = builder
                                    .setDefaultCredentialsProvider(cred)
                                    .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
                        }
                    }
                }
            }
        }

        return builder.build();
    }

    private void setHeaders(HttpRequest request, String apiKey) {
        request.addHeader("X-Insert-Key", apiKey);
        request.addHeader("Accept", "application/json");
    }
}

