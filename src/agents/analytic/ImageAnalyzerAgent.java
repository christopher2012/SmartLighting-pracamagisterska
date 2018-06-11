package agents.analytic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import agents.analytic.EnvironmentAnalyzerAgent.DataSender;
import agents.highlevel.DecisionAgent;
import agents.lowlevel.AstronomicalClockAgent;
import agents.lowlevel.CameraAgent;
import agents.lowlevel.IlluminanceAgent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import smart_lighting.Manager;
import utils.JSONKey;

public class ImageAnalyzerAgent extends Agent {

	public final static String PREFIX_AGENT = "IMAGE_ANALYZER_";
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
						if (acceptedIDList.contains(jsonObject.getString(JSONKey.LAMP_ID))) {
							if (msg.getSender().getLocalName().contains(CameraAgent.PREFIX_AGENT))
								analyzeImage(msg.getContentObject().toString());
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

		@SuppressWarnings("deprecation")
		private void analyzeImage(String msg) throws ParseException {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(msg);
				DateFormat df = new SimpleDateFormat(AstronomicalClockAgent.TIME_FORMAT);
				Map<String, String> map = new HashMap<>();
				
				sendData(new JSONObject(msg));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		public void sendData(JSONObject jsonObject) {
			DFAgentDescription[] result = null;
			try {
				result = DecisionAgent.getDFAgents(ImageAnalyzerAgent.this);
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
