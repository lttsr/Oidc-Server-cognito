package app.usecase.cliant;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.context.orm.OrmRepository;
import app.model.cliant.CliantKey;
import lombok.RequiredArgsConstructor;

/**
 * 企業サービス
 */
@Service
@RequiredArgsConstructor
public class CliantKeyService {
    private final OrmRepository rep;

    // 企業IDからAPIキーを取得します。
    public Optional<CliantKey> getCliantKey(Long cliantId) {
        Optional<CliantKey> cliantKey = CliantKey.getCliantKey(rep, cliantId);
        if (!cliantKey.isPresent()) {
            throw new IllegalArgumentException("Invalid API key");
        }
        cliantKey.get().isValid();
        return cliantKey;
    }
}
