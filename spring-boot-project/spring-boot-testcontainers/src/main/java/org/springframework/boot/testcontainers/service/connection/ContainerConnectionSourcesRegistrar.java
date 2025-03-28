/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.testcontainers.service.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetailsFactories;
import org.springframework.core.log.LogMessage;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Class used to register bean definitions from a list of
 * {@link ContainerConnectionSource} instances.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
class ContainerConnectionSourcesRegistrar {

	private static final Log logger = LogFactory.getLog(ContainerConnectionSourcesRegistrar.class);

	private final ListableBeanFactory beanFactory;

	private final ConnectionDetailsFactories connectionDetailsFactories;

	private final List<ContainerConnectionSource<?>> sources;

	ContainerConnectionSourcesRegistrar(ListableBeanFactory beanFactory,
			ConnectionDetailsFactories connectionDetailsFactories, List<ContainerConnectionSource<?>> sources) {
		this.beanFactory = beanFactory;
		this.connectionDetailsFactories = connectionDetailsFactories;
		this.sources = sources;
	}

	void registerBeanDefinitions(BeanDefinitionRegistry registry) {
		this.sources.forEach((source) -> registerBeanDefinition(registry, source));
	}

	private void registerBeanDefinition(BeanDefinitionRegistry registry, ContainerConnectionSource<?> source) {
		this.connectionDetailsFactories.getConnectionDetails(source, true)
			.forEach((connectionDetailsType, connectionDetails) -> registerBeanDefinition(registry, source,
					connectionDetailsType, connectionDetails));
	}

	@SuppressWarnings("unchecked")
	private <T> void registerBeanDefinition(BeanDefinitionRegistry registry, ContainerConnectionSource<?> source,
			Class<?> connectionDetailsType, ConnectionDetails connectionDetails) {
		String[] existingBeans = this.beanFactory.getBeanNamesForType(connectionDetailsType);
		if (!ObjectUtils.isEmpty(existingBeans)) {
			logger.debug(LogMessage.of(() -> "Skipping registration of %s due to existing beans %s".formatted(source,
					Arrays.asList(existingBeans))));
			return;
		}
		String beanName = getBeanName(source, connectionDetails);
		Class<T> beanType = (Class<T>) connectionDetails.getClass();
		Supplier<T> beanSupplier = () -> (T) connectionDetails;
		logger.debug(LogMessage.of(() -> "Registering '%s' for %s".formatted(beanName, source)));
		registry.registerBeanDefinition(beanName, new RootBeanDefinition(beanType, beanSupplier));
	}

	private String getBeanName(ContainerConnectionSource<?> source, ConnectionDetails connectionDetails) {
		List<String> parts = new ArrayList<>();
		parts.add(ClassUtils.getShortNameAsProperty(connectionDetails.getClass()));
		parts.add("for");
		parts.add(source.getBeanNameSuffix());
		return StringUtils.uncapitalize(parts.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
	}

}
