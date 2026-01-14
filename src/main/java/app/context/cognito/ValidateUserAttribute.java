package app.context.cognito;

import java.util.HashMap;
import java.util.Map;

import app.context.exception.InvalidAttributeException;
import app.context.messages.MessageUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeDataType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SchemaAttributeType;

public class ValidateUserAttribute {

    /**
     * ユーザー属性をバリデーション
     */
    public static void validate(DescribeUserPoolResponse schema, Map<String, String> userAttributes) {
        Map<String, String> errors = new HashMap<>();

        // スキーマ属性をMapに変換（高速検索用）
        Map<String, SchemaAttributeType> schemaMap = new HashMap<>();
        schema.userPool().schemaAttributes().forEach(attr -> {
            schemaMap.put(attr.name(), attr);
        });

        userAttributes.forEach((key, value) -> {
            // Cognito スキーマに存在するかチェック
            SchemaAttributeType schemaAttributeType = schemaMap.get(key);
            if (schemaAttributeType == null) {
                errors.put(key, MessageUtils.getMessage("cognito.error.invalid.attribute"));
                return;
            }

            // 変更可能かチェック
            if (schemaAttributeType.mutable() != null && !schemaAttributeType.mutable()) {
                errors.put(key, MessageUtils.getMessage("cognito.error.mutable.attribute"));
                return;
            }

            // 必須チェック
            if (schemaAttributeType.required() != null && schemaAttributeType.required()
                    && (value == null || value.isBlank())) {
                errors.put(key, MessageUtils.getMessage("validation.userAttribute.required",
                        new Object[] { key }));
                return;
            }

            // 値がある場合のみ形式・制約チェック
            if (value != null && !value.isBlank()) {
                // データ型に基づく動的バリデーション
                String error = validateByDataType(key, value, schemaAttributeType);
                if (error != null) {
                    errors.put(key, error);
                    return;
                }

                // 特定属性の追加フォーマットチェック
                error = validateSpecialFormat(key, value);
                if (error != null) {
                    errors.put(key, error);
                }
            }
        });

        if (!errors.isEmpty()) {
            throw new InvalidAttributeException(errors);
        }
    }

    /**
     * スキーマのデータ型と制約に基づく動的バリデーション
     */
    private static String validateByDataType(String key, String value,
            SchemaAttributeType schema) {
        AttributeDataType dataType = schema.attributeDataType();

        if (dataType == null) {
            return null;
        }

        switch (dataType) {
            case STRING:
                return validateStringConstraints(key, value, schema);
            case NUMBER:
                return validateNumberConstraints(key, value, schema);
            case DATE_TIME:
                return validateDateTime(key, value);
            case BOOLEAN:
                return validateBoolean(key, value);
            default:
                return null;
        }
    }

    /**
     * String型の制約チェック（MaxLength, MinLength）
     */
    private static String validateStringConstraints(String key, String value,
            SchemaAttributeType schema) {
        if (schema.stringAttributeConstraints() == null) {
            return null;
        }

        var constraints = schema.stringAttributeConstraints();

        // MinLength チェック
        if (constraints.minLength() != null) {
            try {
                int minLength = Integer.parseInt(constraints.minLength());
                if (value.length() < minLength) {
                    return MessageUtils.getMessage(
                            "validation.userAttribute.string.minLength",
                            new Object[] { key, minLength });
                }
            } catch (NumberFormatException e) {
                // スキーマの設定が不正な場合はスキップ
            }
        }

        // MaxLength チェック
        if (constraints.maxLength() != null) {
            try {
                int maxLength = Integer.parseInt(constraints.maxLength());
                if (value.length() > maxLength) {
                    return MessageUtils.getMessage(
                            "validation.userAttribute.string.maxLength",
                            new Object[] { key, maxLength });
                }
            } catch (NumberFormatException e) {
                // スキーマの設定が不正な場合はスキップ
            }
        }

        return null;
    }

    /**
     * Number型の制約チェック（MaxValue, MinValue）
     */
    private static String validateNumberConstraints(String key, String value,
            SchemaAttributeType schema) {
        // 数値として解析可能かチェック
        double numValue;
        try {
            numValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return MessageUtils.getMessage(
                    "validation.userAttribute.number.invalid",
                    new Object[] { key });
        }

        if (schema.numberAttributeConstraints() == null) {
            return null;
        }

        var constraints = schema.numberAttributeConstraints();

        // MinValue チェック
        if (constraints.minValue() != null) {
            try {
                double minValue = Double.parseDouble(constraints.minValue());
                if (numValue < minValue) {
                    return MessageUtils.getMessage(
                            "validation.userAttribute.number.minValue",
                            new Object[] { key, minValue });
                }
            } catch (NumberFormatException e) {
                // スキーマの設定が不正な場合はスキップ
            }
        }

        // MaxValue チェック
        if (constraints.maxValue() != null) {
            try {
                double maxValue = Double.parseDouble(constraints.maxValue());
                if (numValue > maxValue) {
                    return MessageUtils.getMessage(
                            "validation.userAttribute.number.maxValue",
                            new Object[] { key, maxValue });
                }
            } catch (NumberFormatException e) {
                // スキーマの設定が不正な場合はスキップ
            }
        }

        return null;
    }

    /**
     * DateTime型のバリデーション
     */
    private static String validateDateTime(String key, String value) {
        // ISO 8601 形式をチェック（簡易版）
        if (!value.matches("^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z?)?$")) {
            return MessageUtils.getMessage(
                    "validation.userAttribute.datetime.invalid",
                    new Object[] { key });
        }
        return null;
    }

    /**
     * Boolean型のバリデーション
     */
    private static String validateBoolean(String key, String value) {
        if (!value.matches("^(true|false)$")) {
            return MessageUtils.getMessage(
                    "validation.userAttribute.boolean.invalid",
                    new Object[] { key });
        }
        return null;
    }

    /**
     * 特定属性の追加フォーマットチェック（email, phone_number など）
     */
    private static String validateSpecialFormat(String key, String value) {
        return switch (key) {
            case "email" -> validateEmailFormat(value);
            case "phone_number" -> validatePhoneNumberFormat(value);
            case "birthdate" -> validateBirthdateFormat(value);
            case "gender" -> validateGenderFormat(value);
            default -> null;
        };
    }

    private static String validateEmailFormat(String value) {
        if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return MessageUtils.getMessage("validation.userAttribute.email.invalid");
        }
        return null;
    }

    private static String validatePhoneNumberFormat(String value) {
        if (!value.matches("^\\+[1-9]\\d{1,14}$")) {
            return MessageUtils.getMessage("validation.userAttribute.phoneNumber.invalid");
        }
        return null;
    }

    private static String validateBirthdateFormat(String value) {
        if (!value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return MessageUtils.getMessage("validation.userAttribute.birthdate.invalid");
        }
        return null;
    }

    private static String validateGenderFormat(String value) {
        if (!value.matches("^(male|female|other)$")) {
            return MessageUtils.getMessage("validation.userAttribute.gender.invalid");
        }
        return null;
    }
}
