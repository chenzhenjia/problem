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
import dev.niubi.problem.ProblemException;
import dev.niubi.problem.ProblemStatus;
import dev.niubi.problem.spring.Problems.General;
import dev.niubi.problem.spring.ResponseProblem;
import dev.niubi.problem.spring.web.ProblemAdviceManager;
import java.util.Optional;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("${dev.niubi.problem.servlet.path:${problem.path:/problem}}")
public class ProblemErrorController {

  private final ProblemAdviceManager problemAdviceManager;

  public ProblemErrorController(ProblemAdviceManager problemAdviceManager) {
    this.problemAdviceManager = problemAdviceManager;
  }

  @RequestMapping
  public ResponseEntity<Problem> error(HttpServletRequest request) {
    Throwable throwable = getError(new ServletWebRequest(request));
    Throwable throwable1 = Optional.ofNullable(throwable)
        .orElseGet(() -> new ProblemException(General.INTERNAL_SERVER_ERROR));
    ResponseProblem responseProblem = problemAdviceManager.handleProblem(throwable1);
    if (responseProblem == null) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    int statusCode = Optional.ofNullable(responseProblem.getStatus())
        .orElse(ProblemStatus.INTERNAL_SERVER_ERROR)
        .value();
    return ResponseEntity.status(statusCode)
        .headers(responseProblem.getHeaders())
        .body(responseProblem);
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
