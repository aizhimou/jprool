package top.asimov.jprool.api;

import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import top.asimov.jprool.pool.ProxyPool;
import top.asimov.jprool.proxy.AbstractProxy;

@RestController
public class ProxyController {

  private final ProxyPool proxyPool;

  public ProxyController(ProxyPool proxyPool) {
    this.proxyPool = proxyPool;
  }

  @GetMapping("/get")
  public ResponseEntity<AbstractProxy> get() {
    return ResponseEntity.ok(proxyPool.get());
  }

  @GetMapping("/list")
  public ResponseEntity<Collection<AbstractProxy>> list() {
    return ResponseEntity.ok(proxyPool.list());
  }

  @PostMapping("/clear-invalie")
  public ResponseEntity<Collection<AbstractProxy>> clearInvalie() {
    proxyPool.clearInvalie();
    return ResponseEntity.ok(proxyPool.list());
  }

  @PostMapping("/clear-all")
  public ResponseEntity<Collection<AbstractProxy>> clearAll() {
    proxyPool.clearAll();
    return ResponseEntity.ok(proxyPool.list());
  }

}
