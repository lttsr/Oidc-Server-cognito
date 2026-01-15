package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import app.config.AppProperties.RedisRateProps;
import lombok.Builder;

/*
 * アクセスレート制限設定
 */
@Configuration
public class RedisConfig {

    /**
     * Redis テンプレート
     *
     * @param connectionFactory
     * @param props
     * @return RedisWrapper
     */
    @Bean
    public RedisWrapper redisWrapper(RedisConnectionFactory connectionFactory,
            AppProperties props) {
        RedisRateProps rateProps = props.getRedis();
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        return RedisWrapper.builder()
                .template(template)
                .rateLimit(rateProps.rateLimit())
                .rateTime(rateProps.rateTime())
                .build();
    }

    @Builder
    public record RedisWrapper(
            /* Redis テンプレート */
            StringRedisTemplate template,
            /* 最大アクセス回数 */
            int rateLimit,
            /* アクセス期間 */
            int rateTime) {
    }

}
