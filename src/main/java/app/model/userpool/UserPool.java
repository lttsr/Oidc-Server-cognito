package app.model.userpool;

import java.util.List;

import app.context.DomainEntity;
import app.context.orm.OrmRepository;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPool implements DomainEntity {

    /** 企業ID */
    @NotNull
    private Long cliantId;

    /** ユーザープールID */
    @NotNull
    private String userPoolId;

    /** ユーザープールエイリアス */
    @NotNull
    private String userPoolAlias;

    /** リージョン */
    @NotNull
    private String region;

    /** クライアントID */
    @NotNull
    private String clientId;

    /** クライアントシークレット */
    private String clientSecret;

    /**
     * 企業IDに紐づく全てのUserPoolを取得します。
     *
     * @param rep
     * @param cliantId
     * @return
     */
    public static List<UserPool> findAllByCliantId(OrmRepository rep, Long cliantId) {
        return rep.findBy(UserPool.class, "cliantId", cliantId);
    }

    /**
     * UserPoolIdに一致するUserPoolを1件返します。
     *
     * @param rep
     * @param userPoolId
     * @return
     */
    public static UserPool findByUserPoolId(OrmRepository rep, String userPoolId) {
        return rep.get(UserPool.class, userPoolId).orElse(null);
    }
}
