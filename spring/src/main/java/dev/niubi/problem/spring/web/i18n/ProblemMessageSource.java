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

import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

public class ProblemMessageSource extends ResourceBundleMessageSource {

  @Nullable
  private final MessageSource messageSource;

  public ProblemMessageSource(@Nullable MessageSource messageSource) {
    this.messageSource = messageSource;
    setBasename("dev.niubi.problem.messages");
  }

  public static MessageSource getMessageSource() {
    return new ProblemMessageSource(null);
  }

  @Override
  protected String getMessageInternal(String code, Object[] args, Locale locale) {
    String msg = null;
    if (messageSource != null) {
      msg = messageSource.getMessage(code, args, null, locale);
    }
    if (msg != null) {
      return msg;
    }
    return super.getMessageInternal(code, args, locale);
  }

}
