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

package dev.niubi.problem.spring.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.ExceptionDepthComparator;
import org.springframework.core.ResolvableType;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * 异常转换的查找类
 */
public class ExceptionConverterResolver {

  private final Map<Class<? extends Throwable>, ExceptionConverter<?>> mappedMethods =
      new HashMap<>(16);

  private final Map<Class<? extends Throwable>, ExceptionConverter<?>> exceptionLookupCache =
      new ConcurrentReferenceHashMap<>(16);

  public ExceptionConverterResolver(List<ExceptionConverter<?>> converters) {
    for (ExceptionConverter<?> converter : converters) {
      addConverter(converter);
    }
  }

  public ExceptionConverterResolver() {
  }

  public void addConverter(ExceptionConverter<? extends Throwable> converter) {
    Class<? extends Throwable> typeInfo;
    try {
      typeInfo = getRequiredTypeInfo(converter.getClass(),
          ExceptionConverter.class);
      if (typeInfo == null && converter instanceof DecoratingProxy) {
        typeInfo = getRequiredTypeInfo(((DecoratingProxy) converter).getDecoratedClass(),
            ExceptionConverter.class);
      }
    } catch (TypeNotPresentException e) {
      return;
    }
    if (typeInfo == null) {
      throw new IllegalArgumentException("异常转换接口定义错误");
    }

    addExceptionMapping(typeInfo, converter);
  }

  @SuppressWarnings({"SameParameterValue", "unchecked"})
  @Nullable
  private Class<? extends Throwable> getRequiredTypeInfo(Class<?> converterClass,
      Class<?> genericIfc) throws TypeNotPresentException {

    ResolvableType resolvableType = ResolvableType.forClass(converterClass).as(genericIfc);
    ResolvableType[] generics = resolvableType.getGenerics();
    if (generics.length < 1) {
      return null;
    }
    Class<?> sourceClass = generics[0].resolve();
    if (sourceClass == null || !Throwable.class.isAssignableFrom(sourceClass)) {
      return null;
    }
    return (Class<? extends Throwable>) sourceClass;
  }

  private void addExceptionMapping(Class<? extends Throwable> exceptionType,
      ExceptionConverter<?> converter) {
    this.mappedMethods.put(exceptionType, converter);
  }

  @SuppressWarnings({"unchecked"})
  public ExceptionConverter<Throwable> resolveConverterByThrowable(Throwable exception) {
    ExceptionConverter<?> converter = resolveConverterByExceptionType(exception.getClass());
    if (converter == null) {
      Throwable cause = exception.getCause();
      if (cause != null) {
        converter = resolveConverterByThrowable(cause);
      }
    }
    return (ExceptionConverter<Throwable>) converter;
  }

  @Nullable
  public ExceptionConverter<?> resolveConverterByExceptionType(
      Class<? extends Throwable> exceptionType) {
    ExceptionConverter<?> converter = this.exceptionLookupCache.get(exceptionType);
    if (converter == null) {
      converter = getMappedConverter(exceptionType);
      this.exceptionLookupCache.put(exceptionType, converter);
    }
    return converter;
  }

  private ExceptionConverter<?> getMappedConverter(Class<? extends Throwable> exceptionType) {
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
      return null;
    }
  }
}
