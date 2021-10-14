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
import dev.niubi.problem.spring.HttpStatusAdapter;
import dev.niubi.problem.spring.Problems.Http;
import dev.niubi.problem.spring.ResponseProblem;
import dev.niubi.problem.spring.web.ProblemAdvice;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

@ProblemAdvice
public interface WebFluxProblemAdvice {

  @ExceptionHandler(MethodNotAllowedException.class)
  default ResponseProblem handleMethodNotAllowed(MethodNotAllowedException exception) {
    Problem problem = Http.METHOD_NOT_ALLOWED;
    final Set<HttpMethod> methods = exception.getSupportedMethods();
    return ResponseProblem.with(problem)
        .withProperties(map -> {
          if (!methods.isEmpty()) {
            map.put("allow", methods);
            map.put("method", exception.getHttpMethod());
          }
        })
        .withHeader(headers -> {
          if (!methods.isEmpty()) {
            headers.setAllow(methods);
          }
        });
  }

  @ExceptionHandler(NotAcceptableStatusException.class)
  default Problem handleNotAcceptableStatus(NotAcceptableStatusException exception) {
    List<MediaType> supportedMediaTypes = exception.getSupportedMediaTypes();
    return ResponseProblem.with(Http.NOT_ACCEPTABLE)
        .withProperties(map -> map.put("accept", supportedMediaTypes))
        .withHeader(headers -> headers.setAccept(supportedMediaTypes));
  }

  @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
  default ResponseProblem handleUnsupportedMediaTypeStatus(UnsupportedMediaTypeStatusException exception) {
    List<MediaType> supportedMediaTypes = exception.getSupportedMediaTypes();
    return ResponseProblem.with(Http.UNSUPPORTED_MEDIA_TYPE)
        .withProperties(map -> map.put("accept", supportedMediaTypes))
        .withHeader(headers -> headers.setAccept(supportedMediaTypes));
  }

  @ExceptionHandler(ResponseStatusException.class)
  default Problem handleResponseStatus(ResponseStatusException exception) {
    HttpStatus status = exception.getStatus();
    return Problem.statusOnly(new HttpStatusAdapter(status));
  }

  @ExceptionHandler(ServerWebInputException.class)
  default Problem handleServerWebInput(ServerWebInputException exception) {
    return Http.BAD_REQUEST;
  }
}
