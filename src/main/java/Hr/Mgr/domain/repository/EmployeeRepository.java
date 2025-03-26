package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.ReviewScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    @Query("SELECT emp FROM Employee emp " +
            "JOIN FETCH emp.department")
    List<Employee> findAllEmployeesWithDepartment();
}
