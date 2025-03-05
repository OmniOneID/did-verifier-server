package org.omnione.did.verifier.v1.admin.service;

import org.omnione.did.verifier.v1.admin.dto.ProcessDTO;

import java.util.List;

/**
 * The ProcessService interface provides methods for querying the database for processes.
 * It is designed to facilitate the retrieval of processes from the database, ensuring that the data is accurate and up-to-date.
 */
public interface ProcessService {
    List<ProcessDTO> getProcessList(String name);

    ProcessDTO getProcessInfo(long processId);

    void saveProcess(ProcessDTO processDTO);

    ProcessDTO updateProcess(ProcessDTO processDTO);

    void deleteProcess(long processId);
}
