package app.context.orm;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** JPA コンポーネントを生成するための設定情報を表現します。 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrmRepositoryProperties extends JpaProperties {
    /** スキーマ紐付け対象とするパッケージ。(annotatedClassesとどちらかを設定) */
    private String[] packageToScan;
    /** Entityとして登録するクラス。(packageToScanとどちらかを設定) */
    private Class<?>[] annotatedClasses;
    private HibernateProperties hibernate = new HibernateProperties();

    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(String name,
            final DataSource dataSource) {
        EntityManagerFactoryBuilder emfBuilder = new EntityManagerFactoryBuilder(
                vendorAdapter(), getProperties(), null);
        var builder = emfBuilder
                .dataSource(dataSource)
                .persistenceUnit(name)
                .properties(hibernate.determineHibernateProperties(getProperties(), new HibernateSettings()))
                .jta(false);
        if (ArrayUtils.isNotEmpty(annotatedClasses)) {
            builder.packages(annotatedClasses);
        } else {
            builder.packages(packageToScan);
        }
        return builder.build();
    }

    private JpaVendorAdapter vendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(isShowSql());
        if (getDatabase() != null) {
            adapter.setDatabase(getDatabase());
        }
        adapter.setDatabasePlatform(getDatabasePlatform());
        adapter.setGenerateDdl(isGenerateDdl());
        return adapter;
    }

    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    public void setPackageToScan(String... packageToScan) {
        this.packageToScan = packageToScan;
    }

    public void setAnnotatedClasses(Class<?>... annotatedClasses) {
        this.annotatedClasses = annotatedClasses;
    }

}
