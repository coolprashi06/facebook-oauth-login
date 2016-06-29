package service;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class FirstClass extends Application {
	private Set<Object> singletons = new HashSet<Object>();

	public FirstClass() {

		//root RESTful resource
		singletons.add(new OAuthService());

	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
