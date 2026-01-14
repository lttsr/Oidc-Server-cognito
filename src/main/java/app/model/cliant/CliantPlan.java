package app.model.cliant;

import app.context.DomainEntity;
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
public class CliantPlan implements DomainEntity {
    @Id
    @Column(name = "plan_id")
    private String planId;
    /* プラン名 */
    @NotNull
    private String planName;
    /* プラン価格 */
    @NotNull
    private int price;
    /* プラン説明 */
    private String planDescription;
}
