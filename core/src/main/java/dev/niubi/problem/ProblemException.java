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

package dev.niubi.problem;

import java.util.Optional;

public class ProblemException extends RuntimeException {

  private final Problem problem;
  private final String message;

  public ProblemException(Problem problem) {
    this.problem = problem;
    this.message = Optional.ofNullable(problem.getDetail())
        .orElseGet(() -> {
          if (problem.getTitle() != null) {
            return problem.getTitle();
          }
          return problem.toString();
        });
  }

  public Problem getProblem() {
    return problem;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
