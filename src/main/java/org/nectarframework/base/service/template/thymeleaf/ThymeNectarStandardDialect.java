/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.nectarframework.base.service.template.thymeleaf;

import java.util.Map;

import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;

public class ThymeNectarStandardDialect extends StandardDialect {

	public static final String NAME = "NectarStandard";
	public static final String PREFIX = "th";
	public static final int PROCESSOR_PRECEDENCE = 1000;

	public ThymeNectarStandardDialect() {
		super(NAME, PREFIX, PROCESSOR_PRECEDENCE);
	}

	@Override
	public IStandardVariableExpressionEvaluator getVariableExpressionEvaluator() {
		return ThymeVariableExpressionEvaluator.INSTANCE;
	}

	@Override
	public Map<String, Object> getExecutionAttributes() {

		final Map<String, Object> executionAttributes = super.getExecutionAttributes();

		return executionAttributes;

	}

}
