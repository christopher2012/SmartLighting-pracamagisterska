package model;

public class MicroEvironment {
	float illuminance;
	static boolean isNight;
	public String id;
	float power;
	boolean isMove = true;

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

	public boolean calculatePower() {
		boolean powerIsChange = false;
		float tempPower = 0.0f;
		if (isMove && illuminance < 1.0f)
			tempPower = 1 - (0.75f * illuminance);
		else
			tempPower = 0.0f;
		
		if (power != tempPower)
			powerIsChange = true;
		power = tempPower;

		return powerIsChange;
	}

}
