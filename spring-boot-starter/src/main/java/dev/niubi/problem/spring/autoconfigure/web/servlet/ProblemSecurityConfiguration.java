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

import dev.niubi.problem.spring.web.servlet.ProblemHandlerExceptionResolver;
import dev.niubi.problem.spring.web.servlet.security.SecurityProblemHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({AccessDeniedHandler.class,
    AuthenticationEntryPoint.class, AuthenticationFailureHandler.class})
@ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "security",
    havingValue = "true", matchIfMissing = true)
public class ProblemSecurityConfiguration {

  private final ProblemHandlerExceptionResolver problemHandlerExceptionResolver;

  public ProblemSecurityConfiguration(
      ProblemHandlerExceptionResolver problemHandlerExceptionResolver) {
    this.problemHandlerExceptionResolver = problemHandlerExceptionResolver;
  }

  @Bean
  public SecurityProblemHandler securityProblemHandler() {
    return new SecurityProblemHandler(problemHandlerExceptionResolver);
  }
}
