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
import dev.niubi.problem.ProblemException;
import dev.niubi.problem.ProblemStatus;
import dev.niubi.problem.spring.ResponseProblem;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  private static final String ERROR_INTERNAL_ATTRIBUTE = ProblemErrorController.class.getName() + ".ERROR";
  private final HandlerExceptionProblemResolver handlerExceptionProblemResolver;
  private final String defaultErrorPath;
  private final String problemPath;

  public ProblemErrorController(
      HandlerExceptionProblemResolver handlerExceptionProblemResolver,
      String problemPath, String defaultErrorPath) {
    this.handlerExceptionProblemResolver = handlerExceptionProblemResolver;
    this.problemPath = problemPath;
    this.defaultErrorPath = defaultErrorPath;
  }

  public ProblemErrorController(
      HandlerExceptionProblemResolver handlerExceptionProblemResolver) {
    this(handlerExceptionProblemResolver, "/problem", "/error");
  }

  @RequestMapping
  public ResponseEntity<Problem> error(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Throwable throwable = getError(new ServletWebRequest(request));
    if (throwable == null) {
      response.sendRedirect(defaultErrorPath);
      return null;
    }
    ResponseProblem responseProblem = handlerExceptionProblemResolver.resolveProblem(request, response, throwable);
    if (responseProblem == null) {
      request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, throwable);
      response.sendRedirect(defaultErrorPath);
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
    Throwable exception = getAttribute(webRequest, ERROR_INTERNAL_ATTRIBUTE);
    if (exception == null) {
      exception = getAttribute(webRequest, RequestDispatcher.ERROR_EXCEPTION);
    }
    return exception;
  }


  @SuppressWarnings("unchecked")
  private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
    return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
  }

  @Override
  protected ModelAndView doResolveHandlerMethodException(
      HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handlerMethod, Exception ex) {

    request.setAttribute(ERROR_INTERNAL_ATTRIBUTE, ex);
    if (ex != null) {
      if (ex instanceof ProblemException) {
        return handlerExceptionProblemResolver.resolveView(request, response, ex);
      } else {
        try {
          response.sendRedirect(problemPath);
        } catch (IOException e) {
          return handlerExceptionProblemResolver.resolveView(request, response, e);
        }
      }
    }

    return null;
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
