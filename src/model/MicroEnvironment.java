package model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import agents.highlevel.DecisionAgent;
import smart_lighting.GuiGenerator;

public class MicroEnvironment {

	Vector<String> near060 = new Vector<>();
	float illuminance;
	static boolean isNight;
	public String id;
	float power;
	boolean isMove;
	boolean isVehicle;
	boolean isPedestrian;
	boolean isGroup;
	boolean near125;
	boolean near050 = false;
	public static final int PATTERN_PARK = 3;
	public static final int PATTERN_SCHOOL = 1;
	public static final int PATTERN_DANGER = 2;

	public static MutableGraph<String> routeGraph = GraphBuilder.directed().allowsSelfLoops(true).build();;

	public boolean isVehicle() {
		return isVehicle;
	}

	public void addNear060(String id) {
		near060.add(id);
	}

	public Vector<String> getNear060() {
		return near060;
	}

	public void deleteNear060(String id) {
		near060.remove(id);
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

	public MicroEnvironment(String id) {
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
		MicroEnvironment.isNight = isNight;
	}

	public float getPower() {
		return power;
	}

	public boolean calculatePower() {
		boolean powerIsChange = false;
		float tempPower = power;

		if (illuminance < 1.0f) {
			if (isMove) {
				if (isGroup || isPedestrian)
					tempPower = 1.25f - (0.75f * illuminance) + 0.2f;
				else if (isVehicle)
					tempPower = 0.75f - (0.75f * illuminance) + 0.2f;
				else
					tempPower = 1f * illuminance;

				ArrayList<String> succenssors = new ArrayList<>(routeGraph.successors(id));

				for (String str : succenssors) {
					if (!DecisionAgent.microEnvironments.get(str).getNear060().contains(id))
						DecisionAgent.microEnvironments.get(str).addNear060(id);
				}

				if (GuiGenerator.instance().crossStreet && succenssors.size() > 2)
					tempPower = 1.2f * (1.2f - illuminance);

				if (isGroup && tempPower < 1.1f)
					tempPower = 1.1f;

				if (GuiGenerator.instance().isDangerous) {
					float patternPower = getPatternPower(id, PATTERN_DANGER);
					if (tempPower < patternPower + 0.1f && patternPower > 0)
						tempPower = patternPower - (patternPower * illuminance) + 0.1f;
				}

				if (GuiGenerator.instance().school) {
					float patternPower = getPatternPower(id, PATTERN_SCHOOL);
					if (tempPower < patternPower + 0.15f && patternPower > 0)
						tempPower = patternPower - (patternPower * illuminance) + 0.15f;
				}

			} else
				tempPower = 0.0f;

			if (near060.size() > 0) {
				Vector<String> v2 = (Vector) near060.clone();
				for (String str : v2) {
					if (!DecisionAgent.microEnvironments.get(str).isMove)
						near060.remove(str);
				}
			}

			if (tempPower < 0.6 && near060.size() > 0) {
				tempPower = 0.4f - (0.4f * illuminance) + 0.2f;

				if (GuiGenerator.instance().groupPeople) {
					for (String str : near060) {
						if (!DecisionAgent.microEnvironments.get(str).isGroup)
							tempPower = 1.1f;
					}
				}

				if (GuiGenerator.instance().isDangerous) {
					float patternPower = getPatternPower(id, PATTERN_DANGER);
					if (tempPower < patternPower + 0.1f && patternPower > 0)
						tempPower = patternPower - (patternPower * illuminance) + 0.1f;
				}

				if (GuiGenerator.instance().school) {
					float patternPower = getPatternPower(id, PATTERN_SCHOOL);
					if (tempPower < patternPower && patternPower > 0)
						tempPower = patternPower - (patternPower * illuminance);
				}
			}

			if (GuiGenerator.instance().isPark) {
				float patternPower = getPatternPower(id, PATTERN_PARK);
				if (tempPower < patternPower + 0.1f && patternPower > 0)
					tempPower = patternPower - (patternPower * illuminance) + 0.1f;
			}

		} else
			tempPower = 0.0f;

		if (power != tempPower) {
			powerIsChange = true;
		}

		power = tempPower;

		return powerIsChange;
	}

	private float getPatternPower(String id, int patternID) {
		for (PatternModel pattern : DecisionAgent.patternArray) {
			if (pattern.lampID.equals(id) && pattern.patternID == patternID)
				return pattern.power;
		}

		return (float) 0.0;
	}

}
