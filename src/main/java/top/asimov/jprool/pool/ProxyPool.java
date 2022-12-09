package top.asimov.jprool.pool;

import java.util.Collection;
import top.asimov.jprool.proxy.AbstractProxy;

public interface ProxyPool {

  void add(AbstractProxy proxy);

  void clearInvalie();

  void clearAll();

  AbstractProxy get();

  Integer size();

  Collection<AbstractProxy> list();

}
