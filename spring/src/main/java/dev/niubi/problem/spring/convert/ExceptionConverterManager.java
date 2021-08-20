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

import dev.niubi.problem.Problem;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 异常转换的管理类
 */
public class ExceptionConverterManager implements
    Converter<Throwable, ResponseEntity<Problem>> {

  private final ExceptionConverterResolver exceptionConverterResolver;
  private Function<Problem, Problem> problemCustomizer = problem -> problem;

  public ExceptionConverterManager(List<ExceptionConverter<?>> converters) {
    this.exceptionConverterResolver = new ExceptionConverterResolver(converters);
  }

  public void setProblemCustomizer(
      Function<Problem, Problem> problemCustomizer) {
    this.problemCustomizer = problemCustomizer;
  }

  @Override
  public ResponseEntity<Problem> convert(@Nullable Throwable ex) {
    if (ex == null) {
      return null;
    }
    Throwable rootCause = NestedExceptionUtils.getRootCause(ex);
    if (rootCause == null) {
      rootCause = ex;
    }
    ExceptionConverter<Throwable> exceptionConverter = exceptionConverterResolver
        .resolveConverterByThrowable(rootCause);
    if (exceptionConverter == null) {
      return null;
    }

    ResponseEntity<Problem> responseEntity = exceptionConverter.convert(rootCause);
    Problem problem = responseEntity.getBody();
    if (problem == null) {
      return null;
    }

    Problem newProblem = problemCustomizer.apply(problem);
    return ResponseEntity.status(responseEntity.getStatusCode())
        .headers(responseEntity.getHeaders())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(newProblem);
  }
}
