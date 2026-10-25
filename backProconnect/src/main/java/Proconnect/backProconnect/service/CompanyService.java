package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.CompanyDto;
import Proconnect.backProconnect.model.CompanyRequest;
import Proconnect.backProconnect.model.PageResponse;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.repository.CompanyRepository;
import Proconnect.backProconnect.security.OwnershipGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final OwnershipGuard guard;

    public CompanyService(CompanyRepository companyRepository, OwnershipGuard guard) {
        this.companyRepository = companyRepository;
        this.guard = guard;
    }

    /**
     * Solo COMPANY_ADMIN o ADMIN crean empresas (403); el creador (sesión) queda como propietario.
     */
    @Transactional
    public CompanyDto create(Long ownerId, CompanyRequest request) {
        guard.requireAnyRole(Role.COMPANY_ADMIN, Role.ADMIN);
        Company company =
                new Company(
                        null,
                        request.name().trim(),
                        request.description().trim(),
                        request.location().trim(),
                        ownerId);
        return toDto(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanyDto> list(Integer page, Integer size) {
        return PageResponse.of(
                companyRepository.findAllBy(Paging.page(page, size)), CompanyService::toDto);
    }

    @Transactional(readOnly = true)
    public CompanyDto get(Long id) {
        return toDto(findOr404(id));
    }

    /** Solo el propietario o un ADMIN (403); 404 si no existe. El propietario no cambia. */
    @Transactional
    public CompanyDto update(Long id, CompanyRequest request) {
        Company company = findOr404(id);
        guard.assertOwnerOrAdmin(company.getUserId());
        company.setName(request.name().trim());
        company.setDescription(request.description().trim());
        company.setLocation(request.location().trim());
        return toDto(companyRepository.save(company));
    }

    /** Solo el propietario o un ADMIN (403); 404 si no existe. */
    @Transactional
    public void delete(Long id) {
        Company company = findOr404(id);
        guard.assertOwnerOrAdmin(company.getUserId());
        companyRepository.delete(company);
    }

    /** Empresa existente o 404. */
    public Company findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException("Empresa no encontrada");
        }
        return companyRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada"));
    }

    /** 403 si el actor no es el propietario de la empresa ni ADMIN (404 si no existe). */
    public void assertCanManage(Long companyId) {
        guard.assertOwnerOrAdmin(findOr404(companyId).getUserId());
    }

    static CompanyDto toDto(Company c) {
        return new CompanyDto(
                c.getId(), c.getName(), c.getDescription(), c.getLocation(), c.getUserId());
    }
}
