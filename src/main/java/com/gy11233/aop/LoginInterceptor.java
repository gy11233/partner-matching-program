package com.gy11233.aop;

import com.gy11233.common.ErrorCode;
import com.gy11233.exception.BusinessException;
import com.gy11233.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 登录拦截 AOP
 */
@Aspect
@Component
@Slf4j
public class LoginInterceptor {

    @Resource
    private UserService userService;

    @Before("execution(* com.gy11233.controller.*.*(..)) && " +
            "!execution(* com.gy11233.controller.UserController.userLogin(..)) && " +
            "!execution(* com.gy11233.controller.UserController.userRegister(..)) ")
//            "!execution(* com.gy11233.controller.UserController.recommendUsers(..))")
    public void beforeControllerMethodExecution() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();

        if (userService.getLoginUser(request) == null) {
            log.info("拦截登陆成功！");
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
    }
}
