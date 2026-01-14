package app.controller.userpool.type;

/**
 * MFA設定種別を表現します。
 */
public enum MfaConfigType {
    /* MFA必須 */
    ON,
    /* MFA無効 */
    OFF,
    /* MFAオプション */
    OPTIONAL;
}
