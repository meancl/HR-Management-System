package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.VacationReqDto;
import Hr.Mgr.domain.dto.VacationResDto;

import java.util.List;

public interface VacationService {
    VacationResDto createVacation(VacationReqDto dto);
    VacationResDto getLatestVacationByEmployee(Long employeeId);
    VacationResDto getVacationById(Long vacationId);
    List<VacationResDto> getVacationsByEmployee(Long employeeId);
    VacationResDto updateVacation(Long vacationId, VacationReqDto dto);
    void deleteVacation(Long vacationId);
    VacationResDto approveVacation(Long vacationId);
}
