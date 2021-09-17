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

package dev.niubi.problem.spring.autoconfigure;

import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;

@ConfigurationProperties("dev.niubi.problem")
public class ProblemProperties {

  private boolean enabled;
  private URI domain;
  private boolean i18n;
  private Reactive reactive = new Reactive();
  private Servlet servlet = new Servlet();
  private Feature feature = new Feature();

  public Reactive getReactive() {
    return reactive;
  }

  public void setReactive(Reactive reactive) {
    this.reactive = reactive;
  }

  public Servlet getServlet() {
    return servlet;
  }

  public void setServlet(Servlet servlet) {
    this.servlet = servlet;
  }

  public Feature getFeature() {
    return feature;
  }

  public void setFeature(Feature feature) {
    this.feature = feature;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public URI getDomain() {
    return domain;
  }

  public void setDomain(URI domain) {
    this.domain = domain;
  }

  public boolean isI18n() {
    return i18n;
  }

  public void setI18n(boolean i18n) {
    this.i18n = i18n;
  }

  private static class Mvc {

    private List<MediaType> accepts;

    public List<MediaType> getAccepts() {
      return accepts;
    }

    public void setAccepts(List<MediaType> accepts) {
      this.accepts = accepts;
    }
  }

  public static class Servlet {

    @Value("${problem.path:/problem}")
    private String path = "/problem";

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }

  public static class Reactive {

    private boolean requestId = true;

    public boolean isRequestId() {
      return requestId;
    }

    public void setRequestId(boolean requestId) {
      this.requestId = requestId;
    }
  }

  public static class Feature {

    private boolean security = true;
    private boolean mvc = true;
    private boolean kotlin = true;
    private boolean validation = true;
    private boolean http = true;

    public boolean isSecurity() {
      return security;
    }

    public void setSecurity(boolean security) {
      this.security = security;
    }

    public boolean isMvc() {
      return mvc;
    }

    public void setMvc(boolean mvc) {
      this.mvc = mvc;
    }

    public boolean isKotlin() {
      return kotlin;
    }

    public void setKotlin(boolean kotlin) {
      this.kotlin = kotlin;
    }

    public boolean isValidation() {
      return validation;
    }

    public void setValidation(boolean validation) {
      this.validation = validation;
    }

    public boolean isHttp() {
      return http;
    }

    public void setHttp(boolean http) {
      this.http = http;
    }
  }
}
