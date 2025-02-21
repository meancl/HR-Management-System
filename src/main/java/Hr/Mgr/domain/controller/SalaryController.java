package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.SalaryReqDto;
import Hr.Mgr.domain.dto.SalaryResDto;
import Hr.Mgr.domain.service.SalaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/salaries")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    // 특정 직원의 급여 목록 조회
    @GetMapping("/employee/{employeeId}")
    public String getSalariesByEmployee(@PathVariable Long employeeId, Model model) {
        List<SalaryResDto> salaries = salaryService.getSalariesByEmployee(employeeId);
        model.addAttribute("salaries", salaries);
        model.addAttribute("employeeId", employeeId);
        return "/salary/listSalary";
    }

    // 급여 생성 폼
    @GetMapping("/create/{employeeId}")
    public String showCreateSalaryForm(@PathVariable Long employeeId, Model model) {
        model.addAttribute("salaryReqDto", new SalaryReqDto());
        model.addAttribute("employeeId", employeeId);
        return "/salary/createSalaryForm";
    }

    // 급여 생성 처리
    @PostMapping("/create/{employeeId}")
    public String createSalary(@PathVariable Long employeeId, @ModelAttribute SalaryReqDto salaryReqDto) {
        salaryReqDto.setEmployeeId(employeeId);
        salaryService.createSalary(salaryReqDto);
        return "redirect:/employees/" + employeeId;
    }

    // 급여 수정 폼
    @GetMapping("/edit/{salaryId}")
    public String showEditSalaryForm(@PathVariable Long salaryId, Model model) {
        SalaryResDto salary = salaryService.getSalaryById(salaryId);
        model.addAttribute("salary", salary);
        return "/salary/editSalaryForm";
    }

    // 급여 수정 처리
    @PostMapping("/edit/{salaryId}")
    public String editSalary(@PathVariable Long salaryId, @ModelAttribute SalaryReqDto salaryReqDto) {
        salaryService.updateSalary(salaryId, salaryReqDto);
        return "redirect:/employees/" + salaryReqDto.getEmployeeId();
    }

    // 급여 삭제 처리
    @GetMapping("/delete/{salaryId}")
    public String deleteSalary(@PathVariable Long salaryId) {
        SalaryResDto salary = salaryService.getSalaryById(salaryId);
        salaryService.deleteSalary(salaryId);
        return "redirect:/employees/" + salary.getEmployeeId();
    }
}
