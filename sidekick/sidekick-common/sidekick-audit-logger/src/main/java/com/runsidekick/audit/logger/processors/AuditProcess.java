package com.runsidekick.audit.logger.processors;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.annotations.AuditField;
import com.runsidekick.audit.logger.dto.AuditLog;
import com.runsidekick.audit.logger.providers.AuditLoggerProviderHelper;
import com.runsidekick.audit.logger.services.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Slf4j
@Aspect
@Component
public class AuditProcess {

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    AuditLoggerProviderHelper auditLoggerProviderHelper;

    @Pointcut("@annotation(com.runsidekick.audit.logger.annotations.Audit)")
    public void anyRun() {
    }

    @Before(value = "anyRun()")
    public void before(JoinPoint jointPoint) {
        MethodSignature signature = (MethodSignature) jointPoint.getSignature();
        Method method = signature.getMethod();

        Audit auditAnnotation = method.getAnnotation(Audit.class);
        if (auditAnnotation != null) {
            AuditLog auditLog = auditLogService.createNewAuditLog(auditAnnotation.action(), auditAnnotation.domain());
            setAuditFieldParameters(jointPoint);
            auditLoggerProviderHelper.beforeProcess(auditLog);
        }
    }


    @AfterReturning(value = "anyRun()", returning = "returnValue")
    public void after(JoinPoint joinPoint, Object returnValue) {
        AuditLog auditLog = auditLogService.getCurrentAuditLog().get();
        try {
            if (returnValue instanceof ResponseEntity) {
                ResponseEntity response = (ResponseEntity) returnValue;
                auditLog.setResult(response.getStatusCode().toString());
            }

            auditLoggerProviderHelper.afterProcess(auditLog);
        } finally {
            auditLogService.removeAuditLog(auditLog);
        }
    }

    @AfterThrowing(value = "anyRun()", throwing = "throwable")
    public void afterThrow(JoinPoint joinPoint, Exception throwable) {
        AuditLog currentAuditLog = auditLogService.getCurrentAuditLog().get();

        try {
            currentAuditLog.setErrorMessage(throwable.getMessage());
            currentAuditLog.setErrorType(throwable.getClass().getSimpleName());

            auditLoggerProviderHelper.afterProcess(currentAuditLog);
        } finally {
            auditLogService.removeAuditLog(currentAuditLog);
        }
    }

    private void setAuditFieldParameters(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        if (args != null && args.length != 0) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();

            Method method = signature.getMethod();
            Annotation[][] parametersAnnotations = method.getParameterAnnotations();

            List<String> parameterNames = Arrays.asList(signature.getParameterNames());

            for (Annotation[] annotations : parametersAnnotations) {
                for (Annotation annotation : annotations) {
                    if ((annotation instanceof AuditField)) {
                        String parameterName = ((AuditField) annotation).name();
                        int parameterOrder = parameterNames.indexOf(parameterName);
                        if (parameterOrder > 0 && parameterOrder < args.length) {
                            Object value = args[parameterOrder];
                            auditLogService.getCurrentAuditLog().ifPresent(auditLog -> auditLog.addAuditLogField(
                                    parameterName, value.toString()));
                        }
                    }
                }
            }
        }
    }
}
