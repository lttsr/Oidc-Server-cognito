package app.context.cognito;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

/**
 * CognitoIdentityProviderClient のファクトリークラス
 */
@Component
public class CognitoClientFactory {

    private final ConcurrentHashMap<String, CognitoIdentityProviderClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Region に対応するクライアントを取得
     */
    public CognitoIdentityProviderClient getClient(String region) {
        return clientCache.computeIfAbsent(region, this::createClient);
    }

    /**
     * 新しいクライアントを作成
     */
    private CognitoIdentityProviderClient createClient(String region) {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();
    }

    /**
     * キャッシュをクリア
     */
    public void clearCache() {
        clientCache.values().forEach(CognitoIdentityProviderClient::close);
        clientCache.clear();
    }
}
