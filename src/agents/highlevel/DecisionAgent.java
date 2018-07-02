package agents.highlevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import agents.analytic.EnvironmentAnalyzerAgent;
import agents.analytic.ImageAnalyzerAgent;
import agents.analytic.MovementAnalyzerAgent;
import agents.lowlevel.AstronomicalClockAgent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import model.Decision;
import model.MicroEnvironment;
import model.PatternModel;
import smart_lighting.GuiGenerator;
import smart_lighting.Manager;
import utils.JSONKey;

public class DecisionAgent extends Agent {

	public final static String PREFIX_AGENT = "DECISION_ENGINE_";
	public static ConcurrentHashMap<String, MicroEnvironment> microEnvironments = new ConcurrentHashMap<>();

	private ArrayList<String> acceptedIDList = new ArrayList<>();
	public static ArrayList<PatternModel> patternArray = new ArrayList<>();

	@Override
	protected void setup() {
		register();

		acceptedIDList = (ArrayList<String>) getArguments()[0];
		for (String key : acceptedIDList) {
			microEnvironments.put(key, new MicroEnvironment(key));
		}
		addBehaviour(new DecisionProcessor(this));
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

	class DecisionProcessor extends CyclicBehaviour {

		public DecisionProcessor(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null)
				try {
					if (msg != null) {
						JSONObject jsonObject = new JSONObject(msg.getContentObject().toString());
						if (acceptedIDList.contains(jsonObject.getString(JSONKey.LAMP_ID))) {
							if (msg.getSender().getLocalName().contains(EnvironmentAnalyzerAgent.PREFIX_AGENT))
								processEnviromentData(new JSONObject(msg.getContentObject().toString()));
							else if (msg.getSender().getLocalName().contains(ImageAnalyzerAgent.PREFIX_AGENT))
								processImageData(new JSONObject(msg.getContentObject().toString()));
							else if (msg.getSender().getLocalName().contains(MovementAnalyzerAgent.PREFIX_AGENT))
								processMovementData(new JSONObject(msg.getContentObject().toString()));
						} else if (jsonObject.getString(JSONKey.LAMP_ID).equals(AstronomicalClockAgent.ID)) {
							if (jsonObject.has(JSONKey.TIME_OF_DAY))
								if (jsonObject.getString(JSONKey.TIME_OF_DAY).equals(JSONKey.NIGHT))
									MicroEnvironment.setNight(true);
								else
									MicroEnvironment.setNight(false);
						}

					}
				} catch (UnreadableException | JSONException e) {
					e.printStackTrace();
				}
			block();
		}

		private void processEnviromentData(JSONObject msg) throws JSONException {

			String lampID = msg.getString(JSONKey.LAMP_ID);
			MicroEnvironment microEvironment = microEnvironments.get(lampID);

			if (msg.has(JSONKey.VALUE)) {
				microEvironment.setIlluminance(Float.valueOf(msg.getString(JSONKey.VALUE)));
				microEvironment.calculatePower();
				sendDecision(new Decision(lampID, microEvironment.getPower()));

				GuiGenerator.instance().getStreetLightInfo(lampID).updateInfo(microEnvironments.get(lampID));

			}
		}

		private void processImageData(JSONObject msg) throws JSONException {

			String lampID = msg.getString(JSONKey.LAMP_ID);
			MicroEnvironment microEvironment = microEnvironments.get(lampID);

			if (msg.has("readings")) {
				microEvironment.clearActors();
				JSONArray jsonarray = msg.getJSONArray("readings");
				if (jsonarray.length() > 0) {
					for (int i = 0; i < jsonarray.length(); i++) {
						JSONObject jsonObject = jsonarray.getJSONObject(i);
						if (jsonObject.getString("type").equals("VEHICLE")) {
							microEvironment.setVehicle(true);
						} else if (jsonObject.getString("type").equals("PEDESTRIAN")) {
							microEvironment.setPedestrian(true);
						} else if (jsonObject.getString("type").equals("GROUP")) {
							microEvironment.setGroup(true);
						}
					}
				}
			}
		}

		private void processMovementData(JSONObject msg) throws JSONException {

			String lampID = msg.getString("lamp_id");

			MicroEnvironment microEvironment = DecisionAgent.microEnvironments.get(lampID);
			if (msg.has("sensingMovement")) {
				microEvironment.setMove(msg.getBoolean("sensingMovement"));
			}

		}

		private void sendDecision(Decision decision) {
			DFAgentDescription[] result = null;
			try {
				result = ExecutionAgent.getDFAgents(DecisionAgent.this);
			} catch (FIPAException e) {
				e.printStackTrace();
			}

			if (result != null & result.length > 0) {
				ACLMessage message = new ACLMessage(ACLMessage.INFORM);
				for (int i = 0; i < result.length; i++) {
					message.addReceiver(result[i].getName());
				}

				Map map = new HashMap();
				map.put(JSONKey.LAMP_ID, decision.lampID);
				map.put(JSONKey.POWER, decision.power);
				try {
					message.setContentObject(new JSONObject(map).toString());
					myAgent.send(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
