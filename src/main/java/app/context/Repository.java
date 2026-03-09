package app.context;

import java.util.List;
import java.util.Optional;

/**
 * 特定のドメインオブジェクトに依存しない汎用的なRepositoryです。
 * <p>
 * タイプセーフでないRepositoryとして利用することができます。
 */
public interface Repository {

    /**
     * プライマリキーに一致する{@link DomainEntity}を返します。
     *
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id    プライマリキー
     * @return プライマリキーに一致した{@link DomainEntity}。
     */
    <T extends DomainEntity> Optional<T> get(final Class<T> clazz, final Object id);

    /**
     * プライマリキーに一致する{@link DomainEntity}を返します。
     *
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id    プライマリキー
     * @return プライマリキーに一致した{@link DomainEntity}。一致しない時は例外。
     */
    <T extends DomainEntity> T load(final Class<T> clazz, final Object id);

    /**
     * プライマリキーに一致する{@link DomainEntity}を返します。
     * <p>
     * ロック付(for update)で取得を行うため、デッドロック回避を意識するようにしてください。
     *
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id    プライマリキー
     * @return プライマリキーに一致した{@link DomainEntity}。一致しない時は例外。
     */
    <T extends DomainEntity> T loadForUpdate(final Class<T> clazz, final Object id);

    /**
     * プライマリキーに一致する{@link DomainEntity}が存在するか返します。
     *
     * @param <T>   確認型
     * @param clazz 対象クラス
     * @param id    プライマリキー
     * @return 存在する時はtrue
     */
    <T extends DomainEntity> boolean exists(final Class<T> clazz, final Object id);

    /**
     * {@link DomainEntity}を新規追加します。
     *
     * @param entity 追加対象{@link DomainEntity}
     * @return 追加した{@link DomainEntity}のプライマリキー
     */
    <T extends DomainEntity> T save(final T entity);

    /**
     * {@link DomainEntity}を新規追加または更新します。
     * <p>
     * 既に同一のプライマリキーが存在するときは更新。
     * 存在しない時は新規追加となります。
     *
     * @param entity 追加対象{@link DomainEntity}
     */
    <T extends DomainEntity> T saveOrUpdate(final T entity);

    /**
     * {@link DomainEntity}を更新します。
     *
     * @param entity 更新対象{@link DomainEntity}
     */
    <T extends DomainEntity> T update(final T entity);

    /**
     * {@link DomainEntity}を削除します。
     *
     * @param entity 削除対象{@link DomainEntity}
     */
    <T extends DomainEntity> T delete(final T entity);

    /**
     * 指定されたクラスの全ての{@link DomainEntity}を返します。
     * <p>
     *
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @return 指定されたクラスの全ての{@link DomainEntity}
     */
    <T extends DomainEntity> List<T> findAll(Class<T> clazz);

    /**
     * 指定されたフィールド名と値に一致する全ての{@link DomainEntity}を返します。
     * <p>
     * 存在しない場合は空リストを返します。
     *
     * @param <T>       戻り値の型
     * @param clazz     取得するインスタンスのクラス
     * @param fieldName 検索対象のフィールド名
     * @param value     検索値
     * @return 指定されたフィールド名と値に一致した{@link DomainEntity}のリスト
     */
    <T extends DomainEntity> List<T> findBy(Class<T> clazz, String fieldName, Object value);

}
