package top.asimov.jprool.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.monkeywie.proxyee.proxy.ProxyType;
import java.time.LocalDateTime;
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
public class ZdayeProxyProvider extends AbstractProxyProvider {

  private final List<ProxyProviderConfig> providerConfigList;

  private HashSet<String> unduplicateProxySet;

  private ProxyProviderConfig providerConfig;

  public ZdayeProxyProvider(ProxyPool proxyPool, List<ProxyProviderConfig> providerConfigList) {
    this.providerConfigList = providerConfigList;
    super.proxyPool = proxyPool;
    loadZdayeConfig();
  }

  public ProxyProviderConfig getProviderConfig() {
    return this.providerConfig;
  }

  private void loadZdayeConfig() {
    this.providerConfig = providerConfigList.stream()
        .filter(config -> config.getProvider().equals("zdaye.com"))
        .findFirst().orElse(null);
    if (Objects.nonNull(this.providerConfig)) {
      this.unduplicateProxySet = new HashSet<>(providerConfig.getDeduplicatePoolSize());
    }
  }

  @Override
  protected JsonNode getProxyJsonNode(JsonNode rawJsonNode, BusinessConfig businessConfig) {
    String code = rawJsonNode.get("code").asText();
    if (!businessConfig.getRequestConfig().getSuccessCode().equals(code)) {
      log.error("站大爷代理API请求错误：{}", rawJsonNode.get("msg").asText());
      return null;
    }
    return rawJsonNode.get("data").get("proxy_list");
  }

  @Override
  protected List<AbstractProxy> convert(JsonNode proxyJsonNode, BusinessConfig businessConfig) {
    ProxyType proxyType = ProxyType.valueOf(businessConfig.getProxyType().toUpperCase());
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
            .proxyType(proxyType)
            .username(businessConfig.getUsername())
            .password(businessConfig.getPassword())
            .ip(ip)
            .port(Integer.parseInt(port))
            .host(host)
            .region(map.get("adr"))
            .expirationTimestamp(LocalDateTime.now().plusSeconds(Long.parseLong(map.get("timeout"))))
            .source("zdaye.com")
            .build());
      }
    }
    return list;
  }

  protected Boolean unduplicate(String host) {
    if (unduplicateProxySet.size() >= providerConfig.getDeduplicatePoolSize()) {
      log.info("clear {} unduplicateProxySet !", getClass().getSimpleName());
      unduplicateProxySet.clear();
    }
    return unduplicateProxySet.add(host);
  }
}
