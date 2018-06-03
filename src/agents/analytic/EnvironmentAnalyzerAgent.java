package agents.analytic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import agents.highlevel.DecisionAgent;
import agents.lowlevel.AstronomicalClockAgent;
import agents.lowlevel.IlluminanceAgent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import model.Decision;
import smart_lighting.Manager;
import utils.JSONKey;

public class EnvironmentAnalyzerAgent extends Agent {

	public final static String PREFIX_AGENT = "ENVIRONMENT_ANALYZER_";
	private ArrayList<String> acceptedIDList = new ArrayList<>();

	@Override
	protected void setup() {
		register();
		acceptedIDList = (ArrayList<String>) getArguments()[0];
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
						JSONObject jsonObject = new JSONObject(msg.getContentObject().toString());
						if (acceptedIDList.contains(jsonObject.getString(JSONKey.LAMP_ID))
								|| jsonObject.getString(JSONKey.LAMP_ID).equals("-1")) {
							if (msg.getSender().getLocalName().contains(AstronomicalClockAgent.PREFIX_AGENT))
								analyzeAstronomicalClock(msg.getContentObject().toString());
							else if (msg.getSender().getLocalName().contains(IlluminanceAgent.PREFIX_AGENT))
								analyzeIlluminance(msg.getContentObject().toString());
						}
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			block();
		}

		private void analyzeIlluminance(String msg) {
			try {
				System.out.println("message Ill: " + msg.toString());
				sendData(new JSONObject(msg));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings("deprecation")
		private void analyzeAstronomicalClock(String msg) throws ParseException {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(msg);
				DateFormat df = new SimpleDateFormat(AstronomicalClockAgent.TIME_FORMAT);
				Date sunrise = df.parse(jsonObject.getString(JSONKey.SUNRISE));
				Date sunset = df.parse(jsonObject.getString(JSONKey.SUNSET));
				String timeOfDay = sunrise.getTime() > sunset.getTime() ? JSONKey.DAY : JSONKey.NIGHT;
				Map<String, String> map = new HashMap<>();
				map.put(JSONKey.LAMP_ID, jsonObject.getString(JSONKey.LAMP_ID));
				map.put(JSONKey.TIME, jsonObject.getString(JSONKey.TIME));
				map.put(JSONKey.TIME_OF_DAY, timeOfDay);
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

				ACLMessage message = new ACLMessage(ACLMessage.INFORM);
				for (int i = 0; i < result.length; i++) {
					message.addReceiver(result[i].getName());
				}

				try {
					message.setContentObject(jsonObject.toString());
					myAgent.send(message);
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
