package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.VpSubmit;
import org.omnione.did.base.db.repository.TransactionRepository;
import org.omnione.did.base.db.repository.VpSubmitRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.data.model.vp.VerifiablePresentation;
import org.omnione.did.verifier.v1.admin.dto.VpSubmitDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class VpSubmitQueryService {
    private final VpSubmitRepository vpSubmitRepository;
    private final TransactionRepository transactionRepository;

    public Page<VpSubmitDTO> searchVpSubmitList(String searchKey, String searchValue, Pageable pageable) {
        Page<VpSubmit> VpSubmitPage = vpSubmitRepository.searchVpSubmitList(searchKey, searchValue, pageable);

        List<VpSubmitDTO> VpSubmitDtos = VpSubmitPage.getContent().stream()
                .map(this::convertVpSubmitDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(VpSubmitDtos, pageable, VpSubmitPage.getTotalElements());
    }
    public VpSubmitDTO convertVpSubmitDTO(VpSubmit vpSubmit) {

        VerifiablePresentation verifiablePresentation = new VerifiablePresentation();
        verifiablePresentation.fromJson(vpSubmit.getVp());
        String holderDID = verifiablePresentation.getHolder();

        String transactionStatus = transactionRepository.findById(vpSubmit.getTransactionId())
                .map(transaction -> transaction.getStatus().toString())
                .orElseThrow(() -> new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND));

        return VpSubmitDTO.builder()
                .id(vpSubmit.getId())
                .vp(vpSubmit.getVp())
                .holderDID(holderDID)
                .transactionId(vpSubmit.getTransactionId())
                .transactionStatus(transactionStatus)
                .createdAt(VpSubmitDTO.formatInstant(vpSubmit.getCreatedAt(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}
