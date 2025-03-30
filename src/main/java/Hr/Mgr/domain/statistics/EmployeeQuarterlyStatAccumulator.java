package Hr.Mgr.domain.statistics;

import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.QuarterlyAttendanceStatistics;
import Hr.Mgr.domain.enums.AttendanceStatus;
import Hr.Mgr.domain.init.DataInitializer;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeQuarterlyStatAccumulator {
    private final Long employeeId;
    private final String department;
    private final int year;
    private final int quarter;

    private int presentDays = 0;
    private int lateCount = 0;
    private int totalWorkMinutes = 0;
    private int totalOvertimeMinutes = 0;
    private int holidayWorkCount = 0;
    private int countedTime = 0;
    private int totalStartSeconds = 0;
    private int totalEndSeconds = 0;

    private final Map<String, Integer> weeklyWorkMinutes = new HashMap<>();

    public EmployeeQuarterlyStatAccumulator(Long employeeId, String department, int year, int quarter) {
        this.employeeId = employeeId;
        this.department = department;
        this.year = year;
        this.quarter = quarter;
    }

    public void accumulate(List<AttendanceResDto> data, DataInitializer.DepartmentPolicy policy) {
        for (AttendanceResDto att : data) {
            if (att.getStatus() == AttendanceStatus.HOLIDAY) continue;

            presentDays++;

            if (att.getStatus() == AttendanceStatus.LATE) lateCount++;

            LocalDate date = att.getAttendanceDate();
            LocalTime in = att.getCheckInTime();
            LocalTime out = att.getCheckOutTime();

            if (in != null && out != null) {
                int worked = (int) Duration.between(in, out).toMinutes();
                totalWorkMinutes += worked;

                totalStartSeconds += in.toSecondOfDay();
                totalEndSeconds += out.toSecondOfDay();
                countedTime++;

                String weekKey = date.getYear() + "-W" + date.get(WeekFields.ISO.weekOfWeekBasedYear());
                weeklyWorkMinutes.put(weekKey, weeklyWorkMinutes.getOrDefault(weekKey, 0) + worked);
            }

            if (out != null && out.isAfter(policy.baseEndTime)) {
                totalOvertimeMinutes += (int) Duration.between(policy.baseEndTime, out).toMinutes();
            }

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                holidayWorkCount++;
            }
        }
    }

    public QuarterlyAttendanceStatistics toFinalStatistics() {
        double holidayRatio = presentDays == 0 ? 0.0 : (double) holidayWorkCount / presentDays;
        int avgWorkMinutes = presentDays == 0 ? 0 : totalWorkMinutes / presentDays;
        int avgOvertime = presentDays == 0 ? 0 : totalOvertimeMinutes / presentDays;
        LocalTime avgStartTime = countedTime == 0 ? LocalTime.MIDNIGHT : LocalTime.ofSecondOfDay(totalStartSeconds / countedTime);
        LocalTime avgEndTime = countedTime == 0 ? LocalTime.MIDNIGHT : LocalTime.ofSecondOfDay(totalEndSeconds / countedTime);

        return QuarterlyAttendanceStatistics.builder()
                .employeeId(employeeId)
                .department(department)
                .year(year)
                .quarter(quarter)
                .presentDays(presentDays)
                .lateCount(lateCount)
                .avgWorkMinutes(avgWorkMinutes)
                .avgOvertimeMinutes(avgOvertime)
                .avgStartTime(avgStartTime)
                .avgEndTime(avgEndTime)
                .weeklyWorkMinutes(weeklyWorkMinutes)
                .holidayWorkRatio(holidayRatio)
                .build();
    }
}

