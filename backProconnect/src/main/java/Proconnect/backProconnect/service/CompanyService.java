package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.model.CompanyDto;
import Proconnect.backProconnect.repository.CompanyRepository;

@Service
public class CompanyService {
    
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    public String AddCompany(CompanyDto company){
        try {
            if(company == null){
                return "Company cannot be null";
            }
            Company companyEntity = new Company(company.getId(), company.getName(), company.getDescription(), company.getLocation(), company.getUserId());
            companyRepository.save(companyEntity);
            return "Company add successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public List<Company> GetAllCompany(){
        return companyRepository.findAll();
    }

    public String EditCompanyById(Long Id,CompanyDto newCompany){
        try {
            if(Id == null || Id <= 0){
                return "Id cannot be null or zero";
            }
            Optional<Company> companyOpt = companyRepository.findById(Id);
            if(!companyOpt.isPresent()){
                return "Company does not exists";
            }
            Company company = companyOpt.get();
            company.setDescription(newCompany.getDescription());
            company.setLocation(newCompany.getLocation());
            company.setName(newCompany.getName());
            companyRepository.save(company);
            return "Company Edit Successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public String DeleteCompanyById(Long Id){
        try {
            if(Id == null || Id <= 0){
                return "Id cannot be null or zero";
            }
            companyRepository.deleteById(Id);
            return "Company Delete successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
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
