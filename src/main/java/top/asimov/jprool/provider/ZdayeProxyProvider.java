package top.asimov.jprool.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.monkeywie.proxyee.proxy.ProxyType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import top.asimov.jprool.pool.ProxyPool;
import top.asimov.jprool.provider.ProxyProviderConfig.BusinessConfig;
import top.asimov.jprool.proxy.AbstractProxy;
import top.asimov.jprool.proxy.TimelinessProxy;

@Slf4j
@Component
public class ZdayeProxyProvider extends AbstractProxyProvider {

  private final List<ProxyProviderConfig> providerConfigList;

  private final static String PROVIDER_NAME = "zdaye.com";

  private HashSet<String> unduplicateProxySet;

  private ProxyProviderConfig providerConfig;

  public ZdayeProxyProvider(ProxyPool proxyPool, List<ProxyProviderConfig> providerConfigList) {
    this.providerConfigList = providerConfigList;
    super.proxyPool = proxyPool;
    loadConfig();
  }

  @Override
  public void run(ApplicationArguments args) {
    super.scheduledAddProxyToPool(providerConfig);
  }

  @Override
  protected void loadConfig() {
    providerConfig = super.loadConfig(providerConfigList, PROVIDER_NAME);
    if (Objects.nonNull(this.providerConfig)) {
      this.unduplicateProxySet = new HashSet<>(providerConfig.getDeduplicatePoolSize());
    }
  }

  @Override
  protected List<AbstractProxy> convert(String response, BusinessConfig config) {
    try {
      JsonNode rawJsonNode = objectMapper.readTree(response);

      String code = rawJsonNode.get("code").asText();
      if (!config.getRequestConfig().getSuccessCode().equals(code)) {
        log.error("站大爷代理API返回错误：{}", rawJsonNode.get("msg").asText());
        return Collections.emptyList();
      }
      JsonNode proxyJsonNode = rawJsonNode.get("data").get("proxy_list");

      ProxyType proxyType = ProxyType.valueOf(config.getProxyType().toUpperCase());
      List<AbstractProxy> list = new ArrayList<>(proxyJsonNode.size());
      for (JsonNode proxy : proxyJsonNode) {
        Map<String, String> map = objectMapper.convertValue(proxy, new TypeReference<>() {});
        String ip = map.get("ip");
        String port = map.get("port");
        String host = ip.concat(":").concat(port);
        if (unduplicate(host)) {
          list.add(TimelinessProxy.builder()
              .proxyType(proxyType)
              .username(config.getUsername())
              .password(config.getPassword())
              .ip(ip)
              .port(Integer.parseInt(port))
              .host(host)
              .region(map.get("adr"))
              .expirationTimestamp(LocalDateTime.now().plusSeconds(Long.parseLong(map.get("timeout"))))
              .source(PROVIDER_NAME)
              .build());
        }
      }
      return list;
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("站大爷代理转换错误：{}", jsonProcessingException.getMessage());
    }
    return Collections.emptyList();
  }

  protected Boolean unduplicate(String host) {
    if (unduplicateProxySet.size() >= providerConfig.getDeduplicatePoolSize()) {
      log.info("clear {} unduplicateProxySet !", getClass().getSimpleName());
      unduplicateProxySet.clear();
    }
    return unduplicateProxySet.add(host);
  }

}
