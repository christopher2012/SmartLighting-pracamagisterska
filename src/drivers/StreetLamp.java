package drivers;

import drivers.remote.StreetLampRemote;
import javafx.application.Platform;
import smart_lighting.GuiGenerator;
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
				if (GuiGenerator.instance().simulationIsWorking) {
					if (power == 0.0)
						streetLampModel.circle.setRadius(0);
					else
						streetLampModel.circle.setRadius(80 * power + 0.3);
					streetLampModel.circle.setOpacity(0.7);
				}
			}
		});
	}

}
