package smart_lighting;

public class CloudinessSimulation {

	String[] cloudinnessItem = new String[] { "BRAK", "MA£E", "ŒREDNIE", "DU¯E" };
	float[] cloudinnessItemRatio = new float[] { 1.0f, 0.75f, 0.5f, 0.25f };
	int currentItem = 0;

	private static CloudinessSimulation simulation = null;

	public static CloudinessSimulation getInstance() {
		if (simulation == null)
			simulation = new CloudinessSimulation();

		return simulation;
	}

	public String[] getCloudinnessItem() {
		return cloudinnessItem;
	}

	public float getCloudinnessItemRatio() {
		return cloudinnessItemRatio[currentItem];
	}
	
	public void setCurrentItem(int currentItem) {
		this.currentItem = currentItem;
	}
	

}
