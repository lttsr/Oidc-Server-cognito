package app.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import app.config.DbConfig.DefaultRepository;
import app.context.orm.DataSourceProperties;
import app.context.orm.OrmRepositoryProperties;
import jakarta.persistence.EntityManagerFactory;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*
 * Springboot アプリケーション設定
 */
@ConfigurationProperties("auth-service")
@Builder
@Data
public class AppProperties {
    private HttpProps http;
    private RedisRateProps redis;
    private DbProps db;
    private MailProps mail;
    private AwsProps aws;

    /**
     * HTTP プロパティ
     */
    @Builder
    public record HttpProps(
            /* サーバーベースURL */
            String baseUrl) {
    }

    /**
     * AWS プロパティ
     */
    @Builder
    public record AwsProps() {
    }

    /**
     * Redis レート制限設定
     */
    @Builder
    public record RedisRateProps(
            /* アクセス期間 */
            int rateTime,
            /* 最大アクセス回数 */
            int rateLimit) {
    }

    /** 標準スキーマのDataSourceを生成します。 */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DbProps extends DataSourceProperties {
        private OrmRepositoryProperties jpa = new OrmRepositoryProperties();

        @Override
        public DataSource dataSource() {
            return super.dataSource();
        }

        public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
                final DataSource dataSource) {
            return jpa.entityManagerFactoryBean(
                    DefaultRepository.BeanNameEmf, dataSource);
        }

        public JpaTransactionManager transactionManager(final EntityManagerFactory emf) {
            return jpa.transactionManager(emf);
        }
    }

    /**
     * Mail プロパティ
     */
    @Builder
    public record MailProps(
            /* メール送信者ホスト */
            String host,
            /* メール送信者ポート */
            String port,
            /* メール送信者ユーザー名 */
            String username,
            /* メール送信者パスワード */
            String password) {
    }
}
