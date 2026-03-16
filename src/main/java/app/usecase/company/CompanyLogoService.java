package app.usecase.company;

import java.util.Optional;

import org.springframework.stereotype.Service;

import app.context.orm.OrmRepository;
import app.model.company.CompanyLogo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyLogoService {

    private final OrmRepository rep;

    public Optional<String> findLogoPathByCompanyId(Long companyId) {
        return rep.get(CompanyLogo.class, companyId)
                .map(CompanyLogo::getFilePath);
    }
}

