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
import dev.niubi.problem.ProblemStatus;
import dev.niubi.problem.Status;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.ResponseEntity;

/**
 * 把异常类转为 {@code ResponseEntity<Problem>}
 *
 * @param <T> 异常类
 */
public interface ExceptionConverter<T extends Throwable> extends
    Converter<T, ResponseEntity<Problem>> {

  @NotNull
  default ResponseEntity<Problem> convert(@NotNull T throwable) {
    Problem problem = toProblem(throwable);
    Status status = Optional.ofNullable(problem.getStatus())
        .orElse(ProblemStatus.INTERNAL_SERVER_ERROR);
    return ResponseEntity.status(status.value()).body(problem);
  }

  Problem toProblem(T throwable);
}
