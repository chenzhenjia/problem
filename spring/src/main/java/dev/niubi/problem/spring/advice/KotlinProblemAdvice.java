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

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException;
import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.Problems.Kotlin;
import dev.niubi.problem.spring.web.ProblemAdvice;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ProblemAdvice
public interface KotlinProblemAdvice {

  @ExceptionHandler(MissingKotlinParameterException.class)
  default Problem handleMissingKotlinParameter(MissingKotlinParameterException throwable) {
    List<Reference> path = throwable.getPath();
    List<String> nullFields = path.stream()
        .map(Reference::getFieldName)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    return Kotlin.MISSING_PARAMETER.withProperties(map -> map.put("null_fields", nullFields));
  }
}
