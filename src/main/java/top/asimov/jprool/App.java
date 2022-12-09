package top.asimov.jprool;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import top.asimov.jprool.pool.RandomProxyPool;

@SpringBootApplication
public class App implements ApplicationContextAware {

  private static ApplicationContext ac;

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);

    HttpProxyServerConfig config = new HttpProxyServerConfig();
    new HttpProxyServer()
        .serverConfig(config)
        .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
          @Override
          public void init(HttpProxyInterceptPipeline pipeline) {
            pipeline.addLast(new FullRequestIntercept() {
              @Override
              public boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
                return true;
              }

              @Override
              public void handleRequest(FullHttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
                RandomProxyPool proxyPool = ac.getBean(RandomProxyPool.class);
                ProxyConfig proxy = proxyPool.getProxy();
                pipeline.setProxyConfig(proxy);
              }
            });
          }
        })
        .startAsync(8887);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ac = applicationContext;
  }
}
