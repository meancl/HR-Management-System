package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.VacationReqDto;
import Hr.Mgr.domain.dto.VacationResDto;
import Hr.Mgr.domain.enums.VacationType;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.SalaryService;
import Hr.Mgr.domain.service.VacationService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VacationServiceImplTest {

    @Autowired
    private VacationService vacationService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Long employeeId;

    @BeforeEach
    void createEmployee(){
        EmployeeReqDto employeeReqDto = new EmployeeReqDto();
        employeeReqDto.setName("민재");
        employeeReqDto.setEmail("sbe03253@naver.com");
        employeeReqDto.setPassword(bCryptPasswordEncoder.encode("passwd1234"));
        employeeReqDto.setAge(30);

        employeeId =  employeeService.createEmployee(employeeReqDto);
    }


    @Test
    void createVacation() {
        VacationReqDto vacationReqDto = new VacationReqDto();
        vacationReqDto.setApproved(false);
        vacationReqDto.setPaid(false);
        vacationReqDto.setType(VacationType.ANNUAL);
        vacationReqDto.setStartDate(LocalDate.of(2025,1,13));
        vacationReqDto.setEndDate(LocalDate.of(2025, 1, 15));

        vacationReqDto.setEmployeeId(employeeId);

        VacationResDto vacation = vacationService.createVacation(vacationReqDto);

        assertThat(vacation.getType()).isEqualTo(VacationType.ANNUAL);
    }

    @Test
    void getLatestVacationByEmployee() {
        VacationReqDto vacationReqDto = new VacationReqDto();
        vacationReqDto.setApproved(false);
        vacationReqDto.setPaid(false);
        vacationReqDto.setType(VacationType.ANNUAL);
        vacationReqDto.setStartDate(LocalDate.of(2025,1,13));
        vacationReqDto.setEndDate(LocalDate.of(2025, 1, 15));
        vacationReqDto.setEmployeeId(employeeId);

        VacationResDto vacation = vacationService.createVacation(vacationReqDto);

        VacationResDto latestVacationByEmployee = vacationService.getLatestVacationByEmployee(vacation.getEmployeeId());

        assertThat(vacation.getId()).isEqualTo(latestVacationByEmployee.getId());
    }

    @Test
    void getVacationsByEmployee() {
        VacationReqDto vacationReqDto1 = new VacationReqDto();
        vacationReqDto1.setApproved(false);
        vacationReqDto1.setPaid(false);
        vacationReqDto1.setType(VacationType.ANNUAL);
        vacationReqDto1.setStartDate(LocalDate.of(2025,1,13));
        vacationReqDto1.setEndDate(LocalDate.of(2025, 1, 15));
        vacationReqDto1.setEmployeeId(employeeId);

        VacationReqDto vacationReqDto2 = new VacationReqDto();
        vacationReqDto2.setApproved(false);
        vacationReqDto2.setPaid(false);
        vacationReqDto2.setType(VacationType.ANNUAL);
        vacationReqDto2.setStartDate(LocalDate.of(2025,1,13));
        vacationReqDto2.setEndDate(LocalDate.of(2025, 1, 15));
        vacationReqDto2.setEmployeeId(employeeId);

        VacationResDto vacation1 = vacationService.createVacation(vacationReqDto1);
        VacationResDto vacation2 = vacationService.createVacation(vacationReqDto2);

        List<VacationResDto> vacationsByEmployee = vacationService.getVacationsByEmployee(employeeId);
        assertThat(vacationsByEmployee.size()).isEqualTo(2);
    }

    @Test
    void updateVacation() {
        VacationReqDto vacationReqDto = new VacationReqDto();
        vacationReqDto.setApproved(false);
        vacationReqDto.setPaid(false);
        vacationReqDto.setType(VacationType.ANNUAL);
        vacationReqDto.setStartDate(LocalDate.of(2025,1,13));
        vacationReqDto.setEndDate(LocalDate.of(2025, 1, 15));
        vacationReqDto.setEmployeeId(employeeId);

        VacationResDto vacation = vacationService.createVacation(vacationReqDto);


        vacationReqDto.setType(VacationType.OTHER);
        VacationResDto vacationResDto = vacationService.updateVacation(vacation.getId(), vacationReqDto);

        assertThat(vacation.getType()).isNotEqualTo(vacationResDto.getType());
    }

    @Test
    void deleteVacation() {
        VacationReqDto vacationReqDto = new VacationReqDto();
        vacationReqDto.setApproved(false);
        vacationReqDto.setPaid(false);
        vacationReqDto.setType(VacationType.ANNUAL);
        vacationReqDto.setStartDate(LocalDate.of(2025,1,13));
        vacationReqDto.setEndDate(LocalDate.of(2025, 1, 15));
        vacationReqDto.setEmployeeId(employeeId);

        VacationResDto vacation = vacationService.createVacation(vacationReqDto);

        vacationService.deleteVacation(vacation.getId());
        assertThrows(IllegalArgumentException.class,
                () -> vacationService.getLatestVacationByEmployee(employeeId));

    }

    @Test
    void approveVacation() {
        VacationReqDto vacationReqDto = new VacationReqDto();
        vacationReqDto.setApproved(false);
        vacationReqDto.setPaid(false);
        vacationReqDto.setType(VacationType.ANNUAL);
        vacationReqDto.setStartDate(LocalDate.of(2025,1,13));
        vacationReqDto.setEndDate(LocalDate.of(2025, 1, 15));
        vacationReqDto.setEmployeeId(employeeId);

        VacationResDto vacation = vacationService.createVacation(vacationReqDto);

        VacationResDto vacationResDto = vacationService.approveVacation(vacation.getId());
        assertThat(vacationResDto.getApproved()).isEqualTo(true);
    }
}