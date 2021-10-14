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

package dev.niubi.problem.spring.web.i18n;

import dev.niubi.problem.Problem;
import dev.niubi.problem.Problem.ExtendedProblem;
import dev.niubi.problem.spring.ResponseProblem;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;

public abstract class AbstractMessageSourceProblemFunction<T> implements BiConsumer<T, ResponseProblem> {

  private static final Pattern REGEX = Pattern.compile("^\\{(.+)}$");

  private final MessageSource messageSource;

  public AbstractMessageSourceProblemFunction(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Nullable
  private Object[] buildArgs(Problem problem) {
    if (!(problem instanceof ExtendedProblem)) {
      return null;
    }
    Stream<Object> stream1 = Optional.ofNullable(
            ((ExtendedProblem) problem).getProperties())
        .map(Map::values)
        .map(Collection::stream)
        .orElse(Stream.empty());
    Stream<?> stream2 = Optional.ofNullable(
            ((ExtendedProblem) problem).getExtendedProperties())
        .map(Stream::of)
        .orElse(Stream.empty());
    return Stream.concat(stream1, stream2)
        .toArray();
  }


  private String getMessage(String s, Object[] args, String defaultMessage, Locale locale) {
    if (s == null) {
      return null;
    }
    Matcher matcher = REGEX.matcher(s);
    if (matcher.matches()) {
      String code = matcher.group(1);
      return messageSource.getMessage(code, args, defaultMessage, locale);
    }
    return s;
  }

  @Override
  public void accept(T t, ResponseProblem problem) {
    Locale locale = getLocale(t);
    Object[] args = buildArgs(problem);
    if (problem.getTitle() != null) {
      String title = getMessage(problem.getTitle(), args, null, locale);
      problem.withTitle(title);
    }
    if (problem.getDetail() != null) {
      String detail = getMessage(problem.getDetail(), args, null, locale);
      problem.withDetail(detail);
    }
  }

  protected abstract Locale getLocale(T t);
}
