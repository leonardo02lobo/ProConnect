package Proconnect.backProconnect.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.CompanyDto;
import Proconnect.backProconnect.model.CompanyRequest;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.CompanyService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public List<CompanyDto> list() {
        return companyService.list();
    }

    @GetMapping("/{id}")
    public CompanyDto get(@PathVariable Long id) {
        return companyService.get(id);
    }

    @PostMapping
    public ResponseEntity<CompanyDto> create(@RequestBody @Valid CompanyRequest request) {
        CompanyDto created = companyService.create(CurrentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/companies/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public CompanyDto update(@PathVariable Long id, @RequestBody @Valid CompanyRequest request) {
        return companyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        companyService.delete(id);
    }
}
