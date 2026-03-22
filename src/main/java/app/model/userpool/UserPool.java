package app.model.userpool;

import java.util.List;
import java.util.Optional;

import app.context.DomainEntity;
import app.context.orm.OrmRepository;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    @Column(name = "company_id")
    private Long companyId;

    /** ユーザープールID */
    @Id
    @Column(name = "user_pool_id")
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class UserPoolId {

        /** 企業ID */
        @NotNull
        private Long companyId;

        /** ユーザープールID */
        @NotNull
        private String userPoolId;

        public String toString() {
            return companyId + "-" + userPoolId;
        }
    }

    public UserPoolId of() {
        return UserPoolId.of(this.companyId, this.userPoolId);
    }

    /**
     * 企業ID,ユーザープールIDに紐づくユーザープール情報を返却します。
     *
     * @param rep
     * @param companyId
     * @param userPoolId
     * @return
     */
    public static Optional<UserPool> get(OrmRepository rep, Long companyId, String userPoolId) {
        return rep.get(UserPool.class, UserPoolId.of(companyId, userPoolId));
    }

    /**
     * 企業IDに紐づく全てのUserPoolを取得します。
     *
     * @param rep
     * @param companyId
     * @return
     */
    public static List<UserPool> findAllByCompanyId(OrmRepository rep, Long companyId) {
        return rep.findBy(UserPool.class, "companyId", companyId);
    }

    /**
     * UserPoolIdに一致するUserPoolを1件返します。
     *
     * @param rep
     * @param userPoolId
     * @return
     */
    public static List<UserPool> findByUserPoolId(OrmRepository rep, String userPoolId) {
        return rep.findBy(UserPool.class, "userpoolId", userPoolId);
    }
}
