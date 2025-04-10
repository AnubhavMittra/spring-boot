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

package org.springframework.boot.testcontainers.service.connection.redis;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

/**
 * {@link ContainerConnectionDetailsFactory} to create {@link RedisConnectionDetails} from
 * a {@link ServiceConnection @ServiceConnection}-annotated {@link GenericContainer} using
 * the {@code "redis"} image.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
class RedisContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<RedisConnectionDetails, Container<?>> {

	RedisContainerConnectionDetailsFactory() {
		super("redis");
	}

	@Override
	public RedisConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
		return new RedisContainerConnectionDetails(source);
	}

	/**
	 * {@link RedisConnectionDetails} backed by a {@link ContainerConnectionSource}.
	 */
	private static final class RedisContainerConnectionDetails extends ContainerConnectionDetails<Container<?>>
			implements RedisConnectionDetails {

		private RedisContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
			super(source);
		}

		@Override
		public Standalone getStandalone() {
			return Standalone.of(getContainer().getHost(), getContainer().getFirstMappedPort());
		}

	}

}
