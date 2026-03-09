package app.usecase.userpool;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import app.context.orm.OrmRepository;
import app.model.userpool.UserPool;
import app.usecase.cognito.CognitoUserPoolService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolResponse;

@RequiredArgsConstructor
@Service
public class UserPoolService {

    private final CognitoUserPoolService cognitoUserPoolService;
    private final OrmRepository rep;

    // DescribeUserPool APIを使用して最新のユーザプール情報を取得します。
    public DescribeUserPoolResponse getUserPoolAttributes() {
        return cognitoUserPoolService.describeUserPool();
    }

    // 企業IDに紐づく全てのUserPoolを取得します。存在しない場合は空リストを返します。
    public List<UserPool> findAllByCliantId(Long cliantId) {
        return UserPool.findAllByCliantId(rep, cliantId);
    }

    // IssuerからUserPoolを取得します。
    // issuer例:
    // https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_xxxxxx
    public UserPool findByIssuer(String issuer) {
        String userPoolId = extractUserPoolIdFromIssuer(issuer);
        if (!StringUtils.hasText(userPoolId)) {
            return null;
        }
        return UserPool.findByUserPoolId(rep, userPoolId);
    }

    // Cognitoのissuer URLからuserPoolIdを取得します。
    private static String extractUserPoolIdFromIssuer(String issuer) {
        if (!StringUtils.hasText(issuer)) {
            return null;
        }
        int lastSlash = issuer.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == issuer.length() - 1) {
            return null;
        }
        return issuer.substring(lastSlash + 1);
    }

}
