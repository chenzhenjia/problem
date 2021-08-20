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

package dev.niubi.problem.spring.convert.validation;

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.Problems.Validation;
import dev.niubi.problem.spring.convert.ExceptionConverter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

abstract class BindResultExceptionConverter<T extends Throwable> implements
    ExceptionConverter<T> {

  private final MessageSource messageSource;

  protected BindResultExceptionConverter(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public Problem toProblem(
      T ex) {
    BindingResult bindingResult = getBindingResult(ex);
    final Stream<InvalidParameter> fieldErrors = bindingResult.getFieldErrors().stream()
        .map(this::createFieldError);
    final Stream<InvalidParameter> globalErrors = bindingResult.getGlobalErrors().stream()
        .map(this::createFieldError);
    List<InvalidParameter> errors = Stream.concat(fieldErrors, globalErrors)
        .collect(Collectors.toList());
    InvalidParameters invalidParameters = new InvalidParameters(errors);
    return Validation.VALID_FAILED.withProperties(invalidParameters);
  }

  abstract BindingResult getBindingResult(T ex);

  private InvalidParameter createFieldError(
      final org.springframework.validation.FieldError fieldError) {
    String message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
    return new InvalidParameter(fieldError.getField(), message);
  }

  private InvalidParameter createFieldError(final ObjectError objectError) {
    String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
    return new InvalidParameter(objectError.getObjectName(), message);
  }
}
