package app.usecase.company;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.context.orm.OrmRepository;
import app.model.company.OAuthClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthClientService {
    private final OrmRepository rep;

    /** クライアントIDから企業IDを取得します。 */
    public Optional<Long> findCompanyIdByClientId(String clientId) {
        return OAuthClient.findByClientId(rep, clientId)
                .map(OAuthClient::getCompanyId);
    }
}
