package app.model.company;

import java.io.Serializable;

import app.context.DomainEntity;
import app.model.company.CompanyUserPoolBind.CompanyUserPoolBindId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company_user_pool_bind")
@IdClass(CompanyUserPoolBindId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUserPoolBind implements DomainEntity {
    @Id
    @NotNull
    @Column(name = "company_id")
    /** 企業ID */
    private Long companyId;
    @Id
    @NotNull
    /** ユーザープールID */
    private String userPoolId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyUserPoolBindId implements Serializable {
        @NotNull
        /** 企業ID */
        private Long companyId;
        /** ユーザープールID */
        @NotNull
        private String userPoolId;

        @Override
        public String toString() {
            return companyId + "-" + userPoolId;
        }

        public static CompanyUserPoolBindId of(Long companyId, String userPoolId) {
            return new CompanyUserPoolBindId(companyId, userPoolId);
        }
    }
}
