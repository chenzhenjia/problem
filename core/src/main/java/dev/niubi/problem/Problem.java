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

package dev.niubi.problem;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

@JsonInclude(Include.NON_NULL)
public class Problem {

  private static final URI BLANK = URI.create("about:blank");
  private final URI type;
  private final @Nullable String title;
  private final @Nullable Status status;
  private final @Nullable String detail;

  private Problem(URI type, Status status) {
    this(type, null, status, null);
  }

  @JsonCreator
  public Problem(@JsonProperty("type") URI type, @JsonProperty("title") String title,
      @JsonProperty("status") int status, @JsonProperty("detail") String detail) {
    this(type, title, Optional.ofNullable(ProblemStatus.valueOf(status))
        .orElse(ProblemStatus.INTERNAL_SERVER_ERROR), detail);
  }

  public Problem(@Nullable URI type, @Nullable String title, @Nullable Status status,
      @Nullable String detail) {
    this.type = Optional.ofNullable(type).orElse(BLANK);
    this.title = title;
    this.status = status;
    this.detail = detail;
  }

  public static Problem create(final Status status, final String type) {
    Objects.requireNonNull(type, "type must not be null!");
    Objects.requireNonNull(status, "Status must not be null!");
    return new Problem(URI.create(type), null, status, null);
  }

  public static Problem badRequest(final String code) {
    return create(ProblemStatus.BAD_REQUEST, code);
  }

  public static Problem unauthorized(final String code) {
    return create(ProblemStatus.UNAUTHORIZED, code);
  }

  public static Problem forbidden(final String code) {
    return create(ProblemStatus.FORBIDDEN, code);
  }

  public static Problem notFound(final String code) {
    return create(ProblemStatus.NOT_FOUND, code);
  }

  public static Problem conflict(final String code) {
    return create(ProblemStatus.CONFLICT, code);
  }

  public static Problem unprocessableEntity(final String code) {
    return create(ProblemStatus.UNPROCESSABLE_ENTITY, code);
  }

  public static Problem internalServerError(final String code) {
    return create(ProblemStatus.INTERNAL_SERVER_ERROR, code);
  }

  public static Problem notImplemented(final String code) {
    return create(ProblemStatus.NOT_IMPLEMENTED, code);
  }

  public static Problem serviceUnavailable(final String code) {
    return create(ProblemStatus.SERVICE_UNAVAILABLE, code);
  }

  public static Problem statusOnly(Status status) {

    Objects.requireNonNull(status, "Status must not be null!");

    return new Problem(BLANK, null, status, null);
  }

  public Problem withType(URI type) {
    Objects.requireNonNull(type, "type must not be null!");
    return this.type == type ? this
        : new Problem(type, this.title, this.status, this.detail);
  }

  public Problem withType(String type) {
    Objects.requireNonNull(type, "type must not be null!");
    return new Problem(URI.create(type), this.title, this.status, this.detail);
  }

  public Problem withTitle(@Nullable String title) {
    return Objects.equals(this.title, title) ? this
        : new Problem(this.type, title, this.status, this.detail);
  }

  public Problem withStatus(Status status) {
    Objects.requireNonNull(status, "HttpStatus must not be null!");
    return this.status == status ? this
        : new Problem(this.type, this.title, status, this.detail);
  }

  public Problem withDetail(@Nullable String detail) {
    return Objects.equals(this.detail, detail) ? this
        : new Problem(this.type, this.title, this.status, detail);
  }

  public <T> ExtendedProblem<T> withProperties(T payload) {
    if (payload instanceof Map) {
      throw new IllegalArgumentException("payload 不能传map");
    }
    return new ExtendedProblem<>(type, title, status, detail, payload, null);
  }

  public ExtendedProblem<Map<String, Object>> withProperties(
      Consumer<Map<String, Object>> consumer) {

    Objects.requireNonNull(consumer, "Consumer must not be null!");

    Map<String, Object> map = new HashMap<>();
    consumer.accept(map);

    return withProperties(map);
  }

