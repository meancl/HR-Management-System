package Hr.Mgr.domain.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

@Aspect
@Component
public class DataSourceRoutingAspect {

    @Value("${server-name}")
    String serverName;
    @Pointcut("@within(org.springframework.stereotype.Repository) || execution(* Hr.Mgr.domain.repository..*(..))")
    public void repositoryAccess() {}
    @Pointcut("execution(* *..*ServiceImpl.*(..))")
    public void serviceMethods() {}
    @Before("serviceMethods() || repositoryAccess()")
    public void setDataSource(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();

        boolean readOnly = method.isAnnotationPresent(Transactional.class) &&
                method.getAnnotation(Transactional.class).readOnly();

        // A 서버는 무조건 master
        if (isServerA()) {
            if(readOnly || method.getName().startsWith("get") || method.getName().startsWith("find"))
                DbContextHolder.useMasterRead();
            else
                DbContextHolder.useMasterWrite();
        } else {
            if (readOnly || method.getName().startsWith("get") || method.getName().startsWith("find")) {
                DbContextHolder.useReplicaRead();
            } else {
                DbContextHolder.useReplicaWrite();
            }
        }
    }

    @After("execution(* *..*ServiceImpl.*(..))")
    public void clear() {
        DbContextHolder.clear();
    }

    private boolean isServerA() {
        // 호스트 이름, 환경 변수, config 등으로 분기
        return serverName.equalsIgnoreCase("master");
    }
}