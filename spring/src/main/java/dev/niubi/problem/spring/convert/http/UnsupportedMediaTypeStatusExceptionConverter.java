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

package dev.niubi.problem.spring.convert.http;

import dev.niubi.problem.Problem;
import dev.niubi.problem.spring.Problems.Web;
import dev.niubi.problem.spring.convert.ExceptionConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

public class UnsupportedMediaTypeStatusExceptionConverter implements
    ExceptionConverter<UnsupportedMediaTypeStatusException> {

  @Override
  @NonNull
  public ResponseEntity<Problem> convert(
      @NonNull UnsupportedMediaTypeStatusException throwable) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(throwable.getSupportedMediaTypes());
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).headers(headers)
        .body(toProblem(throwable));
  }

  @Override
  public Problem toProblem(
      @NonNull UnsupportedMediaTypeStatusException throwable) {
    return Web.UNSUPPORTED_MEDIA_TYPE;
  }
}
