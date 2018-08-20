package net.tngroup.acrestnode.node.aspects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
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

    @AfterReturning(value = "commandPointcut()")
    public void commandMethod(JoinPoint joinPoint) {
        String command = (String) joinPoint.getArgs()[0];
        logger.info("Command `" + command + "` successfully performed");
    }

    @AfterThrowing(value = "commandPointcut()", throwing = "ex")
    public void commandMethod(JoinPoint joinPoint, Throwable ex) {
        String command = (String) joinPoint.getArgs()[0];
        logger.warn("Command `" + command + "` was not performed: " + ex.getMessage());
    }

}
