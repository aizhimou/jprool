package top.asimov.jprool.provider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseProviderConfig {

  private Boolean enabled;

  private Integer deduplicatePoolSize;

  private String requestBaseUrl;

  private Integer requestRate;

  private Integer requestTimeout;

  private String requestSuccessCode;

  private Integer requestInitialDelay;

}
