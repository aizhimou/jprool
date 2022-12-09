package top.asimov.jprool.proxy;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.asimov.jprool.proxy.enums.ProtocolEnum;

/**
 * 基本代理类
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractProxy {

  /**
   * 代理协议
   */
  private ProtocolEnum protocol;

  /**
   * 代理IP
   */
  private String ip;

  /**
   * 代理端口
   */
  private Integer port;

  /**
   * 代理 host
   */
  private String host;

  /**
   * 代理认证用户名
   */
  private String username;

  /**
   * 代理认证密码
   */
  private String password;

  /**
   * ISP
   */
  private String isp;

  /**
   * 代理位置
   */
  private String region;

  /**
   * 代理来源
   */
  private String source;

  public abstract Boolean invalid();

}
