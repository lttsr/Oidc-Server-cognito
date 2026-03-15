package app.model.cliant;

import java.io.Serializable;

import app.context.DomainEntity;
import app.model.cliant.CliantUserPoolBind.CliantUserPoolBindId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@IdClass(CliantUserPoolBindId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CliantUserPoolBind implements DomainEntity {
    @Id
    @NotNull
    /** 企業ID */
    private Long cliantId;
    @Id
    @NotNull
    /** ユーザープールID */
    private String userPoolId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CliantUserPoolBindId implements Serializable {
        @NotNull
        /** 企業ID */
        private Long cliantId;
        /** ユーザープールID */
        @NotNull
        private String userPoolId;

        @Override
        public String toString() {
            return cliantId + "-" + userPoolId;
        }

        public static CliantUserPoolBindId of(Long cliantId, String userPoolId) {
            return new CliantUserPoolBindId(cliantId, userPoolId);
        }
    }
}
