package app.model.cliant;

import java.time.LocalDateTime;
import java.util.Optional;

import app.context.DomainEntity;
import app.context.orm.OrmRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cliant_key")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CliantKey implements DomainEntity {

    @Id
    @NotNull
    private Long cliantId;

    /** 秘密鍵のハッシュ値 */
    @NotNull
    private String secretKeyHash;

    /** 有効期間開始日時 */
    @NotNull
    private LocalDateTime expiresStartDate;

    /** 有効期間終了日時 */
    @NotNull
    private LocalDateTime expiresEndDate;

    /** APIキーを取得します。 */
    public static Optional<CliantKey> getCliantKey(OrmRepository rep, Long cliantId) {
        return rep.get(CliantKey.class, cliantId);
    }

    /** APIキー認証の有効性を確認するビジネスロジック */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return now.isEqual(expiresStartDate) || now.isAfter(expiresStartDate)
                && now.isBefore(expiresEndDate);
    }
}
