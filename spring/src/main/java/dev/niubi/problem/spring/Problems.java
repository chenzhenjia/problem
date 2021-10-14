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

package dev.niubi.problem.spring;

import dev.niubi.problem.Problem;
import dev.niubi.problem.ProblemStatus;

public class Problems {

  public static class General {

    public static final Problem ILLEGAL_ARGUMENT = Problem.create(ProblemStatus.BAD_REQUEST, "illegal_argument",
        "{dev.niubi.problem.spring.Problems.illegal_argument.title}",
        "{dev.niubi.problem.spring.Problems.General.ILLEGAL_ARGUMENT}");
    public static final Problem INTERNAL_SERVER_ERROR = Problem.create(ProblemStatus.INTERNAL_SERVER_ERROR,
        "internal_server_error", "{dev.niubi.problem.spring.Problems.internal_server_error.title}",
        "{dev.niubi.problem.spring.Problems.General.INTERNAL_SERVER_ERROR}");
    public static final Problem UNSUPPORTED_OPERATION = Problem.create(ProblemStatus.NOT_IMPLEMENTED,
        "unsupported_operation", "{dev.niubi.problem.spring.Problems.unsupported_operation.title}",
        "{dev.niubi.problem.spring.Problems.General.UNSUPPORTED_OPERATION}");
  }

  public static class Http {

    public static final Problem METHOD_NOT_ALLOWED = Problem.create(ProblemStatus.METHOD_NOT_ALLOWED,
        "method_not_allowed", "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Http.METHOD_NOT_ALLOWED}");
    public static final Problem NOT_ACCEPTABLE = Problem.create(ProblemStatus.NOT_ACCEPTABLE,
        "not_acceptable", "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Http.NOT_ACCEPTABLE}");
    public static final Problem BAD_REQUEST = Problem.create(ProblemStatus.BAD_REQUEST,
        "bad_request", "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Http.BAD_REQUEST}");
    public static final Problem UNAVAILABLE = Problem.create(ProblemStatus.UNAVAILABLE_FOR_LEGAL_REASONS,
        "unavailable", "{dev.niubi.problem.spring.Problems.unavailable.title}",
        "{dev.niubi.problem.spring.Problems.Http.UNAVAILABLE}");
    public static final Problem UNSUPPORTED_MEDIA_TYPE = Problem.create(ProblemStatus.UNSUPPORTED_MEDIA_TYPE,
        "unsupported_media_type", "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Http.UNSUPPORTED_MEDIA_TYPE}");
  }

  public static class Security {

    public static final Problem ACCESS_DENIED = Problem.create(ProblemStatus.FORBIDDEN, "access_denied",
        "{dev.niubi.problem.spring.Problems.access_denied.title}",
        "{dev.niubi.problem.spring.Problems.Security.ACCESS_DENIED}");
    public static final Problem UNAUTHORIZED = Problem.create(ProblemStatus.UNAUTHORIZED,
        "unauthorized", "{dev.niubi.problem.spring.Problems.unauthorized.title}",
        "{dev.niubi.problem.spring.Problems.Security.UNAUTHORIZED}");
    public static final Problem ACCOUNT_EXPIRED = Problem.create(ProblemStatus.UNAUTHORIZED,
        "account_expired", "{dev.niubi.problem.spring.Problems.authorization_failed.title}",
        "{dev.niubi.problem.spring.Problems.Security.ACCOUNT_EXPIRED}");
    public static final Problem CREDENTIALS_EXPIRED = Problem.create(ProblemStatus.UNAUTHORIZED,
        "credentials_expired", "{dev.niubi.problem.spring.Problems.authorization_failed.title}",
        "{dev.niubi.problem.spring.Problems.Security.CREDENTIALS_EXPIRED}");
    public static final Problem ACCOUNT_LOCKED = Problem.create(ProblemStatus.UNAUTHORIZED,
        "account_locked", "{dev.niubi.problem.spring.Problems.authorization_failed.title}",
        "{dev.niubi.problem.spring.Problems.Security.ACCOUNT_LOCKED}");
    public static final Problem USERNAME_NOT_FOUND = Problem.create(ProblemStatus.UNAUTHORIZED,
        "username_not_found", "{dev.niubi.problem.spring.Problems.authorization_failed.title}",
        "{dev.niubi.problem.spring.Problems.Security.USERNAME_NOT_FOUND}");
    public static final Problem BAD_CREDENTIALS = Problem.create(ProblemStatus.UNAUTHORIZED,
        "invalid_credentials", "{dev.niubi.problem.spring.Problems.authorization_failed.title}",
        "{dev.niubi.problem.spring.Problems.Security.BAD_CREDENTIALS}");
  }

  public static class Servlet {

    public static final Problem MISSING_REQUEST_VALUE = Problem.create(ProblemStatus.BAD_REQUEST,
        "missing_request_value", "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Servlet.MISSING_REQUEST_VALUE}");
    public static final Problem MISSING_REQUEST_PART = Problem.create(ProblemStatus.BAD_REQUEST,
        "missing_request_part", "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Servlet.MISSING_REQUEST_PART}");
    public static final Problem NO_HANDLER_FOUND = Problem.create(ProblemStatus.NOT_FOUND,
        "no_handler_found", "{dev.niubi.problem.spring.Problems.not_found.title}",
        "{dev.niubi.problem.spring.Problems.Servlet.NO_HANDLER_FOUND}");
    public static final Problem REQUEST_BINDING = Problem.create(ProblemStatus.BAD_REQUEST,
        "request_binding", "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Servlet.REQUEST_BINDING}");
  }

  public static class Validation {

    public static final Problem CONSTRAINT_VIOLATION = Problem.create(ProblemStatus.BAD_REQUEST,
        "constraint_violation", "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Validation.CONSTRAINT_VIOLATION}");
    public static final Problem VALID_FAILED = Problem.create(ProblemStatus.BAD_REQUEST, "validation_failed",
        "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Validation.VALID_FAILED}");
  }

  public static class Kotlin {

    public static final Problem MISSING_PARAMETER = Problem.create(ProblemStatus.BAD_REQUEST, "missing_parameter",
        "{dev.niubi.problem.spring.Problems.bad_request.title}",
        "{dev.niubi.problem.spring.Problems.Kotlin.MISSING_PARAMETER}");
  }
}
