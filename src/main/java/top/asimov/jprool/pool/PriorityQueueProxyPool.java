package top.asimov.jprool.pool;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.asimov.jprool.exception.ServiceException;
import top.asimov.jprool.proxy.AbstractProxy;

@Slf4j
@Component
public class PriorityQueueProxyPool implements ProxyPool{

  private static Queue<AbstractProxy> pool;

  @Value("${proxy.pool.init-capacity}")
  private void setPool(Integer initCapacity) {
     pool = new PriorityBlockingQueue<>(initCapacity);
  }

  @Override
  public void add(AbstractProxy proxy) {
    pool.add(proxy);
    log.info("success added proxy [{}] to proxy pool", proxy.getHost());
  }

  @Override
  public AbstractProxy get() throws RuntimeException {
    AbstractProxy proxy = pool.peek();
    if (Objects.isNull(proxy)) {
      throw new ServiceException("暂无有效代理，请补充代理后再尝试。");
    }
    if (proxy.invalid()) {
      pool.poll();
      return get();
    }
    return proxy;
  }

  @Override
  public Integer size() {
    return pool.size();
  }

  @Override
  public Queue<AbstractProxy> list() {
    return pool;
  }

  @Override
  public void clearInvalie() {
    pool.removeIf(AbstractProxy::invalid);
  }

  @Override
  public void clearAll() {
    pool.clear();
  }
}
