package net.tngroup.acrestnode.node.aspects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NodeAspect {

    private final Logger logger = LogManager.getFormatterLogger("CommonLogger");

    @Pointcut("execution(public * net.tngroup.acrestnode.node.AutoExecutor.run(..))")
    public void startPointcut() {
    }

    @Pointcut("execution(public * net.tngroup.acrestnode.node.Processor.*(..))")
    public void commandPointcut() {
    }

    @After(value = "startPointcut()")
    public void startMethod() {
        logger.info("Application initialized");
    }

    @AfterReturning(value = "commandPointcut()", returning = "result")
    public void commandMethod(JoinPoint joinPoint, boolean result) {
        String command = (String) joinPoint.getArgs()[0];
        if (result) logger.info("Command `" + command + "` successfully performed");
        else logger.warn("Command `" + command + "` was not performed");
    }

}
