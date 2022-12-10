package top.asimov.jprool.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class ProxyProviderConfigLoader {

  @Value("classpath:proxy-provider.json")
  private Resource resource;

  @Bean
  List<ProxyProviderConfig> readProviderConfig() throws IOException {
    try (InputStream inputStream = resource.getInputStream()) {
      return new ObjectMapper().readValue(inputStream, new TypeReference<>() {});
    }
  }

}
