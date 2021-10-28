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

package dev.niubi.problem.mvc.example;

import dev.niubi.problem.spring.web.servlet.security.SecurityProblemHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration(proxyBeanMethods = false)
public class WebMvcSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final SecurityProblemHandler securityProblemHandler;

  public WebMvcSecurityConfiguration(
      SecurityProblemHandler securityProblemHandler) {
    this.securityProblemHandler = securityProblemHandler;
  }

  @Override
  protected void configure(
      HttpSecurity http) throws Exception {
    http.csrf().disable()
        .formLogin()
        .loginProcessingUrl("/login")
        .failureHandler(securityProblemHandler)
        .and()
        .authorizeRequests()
        .mvcMatchers("/api/**").permitAll()
//        .anyRequest().authenticated()
    ;
  }
}
