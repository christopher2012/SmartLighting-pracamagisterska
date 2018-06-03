package agents.lowlevel;

import drivers.remote.StreetLampRemote;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class StreetLampAgent extends Agent {

	public static String PREFIX_AGENT = "STREET_LAMP_";

	StreetLampRemote streetLampDriver;
	AID managerAID;

	@Override
	protected void setup() {
		super.setup();
		streetLampDriver = (StreetLampRemote) getArguments()[0];
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = receive();
				if (msg != null)
					try {
						streetLampDriver.setPower((float) msg.getContentObject());
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				block();
			}
		});
	}
}
