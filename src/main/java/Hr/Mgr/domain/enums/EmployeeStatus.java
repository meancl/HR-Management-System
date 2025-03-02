package Hr.Mgr.domain.enums;

public enum EmployeeStatus {
    ACTIVE("근무 중"),
    LEAVE_OF_ABSENCE("휴직"),
    TERMINATED("퇴사"),
    PROBATION("수습"),
    SUSPENDED("정직"),
    PENDING("입사 대기"),
    RETIRED("정년 퇴직");

    private final String description;

    EmployeeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
