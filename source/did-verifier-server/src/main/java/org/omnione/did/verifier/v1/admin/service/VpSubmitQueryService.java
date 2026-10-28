package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.constant.TransactionType;
import org.omnione.did.base.db.domain.VpSubmit;
import org.omnione.did.base.db.repository.TransactionRepository;
import org.omnione.did.base.db.repository.VpSubmitRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.VpSubmitDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VpSubmitQueryService {
    private final VpSubmitRepository vpSubmitRepository;
    private final TransactionRepository transactionRepository;

    public Page<VpSubmitDTO> searchVpSubmitList(String searchKey, String searchValue, Pageable pageable) {
        Page<VpSubmit> vpSubmitPage = vpSubmitRepository.searchVpSubmitList(searchKey, searchValue, pageable);

        List<VpSubmitDTO> vpSubmitDtos = vpSubmitPage.getContent().stream()
                .map(this::convertVpSubmitDTO)
                .toList();

        return new PageImpl<>(vpSubmitDtos, pageable, vpSubmitPage.getTotalElements());
    }
    public VpSubmitDTO convertVpSubmitDTO(VpSubmit vpSubmit) {
        org.omnione.did.base.db.domain.Transaction transaction = transactionRepository.findById(vpSubmit.getTransactionId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND));

        return VpSubmitDTO.builder()
                .id(vpSubmit.getId())
                .vp(vpSubmit.getVp())
                .holderDID(vpSubmit.getHolderDid())
                .transactionId(vpSubmit.getTransactionId())
                .txId(transaction.getTxId())
                .transactionStatus(transaction.getStatus().toString())
                .errorCode(vpSubmit.getErrorCode())
                .format(vpSubmit.getFormat())
                .protocol(resolveProtocol(transaction.getType()))
                .createdAt(VpSubmitDTO.formatInstant(vpSubmit.getCreatedAt(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    /** Converts transaction.type into the protocol name shown to users. */
    private String resolveProtocol(TransactionType type) {
        return type == TransactionType.OID4VP ? "OID4VP" : "OpenDID";
    }
}
