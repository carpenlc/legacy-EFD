package com.solers.delivery;

import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;

public class MockInputHandler implements InputHandler {

    private final String [] values;
    private int count;

    public MockInputHandler(String... values) {
	            this.values = values;
	}
	        
    @Override
    public void handleInput(InputRequest request) {
        request.setInput(values[count++]);
    }

}
