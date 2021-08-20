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

package dev.niubi.problem.spring.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ExceptionConverterManagerBuilder {

  private final List<ExceptionConverter<? extends Throwable>> converters;

  public ExceptionConverterManagerBuilder() {
    this.converters = new ArrayList<>();
  }

  public ExceptionConverterManagerBuilder addConverter(
      ExceptionConverter<? extends Throwable> converter) {
    Objects.requireNonNull(converter, "ExceptionConverter must not be null!");
    this.converters.add(converter);
    return this;
  }

  private void addAll(List<String> list, List<Object> itemsToAdd) {
    if (itemsToAdd != null) {
      for (Object item : itemsToAdd) {
        Collections.addAll(list, (String[]) item);
      }
    }
  }

  public ExceptionConverterManager build() {
    return new ExceptionConverterManager(
        this.converters);
  }
}
