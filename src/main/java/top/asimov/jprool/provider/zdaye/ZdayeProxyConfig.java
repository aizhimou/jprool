package top.asimov.jprool.provider.zdaye;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.asimov.jprool.provider.BaseProviderConfig;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "proxy.provider.zdaye")
public class ZdayeProxyConfig extends BaseProviderConfig {

  private String api;

  private String akey;

  private Integer count;

  private String adr;

  private Integer timespan;

}
