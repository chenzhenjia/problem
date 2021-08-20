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

package dev.niubi.problem.spring.web.method;

import dev.niubi.problem.Problem;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.ExceptionDepthComparator;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

public class ProblemHandlerMethodResolver {

  private static final MethodFilter PROBLEM_PARAMETER_TYPE_FILTER = method -> {
    Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length != 1) {
      return false;
    }
    Class<?> parameterType = parameterTypes[0];
    return Throwable.class.isAssignableFrom(parameterType);
  };
  private static final MethodFilter PROBLEM_EXCEPTION_METHOD = method -> {
    Class<?> returnType = method.getReturnType();
    return Problem.class.isAssignableFrom(returnType);
  };
  public static final MethodFilter PROBLEM_EXCEPTION_HANDLER_METHODS = ExceptionHandlerMethodResolver
      .EXCEPTION_HANDLER_METHODS.and(PROBLEM_PARAMETER_TYPE_FILTER).and(PROBLEM_EXCEPTION_METHOD);
  private static final Method NO_MATCHING_EXCEPTION_HANDLER_METHOD;

  static {
    try {
      NO_MATCHING_EXCEPTION_HANDLER_METHOD =
          ExceptionHandlerMethodResolver.class.getDeclaredMethod("noMatchingExceptionHandler");
    } catch (NoSuchMethodException ex) {
      throw new IllegalStateException("Expected method not found: " + ex);
    }
  }


  private final Map<Class<? extends Throwable>, Method> mappedMethods = new HashMap<>(16);

  private final Map<Class<? extends Throwable>, Method> exceptionLookupCache = new ConcurrentReferenceHashMap<>(16);


  public ProblemHandlerMethodResolver(Class<?> handlerType) {
    Set<Method> methods = MethodIntrospector.selectMethods(handlerType, PROBLEM_EXCEPTION_HANDLER_METHODS);
    for (Method method : methods) {
      for (Class<? extends Throwable> exceptionType : detectExceptionMappings(method)) {
        addExceptionMapping(exceptionType, method);
      }
    }
  }


  @SuppressWarnings("unchecked")
  private List<Class<? extends Throwable>> detectExceptionMappings(Method method) {
    List<Class<? extends Throwable>> result = new ArrayList<>();
    detectAnnotationExceptionMappings(method, result);
    if (result.isEmpty()) {
      for (Class<?> paramType : method.getParameterTypes()) {
        if (Throwable.class.isAssignableFrom(paramType)) {
          result.add((Class<? extends Throwable>) paramType);
        }
      }
    }
    if (result.isEmpty()) {
      throw new IllegalStateException("No exception types mapped to " + method);
    }
    return result;
  }

  private void detectAnnotationExceptionMappings(Method method, List<Class<? extends Throwable>> result) {
    ExceptionHandler ann = AnnotatedElementUtils.findMergedAnnotation(method, ExceptionHandler.class);
    Assert.state(ann != null, "No ExceptionHandler annotation");
    result.addAll(Arrays.asList(ann.value()));
  }

  private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {
    Method oldMethod = this.mappedMethods.put(exceptionType, method);
    if (oldMethod != null && !oldMethod.equals(method)) {
      throw new IllegalStateException("Ambiguous @ExceptionHandler method mapped for ["
          + exceptionType + "]: {" + oldMethod + ", " + method + "}");
    }
  }

  public boolean hasExceptionMappings() {
    return !this.mappedMethods.isEmpty();
  }

  @Nullable
  public Method resolveMethod(Exception exception) {
    return resolveMethodByThrowable(exception);
  }


  @Nullable
  public Method resolveMethodByThrowable(Throwable exception) {
    Method method = resolveMethodByExceptionType(exception.getClass());
    if (method == null) {
      Throwable cause = exception.getCause();
      if (cause != null) {
        method = resolveMethodByThrowable(cause);
      }
    }
    return method;
  }

  @Nullable
  public Method resolveMethodByExceptionType(Class<? extends Throwable> exceptionType) {
    Method method = this.exceptionLookupCache.get(exceptionType);
    if (method == null) {
      method = getMappedMethod(exceptionType);
      this.exceptionLookupCache.put(exceptionType, method);
    }
    return (method != NO_MATCHING_EXCEPTION_HANDLER_METHOD ? method : null);
  }

  private Method getMappedMethod(Class<? extends Throwable> exceptionType) {
    List<Class<? extends Throwable>> matches = new ArrayList<>();
    for (Class<? extends Throwable> mappedException : this.mappedMethods.keySet()) {
      if (mappedException.isAssignableFrom(exceptionType)) {
        matches.add(mappedException);
      }
    }
    if (!matches.isEmpty()) {
      if (matches.size() > 1) {
        matches.sort(new ExceptionDepthComparator(exceptionType));
      }
      return this.mappedMethods.get(matches.get(0));
    } else {
      return NO_MATCHING_EXCEPTION_HANDLER_METHOD;
    }
  }

  @SuppressWarnings("unused")
  private void noMatchingExceptionHandler() {
  }
}
