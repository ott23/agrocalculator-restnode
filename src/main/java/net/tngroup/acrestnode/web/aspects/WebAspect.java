package net.tngroup.acrestnode.web.aspects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class WebAspect {

    private final Logger logger = LogManager.getFormatterLogger("CommonLogger");

    @Pointcut("execution(public * net.tngroup.acrestnode.web.controllers.*.*(..))")
    public void webPointcut() {
    }

    @Before(value = "webPointcut()")
    public void webMethod(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String address = "unknown address";
        List<String> params = new ArrayList<>();

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) arg;
                address = request.getRemoteAddr();
                break;
            } else {
                params.add(arg.toString());
            }
        }

        StringBuilder sb = new StringBuilder()
                .append("`")
                .append(className)
                .append("`: request to `")
                .append(methodName)
                .append("` from `")
                .append(address)
                .append("`");

        if (params.size() > 0) {
            sb.append(" with params ");
            params.forEach(p -> sb.append(" `").append(p).append("`"));
        }

        logger.info(sb.toString());
    }

    @AfterThrowing(value = "webPointcut()", throwing = "e")
    public void webThrowing(JoinPoint joinPoint, Throwable e) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        StringBuilder sb = new StringBuilder()
                .append("Exception in `")
                .append(className)
                .append("` - `")
                .append(methodName)
                .append("`: ")
                .append(e.getMessage());

        logger.warn(sb.toString());
    }

}
