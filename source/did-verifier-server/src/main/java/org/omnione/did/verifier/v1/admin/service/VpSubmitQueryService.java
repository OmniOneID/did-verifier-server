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

        String holderDID = vpSubmit.getHolderDid();
        
        // If holderDid is null, it means this is a failed/pending transaction
        if (holderDID == null) {
            holderDID = "N/A"; // or could be "Not Available" or "-"
        }

        org.omnione.did.base.db.domain.Transaction transaction = transactionRepository.findById(vpSubmit.getTransactionId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND));

        String transactionStatus = transaction.getStatus().toString();
        String txId = transaction.getTxId(); // 실제 트랜잭션 ID 가져오기

        return VpSubmitDTO.builder()
                .id(vpSubmit.getId())
                .vp(vpSubmit.getVp()) // This can be null for failed/pending transactions
                .holderDID(holderDID)
                .transactionId(vpSubmit.getTransactionId())
                .txId(txId) // 실제 트랜잭션 ID 추가
                .transactionStatus(transactionStatus)
                .createdAt(VpSubmitDTO.formatInstant(vpSubmit.getCreatedAt(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}
