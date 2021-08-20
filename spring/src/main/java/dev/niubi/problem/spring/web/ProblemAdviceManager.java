/*
 * Copyright 2021 陈圳佳
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.niubi.problem.spring.web;

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.ResponseProblem;
import dev.niubi.problem.spring.web.method.ProblemAdviceBean;
import dev.niubi.problem.spring.web.method.ProblemHandlerMethodResolver;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;

public class ProblemAdviceManager implements InitializingBean, ApplicationContextAware {

  protected final Log logger = LogFactory.getLog(getClass());
  private final Map<ProblemAdviceBean, ProblemHandlerMethodResolver> exceptionHandlerAdviceCache =
      new LinkedHashMap<>();
  private final Map<Class<?>, ProblemHandlerMethodResolver> exceptionHandlerCache =
      new ConcurrentHashMap<>(64);
  private Consumer<ResponseProblem> problemCustomizer = problem -> {
  };
  @Nullable
  private ApplicationContext applicationContext;

  @Override
  public void afterPropertiesSet() {
    initProblemAdvice();
  }

  @Nullable
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setProblemCustomizer(
      Consumer<ResponseProblem> problemCustomizer) {
    this.problemCustomizer = problemCustomizer;
  }

  private void initProblemAdvice() {
    if (getApplicationContext() == null) {
      return;
    }
    List<ProblemAdviceBean> adviceBeans = ProblemAdviceBean.findAnnotatedBeans(getApplicationContext());
    for (ProblemAdviceBean adviceBean : adviceBeans) {
      Class<?> beanType = adviceBean.getBeanType();
      if (beanType == null) {
        throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
      }

      ProblemHandlerMethodResolver resolver = new ProblemHandlerMethodResolver(beanType);
      if (resolver.hasExceptionMappings()) {
        this.exceptionHandlerAdviceCache.put(adviceBean, resolver);
      }
    }

    if (logger.isDebugEnabled()) {
      int handlerSize = this.exceptionHandlerAdviceCache.size();
      if (handlerSize == 0) {
        logger.debug("ProblemAdvice beans: none");
      } else {
        logger.debug("ProblemAdvice beans: "
            + handlerSize + " @ExceptionHandler");
      }
    }
  }

  public ResponseProblem handleProblem(@Nullable HandlerMethod handlerMethod, Throwable ex) {
    if (ex == null) {
      return null;
    }
    Throwable rootCause = NestedExceptionUtils.getRootCause(ex);
    if (rootCause == null) {
      rootCause = ex;
    }
    BeanMethod beanMethod = resolverHandleMethod(handlerMethod, rootCause);
    if (beanMethod == null) {
      return null;
    }
    try {
      Object result = beanMethod.invoke(rootCause);
      if (result == null) {
        return null;
      }

      Class<?> resultClass = result.getClass();
      ResponseProblem responseProblem;
      if (result instanceof ResponseProblem) {
        responseProblem = (ResponseProblem) result;
      } else if (Problem.class.isAssignableFrom(resultClass)) {
        Problem problem = ((Problem) result);
        responseProblem = ResponseProblem.with(problem);
      } else {
        return null;
      }
      problemCustomizer.accept(responseProblem);
      return responseProblem;
    } catch (Throwable e) {
      if (!logger.isWarnEnabled()) {
        logger.warn("Failure in @ExceptionHandler ", rootCause);
      }
    }
    return null;
  }

  public ResponseProblem handleProblem(Throwable ex) {
    return handleProblem(null, ex);
  }

  @Nullable
  private BeanMethod resolverHandleMethod(@Nullable HandlerMethod handlerMethod, Throwable exception) {
    Class<?> handlerType = null;

    if (handlerMethod != null) {
      // Local exception handler methods on the controller class itself.
      // To be invoked through the proxy, even in case of an interface-based proxy.
      handlerType = handlerMethod.getBeanType();
      ProblemHandlerMethodResolver resolver = this.exceptionHandlerCache.get(handlerType);
      if (resolver == null) {
        resolver = new ProblemHandlerMethodResolver(handlerType);
        this.exceptionHandlerCache.put(handlerType, resolver);
      }
      Method method = resolver.resolveMethodByThrowable(exception);
      if (method != null) {
        return new BeanMethod(handlerMethod.getBean(), method);
      }
      // For advice applicability check below (involving base packages, assignable types
      // and annotation presence), use target class instead of interface-based proxy.
      if (Proxy.isProxyClass(handlerType)) {
        handlerType = AopUtils.getTargetClass(handlerMethod.getBean());
      }
    }
    for (Map.Entry<ProblemAdviceBean, ProblemHandlerMethodResolver> entry :
        this.exceptionHandlerAdviceCache.entrySet()) {
      ProblemAdviceBean advice = entry.getKey();
      if (advice.isApplicableToBeanType(handlerType)) {
        ProblemHandlerMethodResolver resolver = entry.getValue();
        Method method = resolver.resolveMethodByThrowable(exception);
        if (method != null) {
          return new BeanMethod(advice.resolveBean(), method);
        }
      }
    }
    return null;
  }

  private static class BeanMethod {

    private final Object bean;
    private final Method method;

    public BeanMethod(Object bean, Method method) {
      this.bean = bean;
      this.method = method;
    }

    public Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
      return method.invoke(bean, args);
    }
  }
}
