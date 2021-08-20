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

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.convert.ExceptionConverterManager;
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

public class ProblemHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

  private final ExceptionConverterManager exceptionConverterManager;
  private final ObjectMapper objectMapper;
  private Predicate<HttpServletRequest> predicate = request -> true;
  private ProblemFunction problemFunction = (request, problem) -> problem;

  public ProblemHandlerExceptionResolver(
      ExceptionConverterManager exceptionConverterManager, ObjectMapper objectMapper) {
    this.exceptionConverterManager = exceptionConverterManager;
    this.objectMapper = objectMapper;
    setOrder(Ordered.HIGHEST_PRECEDENCE);
  }

  public void setProblemFunction(ProblemFunction problemFunction) {
    if (problemFunction == null) {
      return;
    }
    this.problemFunction = problemFunction;
  }

  public void setPredicate(Predicate<HttpServletRequest> predicate) {
    if (predicate == null) {
      return;
    }
    this.predicate = predicate;
  }

  @Override
  protected ModelAndView doResolveException(
      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
      Object handler, @Nullable Exception ex) {
    if (ex == null) {
      return null;
    }
    if (!predicate.test(request)) {
      return null;
    }
    ResponseEntity<Problem> responseEntity = exceptionConverterManager.convert(ex);
    if (responseEntity == null || responseEntity.getBody() == null) {
      return null;
    }
    Problem body = responseEntity.getBody();
    ProblemJsonView view = new ProblemJsonView(objectMapper, responseEntity.getHeaders());
    ModelAndView mv = new ModelAndView(view);
    mv.setStatus(responseEntity.getStatusCode());
    mv.addObject(Problem.class.getName(), problemFunction.apply(request, body));
    return mv;
  }
}
