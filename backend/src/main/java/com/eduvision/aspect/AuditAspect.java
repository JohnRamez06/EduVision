package com.eduvision.aspect;

import com.eduvision.model.AuditAction;
import com.eduvision.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Around("execution(* com.eduvision.controller.*.*(..))")
    public Object auditControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String userId = auth != null ? auth.getName() : null;
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuditAction action = mapMethodToAction(method, uri);

        long start = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            success = false;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            auditLogService.logAction(userId, null, action, "controller", uri,
                null, null, ip, userAgent, success);
        }
        return result;
    }

    private AuditAction mapMethodToAction(String method, String uri) {
        switch (method) {
            case "GET": return AuditAction.read;
            case "POST": return AuditAction.create;
            case "PUT": return AuditAction.update;
            case "DELETE": return AuditAction.delete;
            default: return AuditAction.read;
        }
    }
}