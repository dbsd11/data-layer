package group.bison.data.data_layer.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class HttpClientFactoryUtil {

    public static final int GLOBAL_HTTP_READ_TIMEOUT = Integer.valueOf(StringUtils.defaultIfEmpty(System.getenv("globalHttpReadTimeout"), "5000"));

    private static final HttpClient CUSTOM_CLIENT = HttpClients.custom().useSystemProperties()
            .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(GLOBAL_HTTP_READ_TIMEOUT).build())
            .setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(GLOBAL_HTTP_READ_TIMEOUT).build())
            .evictExpiredConnections()
            .build();

    private static final ClientHttpRequestFactory CLIENT_HTTP_REQUEST_FACTORY = new HttpComponentsClientHttpRequestFactory(CUSTOM_CLIENT) {
        @Override
        protected void postProcessHttpRequest(HttpUriRequest request) {
            request.getParams().setParameter("", "");
            super.postProcessHttpRequest(request);
        }
    };

    public static ClientHttpRequestFactory getClientHttpRequestFactory() {
        return CLIENT_HTTP_REQUEST_FACTORY;
    }
}
