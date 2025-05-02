package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Page<EmployeeResDto>> listEmployees(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeResDto> employees = employeeService.findAllEmployeeDtos(pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResDto> getEmployee(@PathVariable("id") Long id) {
        EmployeeResDto employee = employeeService.findEmployeeDtoById(id);
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity<Void> registerEmployee(@RequestBody EmployeeReqDto employeeReqDto) {
        employeeService.createEmployee(employeeReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateEmployee(@PathVariable("id") Long id, @RequestBody EmployeeReqDto employeeReqDto) {
        employeeService.updateEmployee(id, employeeReqDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
