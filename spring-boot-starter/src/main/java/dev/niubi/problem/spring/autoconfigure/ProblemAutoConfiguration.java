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

package dev.niubi.problem.spring.autoconfigure;

import dev.niubi.problem.spring.DomainProblemCustomizer;
import dev.niubi.problem.spring.ResponseProblem;
import dev.niubi.problem.spring.autoconfigure.web.reactive.ProblemFluxAutoConfiguration;
import dev.niubi.problem.spring.autoconfigure.web.servlet.ProblemMvcAutoConfiguration;
import dev.niubi.problem.spring.web.ProblemAdviceManager;
import java.util.function.Consumer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({ProblemAdviceAutoConfiguration.class, ProblemFluxAutoConfiguration.class,
    ProblemMvcAutoConfiguration.class})
@ConditionalOnProperty(value = "dev.niubi.problem.enabled", havingValue = "true")
@EnableConfigurationProperties(ProblemProperties.class)
public class ProblemAutoConfiguration {

  private final ProblemProperties properties;

  public ProblemAutoConfiguration(ProblemProperties properties) {
    this.properties = properties;
  }

  @Bean
  public ProblemAdviceManager problemAdviceManager(
      ObjectProvider<Consumer<ResponseProblem>> problemCustomizerProvider) {
    ProblemAdviceManager problemAdviceManager = new ProblemAdviceManager();
    Consumer<ResponseProblem> problemConsumer = problemCustomizerProvider.orderedStream()
        .reduce(responseProblem -> {
        }, Consumer::andThen);
    if (properties.getDomain() != null) {
      problemConsumer = problemConsumer.andThen(DomainProblemCustomizer.of(properties.getDomain()));
    }
    problemAdviceManager.setProblemCustomizer(problemConsumer);
    return problemAdviceManager;
  }
}
