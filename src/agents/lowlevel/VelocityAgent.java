package agents.lowlevel;

import agents.analytic.MovementAnalyzerAgent;
import agents.lowlevel.MovementAgent.SendResult;
import drivers.Velocity;
import drivers.remote.MovementRemote;
import drivers.remote.VelocityRemote;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import utils.Configuration;

public class VelocityAgent extends Agent {

	public static String PREFIX_AGENT = "VELOCITY_";

	VelocityRemote velocityRemote;

	@Override
	protected void setup() {
		super.setup();
		velocityRemote = (VelocityRemote) getArguments()[0];
		addBehaviour(new SendResult(this, Configuration.VELOCITY_AGENT_PERIOD));
	}

	class SendResult extends TickerBehaviour {

		public SendResult(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			velocityRemote.getData(new Velocity.OnResult() {
				
				@Override
				public void onResult(String data) {
					DFAgentDescription[] result = null;
					try {
						result = MovementAnalyzerAgent.getDFAgents(VelocityAgent.this);
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
				}
			});
		}
	}
}
