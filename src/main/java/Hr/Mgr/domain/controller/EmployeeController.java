package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ✅ 직원확인 폼 표시
    @GetMapping
    public String listEmployees(Model model) {
        List<EmployeeResDto> employees = employeeService.findAllEmployeeDtos();
        model.addAttribute("employees", employees);
        return "/employee/listEmployees";
    }

    // ✅ 특정 직원확인 폼 표시
    @GetMapping("/{id}")
    public String listEmployee(@PathVariable("id") Long id, Model model) {
        EmployeeResDto employee = employeeService.findEmployeeDtoById(id);
        model.addAttribute("employee", employee);
        return "/employee/listEmployee";
    }

    // ✅ 회원가입 폼 표시
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("employeeReqDto", new EmployeeReqDto());
        return "/employee/createEmployeeForm"; // Thymeleaf 템플릿 파일명
    }

    // ✅ 회원가입 처리
    @PostMapping("/register")
    public String registerEmployee(@ModelAttribute EmployeeReqDto employeeReqDto) {
        employeeService.createEmployee(employeeReqDto);

        // TODO. 로그로 확인
        return "redirect:/employees"; // 회원가입 후 로그인 페이지로 이동
    }

    // ✅ 직원 수정 폼
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        EmployeeResDto employeeById = employeeService.findEmployeeDtoById(id);

        EmployeeReqDto employeeReqDto = new EmployeeReqDto();

        employeeReqDto.setAge(employeeById.getAge());
        employeeReqDto.setName(employeeById.getName());
        employeeReqDto.setEmail(employeeById.getEmail());

        model.addAttribute("empId", id);

        model.addAttribute("employeeDto", employeeReqDto);
        return "/employee/editEmployeeForm";
    }
    // ✅ 직원 수정 처리
    @PostMapping("/edit/{id}")
    public String patchEmployee(@PathVariable("id") Long id, @ModelAttribute EmployeeReqDto employeeReqDto) {
        employeeService.updateEmployee(id, employeeReqDto);
        return "redirect:/employees";
    }
    // ✅ 직원 삭제 처리
    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
}
