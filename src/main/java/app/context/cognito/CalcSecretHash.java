package app.context.cognito;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import app.context.exception.EncodeExeption;

public class CalcSecretHash {

    /**
     * AWS Cognito用のSECRET_HASHを計算します
     *
     * @param userName     ユーザー名
     * @param clientId     クライアントID
     * @param clientSecret クライアントシークレット
     * @return Base64エンコードされたSECRET_HASH
     */
    public static String generateSecretHash(String userName, String clientId, String clientSecret) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                clientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(clientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new EncodeExeption(e);
        }
    }
}
