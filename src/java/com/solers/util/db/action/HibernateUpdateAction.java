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
package com.solers.util.db.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import com.solers.util.db.Database;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class HibernateUpdateAction implements DatabaseAction {
    
    private static final Logger log = Logger.getLogger(HibernateUpdateAction.class);
    
    private Properties hibernateProperties;
    private Class<?> [] hibernateClasses;
    
    public void setHibernateProperties(Properties hibernateProperties) {
        this.hibernateProperties = hibernateProperties;
    }

    public void setHibernateClasses(Class<?>[] hibernateClasses) {
        this.hibernateClasses = Arrays.copyOf(hibernateClasses, hibernateClasses.length);
    }
    
    @Override
    public void execute(Database db) {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.setProperties(hibernateProperties);
        
        for (Class<?> clazz : hibernateClasses) {
            config.addAnnotatedClass(clazz);
        }
        
        Dialect dialect = Dialect.getDialect(hibernateProperties);
        
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = db.getConnection();
            stmt = conn.createStatement();
            DatabaseMetadata meta = new DatabaseMetadata(conn, dialect);
            
            String [] sqlStatements = config.generateSchemaUpdateScript(dialect, meta);
            for (String sql : sqlStatements) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException ex) {
            log.error("Error updating hibernate", ex);
        } finally {
            close(stmt);
            close(conn);
        }
    }
    
    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
                log.error("Error closing connection: " + ignore.getMessage(), ignore);
            }
        }
    }
    
    private void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                log.error("Error closing statement: " + ignore.getMessage(), ignore);
            }
        }
    }
    
}
