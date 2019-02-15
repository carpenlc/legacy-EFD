package com.solers.delivery.install;

import java.io.File;

import com.solers.security.password.MapPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.util.IOConsole;

public class TestSecurityProviderTask extends SecuritySetupTask {

	@Override
	protected PasswordProviderConsole getPasswordConsole() {
		// TODO Auto-generated method stub
		return new TestPassworProviderConsole();
	}

	class TestPassworProviderConsole extends PasswordProviderConsole {

		@Override
		protected PasswordProvider getCurrentProvider(IOConsole console,
				File file) {
			// TODO Auto-generated method stub
			return new MapPasswordProvider();
		}
		
	}
}
