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

import static java.util.stream.Collectors.toList;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class ConstraintViolationProblemUtil {

  private static final Set<String> internalAnnotationAttributes = new HashSet<>(4);
  private static final ConstraintViolationProblemUtil INSTANCE = new ConstraintViolationProblemUtil();

  static {
    internalAnnotationAttributes.add("message");
    internalAnnotationAttributes.add("groups");
    internalAnnotationAttributes.add("payload");
  }

  public static ConstraintViolationProblemUtil getInstance() {
    return INSTANCE;
  }

  protected InvalidParameters createInvalidParameters(MessageSource messageSource,
      Set<ConstraintViolation<?>> constraintViolations) {
    final List<InvalidParameter> invalidParameters = constraintViolations
        .stream()
        .map(constraintViolation -> createFieldError(messageSource, constraintViolation))
        .collect(toList());
    return new InvalidParameters(invalidParameters);
  }

  protected InvalidParameter createFieldError(final MessageSource messageSource,
      final ConstraintViolation<?> violation) {
    ConstraintDescriptor<?> cd = violation.getConstraintDescriptor();
    String field = violation.getPropertyPath().toString();
    String code = determineErrorCode(cd);
    Object[] errorArgs = getArgumentsForConstraint(field, cd);
    String message = messageSource.getMessage(code, errorArgs, violation.getMessage(),
        LocaleContextHolder.getLocale());
    return new InvalidParameter(field, message);
  }

  protected String determineErrorCode(ConstraintDescriptor<?> descriptor) {
    return descriptor.getAnnotation().annotationType().getSimpleName();
  }

  protected Object[] getArgumentsForConstraint(String field, ConstraintDescriptor<?> descriptor) {
    List<Object> arguments = new ArrayList<>();
    arguments.add(getResolvableField(field));
    // Using a TreeMap for alphabetical ordering of attribute names
    Map<String, Object> attributesToExpose = new TreeMap<>();

    descriptor.getAttributes().forEach((attributeName, attributeValue) -> {
      if (!internalAnnotationAttributes.contains(attributeName)) {
        if (attributeValue instanceof String) {
          attributeValue = new ResolvableAttribute(attributeValue.toString());
        }
        attributesToExpose.put(attributeName, attributeValue);
      }
    });
    arguments.addAll(attributesToExpose.values());
    return arguments.toArray();
  }

  protected MessageSourceResolvable getResolvableField(String field) {
    String[] codes = new String[] {field, field};
    return new DefaultMessageSourceResolvable(codes, field);
  }

  protected static class ResolvableAttribute implements MessageSourceResolvable, Serializable {

    private final String resolvableString;

    public ResolvableAttribute(String resolvableString) {
      this.resolvableString = resolvableString;
    }

    @Override
    public String[] getCodes() {
      return new String[] {this.resolvableString};
    }

    @Override
    @Nullable
    public Object[] getArguments() {
      return null;
    }

    @Override
    public String getDefaultMessage() {
      return this.resolvableString;
    }

    @Override
    public String toString() {
      return this.resolvableString;
    }
  }
}
