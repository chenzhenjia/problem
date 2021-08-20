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

package dev.niubi.problem.spring.convert.http;

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.Problems.Web;
import dev.niubi.problem.spring.convert.ExceptionConverter;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.MethodNotAllowedException;

public class MethodNotAllowedExceptionConverter implements
    ExceptionConverter<MethodNotAllowedException> {

  @NotNull
  @Override
  public ResponseEntity<Problem> convert(@NotNull MethodNotAllowedException exception) {
    Problem problem = toProblem(exception);
    final Set<HttpMethod> methods = exception.getSupportedMethods();
    HttpHeaders headers = new HttpHeaders();
    if (!methods.isEmpty()) {
      headers.setAllow(methods);
    }
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).body(problem);
  }

  @Override
  public Problem toProblem(@NotNull MethodNotAllowedException throwable) {
    return Web.METHOD_NOT_ALLOWED;
  }
}
