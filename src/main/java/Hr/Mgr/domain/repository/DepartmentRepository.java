package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("SELECT dep FROM Department dep " +
            "LEFT JOIN FETCH dep.employees emp " +
            "WHERE emp.id = :employeeId")
    Optional<Department> findByEmployeeId(@Param("employeeId") Long id);
}
