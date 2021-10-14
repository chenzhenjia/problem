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

package dev.niubi.problem.spring.advice;

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.Problems.Security;
import dev.niubi.problem.spring.web.ProblemAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ProblemAdvice
public interface SecurityProblemAdvice {

  @ExceptionHandler(AccessDeniedException.class)
  default Problem handleAccessDenied(AccessDeniedException exception) {
    return Security.ACCESS_DENIED;
  }

  @ExceptionHandler(AuthenticationException.class)
  default Problem handleAuthentication(AuthenticationException exception) {
    Problem problem;
    if (exception instanceof AccountExpiredException) {
      problem = Security.ACCOUNT_EXPIRED;
    } else if (exception instanceof CredentialsExpiredException) {
      problem = Security.ACCOUNT_EXPIRED;
    } else if (exception instanceof DisabledException) {
      problem = Security.CREDENTIALS_EXPIRED;
    } else if (exception instanceof LockedException) {
      problem = Security.ACCOUNT_LOCKED;
    } else if (exception instanceof UsernameNotFoundException) {
      problem = Security.USERNAME_NOT_FOUND;
    } else if (exception instanceof BadCredentialsException) {
      problem = Security.BAD_CREDENTIALS;
    } else {
      problem = Security.UNAUTHORIZED;
    }
    return problem;
  }
}
