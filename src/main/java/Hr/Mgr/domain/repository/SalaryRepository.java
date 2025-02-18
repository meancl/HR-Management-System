package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.Salary;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {
    Optional<Salary> findTopByEmployeeIdOrderByPaymentDateDesc(Long employeeId);
    List<Salary> findByEmployeeIdAndPaymentDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    List<Salary> findByEmployeeId(Long employeeId);
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Salary s SET s.amount = :amount, s.paymentDate = :paymentDate WHERE s.id = :id")
    void updateSalaryAmountAndLocalDate(@Param("id") Long id,
                                        @Param("amount") BigDecimal amount,
                                        @Param("paymentDate") LocalDate paymentDate);
}
