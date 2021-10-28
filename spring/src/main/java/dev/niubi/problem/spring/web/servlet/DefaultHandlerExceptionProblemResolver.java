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
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;

public class DefaultHandlerExceptionProblemResolver implements HandlerExceptionProblemResolver {

  private static final Predicate<HttpServletRequest> NOOP_PREDICATE = request -> true;
  private static final ProblemConsumer NOOP_CONSUMER = (request, problem) -> {
  };
  private final ProblemAdviceManager problemAdviceManager;
  private final ObjectMapper objectMapper;
  private final Predicate<HttpServletRequest> predicate;
  private final ProblemConsumer problemConsumer;

  public DefaultHandlerExceptionProblemResolver(ProblemAdviceManager problemAdviceManager, ObjectMapper objectMapper) {
    this(problemAdviceManager, objectMapper, null, null);
  }

  public DefaultHandlerExceptionProblemResolver(ProblemAdviceManager problemAdviceManager, ObjectMapper objectMapper,
      ProblemConsumer problemConsumer, Predicate<HttpServletRequest> predicate) {
    this.problemAdviceManager = problemAdviceManager;
    this.objectMapper = objectMapper;
    this.predicate = Optional.ofNullable(predicate).orElse(NOOP_PREDICATE);
    this.problemConsumer = Optional.ofNullable(problemConsumer).orElse(NOOP_CONSUMER);
  }


  @Override
  public ModelAndView resolveView(
      HttpServletRequest request, HttpServletResponse response, Throwable ex) {
    ResponseProblem responseProblem = resolveProblem(request, response, ex);
    if (responseProblem == null) {
      return null;
    }
    ProblemJsonView view = new ProblemJsonView(objectMapper, responseProblem.getHeaders());
    ModelAndView mv = new ModelAndView(view);
    int value = Optional.ofNullable(responseProblem.getStatus()).orElse(ProblemStatus.INTERNAL_SERVER_ERROR).value();
    mv.setStatus(HttpStatus.valueOf(value));
    mv.addObject(Problem.class.getName(), responseProblem);
    return mv;
  }

  @Nullable
  public ResponseProblem resolveProblem(
      HttpServletRequest request, HttpServletResponse response, Throwable ex) {
    if (ex == null) {
      return null;
    }
    if (!predicate.test(request)) {
      return null;
    }
    ResponseProblem responseProblem = problemAdviceManager.handleProblem(ex);
    if (responseProblem == null) {
      return null;
    }
    problemConsumer.accept(request, responseProblem);
    return responseProblem;
  }
}
