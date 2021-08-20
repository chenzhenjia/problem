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
import dev.niubi.problem.spring.advice.CustomProblemAdvice;
import dev.niubi.problem.spring.advice.GeneralProblemAdvice;
import dev.niubi.problem.spring.advice.KotlinProblemAdvice;
import dev.niubi.problem.spring.advice.SecurityProblemAdvice;
import dev.niubi.problem.spring.advice.ThrowableProblemAdvice;
import dev.niubi.problem.spring.advice.WebFluxProblemAdvice;
import dev.niubi.problem.spring.advice.WebMvcProblemAdvice;
import dev.niubi.problem.spring.advice.validation.BindResultProblemAdvice;
import dev.niubi.problem.spring.advice.validation.ConstraintViolationProblemAdvice;
import jakarta.validation.ConstraintViolationException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class ProblemAdviceAutoConfiguration {

  @Bean
  public CustomProblemAdvice customProblemAdvice() {
    return new CustomProblemAdvice();
  }

  @Bean
  public GeneralProblemAdvice generalProblemAdvice() {
    return new GeneralProblemAdvice() {
    };
  }

  @Bean
  @Order
  public ThrowableProblemAdvice throwableProblemAdvice() {
    return new ThrowableProblemAdvice() {
    };
  }

  @ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "validation",
      havingValue = "true", matchIfMissing = true)
  @Bean
  public BindResultProblemAdvice bindResultProblemAdvice(
      MessageSource messageSource
  ) {
    return () -> messageSource;
  }

  @ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "validation",
      havingValue = "true", matchIfMissing = true)
  @ConditionalOnClass(ConstraintViolationException.class)
  @Configuration(proxyBeanMethods = false)
  public static class ConstraintViolationProblemConfiguration {

    @Bean
    public ConstraintViolationProblemAdvice constraintViolationProblemAdvice(
        MessageSource messageSource
    ) {
      return () -> messageSource;
    }
  }

  @ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "flux",
      havingValue = "true", matchIfMissing = true)
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(MethodNotAllowedException.class)
  public static class WebFluxProblemConfiguration {

    @Bean
    @Order(Integer.MAX_VALUE - 1)
    public WebFluxProblemAdvice webFluxProblemAdvice() {
      return new WebFluxProblemAdvice() {
      };
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(MissingKotlinParameterException.class)
  @ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "kotlin",
      havingValue = "true", matchIfMissing = true)
  public static class KotlinProblemAdviceConfiguration {

    @Bean
    public KotlinProblemAdvice kotlinProblemAdvice() {
      return new KotlinProblemAdvice() {
      };
    }
  }


  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass({Servlet.class, DispatcherServlet.class, ServletException.class})
  @ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "mvc",
      havingValue = "true", matchIfMissing = true)
  public static class WebMvcProblemAdviceConfiguration {

    @Bean
    @Order(Integer.MAX_VALUE - 1)
    public WebMvcProblemAdvice webMvcProblemAdvice() {
      return new WebMvcProblemAdvice() {
      };
    }
  }


  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass({AccessDeniedException.class, AuthenticationException.class})
  @ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "security",
      havingValue = "true", matchIfMissing = true)
  public static class SecurityProblemAdviceConfiguration {

    @Bean
    @Order(Integer.MAX_VALUE - 1)
    public SecurityProblemAdvice securityProblemAdvice() {
      return new SecurityProblemAdvice() {
      };
    }
  }
}
