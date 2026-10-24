package Proconnect.backProconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.CompanyDto;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.CompanyService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/company")
public class CompanyController {
    
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddCompany(@RequestBody @Valid CompanyDto company){
        company.setUserId(CurrentUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.AddCompany(company));
    }

    @GetMapping
    public ResponseEntity<?> GetAllCompany(){
        return ResponseEntity.ok().body(companyService.GetAllCompany());
    }

    @PutMapping("/edit/{Id}")
    public ResponseEntity<?> EditCompanyById(@PathVariable Long Id, @RequestBody @Valid CompanyDto company){
        company.setUserId(CurrentUser.id());
        return ResponseEntity.ok().body(companyService.EditCompanyById(Id,company)); 
    }

    @DeleteMapping("/delete/{Id}")
    public ResponseEntity<?> DeleteCompanyById(@PathVariable Long Id){
        return ResponseEntity.ok().body(companyService.DeleteCompanyById(Id)); 
    }
}
