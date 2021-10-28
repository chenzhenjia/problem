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

import dev.niubi.problem.spring.autoconfigure.ProblemProperties;
import dev.niubi.problem.spring.web.servlet.HandlerExceptionProblemResolver;
import dev.niubi.problem.spring.web.servlet.security.SecurityProblemHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({AccessDeniedHandler.class,
    AuthenticationEntryPoint.class, AuthenticationFailureHandler.class})
@ConditionalOnProperty(prefix = "dev.niubi.problem.feature", name = "security",
    havingValue = "true", matchIfMissing = true)
public class ProblemSecurityConfiguration {

  private final HandlerExceptionProblemResolver handlerExceptionProblemResolver;

  public ProblemSecurityConfiguration(
      HandlerExceptionProblemResolver handlerExceptionProblemResolver) {
    this.handlerExceptionProblemResolver = handlerExceptionProblemResolver;
  }

  @Bean
  public SecurityProblemHandler securityProblemHandler() {
    return new SecurityProblemHandler(handlerExceptionProblemResolver);
  }

  @Configuration(proxyBeanMethods = false)
  @Order(99)
  @ConditionalOnClass({WebSecurityConfigurerAdapter.class,})
  public static class ProblemMvcSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final ProblemProperties properties;

    public ProblemMvcSecurityConfiguration(ProblemProperties properties) {
      this.properties = properties;
    }

    @Override
    protected void configure(
        HttpSecurity http) throws Exception {
      http.requestMatcher(new AntPathRequestMatcher(properties.getServlet().getPath()))
          .authorizeRequests(registry -> registry.anyRequest().permitAll())
          .csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) {
      web.ignoring().mvcMatchers(properties.getServlet().getPath());
    }
  }
}
