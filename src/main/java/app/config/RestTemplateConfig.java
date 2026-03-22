package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate設定
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate createRequestFactory(AppProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getHttp().connectTimeout());
        factory.setReadTimeout(props.getHttp().readTimeout());
        return new RestTemplate(factory);
    }
}
