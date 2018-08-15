package net.tngroup.acrestnode.nodeclient.aspects;

import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class StatusAspect {

    private final Logger logger = LogManager.getFormatterLogger("CommonLogger");

    @Pointcut("execution(* net.tngroup.acrestnode.nodeclient.components.StatusComponent.connected(..))")
    public void connectedPointcut() {
    }

    @Pointcut("execution(* net.tngroup.acrestnode.nodeclient.components.StatusComponent.disconnected(..))")
    public void disconnectedPointcut() {
    }

    @Before(value = "connectedPointcut()")
    public void connectedMethod(JoinPoint joinPoint) {
        Channel channel = (Channel) joinPoint.getArgs()[0];
        logger.info("Server with address '%s': connected", channel.remoteAddress().toString());
    }

    @Before(value = "disconnectedPointcut()")
    public void disconnectedMethod(JoinPoint joinPoint) {
        Channel channel = (Channel) joinPoint.getArgs()[0];
        logger.info("Server with address '%s': disconnected", channel.remoteAddress().toString());
    }

}
