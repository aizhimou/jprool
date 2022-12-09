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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import top.asimov.jprool.pool.ProxyPool;
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

  protected abstract String createRequestUrl();

  protected abstract JsonNode getProxyJsonNode(JsonNode rawJsonNode);

  protected abstract List<AbstractProxy> convert(JsonNode proxyJsonNode);

  @Override
  public void addroxyToPool(BaseProviderConfig config) {
    List<AbstractProxy> proxies = getProxies(config);
    log.info("prepare add proxies {} to proxy pool", proxies.stream().map(AbstractProxy::getHost).toList());
    proxies.forEach(proxyPool::add);
  }

  public List<AbstractProxy> getProxies(BaseProviderConfig config) {
    String requestUrl = createRequestUrl();
    log.info("call proxy provider api: {}", requestUrl);
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl))
          .timeout(Duration.ofSeconds(config.getRequestTimeout())).GET().build();
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      if (HttpURLConnection.HTTP_OK != response.statusCode()) {
        log.error("calling proxy provider api error");
        return Collections.emptyList();
      }
      JsonNode rawJsonNode = objectMapper.readTree(response.body());
      JsonNode proxyJsonNode = getProxyJsonNode(rawJsonNode);
      return convert(proxyJsonNode);
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("convert provider proxy error");
    } catch (IOException | InterruptedException ioException) {
      log.error("sendding proxy provider api request error");
    }
    return Collections.emptyList();
  }

}
