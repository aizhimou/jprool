package top.asimov.jprool.provider.qg;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.asimov.jprool.provider.BaseProviderConfig;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "proxy.provider.qg")
public class QgProxyConfig extends BaseProviderConfig {

  private String businessKey;

  private String authKey;

  private String authPwd;

  private Integer num;

}
