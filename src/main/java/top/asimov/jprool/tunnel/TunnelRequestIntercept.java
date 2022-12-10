package top.asimov.jprool.tunnel;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import org.springframework.stereotype.Component;
import top.asimov.jprool.pool.ProxyPool;
import top.asimov.jprool.proxy.AbstractProxy;

@Component
public class TunnelRequestIntercept {

  private final ProxyPool proxyPool;
  private static FullRequestIntercept requestIntercept;

  public TunnelRequestIntercept(ProxyPool proxyPool) {
    this.proxyPool = proxyPool;
    requestIntercept = new FullRequestIntercept() {
      @Override
      public boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
        return true;
      }

      @Override
      public void handleRequest(FullHttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
        pipeline.setProxyConfig(getProxy());
      }
    };
  }

  public FullRequestIntercept getRequestIntercept() {
    return requestIntercept;
  }

  private ProxyConfig getProxy() {
    AbstractProxy abstractProxy = proxyPool.get();
    return new ProxyConfig(abstractProxy.getProxyType(),
        abstractProxy.getIp(),abstractProxy.getPort(),
        abstractProxy.getUsername(), abstractProxy.getPassword());
  }
}
