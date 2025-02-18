package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.VacationReqDto;
import Hr.Mgr.domain.dto.VacationResDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.Vacation;
import Hr.Mgr.domain.enums.VacationType;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.repository.VacationRepository;
import Hr.Mgr.domain.service.VacationService;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VacationServiceImpl implements VacationService {

    private final VacationRepository vacationRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public VacationResDto createVacation(VacationReqDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Vacation vacation = new Vacation(employee, dto.getType(), dto.getStartDate(), dto.getEndDate());
        return new VacationResDto(vacationRepository.save(vacation));
    }

    @Override
    @Transactional(readOnly = true)
    public VacationResDto getLatestVacationByEmployee(Long employeeId) {
        Optional<Vacation> latestVacation = vacationRepository.findTopByEmployeeIdOrderByStartDateDesc(employeeId);
        return latestVacation.map(VacationResDto::new).orElseThrow(() -> new IllegalArgumentException("No vacation found for employee"));
    }

    @Override
    public VacationResDto getVacationById(Long vacationId) {
        return vacationRepository.findById(vacationId).map(VacationResDto::new)
                .orElseThrow(() -> new IllegalArgumentException("No vacation found for employee"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacationResDto> getVacationsByEmployee(Long employeeId) {
        return vacationRepository.findByEmployeeId(employeeId).stream().map(VacationResDto::new).toList();
    }

    @Override
    @Transactional
    public VacationResDto updateVacation(Long vacationId, VacationReqDto dto) {
        Vacation vacation = vacationRepository.findById(vacationId)
                .orElseThrow(() -> new IllegalArgumentException("Vacation not found"));

        if(dto.getType() != null) vacation.setType(dto.getType());
        if(dto.getStartDate() != null) vacation.setStartDate(dto.getStartDate());
        if(dto.getEndDate() != null) vacation.setEndDate(dto.getEndDate());
        if(dto.getEndDate() != null) vacation.setEndDate(dto.getEndDate());
        if(dto.getPaid() != null) vacation.setPaid(dto.getPaid());

        return new VacationResDto(vacationRepository.save(vacation));
    }

    @Override
    @Transactional
    public void deleteVacation(Long vacationId) {
        Vacation vacation = vacationRepository.findById(vacationId)
                .orElseThrow(() -> new IllegalArgumentException("Vacation not found"));
        vacationRepository.delete(vacation);
    }

    @Override
    @Transactional
    public VacationResDto approveVacation(Long vacationId) {
        Vacation vacation = vacationRepository.findById(vacationId)
                .orElseThrow(() -> new IllegalArgumentException("Vacation not found"));
        vacation.setApproved(true);
        return new VacationResDto(vacationRepository.save(vacation));
    }
}
