package app.usecase.company;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.context.orm.OrmRepository;
import app.model.company.Company;
import lombok.RequiredArgsConstructor;

/**
 * 企業サービス
 */
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final OrmRepository rep;

    /** 企業IDから企業を取得します。 */
    public Optional<Company> findCompanyById(Long id) {
        return Company.get(rep, id);
    }
}
