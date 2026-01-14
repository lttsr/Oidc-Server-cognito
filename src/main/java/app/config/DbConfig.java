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
 */
@Configuration
public class DbConfig {

    @Bean(name = DefaultRepository.BeanNameDs, destroyMethod = "close")
    @Primary
    DataSource dataSource(AppProperties props) {
        return props.getDb().dataSource();
    }

    @Bean(name = DefaultRepository.BeanNameEmf)
    @Primary
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            AppProperties props,
            @Qualifier(DefaultRepository.BeanNameDs) final DataSource dataSource) {
        return props.getDb().entityManagerFactoryBean(dataSource);
    }

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
