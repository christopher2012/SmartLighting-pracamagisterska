package analytic;

import java.io.IOException;
import java.util.ArrayList;

import org.neo4j.driver.v1.types.Node;

import drivers.Movement;
import drivers.agents.ActorsAgent;
import drivers.agents.MovementAgent;
import drivers.agents.StreetLampAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import smart_lighting.Simulation;
import smart_lighting.Simulation.ActorModel;
import smart_lighting.Simulation.OnCreateActorsListener;
import smart_lighting.Simulation.OnDataLoadListener;
import smart_lighting.Simulation.StreetLampModel;

public class Manager extends Agent implements OnDataLoadListener, OnCreateActorsListener {

	public static OnDataLoadListener onDataLoadListener;
	public static OnCreateActorsListener onCreateActorsListener;

	@Override
	protected void setup() {

		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = receive();
				if (msg != null)
					try {
						// System.out.print(" - " + myAgent.getLocalName() + "
						// <- " + msg.getContentObject());
						// System.out.println(" - localname: " +
						// msg.getSender().getLocalName());
						analyze(msg);
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				block();
			}

			public void analyze(ACLMessage aclMessage) throws UnreadableException, IOException {
				if (aclMessage != null) {
					if (aclMessage.getSender().getLocalName().contains(MovementAgent.PREFIX_MOVEMENT_AGENT)) {
						int streetLampNumber = Integer.valueOf(
								aclMessage.getSender().getLocalName().replace(MovementAgent.PREFIX_MOVEMENT_AGENT, ""));
						ACLMessage wiadomosc = new ACLMessage(ACLMessage.INFORM);
						wiadomosc.addReceiver(
								new AID(StreetLampAgent.PREFIX_STREET_LAMP_AGENT + streetLampNumber, AID.ISLOCALNAME));
						wiadomosc.setContentObject(aclMessage.getContentObject());
						myAgent.send(wiadomosc);
					}

				}
			}
		});

		onDataLoadListener = this;
		onCreateActorsListener = this;
		Thread t = new Thread() {
			@Override
			public void run() {
				Simulation simulation = new Simulation();
				simulation.start();
			}
		};
		t.start();

	}

	@Override
	public void onDataLoad(ArrayList<StreetLampModel> streetLamps) {
		AgentContainer kontener = getContainerController();
		for (StreetLampModel streetLamp : streetLamps) {
			try {
				AgentController movementKontroler = kontener.createNewAgent(
						MovementAgent.PREFIX_MOVEMENT_AGENT + streetLamp.id, "drivers.agents.MovementAgent",
						new Object[] { getAID(), new Movement(streetLamp.id) });
				movementKontroler.start();
				AgentController streetLampKontroler = kontener.createNewAgent(
						StreetLampAgent.PREFIX_STREET_LAMP_AGENT + streetLamp.id, "drivers.agents.StreetLampAgent",
						new Object[] { getAID(), new drivers.StreetLamp(streetLamp) });
				streetLampKontroler.start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onCreateActors(ArrayList<ActorModel> actors) {
		AgentContainer kontener = getContainerController();
		try {
			AgentController movementKontroler = kontener.createNewAgent(ActorsAgent.PREFIX_ACTORS_AGENT + 1,
					"drivers.agents.ActorsAgent", new Object[] { actors });
			movementKontroler.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
}
