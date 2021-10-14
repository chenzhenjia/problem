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

import dev.niubi.problem.ProblemStatus;
import dev.niubi.problem.spring.ResponseProblem;
import dev.niubi.problem.spring.web.ProblemAdviceManager;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ProblemWebExceptionHandler implements ErrorWebExceptionHandler {

  private final ProblemAdviceManager problemAdviceManager;
  private ReactiveProblemConsumer problemFunction = (exchange, problem) -> {
  };
  private List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();

  public ProblemWebExceptionHandler(ProblemAdviceManager problemAdviceManager) {
    this.problemAdviceManager = problemAdviceManager;
  }

  public void setMessageWriters(
      List<HttpMessageWriter<?>> messageWriters) {
    this.messageWriters = messageWriters;
  }

  public void setProblemFunction(
      ReactiveProblemConsumer problemFunction) {
    if (problemFunction == null) {
      return;
    }
    this.problemFunction = problemFunction;
  }

  @Override
  @NotNull
  public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NotNull Throwable ex) {
    if (problemAdviceManager == null) {
      return Mono.error(ex);
    }
    ResponseProblem responseProblem = problemAdviceManager.handleProblem(ex);
    if (responseProblem == null) {
      return Mono.error(ex);
    }
    return writeResponse(exchange, responseProblem);
  }

  protected Mono<Void> writeResponse(ServerWebExchange exchange,
      ResponseProblem responseProblem) {
    problemFunction.accept(exchange, responseProblem);
    int statusCode = Optional.ofNullable(responseProblem.getStatus())
        .orElse(ProblemStatus.INTERNAL_SERVER_ERROR)
        .value();
    return ServerResponse
        .status(statusCode)
        .headers(h -> {
          HttpHeaders headers = responseProblem.getHeaders();
          h.addAll(headers);
          h.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        })
        .body(BodyInserters.fromValue(responseProblem))
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
      return ProblemWebExceptionHandler.this.messageWriters;
    }

    @Override
    @NonNull
    public List<ViewResolver> viewResolvers() {
      return Collections.emptyList();
    }
  }
}
