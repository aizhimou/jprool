package top.asimov.jprool.tunnel;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import org.springframework.stereotype.Component;

@Component
public class TunnelProxyInterceptInitializer {

  private static HttpProxyInterceptInitializer proxyInterceptInitializer;

  public TunnelProxyInterceptInitializer(TunnelRequestIntercept requestIntercept) {
    proxyInterceptInitializer = new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addFirst(requestIntercept.getRequestIntercept());
      }
    };
  }

  public HttpProxyInterceptInitializer getProxyInterceptInitializer() {
    return proxyInterceptInitializer;
  }

}
