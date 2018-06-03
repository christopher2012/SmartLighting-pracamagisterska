package agents.highlevel;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import agents.lowlevel.StreetLampAgent;
import drivers.remote.AstronomicalClockRemote;
import jade.core.AID;
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

public class ExecutionAgent extends Agent {

	public final static String PREFIX_AGENT = "EXECUTION_ENGINE_";
	private ArrayList<String> acceptedIDList = new ArrayList<>();

	@Override
	protected void setup() {
		super.setup();

		acceptedIDList = (ArrayList<String>) getArguments()[0];

		register();
		addBehaviour(new DecisionSender(this));
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
						JSONObject jsonObject = new JSONObject(msg.getContentObject().toString());
						String id = jsonObject.getString(JSONKey.LAMP_ID);
						if (acceptedIDList.contains(id)) {
							System.out.println("exe: " + msg.getContentObject().toString());
							//System.out.println("message: " + msg.getContentObject().toString());
							//System.out.println("sender: " + msg.getSender().toString());
							//System.out.println("receiver: " + getAID().toString());
							ACLMessage command = new ACLMessage(ACLMessage.INFORM);
							command.setContentObject(Float.valueOf(jsonObject.getString(JSONKey.POWER)));
							command.addReceiver(new AID(StreetLampAgent.PREFIX_AGENT + id, AID.ISLOCALNAME));
							send(command);
						}
					}
				} catch (UnreadableException | NumberFormatException | IOException | JSONException e) {
					e.printStackTrace();
				}
			block();
		}
	}
}
