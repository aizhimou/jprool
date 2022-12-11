package top.asimov.jprool.proxy;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 有时效性的代理
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TimelinessProxy extends AbstractProxy implements Comparable<TimelinessProxy> {

  /**
   * 代理过期时间
   */
  private LocalDateTime expirationTimestamp;

  @Override
  public Boolean invalid() {
    return expirationTimestamp.isBefore(LocalDateTime.now());
  }

  @Override
  public int compareTo(TimelinessProxy proxy) {
    return LocalDateTime.now().compareTo(proxy.getExpirationTimestamp());
  }

}
