/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.context.config;

import java.util.Set;
import java.util.function.Consumer;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * 过滤给定属性列表的 PropertySource
 * Internal {@link PropertySource} implementation used by
 * {@link ConfigFileApplicationListener} to filter out properties for specific operations.
 *
 * @author Phillip Webb
 */
class FilteredPropertySource extends PropertySource<PropertySource<?>> {

	/**
	 * 过滤属性
	 */
	private final Set<String> filteredProperties;

	FilteredPropertySource(PropertySource<?> original, Set<String> filteredProperties) {
		super(original.getName(), original);
		this.filteredProperties = filteredProperties;
	}

	@Override
	public Object getProperty(String name) {
		//如果是过滤属性, 则返回 null
		if (this.filteredProperties.contains(name)) {
			return null;
		}
		//获取对应的属性值
		return getSource().getProperty(name);
	}

	static void apply(ConfigurableEnvironment environment, String propertySourceName, Set<String> filteredProperties,
			Consumer<PropertySource<?>> operation) {
		//获取环境中的 MutablePropertySources
		MutablePropertySources propertySources = environment.getPropertySources();
		//根据名称获取对应的 PropertySource
		PropertySource<?> original = propertySources.get(propertySourceName);
		//如果是 null, 则调用函数处理返回
		if (original == null) {
			operation.accept(null);
			return;
		}
		//指定的 PropertySource 不为 null
		//替换成 FilteredPropertySource
		propertySources.replace(propertySourceName, new FilteredPropertySource(original, filteredProperties));
		try {
			//执行函数
			operation.accept(original);
		}
		finally {
			//替换回来
			propertySources.replace(propertySourceName, original);
		}
	}

}
