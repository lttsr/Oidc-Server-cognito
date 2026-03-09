package app.model.cliant;

import java.time.LocalDateTime;
import java.util.Optional;

import app.context.DomainEntity;
import app.context.orm.OrmRepository;
import app.model.cliant.type.ModelStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
public class Cliant implements DomainEntity {

    /** 企業ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cliant_id")
    private Long cliantId;
    /* 企業名 */
    @NotNull
    private String name;
    /* 契約プラン */
    @NotNull
    private String planId;
    /* ステータス */
    @NotNull
    private ModelStatusType status;
    /* 電話番号 */
    private String phoneNumber;
    /* 企業概要 */
    private String description;
    /* 登録日時 */
    @NotNull
    private LocalDateTime registeredDate;
    /* 登録者 */
    @NotNull
    private String registerId;
    /* 更新日時 */
    @NotNull
    private LocalDateTime updatedDate;
    /* 更新者 */
    @NotNull
    private String updateId;

    /** 企業情報を取得します。 */
    public static Optional<Cliant> get(OrmRepository ormRepository, Long id) {
        return ormRepository.get(Cliant.class, id);
    }

    /** 企業情報を取得します。存在しない場合、例外をスローします。 */
    public static Cliant find(OrmRepository ormRepository, Long id) {
        return ormRepository.load(Cliant.class, id);
    }

}
