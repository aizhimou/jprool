package top.asimov.jprool.provider.qg;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import top.asimov.jprool.pool.ProxyPool;
import top.asimov.jprool.provider.AbstractProxyProvider;
import top.asimov.jprool.proxy.AbstractProxy;
import top.asimov.jprool.proxy.TimelinessProxy;
import top.asimov.jprool.proxy.enums.ProtocolEnum;

@Slf4j
@Component
@ConditionalOnProperty(value = "proxy.provider.qg.enabled")
public class QgProxyProvider extends AbstractProxyProvider {

  private final static DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final QgProxyConfig config;

  private final HashSet<TimelinessProxy> unduplicateProxySet;

  public QgProxyProvider(ProxyPool proxyPool, QgProxyConfig config) {
    super.proxyPool = proxyPool;
    this.config = config;
    this.unduplicateProxySet = new HashSet<>(config.getDeduplicatePoolSize());
  }

  @Override
  @PostConstruct
  public void scheduledAddProxyToPool() {
    ses.scheduleAtFixedRate(() -> this.addroxyToPool(config),
        config.getRequestInitialDelay(), config.getRequestRate(), TimeUnit.SECONDS);
  }

  @Override
  protected String createRequestUrl() {
    return String.format("%s?Key=%s&Num=%d", config.getRequestBaseUrl(), config.getAuthKey(), config.getNum());
  }

  @Override
  protected JsonNode getProxyJsonNode(JsonNode rawJsonNode) {
    String code = rawJsonNode.get("Code").asText();
    if (!config.getRequestSuccessCode().equals(code)) {
      log.error("青果代理API请求错误：{}", rawJsonNode.get("Msg").asText());
      return null;
    }
    return rawJsonNode.get("Data");
  }

  @Override
  protected List<AbstractProxy> convert(JsonNode proxyJsonNode) {
    List<AbstractProxy> list = new ArrayList<>(proxyJsonNode.size());
    Map<String, String> map = new HashMap<>(1);
    for (JsonNode proxy : proxyJsonNode) {
      Iterator<Entry<String, JsonNode>> fields = proxy.fields();
      fields.forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
      TimelinessProxy timelinessProxy = TimelinessProxy.builder()
          .ip(map.get("IP"))
          .port(Integer.parseInt(map.get("port")))
          .host(String.format("%s:%s", map.get("IP"), map.get("port")))
          .region(map.get("region"))
          .expirationTimestamp(LocalDateTime.parse(map.get("deadline"), pattern))
          .protocol(ProtocolEnum.http)
          .source("qg.net")
          .build();
      if (unduplicate(timelinessProxy)) {
        list.add(timelinessProxy);
      }
    }
    return list;
  }

  private Boolean unduplicate(TimelinessProxy proxy) {
    if (unduplicateProxySet.size() >= config.getDeduplicatePoolSize()) {
      log.info("clear {} unduplicateProxySet !", getClass().getSimpleName());
      unduplicateProxySet.clear();
    }
    return unduplicateProxySet.add(proxy);
  }

}
