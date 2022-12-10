package top.asimov.jprool.tunnel;

import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InitTunnelProxy {

  @Value("${proxy.tunnel}")
  private Integer port;

  private final TunnelProxyInterceptInitializer proxyInterceptInitializer;

  public InitTunnelProxy(TunnelProxyInterceptInitializer proxyInterceptInitializer) {
    this.proxyInterceptInitializer = proxyInterceptInitializer;
  }

  @PostConstruct
  private void init() {
    new HttpProxyServer()
        .serverConfig(new HttpProxyServerConfig())
        .proxyInterceptInitializer(proxyInterceptInitializer.getProxyInterceptInitializer())
        .startAsync(port);
  }

}
