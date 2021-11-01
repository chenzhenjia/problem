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

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.Problems.Validation;
import dev.niubi.problem.spring.web.ProblemAdvice;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

@ProblemAdvice
public interface BindResultProblemAdvice {

  MessageSource getMessageSource();

  @ExceptionHandler(BindException.class)
  default Problem handleBind(BindException exception) {
    InvalidParameters invalidParameters = BindResultUtil.getInstance()
        .createInvalidParameters(getMessageSource(), exception.getBindingResult());

    return Validation.VALID_FAILED.withProperties(invalidParameters);
  }

  @ExceptionHandler(WebExchangeBindException.class)
  default Problem handleWebExchangeBind(WebExchangeBindException exception) {
    InvalidParameters invalidParameters = BindResultUtil.getInstance()
        .createInvalidParameters(getMessageSource(), exception.getBindingResult());
    return Validation.VALID_FAILED.withProperties(invalidParameters);
  }
}
