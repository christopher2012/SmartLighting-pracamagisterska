package agents.analytic;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import agents.highlevel.DecisionAgent;
import agents.lowlevel.AstronomicalClockAgent;
import analytic.Manager;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class EnvironmentAnalyzerAgent extends Agent {

	public static String PREFIX_AGENT = "ENVIRONMENT_ANALYZER_";

	public final static String TIME_OF_DAY = "time_of_day";
	public final static String TIME = "time";

	@Override
	protected void setup() {
		register();
		addBehaviour(new DataSender(this));
	}

	class DataSender extends CyclicBehaviour {

		public DataSender(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null)
				try {
					if (msg != null) {
						System.out.println("message: " + msg.getContentObject().toString());
						System.out.println("sender: " + msg.getSender().toString());
						//analyze(msg.getContentObject().toString());
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			block();
		}

		@SuppressWarnings("deprecation")
		private void analyze(String msg) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(msg);
				Date sunrise = new Date(jsonObject.getString(AstronomicalClockAgent.SUNRISE));
				Date sunset = new Date(jsonObject.getString(AstronomicalClockAgent.SUNSET));
				String timeOfDay = sunrise.getTime() > sunset.getTime() ? "DAY" : "NIGHT";
				Map<String, String> map = new HashMap<>();
				map.put(TIME, jsonObject.getString(AstronomicalClockAgent.TIME));
				map.put(TIME_OF_DAY, jsonObject.getString(timeOfDay));
				sendData(new JSONObject(map));

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		public void sendData(JSONObject jsonObject) {
			DFAgentDescription[] result = null;
			try {
				result = DecisionAgent.getDFAgents(EnvironmentAnalyzerAgent.this);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			if (result != null & result.length > 0) {


				ACLMessage wiadomosc = new ACLMessage(ACLMessage.INFORM);
				wiadomosc.addReceiver(result[0].getName());

				try {
					wiadomosc.setContentObject(jsonObject.toString());
					myAgent.send(wiadomosc);
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}

			}
		}

	}

	public static DFAgentDescription[] getDFAgents(Agent agent) throws FIPAException {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(PREFIX_AGENT);
		dfd.addServices(sd);

		DFAgentDescription[] result = DFService.search(agent, dfd);

		return result;
	}

	public void register() {

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType(PREFIX_AGENT);
		dfd.addServices(sd);
		Manager.registerService(this, dfd);
	}
}
