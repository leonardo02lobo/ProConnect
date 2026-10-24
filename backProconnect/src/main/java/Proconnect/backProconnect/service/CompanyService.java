package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.model.CompanyDto;
import Proconnect.backProconnect.repository.CompanyRepository;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.security.OwnershipGuard;

@Service
public class CompanyService {
    
    private final CompanyRepository companyRepository;
    private final OwnershipGuard guard;

    public CompanyService(CompanyRepository companyRepository, OwnershipGuard guard){
        this.companyRepository = companyRepository;
        this.guard = guard;
    }

    /** Solo COMPANY_ADMIN o ADMIN crean empresas (403); el creador queda como propietario. */
    public String AddCompany(CompanyDto company){
        guard.requireAnyRole(Role.COMPANY_ADMIN, Role.ADMIN);
        try {
            if(company == null){
                return "Company cannot be null";
            }
            Company companyEntity = new Company(null, company.getName(), company.getDescription(), company.getLocation(), company.getUserId());
            companyRepository.save(companyEntity);
            return "Company add successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public List<Company> GetAllCompany(){
        return companyRepository.findAll();
    }

    /** Solo el propietario o un ADMIN (403); 404 si no existe. */
    public String EditCompanyById(Long Id,CompanyDto newCompany){
        Company company = findOr404(Id);
        guard.assertOwnerOrAdmin(company.getUserId());
        company.setDescription(newCompany.getDescription());
        company.setLocation(newCompany.getLocation());
        company.setName(newCompany.getName());
        companyRepository.save(company);
        return "Company Edit Successufly";
    }

    /** Solo el propietario o un ADMIN (403); 404 si no existe. */
    public String DeleteCompanyById(Long Id){
        Company company = findOr404(Id);
        guard.assertOwnerOrAdmin(company.getUserId());
        companyRepository.delete(company);
        return "Company Delete successufly";
    }

    /** Empresa existente o 404. */
    public Company findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new ResourceNotFoundException();
        }
        return companyRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    /** 403 si el actor no es el propietario de la empresa ni ADMIN. */
    public void assertCanManage(Long companyId) {
        guard.assertOwnerOrAdmin(findOr404(companyId).getUserId());
    }

    public Company FindCompanyById(Long Id){
        try {
            if(Id == null || Id <= 0){
                return null;
            }
            Optional<Company> companyOpt = companyRepository.findById(Id);
            return companyOpt.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
