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

package dev.niubi.problem.spring.web.reactive;

import dev.niubi.problem.Problem;
import org.springframework.web.server.ServerWebExchange;

public class ResponseIdProblemFunction implements ReactiveProblemFunction {

  @Override
  public Problem apply(ServerWebExchange exchange,
      Problem problem) {
    return problem.withProperties(map -> map.put("request_id", exchange.getRequest().getId()));
  }
}
