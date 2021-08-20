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

package dev.niubi.problem.spring.advice;

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.Problems.General;
import dev.niubi.problem.spring.web.ProblemAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ProblemAdvice
public interface ThrowableProblemAdvice {

  @ExceptionHandler(Throwable.class)
  default Problem handleThrowable(Throwable throwable) {
    return General.INTERNAL_SERVER_ERROR.withDetail(throwable.getMessage());
  }
}
