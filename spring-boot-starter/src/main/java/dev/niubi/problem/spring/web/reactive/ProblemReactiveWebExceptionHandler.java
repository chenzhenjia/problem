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

package dev.niubi.problem.spring.web.reactive;

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.convert.ExceptionConverterManager;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ProblemReactiveWebExceptionHandler implements ErrorWebExceptionHandler {

  private final ExceptionConverterManager exceptionConverterManager;
  private ReactiveProblemFunction problemFunction = (exchange, problem) -> problem;
  private List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();

  public ProblemReactiveWebExceptionHandler(
      ExceptionConverterManager exceptionConverterManager) {
    this.exceptionConverterManager = exceptionConverterManager;
  }

  public void setMessageWriters(
      List<HttpMessageWriter<?>> messageWriters) {
    this.messageWriters = messageWriters;
  }

  public void setProblemFunction(
      ReactiveProblemFunction problemFunction) {
    if (problemFunction == null) {
      return;
    }
    this.problemFunction = problemFunction;
  }

  @Override
  @NotNull
  public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NotNull Throwable ex) {
    if (exceptionConverterManager == null) {
      return Mono.error(ex);
    }
    ResponseEntity<Problem> responseEntity = exceptionConverterManager.convert(ex);
    if (responseEntity == null || responseEntity.getBody() == null) {
      return Mono.error(ex);
    }
    return writeResponse(exchange, responseEntity);
  }

  protected Mono<Void> writeResponse(ServerWebExchange exchange,
      ResponseEntity<Problem> responseEntity) {
    HttpStatus status = responseEntity.getStatusCode();
    Problem body = responseEntity.getBody();
    return ServerResponse
        .status(status)
        .headers(h -> {
          HttpHeaders headers = responseEntity.getHeaders();
          h.addAll(headers);
          h.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        })
        .body(BodyInserters.fromValue(problemFunction.apply(exchange, body)))
        .flatMap(response -> {
          exchange.getResponse().getHeaders().putAll(response.headers());
          return response.writeTo(exchange, new ResponseContext());
        })
        ;
  }

  private class ResponseContext implements ServerResponse.Context {

    @Override
    @NonNull
    public List<HttpMessageWriter<?>> messageWriters() {
      return ProblemReactiveWebExceptionHandler.this.messageWriters;
    }

    @Override
    @NonNull
    public List<ViewResolver> viewResolvers() {
      return Collections.emptyList();
    }
  }
}
