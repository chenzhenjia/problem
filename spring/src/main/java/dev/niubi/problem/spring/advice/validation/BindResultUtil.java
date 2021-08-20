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

package dev.niubi.problem.spring.advice.validation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class BindResultUtil {

  private static final BindResultUtil INSTANCE = new BindResultUtil();

  public static BindResultUtil getInstance() {
    return INSTANCE;
  }

  private InvalidParameter createFieldError(final MessageSource messageSource,
      final org.springframework.validation.FieldError fieldError) {
    String message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
    return new InvalidParameter(fieldError.getField(), message);
  }

  private InvalidParameter createObjectError(final MessageSource messageSource, final ObjectError objectError) {
    String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
    return new InvalidParameter(objectError.getObjectName(), message);
  }

  protected InvalidParameters createInvalidParameters(final MessageSource messageSource, BindingResult bindingResult) {
    final Stream<InvalidParameter> fieldErrors = bindingResult.getFieldErrors().stream()
        .map(fieldError -> this.createFieldError(messageSource, fieldError));
    final Stream<InvalidParameter> globalErrors = bindingResult.getGlobalErrors().stream()
        .map(objectError -> this.createObjectError(messageSource, objectError));
    List<InvalidParameter> errors = Stream.concat(fieldErrors, globalErrors)
        .collect(Collectors.toList());
    return new InvalidParameters(errors);
  }
}
