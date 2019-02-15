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

import net.sourceforge.pmd.AbstractJavaRule;
import net.sourceforge.pmd.ast.ASTName;
import net.sourceforge.pmd.ast.ASTPrimaryPrefix;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 * 
 * The IOUtils copy method artificially limits size to 4GiB.
 * Use copyLarge instead, which has no limitations.
 */
public class IOUtilsCopyRule extends AbstractJavaRule {

    @Override
    public Object visit(ASTPrimaryPrefix node, Object data) {
        ASTName name = node.getFirstChildOfType(ASTName.class);
        
        if (name != null) {
            if (name.getImage().equals("IOUtils.copy")) {
                addViolation(data, name);
            }
        }
        return super.visit(node, data);
    }

}
