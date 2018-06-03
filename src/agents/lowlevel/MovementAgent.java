package agents.lowlevel;

import drivers.remote.MovementRemote;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class MovementAgent extends Agent {
	
	public static String PREFIX_AGENT = "MOVEMENT_";

	MovementRemote movementDriver;
	AID managerAID;

	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		super.setup();
		managerAID = (AID) getArguments()[0];
		movementDriver = (MovementRemote) getArguments()[1];
		addBehaviour(new SendResult(this, 500));
	}

	class SendResult extends TickerBehaviour {

		public SendResult(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			ACLMessage wiadomosc = new ACLMessage(ACLMessage.INFORM);
			wiadomosc.addReceiver(managerAID);
			try {
				wiadomosc.setContentObject(movementDriver.isMove());
				myAgent.send(wiadomosc);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}

	}

}
