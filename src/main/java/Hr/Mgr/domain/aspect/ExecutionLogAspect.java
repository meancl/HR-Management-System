package Hr.Mgr.domain.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class ExecutionLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionLogAspect.class);


    @Around("@annotation(Hr.Mgr.domain.aspect.LogStartTime)")
    public Object logStartTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("⏱️ {} 시작 시간: {}", methodName, now);

        return joinPoint.proceed();
    }

    @Around("@annotation(Hr.Mgr.domain.aspect.MeasureExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long end = System.currentTimeMillis();
        logger.info("✅ {} 실행 시간: {}ms", methodName, (end - start));

        return result;
    }
}