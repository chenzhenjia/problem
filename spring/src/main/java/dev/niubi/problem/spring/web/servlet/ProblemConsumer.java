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

package dev.niubi.problem.spring.web.servlet;

import dev.niubi.problem.spring.ResponseProblem;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.servlet.http.HttpServletRequest;

public interface ProblemConsumer extends BiConsumer<HttpServletRequest, ResponseProblem> {

  default ProblemConsumer andThen(ProblemConsumer after) {
    Objects.requireNonNull(after);
    return (l, r) -> {
      accept(l, r);
      after.accept(l, r);
    };
  }
}
