package agents.highlevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
import model.MicroEvironment;
import smart_lighting.GuiGenerator;
import smart_lighting.Manager;
import utils.JSONKey;

public class DecisionAgent extends Agent {

	public final static String PREFIX_AGENT = "DECISION_ENGINE_";
	public Vector<Decision> decisionQueue;
	public Map<String, MicroEvironment> microEnvironments;

	private ArrayList<String> acceptedIDList = new ArrayList<>();

	@Override
	protected void setup() {
		register();

		acceptedIDList = (ArrayList<String>) getArguments()[0];
		microEnvironments = new HashMap<>();
		for (String key : acceptedIDList) {
			microEnvironments.put(key, new MicroEvironment(key));
		}
		addBehaviour(new DecisionProcessor(this));
		addBehaviour(new DecisionSender(this, 5000));
		decisionQueue = new Vector<>();
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
						System.out.println(jsonObject.toString());
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
									MicroEvironment.setNight(true);
								else
									MicroEvironment.setNight(false);
						}

					}
				} catch (UnreadableException | JSONException e) {
					e.printStackTrace();
				}
			block();
		}

		private void processEnviromentData(JSONObject msg) throws JSONException {

			String id = msg.getString(JSONKey.LAMP_ID);
			MicroEvironment microEvironment = microEnvironments.get(id);

			if (msg.has(JSONKey.VALUE)) {
				microEvironment.setIlluminance(Float.valueOf(msg.getString(JSONKey.VALUE)));

				if (microEvironment.calculatePower(microEnvironments))
					sendDecision(new Decision(id, microEvironment.getPower()));

				GuiGenerator.instance().getStreetLightInfo(id).updateInfo(microEnvironments.get(id));

			}
		}

		private void processImageData(JSONObject msg) throws JSONException {

			String id = msg.getString(JSONKey.LAMP_ID);
			MicroEvironment microEvironment = microEnvironments.get(id);

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
			System.out.println(msg.toString());
			MicroEvironment microEvironment = microEnvironments.get(lampID);
			if (msg.has("sensingMovement")) {
				microEvironment.setMove(msg.getBoolean("sensingMovement"));
				microEvironment.calculatePower(microEnvironments);

				if (microEvironment.calculatePower(microEnvironments))
					sendDecision(new Decision(lampID, microEvironment.getPower()));
				GuiGenerator.instance().getStreetLightInfo(lampID).updateInfo(microEnvironments.get(lampID));
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

	class DecisionSender extends TickerBehaviour {

		public DecisionSender(Agent agent, long period) {
			super(agent, period);
		}

		@Override
		protected void onTick() {

			if (decisionQueue.size() > 0) {
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
					for (Decision decision : decisionQueue) {
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
					decisionQueue.clear();
				}
			}

		}
	}
}
