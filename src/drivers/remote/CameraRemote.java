package drivers.remote;

import drivers.Camera;

public interface CameraRemote extends DriverRemote {
	void getData(Camera.OnResult onResult);
}
