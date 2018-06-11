package drivers.remote;

import drivers.Movement.OnResult;

public interface MovementRemote extends DriverRemote {
	void getData(OnResult onResult);
}
