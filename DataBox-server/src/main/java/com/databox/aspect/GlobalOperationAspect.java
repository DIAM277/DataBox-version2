package com.databox.aspect;

import com.databox.annotation.GlobalInterceptor;
import com.databox.annotation.VerifyParam;
import com.databox.entity.constants.Constants;
import com.databox.entity.dto.SessionWebUserDto;
import com.databox.entity.enums.ResponseCodeEnum;
import com.databox.exception.BusinessException;
import com.databox.utils.StringTools;
import com.databox.utils.VerifyUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component("globalOperationAspect")
@Slf4j
public class GlobalOperationAspect {

    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_INTEGER = "java.lang.Integer";
    private static final String TYPE_LONG = "java.lang.Long";

    @Pointcut("@annotation(com.databox.annotation.GlobalInterceptor)")
    private void requestInterceptor() {
    }

    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint point) throws BusinessException {
        try {
            // 获取目标对象、方法参数和方法信息
            Object target = point.getTarget();
            Object[] arguments = point.getArgs();
            String methodName = point.getSignature().getName();
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            // 获取方法上的 GlobalInterceptor 注解
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            // 如果没有注解，直接返回
            if (interceptor == null) {
                return;
            }
            // 校验登录
            if(interceptor.checkLogin() || interceptor.checkAdmin()){
                checkLogin(interceptor.checkAdmin());
            }
            // 校验参数
            if (interceptor.checkParams()) {
                validateParams(method, arguments);
            }

        } catch (BusinessException e) {
            // 捕获业务异常，记录日志并抛出
            log.error("全局拦截器异常", e);
            throw e;
        } catch (Exception e) {
            // 捕获其他异常，记录日志并抛出业务异常
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Throwable e) {
            // 捕获所有其他类型的错误，记录日志并抛出业务异常
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void validateParams(Method method, Object[] arguments) throws BusinessException {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = arguments[i];
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if (verifyParam == null) {
                continue;
            }
            if(TYPE_STRING.equals(parameter.getParameterizedType().getTypeName()) || TYPE_LONG.equals(parameter.getParameterizedType().getTypeName()) || TYPE_INTEGER.equals(parameter.getParameterizedType().getTypeName())){
                checkValue(value, verifyParam);
            } else {
                checkObjValue(parameter, value);
            }
        }
    }

    private void checkObjValue(Parameter parameter, Object value) throws BusinessException {
        try {
            // 获取参数的类型名并通过反射加载该类型的Class对象
            String typeName = parameter.getParameterizedType().getTypeName();
            Class<?> classz = Class.forName(typeName);

            // 获取类中的所有字段
            Field[] fields = classz.getDeclaredFields();

            // 遍历每个字段，检查是否有VerifyParam注解
            for (Field field : fields) {
                VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
                // 如果没有注解则跳过
                if (fieldVerifyParam == null) {
                    continue;
                }
                // 设置字段可访问
                field.setAccessible(true);
                // 获取字段的值
                Object resultValue = field.get(value);
                // 校验字段的值
                checkValue(resultValue, fieldVerifyParam);
            }
        } catch (BusinessException e) {
            // 捕获业务异常，记录日志并抛出
            log.error("校验参数失败", e);
            throw e;
        } catch (Exception e) {
            // 捕获其他异常，记录日志并抛出业务异常
            log.error("校验参数失败", e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    private void checkValue(Object value, VerifyParam verifyParam) throws BusinessException {
        Boolean isEmpty = value == null || StringTools.isEmpty(value.toString());
        Integer length = value == null ? 0 : value.toString().length();

        // 校验空
        if(isEmpty && verifyParam.required()){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 校验长度
        if(!isEmpty && (verifyParam.max() != -1 && length > verifyParam.max() || verifyParam.min() != -1 && length < verifyParam.min())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 校验正则
        if(!isEmpty && !StringTools.isEmpty(verifyParam.regex().getRegex()) && !VerifyUtils.verify(verifyParam.regex(), String.valueOf(value))){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    private void checkLogin(Boolean checkAdmin){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession  session = request.getSession();
        SessionWebUserDto userDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        if(null == userDto){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if(checkAdmin && !userDto.getIsAdmin()){
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }

}
