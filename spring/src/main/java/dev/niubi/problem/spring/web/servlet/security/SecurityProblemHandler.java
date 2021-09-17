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

package dev.niubi.problem.spring.web.servlet.security;

import dev.niubi.problem.spring.web.servlet.ProblemHandlerExceptionResolver;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.servlet.ModelAndView;

public class SecurityProblemHandler implements AccessDeniedHandler, AuthenticationEntryPoint,
    AuthenticationFailureHandler {

  private final ProblemHandlerExceptionResolver problemHandlerExceptionResolver;

  public SecurityProblemHandler(
      ProblemHandlerExceptionResolver problemHandlerExceptionResolver) {
    this.problemHandlerExceptionResolver = problemHandlerExceptionResolver;
  }

  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) {
    render(request, response, authException);
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) {
    render(request, response, accessDeniedException);
  }

  @Override
  public void onAuthenticationFailure(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception) {
    render(request, response, exception);
  }

  private void render(HttpServletRequest request, HttpServletResponse response, Exception ex) {
    ModelAndView mv = problemHandlerExceptionResolver.resolveException(request, response, null, ex);
    if (mv == null) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    try {
      response.setStatus(Optional.ofNullable(mv.getStatus()).map(HttpStatus::value)
          .orElse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
      Objects.requireNonNull(mv.getView()).render(mv.getModel(), request, response);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
