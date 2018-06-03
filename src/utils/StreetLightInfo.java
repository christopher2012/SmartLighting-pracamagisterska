package utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javafx.stage.Window;
import model.MicroEvironment;

public class StreetLightInfo {

	public String id;
	Circle circle;
	public Tooltip tooltip;
	public float power;
	public float illuminance;

	public StreetLightInfo(String id, Tooltip tooltip) {
		this.id = id;
		this.tooltip = tooltip;
		updateTooltip();
	}

	public void updateTooltip() {
		String tooltipText = "ID: " + id + "\n" + "Moc latarni: " + (int) (power * 100) + "%" + "\n"
				+ "Natê¿enie œwiat³a: " + (int) (illuminance * 100) + "%";
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				tooltip.setText(tooltipText);
			}
		});
	}

	public void updateInfo(MicroEvironment microEvironment) {
		illuminance = microEvironment.getIlluminance();
		power = microEvironment.getPower();
		updateTooltip();
	}
}
