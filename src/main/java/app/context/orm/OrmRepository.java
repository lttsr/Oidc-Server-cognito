package app.context.orm;

import java.util.List;
import java.util.Optional;

import app.context.DomainEntity;
import app.context.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

/**
 * JPA ( Hibernate ) の Repository 基底実装。
 * <p>
 * 本コンポーネントは Repository と Entity の 1-n 関係を実現するために SpringData の基盤を
 * 利用しない形で単純な ORM 実装を提供します。
 * <p>
 * OrmRepository を継承して作成される Repository の粒度はデータソース単位となります。
 */
@RequiredArgsConstructor
public abstract class OrmRepository implements Repository {
    /**
     * 管理するEntityManagerを返します。
     * <p>
     * 継承先で管理したいデータソースのEntityManagerを返してください。
     */
    public abstract EntityManager em();

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> Optional<T> get(Class<T> clazz, Object id) {
        T m = em().find(clazz, id);
        if (m != null)
            m.hashCode(); // force loading
        return Optional.ofNullable(m);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T load(Class<T> clazz, Object id) {
        try {
            T m = em().getReference(clazz, id);
            m.hashCode(); // force loading
            return m;
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(clazz.getSimpleName() + "-" + id);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T loadForUpdate(Class<T> clazz, Object id) {
        T m = em().find(clazz, id, LockModeType.PESSIMISTIC_WRITE);
        if (m == null) {
            throw new EntityNotFoundException(clazz.getSimpleName() + "-" + id);
        }
        m.hashCode(); // force loading
        return m;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> boolean exists(Class<T> clazz, Object id) {
        return get(clazz, id).isPresent();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T save(T entity) {
        em().persist(entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T saveOrUpdate(T entity) {
        return em().merge(entity);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T update(T entity) {
        return em().merge(entity);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T delete(T entity) {
        em().remove(entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> List<T> findAll(Class<T> clazz) {
        return em().createQuery("SELECT e FROM " + clazz.getSimpleName() + " e", clazz).getResultList();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> List<T> findBy(Class<T> clazz, String fieldName, Object value) {
        String jpql = String.format("SELECT e FROM %s e WHERE e.%s = :value", clazz.getSimpleName(), fieldName);
        TypedQuery<T> query = em().createQuery(jpql, clazz);
        query.setParameter("value", value);
        return query.getResultList();
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティを全てDBと同期(SQL発行)します。
     * <p>
     * SQL発行タイミングを明確にしたい箇所で呼び出すようにしてください。バッチ処理などでセッションキャッシュが
     * メモリを逼迫するケースでは#flushAndClearを定期的に呼び出してセッションキャッシュの肥大化を防ぐようにしてください。
     */
    public OrmRepository flush() {
        em().flush();
        return this;
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティをDBと同期化した上でセッションキャッシュを初期化します。
     * <p>
     * 大量の更新が発生するバッチ処理などでは暗黙的に保持されるセッションキャッシュがメモリを逼迫して
     * 大きな問題を引き起こすケースが多々見られます。定期的に本処理を呼び出してセッションキャッシュの
     * サイズを定量に維持するようにしてください。
     */
    public OrmRepository flushAndClear() {
        em().flush();
        em().clear();
        return this;
    }

}
