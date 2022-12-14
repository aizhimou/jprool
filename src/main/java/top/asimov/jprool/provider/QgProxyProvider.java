package top.asimov.jprool.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.monkeywie.proxyee.proxy.ProxyType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class QgProxyProvider extends AbstractProxyProvider {

  private final static DateTimeFormatter PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final static String PROVIDER_NAME = "qg.net";

  private final List<ProxyProviderConfig> providerConfigList;

  private HashSet<TimelinessProxy> unduplicateProxySet;

  private ProxyProviderConfig providerConfig;

  public QgProxyProvider(ProxyPool proxyPool, List<ProxyProviderConfig> providerConfigList) {
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
    if (Objects.nonNull(providerConfig)) {
      this.unduplicateProxySet = new HashSet<>(providerConfig.getDeduplicatePoolSize());
    }
  }

  @Override
  protected List<AbstractProxy> convert(String response, BusinessConfig config) {
    try {
      JsonNode rawJsonNode = objectMapper.readTree(response);

      String code = rawJsonNode.get("Code").asText();
      if (!config.getRequestConfig().getSuccessCode().equals(code)) {
        log.error("????????????API???????????????{}", rawJsonNode.get("Msg").asText());
        return Collections.emptyList();
      }
      JsonNode proxyJsonNode = rawJsonNode.get("Data");

      ProxyType proxyType = ProxyType.valueOf(config.getProxyType().toUpperCase());
      List<AbstractProxy> list = new ArrayList<>(proxyJsonNode.size());
      for (JsonNode proxy : proxyJsonNode) {
        Map<String, String> map = objectMapper.convertValue(proxy, new TypeReference<>(){});
        TimelinessProxy timelinessProxy = TimelinessProxy.builder()
            .ip(map.get("IP"))
            .port(Integer.parseInt(map.get("port")))
            .host(String.format("%s:%s", map.get("IP"), map.get("port")))
            .region(map.get("region"))
            .expirationTimestamp(LocalDateTime.parse(map.get("deadline"), PATTERN))
            .proxyType(proxyType)
            .username(config.getUsername())
            .password(config.getPassword())
            .source(PROVIDER_NAME)
            .build();
        if (unduplicate(timelinessProxy)) {
          list.add(timelinessProxy);
        }
      }
      return list;
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("???????????????????????????{}", jsonProcessingException.getMessage());
    }
    return Collections.emptyList();
  }

  protected Boolean unduplicate(TimelinessProxy proxy) {
    if (unduplicateProxySet.size() >= providerConfig.getDeduplicatePoolSize()) {
      log.info("clear {} unduplicateProxySet !", getClass().getSimpleName());
      unduplicateProxySet.clear();
    }
    return unduplicateProxySet.add(proxy);
  }

}
