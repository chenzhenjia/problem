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

import java.net.URI;
import java.util.function.Consumer;

/**
 * 支持problem的域名添加
 */
public class DomainProblemCustomizer implements Consumer<ResponseProblem> {

  private final URI domain;

  private DomainProblemCustomizer(URI domain) {
    this.domain = domain;
  }

  public static Consumer<ResponseProblem> of(URI domain) {
    return new DomainProblemCustomizer(domain);
  }


  @Override
  public void accept(ResponseProblem responseProblem) {
    URI uri = domain.resolve(responseProblem.getType());
    responseProblem.withType(uri);
  }
}
