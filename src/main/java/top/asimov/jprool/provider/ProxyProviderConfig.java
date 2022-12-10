package top.asimov.jprool.provider;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProxyProviderConfig {

  private String provider;
  private Boolean enabled;
  private Integer deduplicatePoolSize;
  private List<BusinessConfig> businessConfigs;

  @Getter
  @Setter
  public static class BusinessConfig {
    private Boolean enabled;
    private String proxyType;
    private String username;
    private String password;
    private Map<String, Object> apiConfig;
    private RequestConfig requestConfig;
  }

  @Getter
  @Setter
  public static class RequestConfig {
    private int initialDelay;
    private int rate;
    private int timeout;
    private String statusCode;
    private String successCode;
  }

}
