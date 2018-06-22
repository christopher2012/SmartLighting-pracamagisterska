package agents.lowlevel;

import agents.analytic.ImageAnalyzerAgent;
import agents.analytic.MovementAnalyzerAgent;
import drivers.Camera;
import drivers.Movement;
import drivers.remote.MovementRemote;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import utils.Configuration;

public class MovementAgent extends Agent {

	public static String PREFIX_AGENT = "MOVEMENT_";

	MovementRemote movementDriver;
	String dataString = "";
	int counter = 0;

	@Override
	protected void setup() {
		super.setup();
		movementDriver = (MovementRemote) getArguments()[0];
		addBehaviour(new SendResult(this, Configuration.MOVEMENT_AGENT_PERIOD));
	}

	class SendResult extends TickerBehaviour {

		public SendResult(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			movementDriver.getData(new Movement.OnResult() {

				@Override
				public void onResult(String data) {
					if (!dataString.equals(data))
						counter = 0;
					if (!dataString.equals(data) || counter < 3 || true){
						DFAgentDescription[] result = null;
						try {
							result = MovementAnalyzerAgent.getDFAgents(MovementAgent.this);
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
								//System.out.println(data);
								myAgent.send(wiadomosc);
							} catch (Exception e) {
								e.printStackTrace(System.out);
							}
						}
						dataString = data;
					}
					counter = counter > 100 ? counter + 1 : 4;
				}
			});
		}
	}
}
