package app.model.master;

import app.context.DomainEntity;
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
@Table(name = "plan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan implements DomainEntity {
    @Id
    @Column(name = "plan_id")
    private Long planId;
    /* プラン名 */
    @NotNull
    private String planName;
    /* プラン価格 */
    @NotNull
    private int price;
    /* プラン説明 */
    private String planDescription;
}
