package top.asimov.jprool.provider;

import top.asimov.jprool.provider.ProxyProviderConfig.BusinessConfig;

@SuppressWarnings("unused")
public interface ProxyProvider {

  // void scheduledAddProxyToPool();

  void addroxyToPool(BusinessConfig config);
}
