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

package dev.niubi.problem.spring.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.niubi.problem.Problem;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

public class ProblemJsonView extends MappingJackson2JsonView {

  private final HttpHeaders headers;

  public ProblemJsonView(ObjectMapper objectMapper, HttpHeaders headers) {
    super(objectMapper);
    this.headers = headers;
    setModelKey(Problem.class.getName());
    setExtractValueFromSingleKeyModel(true);
    setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
  }

  @Override
  public void render(Map<String, ?> model, HttpServletRequest request,
      @NotNull HttpServletResponse response) throws Exception {
    if (headers != null && !headers.isEmpty()) {
      headers.forEach((headerName, headerValues) -> {
        for (String headerValue : headerValues) {
          response.addHeader(headerName, headerValue);
        }
      });
    }
    super.render(model, request, response);
  }
}
