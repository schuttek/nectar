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

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.expression.IExpressionObjects;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.xml.Element;
import org.thymeleaf.standard.expression.IStandardVariableExpression;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;

/**
 * <p>
 *   Evaluator for variable expressions (<tt>${...}</tt>) in Thymeleaf Standard Expressions, using the
 *   SpringEL expression language.
 * </p>
 * <p>
 *   Note a class with this name existed since 2.0.9, but it was completely reimplemented
 *   in Thymeleaf 3.0
 * </p>
 *
 * @author Daniel Fern&aacute;ndez
 * @author Guven Demir
 * 
 * @since 3.0.0
 *
 */
public class ThymeVariableExpressionEvaluator
        implements IStandardVariableExpressionEvaluator {


    public static final ThymeVariableExpressionEvaluator INSTANCE = new ThymeVariableExpressionEvaluator();

    protected ThymeVariableExpressionEvaluator() {
        super();
    }
    
    
    
    
    public final Object evaluate(
            final IExpressionContext context,
            final IStandardVariableExpression expression,
            final StandardExpressionExecutionContext expContext) {
        

        try {

            final String expressionStr = expression.getExpression();
            final boolean useSelectionAsRoot = expression.getUseSelectionAsRoot();

            if (expressionStr == null) {
                throw new TemplateProcessingException("Expression content is null, which is not allowed");
            }
            
        	Log.trace("VariableExpressionEvaluator.evaluate(): "+expressionStr+" ("+(useSelectionAsRoot?"true":"false")+" on "+context.getConfiguration());

            
            IExpressionObjects expressionObjects = context.getExpressionObjects();
            
            
            Log.trace("context is type: "+context.getClass().getName()+" expContext is type: "+expContext.getClass().getName());
            Log.trace("context.getObject(action) is type: "+context.getVariable("action").getClass().getName());
            
            Element actionElm = (Element)context.getVariable("action");
            
            Object returnObj = evalRecurse(actionElm, expressionStr);
            
            Log.trace("returnObj is type "+(returnObj==null?"null":returnObj.getClass().getName()));
            return returnObj;
            /*

            
             * RESOLVE THE EVALUATION ROOT
             
            final ITemplateContext templateContext = (context instanceof ITemplateContext ? (ITemplateContext) context : null);
            final Object evaluationRoot =
                    (useSelectionAsRoot && templateContext != null && templateContext.hasSelectionTarget()?
                            templateContext.getSelectionTarget() : new SPELContextMapWrapper(context, thymeleafEvaluationContext));


            
             * If no conversion is to be made, JUST RETURN
             
            if (!expContext.getPerformTypeConversion()) {
                return exp.expression.getValue(thymeleafEvaluationContext, evaluationRoot);
            }


            
             * If a conversion is to be made, OBTAIN THE CONVERSION SERVICE AND EXECUTE IT
             
            final IStandardConversionService conversionService =
                    StandardExpressions.getConversionService(configuration);

            if (conversionService instanceof SpringStandardConversionService) {
                // The conversion service is a mere bridge with the Spring ConversionService, therefore
                // this makes use of the complete Spring type conversion infrastructure, without needing
                // to manually execute the conversion.
                return exp.expression.getValue(thymeleafEvaluationContext, evaluationRoot, String.class);
            }

            // We need type conversion, but conversion service is not a mere bridge to the Spring one,
            // so we need manual execution.
            final Object result = exp.expression.getValue(thymeleafEvaluationContext, evaluationRoot);
            return conversionService.convert(context, result, String.class);

*/

            
        } catch (final TemplateProcessingException e) {
            throw e;
        } catch(final Exception e) {
            throw new TemplateProcessingException(
                    "Exception evaluating SpringEL expression: \"" + expression.getExpression() + "\"", e);
        }
        
    }




	private Object evalRecurse(Element actionElm, String expressionStr) {
		
		if (expressionStr.indexOf('.') >= 0) {
			String firstPart = expressionStr.substring(0, expressionStr.indexOf('.'));
			String restPart = expressionStr.substring(expressionStr.indexOf('.')+1);
			
			if (actionElm.isName(firstPart)) { // expression start is the name of this element
				return evalRecurse(actionElm, restPart);
			} else {
				return null;
			}
			
		} else {
			if (actionElm.hasAttribute(expressionStr)) {
				return actionElm.get(expressionStr);
			}
			return actionElm.getChildren(expressionStr);
		}
	}





}
