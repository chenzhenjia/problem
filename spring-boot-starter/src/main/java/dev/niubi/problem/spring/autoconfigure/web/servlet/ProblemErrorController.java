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

package dev.niubi.problem.spring.autoconfigure.web.servlet;

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.convert.ExceptionConverterManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("${dev.niubi.problem.servlet.path:${problem.path:/problem}}")
public class ProblemErrorController {

  private final ExceptionConverterManager exceptionConverterManager;

  public ProblemErrorController(ExceptionConverterManager exceptionConverterManager) {
    this.exceptionConverterManager = exceptionConverterManager;
  }

  @RequestMapping
  public ResponseEntity<Problem> error(HttpServletRequest request) {
    Throwable throwable = getError(new ServletWebRequest(request));
    if (throwable != null) {
      ResponseEntity<Problem> response = exceptionConverterManager.convert(throwable);
      if (response != null) {
        return response;
      }
    }
    HttpStatus status = getStatus(request);
    if (status == HttpStatus.NO_CONTENT) {
      return new ResponseEntity<>(status);
    }
    return exceptionConverterManager.convert(new ResponseStatusException(status));
  }

  private Throwable getError(WebRequest webRequest) {
    return (Throwable) webRequest.getAttribute(RequestDispatcher.ERROR_EXCEPTION, RequestAttributes.SCOPE_REQUEST);
  }


  protected HttpStatus getStatus(HttpServletRequest request) {
    Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    if (statusCode == null) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    try {
      return HttpStatus.valueOf(statusCode);
    } catch (Exception ex) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
