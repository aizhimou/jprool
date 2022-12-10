package top.asimov.jprool.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.monkeywie.proxyee.proxy.ProxyType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.asimov.jprool.pool.ProxyPool;
import top.asimov.jprool.provider.ProxyProviderConfig.BusinessConfig;
import top.asimov.jprool.proxy.AbstractProxy;
import top.asimov.jprool.proxy.TimelinessProxy;

@Slf4j
@Component
public class QgProxyProvider extends AbstractProxyProvider {

  private final static DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final List<ProxyProviderConfig> providerConfigList;

  private HashSet<TimelinessProxy> unduplicateProxySet;

  private ProxyProviderConfig providerConfig;

  public QgProxyProvider(ProxyPool proxyPool, List<ProxyProviderConfig> providerConfigList) {
    this.providerConfigList = providerConfigList;
    super.proxyPool = proxyPool;
    loadQgConfig();
  }

  public ProxyProviderConfig getProviderConfig() {
    return this.providerConfig;
  }

  private void loadQgConfig() {
    this.providerConfig = providerConfigList.stream()
        .filter(config -> config.getProvider().equals("qg.net"))
        .findFirst().orElse(null);
    // enabledQgProvider = Objects.nonNull(this.providerConfig);
    if (Objects.nonNull(this.providerConfig)) {
      this.unduplicateProxySet = new HashSet<>(providerConfig.getDeduplicatePoolSize());
    }
  }

  @Override
  protected JsonNode getProxyJsonNode(JsonNode rawJsonNode, BusinessConfig businessConfig) {
    String code = rawJsonNode.get("Code").asText();
    if (!businessConfig.getRequestConfig().getSuccessCode().equals(code)) {
      log.error("青果代理API请求错误：{}", rawJsonNode.get("Msg").asText());
      return null;
    }
    return rawJsonNode.get("Data");
  }

  @Override
  protected List<AbstractProxy> convert(JsonNode proxyJsonNode, BusinessConfig businessConfig) {
    ProxyType proxyType = ProxyType.valueOf(businessConfig.getProxyType().toUpperCase());
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
          .proxyType(proxyType)
          .username(businessConfig.getUsername())
          .password(businessConfig.getPassword())
          .source("qg.net")
          .build();
      if (unduplicate(timelinessProxy)) {
        list.add(timelinessProxy);
      }
    }
    return list;
  }

  private Boolean unduplicate(TimelinessProxy proxy) {
    if (unduplicateProxySet.size() >= providerConfig.getDeduplicatePoolSize()) {
      log.info("clear {} unduplicateProxySet !", getClass().getSimpleName());
      unduplicateProxySet.clear();
    }
    return unduplicateProxySet.add(proxy);
  }

}
