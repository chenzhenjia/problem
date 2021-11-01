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

import dev.niubi.problem.Problem;
import dev.niubi.problem.ProblemStatus;
import dev.niubi.problem.spring.ResponseProblem;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMethodExceptionResolver;

@Controller
@RequestMapping("${dev.niubi.problem.servlet.path:${problem.path:/problem}}")
public class ProblemErrorController extends AbstractHandlerMethodExceptionResolver {

  private final HandlerExceptionProblemResolver handlerExceptionProblemResolver;
  private final String defaultErrorPath;

  public ProblemErrorController(
      HandlerExceptionProblemResolver handlerExceptionProblemResolver, String defaultErrorPath) {
    this.handlerExceptionProblemResolver = handlerExceptionProblemResolver;
    this.defaultErrorPath = defaultErrorPath;
    setWarnLogCategory(ProblemErrorController.class.getName());
  }

  public ProblemErrorController(
      HandlerExceptionProblemResolver handlerExceptionProblemResolver) {
    this(handlerExceptionProblemResolver, "/error");
  }

  @RequestMapping
  public ResponseEntity<Problem> error(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Throwable throwable = getError(new ServletWebRequest(request));
    if (throwable == null) {
      request.getRequestDispatcher(defaultErrorPath).forward(request, response);
      return null;
    }
    ResponseProblem responseProblem = handlerExceptionProblemResolver.resolveProblem(request, response, throwable);
    if (responseProblem == null) {
      request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, throwable);
      request.getRequestDispatcher(defaultErrorPath).forward(request, response);
      return null;
    }
    int statusCode = Optional.ofNullable(responseProblem.getStatus())
        .orElse(ProblemStatus.INTERNAL_SERVER_ERROR)
        .value();
    return ResponseEntity.status(statusCode)
        .headers(responseProblem.getHeaders())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(responseProblem);
  }

  private Throwable getError(WebRequest webRequest) {
    return getAttribute(webRequest, RequestDispatcher.ERROR_EXCEPTION);
  }


  @SuppressWarnings("unchecked")
  private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
    return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
  }

  @Override
  protected ModelAndView doResolveHandlerMethodException(
      HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handlerMethod, @Nullable Exception ex) {
    return handlerExceptionProblemResolver.resolveView(request, response, ex);
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
