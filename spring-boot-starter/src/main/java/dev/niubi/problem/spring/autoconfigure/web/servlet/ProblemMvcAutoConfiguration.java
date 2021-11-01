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

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niubi.problem.spring.autoconfigure.ProblemProperties;
import dev.niubi.problem.spring.web.ProblemAdviceManager;
import dev.niubi.problem.spring.web.i18n.ProblemMessageSource;
import dev.niubi.problem.spring.web.servlet.DefaultHandlerExceptionProblemResolver;
import dev.niubi.problem.spring.web.servlet.HandlerExceptionProblemResolver;
import dev.niubi.problem.spring.web.servlet.MessageSourceProblemConsumer;
import dev.niubi.problem.spring.web.servlet.ProblemConsumer;
import dev.niubi.problem.spring.web.servlet.ProblemErrorController;
import java.util.Arrays;
import javax.servlet.Servlet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@Import(ProblemSecurityConfiguration.class)
public class ProblemMvcAutoConfiguration {

  private final ProblemProperties properties;
  private final ProblemAdviceManager problemAdviceManager;
  private final ServerProperties serverProperties;

  public ProblemMvcAutoConfiguration(
      ProblemProperties properties, ProblemAdviceManager problemAdviceManager,
      ServerProperties serverProperties) {
    this.properties = properties;
    this.problemAdviceManager = problemAdviceManager;
    this.serverProperties = serverProperties;
  }

  @Bean
  @ConditionalOnProperty(value = "dev.niubi.problem.i18n", havingValue = "true")
  public MessageSourceProblemConsumer messageSourceProblemFunction(ObjectProvider<MessageSource> messageSource) {
    ProblemMessageSource problemMessageSource = new ProblemMessageSource(messageSource.getIfAvailable(() -> null));
    return new MessageSourceProblemConsumer(problemMessageSource);
  }

  @Bean
  @ConditionalOnMissingBean
  public HandlerExceptionProblemResolver defaultHandlerExceptionProblemResolver(
      ObjectProvider<ProblemConsumer> problemFunctionProvider, ObjectMapper objectMapper) {

    ProblemConsumer problemConsumer = problemFunctionProvider.orderedStream().reduce(ProblemConsumer::andThen)
        .orElse(null);

    return new DefaultHandlerExceptionProblemResolver(
        problemAdviceManager, objectMapper, problemConsumer, null);
  }

  @Bean

  public ProblemErrorController problemErrorController(
      DispatcherServletPath dispatcherServletPath,
      HandlerExceptionProblemResolver handlerExceptionProblemResolver) {
    String defaultErrorPath = dispatcherServletPath.getRelativePath(serverProperties.getError().getPath());
    return new ProblemErrorController(handlerExceptionProblemResolver, defaultErrorPath);
  }

  @Bean
  public ErrorPageProblem errorPageProblem(DispatcherServletPath dispatcherServletPath) {
    return new ErrorPageProblem(dispatcherServletPath, properties);
  }

  static class ErrorPageProblem implements ErrorPageRegistrar, Ordered {

    private final DispatcherServletPath dispatcherServletPath;
    private final ProblemProperties properties;

    ErrorPageProblem(
        DispatcherServletPath dispatcherServletPath,
        ProblemProperties properties) {
      this.dispatcherServletPath = dispatcherServletPath;
      this.properties = properties;
    }

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
      String relativePath = this.dispatcherServletPath.getRelativePath(properties.getServlet().getPath());
      registry.addErrorPages(new ErrorPage(Throwable.class, relativePath));
      Arrays.stream(HttpStatus.values())
          .filter(HttpStatus::is4xxClientError)
          .map(httpStatus -> new ErrorPage(httpStatus, relativePath))
          .forEach(registry::addErrorPages)
      ;
    }

    @Override
    public int getOrder() {
      return 1;
    }
  }
}
