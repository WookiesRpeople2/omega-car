package com.example.Security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecuredRolesAspect {

  @Before("within(@com.example.Security.SecuredRoles *) || @annotation(com.example.Security.SecuredRoles)")
  public void check(JoinPoint jp) {
    SecuredRoles secured = jp.getTarget().getClass().getAnnotation(SecuredRoles.class);
    if (secured == null) {
      try {
        secured = jp.getSignature().getDeclaringType().getMethod(jp.getSignature().getName()).getAnnotation(SecuredRoles.class);
      } catch (Throwable ignored) {}
    }
    if (secured == null) return;
    String[] roles = secured.value();
    if (!Rbac.hasAnyRole(roles)) {
      throw new AccessDeniedException("Access denied");
    }
  }
}


