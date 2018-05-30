package agents.highlevel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import agents.analytic.EnvironmentAnalyzerAgent;
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

public class DecisionAgent extends Agent {

	public static String PREFIX_AGENT = "DECISION_ENGINE_";

	@Override
	protected void setup() {
		super.setup();
		register();
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
	

	class DecisionSender extends CyclicBehaviour {

		public DecisionSender(Agent agent) {
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
						execute(msg.getContentObject().toString());
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			block();
		}

		@SuppressWarnings("deprecation")
		private void execute(String msg) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(msg);
				System.out.println("message---- decision: " + jsonObject.toString());

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		public void sendDecision(JSONObject jsonObject) {
			
		}
	}
}
