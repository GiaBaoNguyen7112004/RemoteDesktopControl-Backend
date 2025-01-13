package com.baotruongtuan.RdpServer.service;

import java.util.List;

import com.baotruongtuan.RdpServer.repository.AccessRestrictionsRepository;
import com.baotruongtuan.RdpServer.service.imp.AccessRestrictionService;
import com.baotruongtuan.RdpServer.utils.DomainExtractHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.baotruongtuan.RdpServer.dto.AccessRestrictionDTO;
import com.baotruongtuan.RdpServer.entity.AccessRestriction;
import com.baotruongtuan.RdpServer.exception.AppException;
import com.baotruongtuan.RdpServer.exception.ErrorCode;
import com.baotruongtuan.RdpServer.mapper.AccessRestrictionMapper;
import com.baotruongtuan.RdpServer.payload.request.AccessRestrictionCreationRequest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AccessRestrictionServiceImp implements AccessRestrictionService {
    AccessRestrictionMapper accessRestrictionMapper;
    AccessRestrictionsRepository accessRestrictionsRepository;
    DomainExtractHelper domainExtractHelper;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<AccessRestrictionDTO> getAllAccessRestrictions() {
        return accessRestrictionsRepository.findAll().stream()
                .map(accessRestrictionMapper::toAccessRestrictionDTO)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public AccessRestrictionDTO createAccessRestriction(
            AccessRestrictionCreationRequest accessRestrictionCreationRequest) {
        String content = accessRestrictionCreationRequest.getContent();
        String processedContent = (domainExtractHelper.isValidUrl(content))
                ? domainExtractHelper.extractDomain(content).replace(" ", "").toLowerCase()
                : content.replace(" ", "").toLowerCase();

        if (accessRestrictionsRepository.existsAccessRestrictionByContent(processedContent)) {
            throw new AppException(ErrorCode.DUPLICATE_DATA);
        }
        AccessRestriction accessRestriction =
                AccessRestriction.builder().content(processedContent).build();
        return accessRestrictionMapper
                .toAccessRestrictionDTO(accessRestrictionsRepository.save(accessRestriction));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void deleteAccessRestriction(String id) {
        accessRestrictionsRepository.deleteById(id);
    }

    @Override
    public boolean isExistingAccessRestriction(String content) {
        return accessRestrictionsRepository.existsAccessRestrictionByContent(domainExtractHelper
                .processedContent(content));
    }

}
