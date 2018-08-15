package net.tngroup.acrestnode.nodeclient.aspects;

import net.tngroup.acrestnode.nodeclient.models.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MessageAspect {

    private final Logger logger = LogManager.getFormatterLogger("CommonLogger");

    @Pointcut("execution(public * net.tngroup.acrestnode.nodeclient.components.InputMessageComponent.readMessage(..))")
    public void inputMessagePointcut() {
    }

    @Pointcut("execution(* net.tngroup.acrestnode.nodeclient.components.OutputMessageComponent.*(..))")
    public void outputMessagePointcut() {
    }

    @Around(value = "inputMessagePointcut()")
    public Message inputMessageMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        logger.info("Message from server received...");

        Message message = (Message) proceedingJoinPoint.proceed();

        if (message == null) return null;

        StringBuilder sb = new StringBuilder()
                .append("Message from server successfully handled: `")
                .append(message.getType())
                .append("`");

        if (message.getType().equals("command")) {
            sb.append(" - `").append(message.getValue()).append("`");
        }

        logger.info(sb.toString());

        return message;
    }

    @AfterThrowing(value = "inputMessagePointcut()", throwing = "ex")
    public void inputMessageThrowing(Throwable ex) {
        logger.warn("Exception during message receiving: " + ex.getMessage());
    }

    @AfterReturning(value = "outputMessagePointcut()")
    public void outputMessageMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String type = methodName.replace("sendMessage", "").toLowerCase();
        logger.info("Message to server sent: `" + type + "`");
    }

    @AfterThrowing(value = "outputMessagePointcut()", throwing = "ex")
    public void outputMessageThrowing(Throwable ex) {
        logger.warn("Exception during message sending: " + ex.getMessage());
    }

}
