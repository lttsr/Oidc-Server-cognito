package app.model.company;

import java.time.LocalDateTime;
import java.util.Optional;

import app.context.DomainEntity;
import app.context.orm.OrmRepository;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_client")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClient implements DomainEntity {

    /** クライアントID */
    @NotNull
    @Column(unique = true)
    @Id
    private String clientId;

    /** クライアントシークレット */
    @NotNull
    private String clientSecret;

    /** リダイレクトURI */
    @NotNull
    @Column(length = 1000)
    private String redirectUris;

    /** スコープ */
    @NotNull
    private String scopes;

    /* 登録日時 */
    @NotNull
    private LocalDateTime registeredDate;

    /** 企業ID */
    @NotNull
    @Column(name = "company_id")
    private Long companyId;

    /**
     * クライアントIDから設定情報を取得します。
     *
     * @param rep
     * @param clientId
     * @return
     */
    public static Optional<OAuthClient> findByClientId(OrmRepository rep, String clientId) {
        return rep.get(OAuthClient.class, clientId);
    }

}
