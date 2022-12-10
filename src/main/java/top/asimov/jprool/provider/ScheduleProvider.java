package top.asimov.jprool.provider;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleProvider {

  private final QgProxyProvider qgProxyProvider;
  private final ZdayeProxyProvider zdayeProxyProvider;

  public ScheduleProvider(QgProxyProvider qgProxyProvider, ZdayeProxyProvider zdayeProxyProvider) {
    this.qgProxyProvider = qgProxyProvider;
    this.zdayeProxyProvider = zdayeProxyProvider;
  }

  @PostConstruct
  public void init() {
    qgProxyProvider.scheduledAddProxyToPool(qgProxyProvider.getProviderConfig());
    zdayeProxyProvider.scheduledAddProxyToPool(zdayeProxyProvider.getProviderConfig());
  }

}
