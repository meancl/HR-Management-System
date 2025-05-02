package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.SalaryReqDto;
import Hr.Mgr.domain.dto.SalaryResDto;
import Hr.Mgr.domain.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<SalaryResDto>> getSalariesByEmployee(@PathVariable Long employeeId) {
        List<SalaryResDto> salaries = salaryService.getSalariesByEmployee(employeeId);
        return ResponseEntity.ok(salaries);
    }

    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<Void> createSalary(@PathVariable Long employeeId,
                                             @RequestBody SalaryReqDto salaryReqDto) {
        salaryReqDto.setEmployeeId(employeeId);
        salaryService.createSalary(salaryReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{salaryId}")
    public ResponseEntity<Void> editSalary(@PathVariable Long salaryId, @RequestBody SalaryReqDto salaryReqDto) {
        salaryService.updateSalary(salaryId, salaryReqDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{salaryId}")
    public  ResponseEntity<Void> deleteSalary(@PathVariable Long salaryId) {
        SalaryResDto salary = salaryService.getSalaryById(salaryId);
        salaryService.deleteSalary(salaryId);
        return ResponseEntity.noContent().build();
    }
}
