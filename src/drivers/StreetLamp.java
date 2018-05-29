package drivers;

import drivers.interfaces.StreetLampRemote;
import smart_lighting.Simulation;

public class StreetLamp implements StreetLampRemote {

	Simulation.StreetLampModel streetLampModel;

	public StreetLamp(Simulation.StreetLampModel streetLampModel) {
		this.streetLampModel = streetLampModel;
	}

	@Override
	public void changeState(boolean isOn) {
		if (isOn)
			streetLampModel.circle.setFill(javafx.scene.paint.Color.RED);
		else
			streetLampModel.circle.setFill(javafx.scene.paint.Color.YELLOW);
	}

}
