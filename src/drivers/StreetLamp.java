package drivers;

import drivers.remote.StreetLampRemote;
import javafx.application.Platform;
import smart_lighting.Simulation;

public class StreetLamp implements StreetLampRemote {

	Simulation.StreetLampModel streetLampModel;

	public StreetLamp(Simulation.StreetLampModel streetLampModel) {
		this.streetLampModel = streetLampModel;
	}

	@Override
	public void setPower(float power) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				streetLampModel.circle.setRadius(50 * power);
				streetLampModel.circle.setOpacity(0.7);
			}
		});
	}

}
