package top.asimov.jprool.provider.zdaye;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
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
@ConditionalOnProperty(value = "proxy.provider.zdaye.enabled")
public class ZdayeProxyProvider extends AbstractProxyProvider {

  private final ZdayeProxyConfig config;

  private final HashSet<String> unduplicateProxySet;


  public ZdayeProxyProvider(ProxyPool proxyPool, ZdayeProxyConfig config) {
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
    return String.format("%s?Key=%s&Num=%d", config.getRequestBaseUrl(), config.getApi(), config.getCount());
  }

  @Override
  protected JsonNode getProxyJsonNode(JsonNode rawJsonNode) {
    String code = rawJsonNode.get("code").asText();
    if (!config.getRequestSuccessCode().equals(code)) {
      log.error("站大爷代理API请求错误：{}", rawJsonNode.get("msg").asText());
      return null;
    }
    return rawJsonNode.get("data").get("proxy_list");
  }

  @Override
  protected List<AbstractProxy> convert(JsonNode proxyJsonNode) {
    List<AbstractProxy> list = new ArrayList<>(proxyJsonNode.size());
    for (JsonNode proxy : proxyJsonNode) {
      Map<String, String> map = new HashMap<>(1);
      Iterator<Entry<String, JsonNode>> fields = proxy.fields();
      fields.forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
      String ip = map.get("ip");
      String port = map.get("port");
      String host = ip.concat(":").concat(port);
      if (unduplicate(host)) {
        list.add(TimelinessProxy.builder()
            .ip(ip)
            .port(Integer.parseInt(port))
            .host(host)
            .region(map.get("adr"))
            .expirationTimestamp(LocalDateTime.now().plusSeconds(Long.parseLong(map.get("timeout"))))
            .protocol(ProtocolEnum.http)
            .source("zdaye.com")
            .build());
      }
    }
    return list;
  }

  protected Boolean unduplicate(String host) {
    if (unduplicateProxySet.size() >= config.getDeduplicatePoolSize()) {
      log.info("clear {} unduplicateProxySet !", getClass().getSimpleName());
      unduplicateProxySet.clear();
    }
    return unduplicateProxySet.add(host);
  }
}
