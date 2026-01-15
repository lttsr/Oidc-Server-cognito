package app.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

import app.context.orm.OrmRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;

/**
 * アプリケーションのデータベース接続定義を表現します。
 * データベース接続はHikariCPを使用して管理されます。
 */
@Configuration
public class DbConfig {

    /**
     * データベース接続を生成します。
     *
     * @param props
     * @return DataSource
     */
    @Bean(name = DefaultRepository.BeanNameDs, destroyMethod = "close")
    @Primary
    DataSource dataSource(AppProperties props) {
        return props.getDb().dataSource();
    }

    /**
     * EntityManagerFactoryを生成します。
     *
     * @param props
     * @param dataSource
     * @return LocalContainerEntityManagerFactoryBean
     */
    @Bean(name = DefaultRepository.BeanNameEmf)
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            AppProperties props,
            @Qualifier(DefaultRepository.BeanNameDs) final DataSource dataSource) {
        return props.getDb().entityManagerFactoryBean(dataSource);
    }

    /**
     * TransactionManagerを生成します。
     *
     * @param props
     * @param emf
     * @return JpaTransactionManager
     */
    @Bean(name = DefaultRepository.BeanNameTx)
    @Primary
    JpaTransactionManager transactionManager(
            AppProperties props,
            @Qualifier(DefaultRepository.BeanNameEmf) final EntityManagerFactory emf) {
        return props.getDb().transactionManager(emf);
    }

    @Component
    public static class DefaultRepository extends OrmRepository {
        public static final String BeanNameDs = "dataSource";
        public static final String BeanNameEmf = "entityManagerFactory";
        public static final String BeanNameTx = "transactionManager";

        @PersistenceContext(unitName = BeanNameEmf)
        private EntityManager em;

        @Override
        public EntityManager em() {
            return em;
        }

        public DefaultRepository em(EntityManager em) {
            this.em = em;
            return this;
        }
    }
}
