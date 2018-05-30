package agents.lowlevel;

import drivers.interfaces.StreetLampRemote;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class StreetLampAgent extends Agent {

	public static String PREFIX_STREET_LAMP_AGENT = "STREET_LAMP_";
	
	StreetLampRemote streetLampDriver;
	AID managerAID;

	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		super.setup();
		managerAID = (AID) getArguments()[0];
		streetLampDriver = (StreetLampRemote) getArguments()[1];
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = receive();
				if (msg != null)
					try {
						//System.out.println(" - " + myAgent.getLocalName() + " <- " + msg.getContentObject());
						streetLampDriver.changeState((boolean) msg.getContentObject());
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				block();
			}
		});
	}

}
