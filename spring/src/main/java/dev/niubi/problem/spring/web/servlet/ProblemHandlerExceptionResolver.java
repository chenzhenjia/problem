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
import dev.niubi.problem.ProblemStatus;
import dev.niubi.problem.spring.ResponseProblem;
import dev.niubi.problem.spring.web.ProblemAdviceManager;
import java.util.Optional;
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMethodExceptionResolver;

public class ProblemHandlerExceptionResolver extends AbstractHandlerMethodExceptionResolver {

  private final ProblemAdviceManager problemAdviceManager;
  private final ObjectMapper objectMapper;
  private Predicate<HttpServletRequest> predicate = request -> true;
  private ProblemConsumer problemConsumer = (request, problem) -> {
  };

  public ProblemHandlerExceptionResolver(ProblemAdviceManager problemAdviceManager, ObjectMapper objectMapper) {
    this.problemAdviceManager = problemAdviceManager;
    this.objectMapper = objectMapper;
    setOrder(Ordered.HIGHEST_PRECEDENCE);
  }

  public void setProblemFunction(ProblemConsumer problemConsumer) {
    if (problemConsumer == null) {
      return;
    }
    this.problemConsumer = problemConsumer;
  }

  public void setPredicate(Predicate<HttpServletRequest> predicate) {
    if (predicate == null) {
      return;
    }
    this.predicate = predicate;
  }

  @Override
  protected ModelAndView doResolveHandlerMethodException(
      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
      HandlerMethod handlerMethod, @Nullable Exception ex) {
    if (ex == null) {
      return null;
    }
    if (!predicate.test(request)) {
      return null;
    }
    ResponseProblem responseProblem = problemAdviceManager.handleProblem(handlerMethod, ex);
    if (responseProblem == null) {
      return null;
    }
    problemConsumer.accept(request, responseProblem);
    ProblemJsonView view = new ProblemJsonView(objectMapper, responseProblem.getHeaders());
    ModelAndView mv = new ModelAndView(view);
    int value = Optional.ofNullable(responseProblem.getStatus()).orElse(ProblemStatus.INTERNAL_SERVER_ERROR).value();
    mv.setStatus(HttpStatus.valueOf(value));
    mv.addObject(Problem.class.getName(), responseProblem);
    return mv;
  }
}
