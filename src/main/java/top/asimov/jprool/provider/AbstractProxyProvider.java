package top.asimov.jprool.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import top.asimov.jprool.pool.ProxyPool;
import top.asimov.jprool.provider.ProxyProviderConfig.BusinessConfig;
import top.asimov.jprool.proxy.AbstractProxy;

@Slf4j
public abstract class AbstractProxyProvider implements ProxyProvider {

  protected final static ObjectMapper objectMapper = new ObjectMapper();

  protected final static HttpClient httpClient = HttpClient.newBuilder().build();

  protected static ScheduledExecutorService ses;

  protected ProxyPool proxyPool;

  @Value("${proxy.provider.pool-size}")
  private void setScheduledExecutorPool(Integer poolSize) {
    ses = new ScheduledThreadPoolExecutor(poolSize);
  }

  protected abstract JsonNode getProxyJsonNode(JsonNode rawJsonNode, BusinessConfig businessConfig);

  protected abstract List<AbstractProxy> convert(JsonNode proxyJsonNode, BusinessConfig businessConfig);

  public void scheduledAddProxyToPool(ProxyProviderConfig providerConfig) {
    if (providerConfig.getEnabled()) {
      List<BusinessConfig> businesss = providerConfig.getBusinessConfigs();
      for (BusinessConfig config : businesss) {
        if (config.getEnabled()) {
          ses.scheduleAtFixedRate(
              () -> this.addroxyToPool(config),
              config.getRequestConfig().getInitialDelay(),
              config.getRequestConfig().getRate(),
              TimeUnit.SECONDS);
        }
      }
    }
  }

  @Override
  public void addroxyToPool(BusinessConfig config) {
    List<AbstractProxy> proxies = getProxies(config);
    log.info("prepare add proxies {} to proxy pool", proxies.stream().map(AbstractProxy::getHost).toList());
    proxies.forEach(proxyPool::add);
  }

  protected String createRequestUrl(Map<String, Object> apiConfig) {
    StringBuilder url = new StringBuilder(String.valueOf(apiConfig.get("baseUrl")).concat("?"));
    for (Entry<String, Object> entry : apiConfig.entrySet()) {
      if ("baseUrl".equals(entry.getKey())) {
        continue;
      }
      url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
    }
    return url.substring(0, url.length() - 1);
  }

  public List<AbstractProxy> getProxies(BusinessConfig businessConfig) {
    String requestUrl = createRequestUrl(businessConfig.getApiConfig());
    log.info("call proxy provider api: {}", requestUrl);
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl))
          .timeout(Duration.ofSeconds(businessConfig.getRequestConfig().getTimeout())).GET().build();
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      if (HttpURLConnection.HTTP_OK != response.statusCode()) {
        log.error("calling proxy provider api error");
        return Collections.emptyList();
      }
      JsonNode rawJsonNode = objectMapper.readTree(response.body());
      JsonNode proxyJsonNode = getProxyJsonNode(rawJsonNode, businessConfig);
      return convert(proxyJsonNode, businessConfig);
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("convert provider proxy error");
    } catch (IOException | InterruptedException ioException) {
      log.error("sendding proxy provider api request error");
    }
    return Collections.emptyList();
  }

}
