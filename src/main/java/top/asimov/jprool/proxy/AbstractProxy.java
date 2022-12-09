package top.asimov.jprool.proxy;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import top.asimov.jprool.proxy.enums.ProtocolEnum;

/**
 * 基本代理类
 */
@Data
@SuperBuilder
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

  /**
   * 代理验证
   */
  // private ProxyAuthentication authentication;

  public abstract Boolean invalid();

}
