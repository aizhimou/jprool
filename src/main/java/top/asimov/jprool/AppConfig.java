package top.asimov.jprool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.asimov.jprool.provider.ProxyProviderConfig;

@Component
public class AppConfig {

  @Value("classpath:proxy-provider.json")
  private Resource resource;

  @Bean
  WebMvcConfigurer createWebMvcConfigurer(@Autowired HandlerInterceptor[] interceptors) {
    return new WebMvcConfigurer() {
      public void addInterceptors(InterceptorRegistry registry) {
        Arrays.stream(interceptors).forEach(registry::addInterceptor);
      }
    };
  }

  @Bean
  List<ProxyProviderConfig> readProviderConfig() throws IOException {
    try (InputStream inputStream = resource.getInputStream()) {
      return new ObjectMapper().readValue(inputStream, new TypeReference<>() {
      });
    }
  }

}
