/****************************************************************
 *
 * Solers, Inc. as the author of Enterprise File Delivery 2.1 (EFD 2.1)
 * source code submitted herewith to the Government under contract
 * retains those intellectual property rights as set forth by the Federal 
 * Acquisition Regulations agreement (FAR). The Government has 
 * unlimited rights to redistribute copies of the EFD 2.1 in 
 * executable or source format to support operational installation 
 * and software maintenance. Additionally, the executable or 
 * source may be used or modified for by third parties as 
 * directed by the government.
 *
 * (c) 2009 Solers, Inc.
 ***********************************************************/
package com.solers.delivery.pmd;

import net.sourceforge.pmd.Rule;

import org.junit.Test;

import test.net.sourceforge.pmd.testframework.RuleTst;
import test.net.sourceforge.pmd.testframework.TestDescriptor;

/**
 * To run this, you need lib/build/pmd and src/test/resources in
 * your classpath
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RulesTestCase extends RuleTst {
    
    @Test
    public void testIOUtilsCopyRule() {
        test("ruleset.xml", "IOUtilsCopyRule");
    }
    
    private void test(String ruleSet, String ruleName) {
        Rule rule = findRule(ruleSet, ruleName);
        for (TestDescriptor t : extractTestsFromXml(rule)) {
            runTest(t);
        }
    }

    public TestDescriptor[] extractTestsFromXml(Rule rule, String testsFileName) {
        return extractTestsFromXml(rule, testsFileName, "/xml/");
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(RulesTestCase.class);
    }
}
