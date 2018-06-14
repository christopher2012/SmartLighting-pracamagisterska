package agents.lowlevel;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import agents.analytic.EnvironmentAnalyzerAgent;
import agents.lowlevel.MovementAgent.SendResult;
import drivers.remote.IlluminanceRemote;
import drivers.remote.MovementRemote;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import utils.Configuration;

public class IlluminanceAgent extends Agent {

	public final static String PREFIX_AGENT = "ILLUMINANCE_";

	IlluminanceRemote illuminanceDriver;
	double illuminanceDouble = 0.0d;
	int counter = 0;

	@Override
	protected void setup() {
		super.setup();
		illuminanceDriver = (IlluminanceRemote) getArguments()[0];
		addBehaviour(new SendResult(this, Configuration.ILLUMINANCE_AGENT_PERIOD));
	}

	class SendResult extends TickerBehaviour {

		public SendResult(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			DFAgentDescription[] result = null;
			try {
				result = EnvironmentAnalyzerAgent.getDFAgents(IlluminanceAgent.this);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			if (result != null & result.length > 0) {
				double illuminance = illuminanceDriver.getIlluminance();

				if ((Double.compare(illuminance, illuminanceDouble) != 0 && counter < 3)) {

					Map<String, String> map = new HashMap<String, String>();
					map.put("lamp_id", illuminanceDriver.getID());
					map.put("value", String.valueOf(illuminance));
					JSONObject jsonObject = new JSONObject(map);

					ACLMessage wiadomosc = new ACLMessage(ACLMessage.INFORM);
					for (int i = 0; i < result.length; i++) {
						wiadomosc.addReceiver(result[i].getName());
					}

					try {
						wiadomosc.setContentObject(jsonObject.toString());
						myAgent.send(wiadomosc);
					} catch (Exception e) {
						e.printStackTrace(System.out);
					}
					counter = 0;
				}
				counter = counter > 100 ? counter + 1 : 4;
			}
		}
	}
}
