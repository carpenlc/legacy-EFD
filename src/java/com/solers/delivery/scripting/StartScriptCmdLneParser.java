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
package com.solers.delivery.scripting;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class StartScriptCmdLneParser {
     
    private static final String VERBOSE = "verbose";
        
    private CommandLine cmd;    
    private static Options options= null;
    private List<String> errors;    
 
    
    public StartScriptCmdLneParser(String[] args) {
        errors = new ArrayList<String>();
        CommandLineParser parser = new BasicParser();
        try {
            cmd = parser.parse(createOptions(),args);
            validate();
        } catch (ParseException e) {           
            errors.add("Error parsing command line: " + e.getMessage());
        } 
    }
    
    public boolean isVerbose() {
        return cmd.hasOption(VERBOSE);
    }  
  
    private void validate() {
        //no validation
    }
    
    public List<String> getErrors(){
        return errors;
    }
    
    @SuppressWarnings("static-access")
    private synchronized static Options createOptions() {
        if(options == null) {
            options = new Options();
            
            Option verbose   = OptionBuilder.hasArg(false)
                                            .withDescription(  "display extra information to the console" )
                                            .create( VERBOSE );      

            options.addOption(verbose);  
        }
        
        return options;
    }
    
    public static void printHelp() {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(" ", createOptions() );            
    }

}
