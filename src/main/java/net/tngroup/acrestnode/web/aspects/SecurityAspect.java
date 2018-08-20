package net.tngroup.acrestnode.web.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.web.security.models.Credentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class SecurityAspect {

    private final Logger logger = LogManager.getFormatterLogger("CommonLogger");

    @Pointcut("execution(public * net.tngroup.acrestnode.web.security.filters.JwtLoginFilter.attemptAuthentication(..))")
    public void loginPointcut() {
    }

    @Around(value = "loginPointcut()")
    public Authentication loginMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        HttpServletRequest request = (HttpServletRequest) proceedingJoinPoint.getArgs()[0];
        Credentials credentials = new ObjectMapper().readValue(request.getInputStream(), Credentials.class);
        String address = request.getRemoteAddr();

        logger.info("`" + address + "` try to login as `" + credentials.getUsername() + "`");

        Authentication authentication = (Authentication) proceedingJoinPoint.proceed();

        if (authentication == null) logger.warn("`" + address + "` failed to login!");
        else logger.info("`" + address + "` logged in");

        return authentication;
    }

}
