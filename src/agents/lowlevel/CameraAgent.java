package agents.lowlevel;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import agents.analytic.EnvironmentAnalyzerAgent;
import agents.analytic.ImageAnalyzerAgent;
import drivers.Camera;
import drivers.remote.CameraRemote;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import utils.Configuration;
import utils.JSONKey;

public class CameraAgent extends Agent {

	public static String PREFIX_AGENT = "CAMERA_";
	public static String DIR_APPROACHING = "APPROACHING";
	public static String DIR_LEAVING = "LEAVING";

	CameraRemote cameraDriver;
	String dataString = "";
	int counter = 0;

	@Override
	protected void setup() {
		super.setup();
		cameraDriver = (CameraRemote) getArguments()[0];
		addBehaviour(new SendResult(this, Configuration.CAMERA_AGENT_PERIOD));
	}

	class SendResult extends TickerBehaviour {

		public SendResult(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {

			cameraDriver.getData(new Camera.OnResult() {

				@Override
				public void onResult(String data) {

					if (!dataString.equals(data) && counter < 3) {
						DFAgentDescription[] result = null;
						try {
							result = ImageAnalyzerAgent.getDFAgents(CameraAgent.this);
						} catch (FIPAException e) {
							e.printStackTrace();
						}
						if (result != null & result.length > 0) {
							ACLMessage wiadomosc = new ACLMessage(ACLMessage.INFORM);
							for (int i = 0; i < result.length; i++) {
								wiadomosc.addReceiver(result[i].getName());
							}

							try {
								wiadomosc.setContentObject(data);
								myAgent.send(wiadomosc);
							} catch (Exception e) {
								e.printStackTrace(System.out);
							}
						}
						counter = 0;
					}
					counter = counter > 100 ? counter + 1 : 4;
				}
			});
		}
	}
}
