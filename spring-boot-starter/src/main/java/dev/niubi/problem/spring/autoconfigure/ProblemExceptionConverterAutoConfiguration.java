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

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException;
import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.DomainProblemCustomizer;
import dev.niubi.problem.spring.convert.ExceptionConverterCustomizer;
import dev.niubi.problem.spring.convert.ExceptionConverterManager;
import dev.niubi.problem.spring.convert.ExceptionConverterManagerBuilder;
import dev.niubi.problem.spring.convert.general.IllegalArgumentExceptionConverter;
import dev.niubi.problem.spring.convert.general.ProblemExceptionConverter;
import dev.niubi.problem.spring.convert.general.ThrowableConverter;
import dev.niubi.problem.spring.convert.general.UnsupportedOperationExceptionConverter;
import dev.niubi.problem.spring.convert.http.MethodNotAllowedExceptionConverter;
import dev.niubi.problem.spring.convert.http.NotAcceptableStatusExceptionConverter;
import dev.niubi.problem.spring.convert.http.ResponseStatusExceptionConverter;
import dev.niubi.problem.spring.convert.http.ServerWebInputExceptionConverter;
import dev.niubi.problem.spring.convert.http.UnsupportedMediaTypeStatusExceptionConverter;
import dev.niubi.problem.spring.convert.kotlin.MissingKotlinParameterExceptionConverter;
import dev.niubi.problem.spring.convert.security.AccessDeniedExceptionConverter;
import dev.niubi.problem.spring.convert.security.AuthenticationExceptionConverter;
import dev.niubi.problem.spring.convert.servlet.MissingServletRequestPartExceptionConverter;
import dev.niubi.problem.spring.convert.servlet.NoHandlerFoundExceptionConverter;
import dev.niubi.problem.spring.convert.servlet.ServletRequestBindingExceptionConverter;
import dev.niubi.problem.spring.convert.validation.BindExceptionConverter;
import dev.niubi.problem.spring.convert.validation.ConstraintViolationExceptionConverter;
import dev.niubi.problem.spring.convert.validation.WebExchangeBindExceptionConverter;
import java.util.function.Function;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProblemProperties.class)
public class ProblemExceptionConverterAutoConfiguration {

  private final ProblemProperties properties;

  public ProblemExceptionConverterAutoConfiguration(ProblemProperties properties) {
    this.properties = properties;
  }

  @Bean
  public ExceptionConverterManager exceptionConverterManager(
      ObjectProvider<ExceptionConverterCustomizer> customizers,
      ObjectProvider<Function<Problem, Problem>> problemCustomizerProvider) {
    ExceptionConverterManagerBuilder managerBuilder = new ExceptionConverterManagerBuilder();
    for (ExceptionConverterCustomizer customizer : customizers) {
      customizer.customize(managerBuilder);
    }
    Function<Problem, Problem> problemCustomizer = problemCustomizerProvider.orderedStream()
        .reduce(Function.identity(), Function::andThen);
    if (properties.getDomain() != null) {
      problemCustomizer = problemCustomizer.andThen(DomainProblemCustomizer.of(properties.getDomain()));
    }
    ExceptionConverterManager manager = managerBuilder.build();
    manager.setProblemCustomizer(problemCustomizer);
    return manager;
  }

  @Configuration(proxyBeanMethods = false)
  public static class ConverterConfiguration {

    @Bean
    public ExceptionConverterCustomizer generalExceptionConverterCustomizer() {
      return builder -> builder.addConverter(new ThrowableConverter())
          .addConverter(new UnsupportedOperationExceptionConverter())
          .addConverter(new IllegalArgumentExceptionConverter())
          .addConverter(new ProblemExceptionConverter());
    }

    @Bean
    public ExceptionConverterCustomizer httpExceptionConverterCustomizer() {
      return builder -> builder.addConverter(new MethodNotAllowedExceptionConverter())
          .addConverter(new NotAcceptableStatusExceptionConverter())
          .addConverter(new UnsupportedMediaTypeStatusExceptionConverter())
          .addConverter(new ResponseStatusExceptionConverter())
          .addConverter(new ServerWebInputExceptionConverter());
    }

    @Bean
    public ExceptionConverterCustomizer validationExceptionConverterCustomizer(
        MessageSource messageSource
    ) {
      return builder -> builder.addConverter(new ConstraintViolationExceptionConverter(messageSource))
          .addConverter(new WebExchangeBindExceptionConverter(messageSource))
          .addConverter(new BindExceptionConverter(messageSource));
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(MissingKotlinParameterException.class)
  public static class KotlinExceptionConverterConfiguration {

    @Bean
    public ExceptionConverterCustomizer missingKotlinParameterExceptionConverterCustomizer() {
      return builder -> builder.addConverter(
          new MissingKotlinParameterExceptionConverter());
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass({Servlet.class, DispatcherServlet.class, ServletException.class})
  public static class WebMvcExceptionConverterConfiguration {

    @Bean
    public ExceptionConverterCustomizer webMvcExceptionConverterCustomizer() {
      return builder -> builder
          .addConverter(new MissingServletRequestPartExceptionConverter())
          .addConverter(new NoHandlerFoundExceptionConverter())
          .addConverter(new ServletRequestBindingExceptionConverter())
          ;
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass({AccessDeniedException.class, AuthenticationException.class})
  public static class SecurityExceptionConverterConfiguration {

    @Bean
    public ExceptionConverterCustomizer securityExceptionConverterCustomizer() {
      return builder -> builder
          .addConverter(new AccessDeniedExceptionConverter())
          .addConverter(new AuthenticationExceptionConverter())
          ;
    }
  }
}
