package drivers;

import drivers.remote.StreetLampRemote;
import smart_lighting.Simulation;

public class StreetLamp implements StreetLampRemote {

	Simulation.StreetLampModel streetLampModel;

	public StreetLamp(Simulation.StreetLampModel streetLampModel) {
		this.streetLampModel = streetLampModel;
	}

	@Override
	public void setPower(float power) {
		streetLampModel.circle.setRadius(50*power);
		streetLampModel.circle.setOpacity(power*0.9);
	}

}
