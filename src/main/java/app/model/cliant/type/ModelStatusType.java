package app.model.cliant.type;

/**
 * 業務モデルにおける汎用状態を表現します。
 */
public enum ModelStatusType {
    /** 0:仮登録 */
    TEMPORARY,
    /** 1:有効 */
    VALID,
    /** 2:無効 */
    INVALID;

    public boolean isValid() {
        return this == VALID;
    }

    public boolean isInvalid() {
        return this == INVALID;
    }

    public boolean canValid() {
        return this == TEMPORARY || this == INVALID;
    }

    public boolean canInvalid() {
        return isValid();
    }

    public boolean canDelete() {
        return this == TEMPORARY;
    }
}
