package app.usecase.cliant;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.context.orm.OrmRepository;
import app.model.cliant.Cliant;
import lombok.RequiredArgsConstructor;

/**
 * 企業サービス
 */
@Service
@RequiredArgsConstructor
public class CliantService {
    private final OrmRepository rep;

    // 企業IDから企業を取得します。
    public Optional<Cliant> findCliantById(Long id) {
        return Cliant.get(rep, id);
    }
}
