package org.nectarframework.junit;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.nectarframework.base.service.template.TemplateService;

public class TemplateServicePatternTest {

	private boolean testPattern(Pattern p, String input, String[] groupMatches) {
		Matcher m = p.matcher(input);
		if (m.matches()) {
			if (m.groupCount() != groupMatches.length)
				return false;
			boolean ret = true;
			for (int i = 0; i < groupMatches.length; i++) {
				if (!m.group(i + 1).equals(groupMatches[i])) {
					ret = false;
				}
			}
			return ret;
		}
		return false;
	}

	@Test
	public void test() {
		System.out.println("Testing: variable "+TemplateService.variablePattern.pattern());
		assertTrue(testPattern(TemplateService.variablePattern, "${cow}", new String[]{"cow"}));
		assertFalse(testPattern(TemplateService.variablePattern, "${cow}", new String[]{"co1w"}));
		assertTrue(testPattern(TemplateService.variablePattern, "${ cow}", new String[]{"cow"}));
		assertFalse(testPattern(TemplateService.variablePattern, "$ {cow}", new String[]{"cow"}));
		assertTrue(testPattern(TemplateService.variablePattern, " ${cow} ", new String[]{"cow"}));
		assertTrue(testPattern(TemplateService.variablePattern, "${c.o.w}", new String[]{"c.o.w"}));
		
		System.out.println("Testing: eachTag "+TemplateService.eachTagPattern.pattern());
		assertTrue(testPattern(TemplateService.eachTagPattern, "milk : ${c.o.w}", new String[]{"milk", "c.o.w"}));
		assertFalse(testPattern(TemplateService.eachTagPattern, "$milk : ${c.o.w}", new String[]{"milk", "c.o.w"}));
		
	}
}
