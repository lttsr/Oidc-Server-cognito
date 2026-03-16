package app.model.company;

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
@Table(name = "company_logo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLogo implements DomainEntity {

    /** 企業ID */
    @Id
    @Column(name = "company_id")
    private Long companyId;

    /** ロゴファイルパス（静的リソースへのパスを想定） */
    @NotNull
    @Column(name = "file_path")
    private String filePath;

    /** 企業IDからロゴ情報を取得します。 */
    public static CompanyLogo findByCompanyId(OrmRepository ormRepository, Long companyId) {
        return ormRepository.load(CompanyLogo.class, companyId);
    }
}

