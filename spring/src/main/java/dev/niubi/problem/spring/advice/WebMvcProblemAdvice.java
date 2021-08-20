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
import dev.niubi.problem.spring.Problems.Http;
import dev.niubi.problem.spring.Problems.Servlet;
import dev.niubi.problem.spring.ResponseProblem;
import dev.niubi.problem.spring.web.ProblemAdvice;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ProblemAdvice
public interface WebMvcProblemAdvice {

  @ExceptionHandler(MissingServletRequestPartException.class)
  default Problem handleMissingServletRequestPart(MissingServletRequestPartException exception) {
    return Servlet.MISSING_REQUEST_PART.withDetail(exception.getMessage())
        .withProperties(map -> map.put("requestPartName", exception.getRequestPartName()));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  default Problem handleNoHandlerFound(NoHandlerFoundException exception) {
    return Servlet.NO_HANDLER_FOUND.withDetail(exception.getMessage()).withProperties(map -> {
      map.put("requestURL", exception.getRequestURL());
      map.put("method", exception.getHttpMethod());
    });
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  default Problem handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException exception) {
    return ResponseProblem.with(Http.METHOD_NOT_ALLOWED)
        .withProperties(map -> {
          map.put("allow", exception.getSupportedHttpMethods());
          map.put("method", exception.getMethod());
        })
        .withHeader(headers -> {
          Set<HttpMethod> supportedMethodSet = exception.getSupportedHttpMethods();

          if (supportedMethodSet != null && !supportedMethodSet.isEmpty()) {
            headers.setAllow(supportedMethodSet);
          }
        });
  }

  @ExceptionHandler(HttpMediaTypeException.class)
  default Problem handleHttpMediaType(
      HttpMediaTypeException exception) {
    return ResponseProblem.with(Http.UNSUPPORTED_MEDIA_TYPE).withHeader(headers -> {
      List<MediaType> supportedMediaTypes = exception.getSupportedMediaTypes();
      headers.setAccept(supportedMediaTypes);
    });
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  default Problem handleHttpMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException exception) {
    return ResponseProblem.with(Http.NOT_ACCEPTABLE)
        .withHeader(headers -> {
          List<MediaType> supportedMediaTypes = exception.getSupportedMediaTypes();
          headers.setAccept(supportedMediaTypes);
        });
  }

  @ExceptionHandler(ServletException.class)
  default Problem handleServlet(
      ServletException exception) {
    return Http.BAD_REQUEST;
  }

  @ExceptionHandler(UnavailableException.class)
  default Problem handleUnavailableException(
      UnavailableException exception) {
    return Http.UNAVAILABLE;
  }

  @ExceptionHandler(ServletRequestBindingException.class)
  default Problem handleServletRequestBinding(ServletRequestBindingException exception) {
    Problem problem;
    if (exception instanceof MissingRequestValueException) {
      problem = Servlet.MISSING_REQUEST_VALUE;
    } else {
      problem = Servlet.REQUEST_BINDING;
    }
    return problem.withDetail(exception.getMessage());
  }
}
