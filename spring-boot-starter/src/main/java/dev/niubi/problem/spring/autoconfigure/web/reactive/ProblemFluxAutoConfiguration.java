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

package dev.niubi.problem.spring.autoconfigure.web.reactive;

import dev.niubi.problem.spring.autoconfigure.ProblemProperties;
import dev.niubi.problem.spring.web.ProblemAdviceManager;
import dev.niubi.problem.spring.web.reactive.ProblemReactiveWebExceptionHandler;
import dev.niubi.problem.spring.web.reactive.ReactiveMessageSourceProblemConsumer;
import dev.niubi.problem.spring.web.reactive.ReactiveProblemConsumer;
import dev.niubi.problem.spring.web.reactive.ResponseIdProblemConsumer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFluxConfigurer.class)
@AutoConfigureBefore(WebFluxAutoConfiguration.class)
public class ProblemFluxAutoConfiguration {

  private final ProblemProperties properties;
  private final ProblemAdviceManager problemAdviceManager;
  private final ServerCodecConfigurer serverCodecConfigurer;

  public ProblemFluxAutoConfiguration(
      ProblemProperties properties,
      ProblemAdviceManager problemAdviceManager,
      ServerCodecConfigurer serverCodecConfigurer) {
    this.properties = properties;
    this.problemAdviceManager = problemAdviceManager;
    this.serverCodecConfigurer = serverCodecConfigurer;
  }

  @Bean
  @ConditionalOnBean(MessageSource.class)
  @ConditionalOnProperty(value = "dev.niubi.problem.i18n", havingValue = "true")
  public ReactiveMessageSourceProblemConsumer reactiveMessageSourceProblemFunction(MessageSource messageSource) {
    return new ReactiveMessageSourceProblemConsumer(messageSource);
  }

  @Bean
  @Order(-2)
  public ErrorWebExceptionHandler problemReactiveWebExceptionHandler(
      ObjectProvider<ReactiveProblemConsumer> problemFunctionObjectProvider) {
    ProblemReactiveWebExceptionHandler exceptionHandler = new ProblemReactiveWebExceptionHandler(
        problemAdviceManager);
    exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
    ReactiveProblemConsumer problemCustomizer = problemFunctionObjectProvider.orderedStream()
        .reduce((exchange, problem) -> {
        }, ReactiveProblemConsumer::andThen);
    if (properties.getReactive().isRequestId()) {
      problemCustomizer = problemCustomizer.andThen(new ResponseIdProblemConsumer());
    }
    exceptionHandler.setProblemFunction(problemCustomizer);
    return exceptionHandler;
  }
}
