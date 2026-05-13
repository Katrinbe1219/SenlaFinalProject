package org.example.core.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerLoggingAspect {
    private static final Logger logger = LogManager.getLogger(ControllerLoggingAspect.class);

    @AfterReturning(
            pointcut = "execution(* org.example.core.controllers..*(..))",
            returning = "result"
    )
    public void logSuccess(JoinPoint joinPoint) {
        String classname = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("{} {} | SUCCESS ", classname, methodName);
    }

    @AfterThrowing(
            pointcut = "execution(* org.example.core.controllers..*(..))",
            throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Exception ex) {
        String classname = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String exceptionName = ex.getClass().getSimpleName();
        logger.error("{} {} {} : {} ", exceptionName, classname, methodName, ex.getMessage());
    }
}
