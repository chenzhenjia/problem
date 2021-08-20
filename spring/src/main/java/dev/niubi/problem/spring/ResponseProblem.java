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

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.niubi.problem.Problem;
import dev.niubi.problem.Status;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;

public class ResponseProblem extends Problem.ExtendedProblem {

  private final Problem problem;
  private final HttpHeaders headers;
  private Problem newProblem;

  ResponseProblem(Problem problem, Object extendedProperties, Map<String, Object> properties) {
    super(problem.getType(), problem.getTitle(), problem.getStatus(), problem.getDetail(), extendedProperties,
        properties);
    this.problem = problem;
    this.newProblem = problem;
    this.headers = new HttpHeaders();
  }


  public static ResponseProblem with(Problem problem) {
    Map<String, Object> properties = null;
    Object extendedProperties = null;
    if (problem instanceof Problem.ExtendedProblem) {
      ExtendedProblem extendedProblem = (ExtendedProblem) problem;
      properties = extendedProblem.getProperties();
      extendedProperties = extendedProblem.getExtendedProperties();
    }
    return new ResponseProblem(problem, extendedProperties, properties);
  }

  public ResponseProblem withHeader(Consumer<HttpHeaders> consumer) {
    Objects.requireNonNull(consumer, "Consumer 不能为空");
    consumer.accept(this.headers);
    return this;
  }

  public ResponseProblem addHeader(String headerName, @Nullable String headerValue) {
    this.headers.add(headerName, headerValue);
    return this;
  }

  public ResponseProblem withType(URI type) {
    this.newProblem = this.newProblem.withType(type);
    return this;
  }

  public ResponseProblem withType(String type) {
    this.newProblem = this.newProblem.withType(type);
    return this;
  }

  @JsonIgnore
  public HttpHeaders getHeaders() {
    return headers;
  }

  public ResponseProblem withTitle(@Nullable String title) {
    this.newProblem = this.newProblem.withTitle(title);
    return this;
  }

  public ResponseProblem withStatus(Status status) {
    this.newProblem = this.newProblem.withStatus(status);
    return this;
  }

  public ResponseProblem withDetail(@Nullable String detail) {
    this.newProblem = this.newProblem.withDetail(detail);
    return this;
  }

  public ResponseProblem withProperties(Object payload) {
    this.newProblem = newProblem.withProperties(payload);
    return this;
  }

  public ResponseProblem withProperties(
      Consumer<Map<String, Object>> consumer) {
    this.newProblem = newProblem.withProperties(consumer);
    return this;
  }

  public ResponseProblem withProperties(
      Map<String, Object> payload) {
    this.newProblem = newProblem.withProperties(payload);
    return this;
  }

  public URI getType() {
    return this.newProblem.getType();
  }

  public @Nullable String getTitle() {
    return this.newProblem.getTitle();
  }

  public @Nullable Status getStatus() {
    return this.newProblem.getStatus();
  }

  public @Nullable String getDetail() {
    return this.newProblem.getDetail();
  }

  @JsonIgnore
  public Problem getProblem() {
    return problem;
  }

  @Override
  public @Nullable Object getExtendedProperties() {
    if (this.newProblem instanceof Problem.ExtendedProblem) {
      return ((ExtendedProblem) this.newProblem).getExtendedProperties();
    }
    return super.getExtendedProperties();
  }

  @Override
  public @Nullable Map<String, Object> getProperties() {
    if (this.newProblem instanceof Problem.ExtendedProblem) {
      return ((ExtendedProblem) this.newProblem).getProperties();
    }
    return super.getProperties();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return this.newProblem.equals(o);
  }

  @Override
  public int hashCode() {
    return this.newProblem.hashCode();
  }

  @Override
  public String toString() {
    return this.newProblem.toString();
  }
}
