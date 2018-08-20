package net.tngroup.acrestnode.databases.aspects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DbAspect {

    private final Logger logger = LogManager.getFormatterLogger("DbLogger");

    @Pointcut("execution(public * net.tngroup.acrestnode.databases.cassandra.services.*.*(..))")
    public void cassandraPointcut() {
    }

    @Pointcut("execution(public * net.tngroup.acrestnode.databases.h2.services.*.*(..))")
    public void h2Pointcut() {
    }

    @After(value = "cassandraPointcut()")
    public void afterCassandra(JoinPoint joinPoint) {
        dbMethod("Cassandra", joinPoint);
    }

    @After(value = "h2Pointcut()")
    public void afterH2(JoinPoint joinPoint) {
        dbMethod("H2", joinPoint);
    }

    private void dbMethod(String db, JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        StringBuilder sb = new StringBuilder()
                .append(db)
                .append(" database: request `")
                .append(className)
                .append("` - `")
                .append(methodName)
                .append("`");

        if (args.length > 0) {
            sb.append(" with params");
            for (Object arg : args) {
                sb.append(" `").append(arg).append("`");
            }
        }

        logger.info(sb.toString());
    }

}
