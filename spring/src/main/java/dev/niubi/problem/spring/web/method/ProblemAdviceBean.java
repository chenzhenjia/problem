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

package dev.niubi.problem.spring.web.method;

import dev.niubi.problem.spring.web.ProblemAdvice;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.HandlerTypePredicate;

public class ProblemAdviceBean implements Ordered {

  private final Object beanOrName;
  private final boolean isSingleton;
  @Nullable
  private final Class<?> beanType;
  private final HandlerTypePredicate beanTypePredicate;
  private final BeanFactory beanFactory;
  private Object resolvedBean;
  private Integer order;

  public ProblemAdviceBean(String beanName, BeanFactory beanFactory, @Nullable ProblemAdvice problemAdvice) {
    Assert.hasText(beanName, "Bean name must contain text");
    Assert.notNull(beanFactory, "BeanFactory must not be null");
    Assert.isTrue(beanFactory.containsBean(beanName), () -> "BeanFactory [" + beanFactory
        + "] does not contain specified controller advice bean '" + beanName + "'");

    this.beanOrName = beanName;
    this.isSingleton = beanFactory.isSingleton(beanName);
    this.beanType = getBeanTypeByBeanFactory(beanName, beanFactory);
    this.beanTypePredicate = createBeanTypePredicate(problemAdvice);
    this.beanFactory = beanFactory;
  }

  private static HandlerTypePredicate createBeanTypePredicate(@Nullable ProblemAdvice problemAdvice) {
    if (problemAdvice != null) {
      return HandlerTypePredicate.builder()
          .basePackage(problemAdvice.basePackages())
          .basePackageClass(problemAdvice.basePackageClasses())
          .assignableType(problemAdvice.assignableTypes())
          .annotation(problemAdvice.annotations())
          .build();
    }
    return HandlerTypePredicate.forAnyHandlerType();
  }

  @Nullable
  private static Class<?> getBeanTypeByBeanFactory(String beanName, BeanFactory beanFactory) {
    Class<?> beanType = beanFactory.getType(beanName);
    return (beanType != null ? ClassUtils.getUserClass(beanType) : null);
  }

  public static List<ProblemAdviceBean> findAnnotatedBeans(ApplicationContext context) {
    ListableBeanFactory beanFactory = context;
    if (context instanceof ConfigurableApplicationContext) {
      // Use internal BeanFactory for potential downcast to ConfigurableBeanFactory above
      beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
    }
    List<ProblemAdviceBean> adviceBeans = new ArrayList<>();
    for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Object.class)) {
      if (!ScopedProxyUtils.isScopedTarget(name)) {
        ProblemAdvice problemAdvice = beanFactory.findAnnotationOnBean(name, ProblemAdvice.class);
        if (problemAdvice != null) {
          // Use the @ControllerAdvice annotation found by findAnnotationOnBean()
          // in order to avoid a subsequent lookup of the same annotation.
          adviceBeans.add(new ProblemAdviceBean(name, beanFactory, problemAdvice));
        }
      }
    }
    OrderComparator.sort(adviceBeans);
    return adviceBeans;
  }

  @Nullable
  public Class<?> getBeanType() {
    return this.beanType;
  }

  @Override
  public int getOrder() {
    if (this.order == null) {
      String beanName = null;
      Object resolvedBean = null;
      if (this.beanFactory != null && this.beanOrName instanceof String) {
        beanName = (String) this.beanOrName;
        String targetBeanName = ScopedProxyUtils.getTargetBeanName(beanName);
        boolean isScopedProxy = this.beanFactory.containsBean(targetBeanName);
        // Avoid eager @ControllerAdvice bean resolution for scoped proxies,
        // since attempting to do so during context initialization would result
        // in an exception due to the current absence of the scope. For example,
        // an HTTP request or session scope is not active during initialization.
        if (!isScopedProxy && !ScopedProxyUtils.isScopedTarget(beanName)) {
          resolvedBean = resolveBean();
        }
      } else {
        resolvedBean = resolveBean();
      }

      if (resolvedBean instanceof Ordered) {
        this.order = ((Ordered) resolvedBean).getOrder();
      } else {
        if (beanName != null && this.beanFactory instanceof ConfigurableBeanFactory) {
          ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) this.beanFactory;
          try {
            BeanDefinition bd = cbf.getMergedBeanDefinition(beanName);
            if (bd instanceof RootBeanDefinition) {
              Method factoryMethod = ((RootBeanDefinition) bd).getResolvedFactoryMethod();
              if (factoryMethod != null) {
                this.order = OrderUtils.getOrder(factoryMethod);
              }
            }
          } catch (NoSuchBeanDefinitionException ex) {
            // ignore -> probably a manually registered singleton
          }
        }
        if (this.order == null) {
          if (this.beanType != null) {
            this.order = OrderUtils.getOrder(this.beanType, Ordered.LOWEST_PRECEDENCE);
          } else {
            this.order = Ordered.LOWEST_PRECEDENCE;
          }
        }
      }
    }
    return this.order;
  }

  public Object resolveBean() {
    if (this.resolvedBean == null) {
      // this.beanOrName must be a String representing the bean name if
      // this.resolvedBean is null.
      Object resolvedBean = obtainBeanFactory().getBean((String) this.beanOrName);
      // Don't cache non-singletons (e.g., prototypes).
      if (!this.isSingleton) {
        return resolvedBean;
      }
      this.resolvedBean = resolvedBean;
    }
    return this.resolvedBean;
  }

  private BeanFactory obtainBeanFactory() {
    Assert.state(this.beanFactory != null, "No BeanFactory set");
    return this.beanFactory;
  }

  public boolean isApplicableToBeanType(@Nullable Class<?> beanType) {
    return this.beanTypePredicate.test(beanType);
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ProblemAdviceBean)) {
      return false;
    }
    ProblemAdviceBean otherAdvice = (ProblemAdviceBean) other;
    return (this.beanOrName.equals(otherAdvice.beanOrName) && this.beanFactory == otherAdvice.beanFactory);
  }

  @Override
  public int hashCode() {
    return this.beanOrName.hashCode();
  }

  @Override
  public String toString() {
    return this.beanOrName.toString();
  }
}