  public ExtendedProblem<Map<String, Object>> withProperties(Map<String, Object> payload) {
    Objects.requireNonNull(payload, "Properties must not be null!");

    return new ExtendedProblem<>(type, title, status, detail, null, payload);
  }

  @JsonProperty
  public URI getType() {
    return this.type;
  }

  @JsonProperty
  @Nullable
  public String getTitle() {
    return this.title;
  }

  @Nullable
  @JsonProperty("status")
  @JsonInclude(Include.NON_NULL)
  Integer getStatusAsInteger() {
    return status != null ? status.value() : null;
  }

  @JsonIgnore
  @Nullable
  public Status getStatus() {
    return this.status;
  }

  @JsonProperty
  @Nullable
  public String getDetail() {
    return this.detail;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (!(o instanceof Problem)) {
      return false;
    }
    Problem problem = (Problem) o;
    return Objects.equals(this.type, problem.type) && Objects.equals(this.title, problem.title)
        && this.status == problem.status && Objects.equals(this.detail, problem.detail)
        ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.type, this.title, this.status, this.detail);
  }

  public String toString() {
    return "Problem(type=" + this.type + ", title=" + this.title + ", status=" + this.status
        + ", detail=" + this.detail + ")";
  }

  public static final class ExtendedProblem<T> extends Problem {

    @Nullable
    private final T extendedProperties;
    private Map<String, Object> mapProperties;

    ExtendedProblem(@Nullable URI type, @Nullable String title, @Nullable Status status,
        @Nullable String detail, @Nullable T properties,
        @Nullable Map<String, Object> mapProperties) {

      super(type, title, status, detail);
      this.mapProperties = mapProperties;
      this.extendedProperties = properties;
    }

    @Override
    public ExtendedProblem<T> withType(@Nullable URI type) {
      return new ExtendedProblem<>(type, getTitle(), getStatus(), getDetail(),
          extendedProperties, mapProperties);
    }

    @Override
    public ExtendedProblem<T> withTitle(@Nullable String title) {
      return new ExtendedProblem<>(getType(), title, getStatus(), getDetail(),
          extendedProperties, mapProperties);
    }

    @Override
    public ExtendedProblem<T> withStatus(@Nullable Status status) {
      return new ExtendedProblem<>(getType(), getTitle(), status, getDetail(),
          extendedProperties, mapProperties);
    }

    @Override
    public ExtendedProblem<T> withDetail(@Nullable String detail) {
      return new ExtendedProblem<>(getType(), getTitle(), getStatus(), detail,
          extendedProperties, mapProperties);
    }

    @Override
    public ExtendedProblem<Map<String, Object>> withProperties(
        Consumer<Map<String, Object>> consumer) {
      Objects.requireNonNull(consumer, "Consumer must not be null!");
      Map<String, Object> map = new HashMap<>();
      Map<String, Object> properties = this.getProperties();
      if (properties != null) {
        map.putAll(properties);
      }
      consumer.accept(map);

      return super.withProperties(map);
    }

    @Nullable
    @JsonUnwrapped
    public T getExtendedProperties() {
      return extendedProperties;
    }

    @Nullable
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
      return mapProperties;
    }

    @JsonAnySetter
    void setPropertiesAsMap(String key, Object value) {
      getOrInitAsMap().put(key, value);
    }

    private Map<String, Object> getOrInitAsMap() {

      if (this.mapProperties == null) {
        this.mapProperties = new LinkedHashMap<>();
      }

      return this.mapProperties;
    }

    @Override
    public boolean equals(Object o) {

      if (this == o) {
        return true;
      }
      if (!(o instanceof ExtendedProblem)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      ExtendedProblem<?> that = (ExtendedProblem<?>) o;
      return Objects.equals(this.extendedProperties, that.extendedProperties);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), extendedProperties);
    }

    public String toString() {
      return "Problem.ExtendedProblem(extendedProperties=" + this.extendedProperties + ")";
    }
  }
}
