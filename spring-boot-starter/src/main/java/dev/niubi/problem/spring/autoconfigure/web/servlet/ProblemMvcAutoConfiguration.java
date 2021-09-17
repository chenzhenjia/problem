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
import dev.niubi.problem.spring.convert.ExceptionConverterManager;
import dev.niubi.problem.spring.web.servlet.MessageSourceProblemFunction;
import dev.niubi.problem.spring.web.servlet.ProblemFunction;
import dev.niubi.problem.spring.web.servlet.ProblemHandlerExceptionResolver;
import javax.servlet.Servlet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
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
import org.springframework.web.servlet.DispatcherServlet;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@Import(ProblemSecurityConfiguration.class)
public class ProblemMvcAutoConfiguration {

  private final ExceptionConverterManager exceptionConverterManager;
  private final ProblemProperties properties;

  public ProblemMvcAutoConfiguration(
      ExceptionConverterManager exceptionConverterManager,
      ProblemProperties properties) {
    this.exceptionConverterManager = exceptionConverterManager;
    this.properties = properties;
  }

  @Bean
  @ConditionalOnBean(MessageSource.class)
  @ConditionalOnProperty(value = "dev.niubi.problem.i18n", havingValue = "true")
  public MessageSourceProblemFunction messageSourceProblemFunction(MessageSource messageSource) {
    return new MessageSourceProblemFunction(messageSource);
  }

  @Bean
  public ProblemHandlerExceptionResolver problemHandlerExceptionResolver(
      ObjectProvider<ProblemFunction> problemFunctionProvider, ObjectMapper objectMapper) {
    ProblemHandlerExceptionResolver problemHandlerExceptionResolver = new ProblemHandlerExceptionResolver(
        exceptionConverterManager, objectMapper);
    ProblemFunction problemFunction = problemFunctionProvider.orderedStream().reduce(ProblemFunction::andThen)
        .orElse(null);
    problemHandlerExceptionResolver.setProblemFunction(problemFunction);
    return problemHandlerExceptionResolver;
  }

  @Bean
  public ProblemErrorController problemErrorController() {
    return new ProblemErrorController(exceptionConverterManager);
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
      ErrorPage errorPage = new ErrorPage(Throwable.class,
          this.dispatcherServletPath.getRelativePath(properties.getServlet().getPath()));
      registry.addErrorPages(errorPage);
    }

    @Override
    public int getOrder() {
      return -1000;
    }
  }
}
