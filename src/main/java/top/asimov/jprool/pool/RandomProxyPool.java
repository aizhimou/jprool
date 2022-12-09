package top.asimov.jprool.pool;

import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import com.github.monkeywie.proxyee.proxy.ProxyType;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.asimov.jprool.exception.ServiceException;
import top.asimov.jprool.proxy.AbstractProxy;

/**
 * 并发数组实现的随机代理池，顺序插入，随机取值
 */
@Slf4j
@Component
@Primary
public class RandomProxyPool implements ProxyPool{

  private static final Random random = new Random();
  private static final List<AbstractProxy> pool = new CopyOnWriteArrayList<>();

  @Override
  public void add(AbstractProxy proxy) {
    pool.add(proxy);
    log.info("success added proxy [{}] to proxy pool", proxy.getHost());
  }

  @Override
  public void clearInvalie() {
    pool.removeIf(AbstractProxy::invalid);
  }

  @Override
  public void clearAll() {
    pool.clear();
  }

  @Override
  public AbstractProxy get() {
    int size = pool.size();
    if (size == 0) {
      throw new ServiceException("暂无有效代理，请补充代理后再尝试。");
    }
    int index = random.nextInt(0, size);
    AbstractProxy proxy = pool.get(index);
    if (proxy.invalid()) {
      pool.remove(index);
      return get();
    }
    return proxy;
  }

  @Override
  public Integer size() {
    return pool.size();
  }

  @Override
  public Collection<AbstractProxy> list() {
    return pool.stream().toList();
  }

  public ProxyConfig getProxy() {
    AbstractProxy proxy = get();
    return new ProxyConfig(ProxyType.HTTP, proxy.getIp(), proxy.getPort());
  }
}
