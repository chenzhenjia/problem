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

    public static final Problem ILLEGAL_ARGUMENT = Problem.badRequest(
        "illegal_argument");
    public static final Problem INTERNAL_SERVER_ERROR = Problem.internalServerError(
        "internal_server_error");
    public static final Problem UNSUPPORTED_OPERATION = Problem.notImplemented("unsupported_operation");
  }

  public static class Web {

    public static final Problem METHOD_NOT_ALLOWED = Problem.create(
        ProblemStatus.METHOD_NOT_ALLOWED, "method_not_allowed");
    public static final Problem NOT_ACCEPTABLE = Problem.create(ProblemStatus.NOT_ACCEPTABLE,
        "not_acceptable");
    public static final Problem SERVER_WEB_INPUT = Problem.create(ProblemStatus.BAD_REQUEST,
        "bad_request");
    public static final Problem UNSUPPORTED_MEDIA_TYPE = Problem.create(
        ProblemStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported_media_type");
  }

  public static class Security {

    public static final Problem ACCESS_DENIED = Problem.create(ProblemStatus.FORBIDDEN, "access_denied");
    public static final Problem UNAUTHORIZED = Problem.create(ProblemStatus.UNAUTHORIZED,
        "unauthorized");
    public static final Problem ACCOUNT_EXPIRED = Problem.create(ProblemStatus.UNAUTHORIZED,
        "account_expired");
    public static final Problem CREDENTIALS_EXPIRED = Problem.create(ProblemStatus.UNAUTHORIZED,
        "credentials_expired");
    public static final Problem ACCOUNT_LOCKED = Problem.create(ProblemStatus.UNAUTHORIZED,
        "account_locked");
    public static final Problem USERNAME_NOT_FOUND = Problem.create(ProblemStatus.UNAUTHORIZED,
        "username_not_found");
  }

  public static class Servlet {

    public static final Problem MISSING_REQUEST_VALUE = Problem.create(ProblemStatus.BAD_REQUEST,
        "missing_request_value");
    public static final Problem MISSING_REQUEST_PART = Problem.create(ProblemStatus.BAD_REQUEST,
        "missing_request_part");
    public static final Problem NO_HANDLER_FOUND = Problem.create(ProblemStatus.NOT_FOUND,
        "no_handler_found");
    public static final Problem REQUEST_BINDING = Problem.create(ProblemStatus.BAD_REQUEST,
        "request_binding");
  }

  public static class Validation {

    public static final Problem CONSTRAINT_VIOLATION = Problem.badRequest(
        "constraint_violation");
    public static final Problem VALID_FAILED = Problem.badRequest("validation_failed");
  }

  public static class Kotlin {

    public static final Problem MISSING_PARAMETER = Problem.badRequest("missing_parameter");
  }
}
