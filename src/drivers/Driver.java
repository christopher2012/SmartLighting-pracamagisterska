package drivers;

import drivers.remote.DriverRemote;

public class Driver implements DriverRemote{

	String id;
	
	public Driver(String id){
		this.id = id;
	}
	
	@Override
	public String getID() {
		return id;
	}

}
