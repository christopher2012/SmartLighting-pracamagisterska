package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

public class MicroEvironment {
	float illuminance;
	static boolean isNight;
	public String id;
	float power;
	boolean isMove;
	boolean isVehicle;
	boolean isPedestrian;
	boolean isGroup;
	boolean near125;
	boolean near050;

	public static MutableGraph<String> routeGraph = GraphBuilder.directed().allowsSelfLoops(true).build();;

	public boolean isVehicle() {
		return isVehicle;
	}

	public void clearActors() {
		isGroup = false;
		isPedestrian = false;
		isVehicle = false;
	}

	public void setVehicle(boolean isVehicle) {
		this.isVehicle = isVehicle;
	}

	public boolean isPedestrian() {
		return isPedestrian;
	}

	public void setPedestrian(boolean isPedestrian) {
		this.isPedestrian = isPedestrian;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public boolean isMove() {
		return isMove;
	}

	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}

	public MicroEvironment(String id) {
		this.id = id;
	}

	public float getIlluminance() {
		return illuminance;
	}

	public void setIlluminance(float illuminance) {
		this.illuminance = illuminance;
	}

	public static boolean isNight() {
		return isNight;
	}

	public static void setNight(boolean isNight) {
		MicroEvironment.isNight = isNight;
	}

	public float getPower() {
		return power;
	}

	public boolean calculatePower(Map<String, MicroEvironment> microEnvirements) {
		boolean powerIsChange = false;
		float tempPower = 0.0f;
		if (isMove && illuminance < 1.0f) {
			if (isGroup || isPedestrian)
				tempPower = 1.25f - (0.75f * illuminance);
			else if (isVehicle)
				tempPower = 0.75f - (0.75f * illuminance);
		} else
			tempPower = 0.0f;

		if(tempPower < 0.5 && near050){
			tempPower = 0.5f;
		}
		
		power = tempPower;
		
		if (power != tempPower)
			powerIsChange = true;
		
		if (microEnvirements != null) {
			ArrayList<String> succenssors = new ArrayList<>(routeGraph.successors(id));
			for (String str : succenssors) {
				if (isPedestrian) {
					microEnvirements.get(str).near050 = true;
				}
				//microEnvirements.get(str).calculatePower(null);
			}
		}

		return powerIsChange;
	}
}
