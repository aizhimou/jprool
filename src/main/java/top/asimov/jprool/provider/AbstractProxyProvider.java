package top.asimov.jprool.provider;

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
import org.springframework.boot.ApplicationRunner;
import org.springframework.lang.NonNull;
import top.asimov.jprool.pool.ProxyPool;
import top.asimov.jprool.provider.ProxyProviderConfig.BusinessConfig;
import top.asimov.jprool.proxy.AbstractProxy;

@Slf4j
public abstract class AbstractProxyProvider implements ProxyProvider, ApplicationRunner {

  protected final static ObjectMapper objectMapper = new ObjectMapper();

  protected final static HttpClient httpClient = HttpClient.newBuilder().build();

  protected static ScheduledExecutorService ses;

  protected ProxyPool proxyPool;

  @NonNull
  protected abstract List<AbstractProxy> convert(String response, BusinessConfig config);

  protected abstract void loadConfig();

  public void addroxyToPool(BusinessConfig config) {
    List<AbstractProxy> proxies = getProxies(config);
    proxies.forEach(proxyPool::add);
  }

  protected ProxyProviderConfig loadConfig(List<ProxyProviderConfig> configList, String providerName) {
    return configList.stream()
        .filter(item -> item.getProvider().equals(providerName))
        .findFirst().orElse(null);
  }

  protected void scheduledAddProxyToPool(ProxyProviderConfig providerConfig) {
    if (providerConfig.getEnabled()) {
      List<BusinessConfig> businesss = providerConfig.getBusinessConfig();
      for (BusinessConfig config : businesss) {
        if (config.getEnabled()) {
          ses.scheduleAtFixedRate(
              () -> this.addroxyToPool(config),
              config.getRequestConfig().getInitialDelay(),
              config.getRequestConfig().getRequestRate(),
              TimeUnit.SECONDS);
        }
      }
    }
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

  private List<AbstractProxy> getProxies(BusinessConfig config) {
    String requestUrl = createRequestUrl(config.getApiConfig());
    log.info("call proxy provider api: {}", requestUrl);
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl))
          .timeout(Duration.ofSeconds(config.getRequestConfig().getTimeout())).GET().build();
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      if (HttpURLConnection.HTTP_OK != response.statusCode()) {
        log.error("calling proxy provider api error");
        return Collections.emptyList();
      }
      return convert(response.body(), config);
    } catch (IOException | InterruptedException ioException) {
      log.error("sendding proxy provider api request error");
    }
    return Collections.emptyList();
  }

  @Value("${proxy.provider.pool-size}")
  private void setScheduledExecutorPool(Integer poolSize) {
    ses = new ScheduledThreadPoolExecutor(poolSize);
  }

}
