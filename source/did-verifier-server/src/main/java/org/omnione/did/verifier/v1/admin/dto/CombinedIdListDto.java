package org.omnione.did.verifier.v1.admin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class CombinedIdListDto {

    private List<String> data;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CombinedIdListDto [data=");
        if (data == null || data.isEmpty()) {
            sb.append("empty");
        } else {
            sb.append("[");
            for (int i = 0; i < Math.min(data.size(), 10); i++) {
                if (i > 0) sb.append(", ");
                sb.append(data.get(i));
            }
            if (data.size() > 10) {
                sb.append(", ... (").append(data.size() - 10).append(" more)");
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }
}