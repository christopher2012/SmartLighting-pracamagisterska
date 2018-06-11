package drivers.remote;

import drivers.Velocity.OnResult;

public interface VelocityRemote extends DriverRemote{
	void getData(OnResult onResult);
}
