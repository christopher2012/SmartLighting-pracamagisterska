package smart_lighting;

import java.io.IOException;
import java.util.ArrayList;

import org.neo4j.driver.v1.types.Node;

import agents.analytic.EnvironmentAnalyzerAgent;
import agents.highlevel.DecisionAgent;
import agents.highlevel.ExecutionAgent;
import agents.lowlevel.AstronomicalClockAgent;
import agents.lowlevel.IlluminanceAgent;
import agents.lowlevel.MovementAgent;
import agents.lowlevel.StreetLampAgent;
import drivers.AstronomicalClock;
import drivers.Illuminance;
import drivers.Movement;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import smart_lighting.Simulation.ActorModel;
import smart_lighting.Simulation.OnCreateActorsListener;
import smart_lighting.Simulation.OnDataLoadListener;
import smart_lighting.Simulation.StreetLampModel;
import utils.Configuration;

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
					if (aclMessage.getSender().getLocalName().contains(MovementAgent.PREFIX_AGENT)) {
						int streetLampNumber = Integer
								.valueOf(aclMessage.getSender().getLocalName().replace(MovementAgent.PREFIX_AGENT, ""));
						ACLMessage wiadomosc = new ACLMessage(ACLMessage.INFORM);
						wiadomosc
								.addReceiver(new AID(StreetLampAgent.PREFIX_AGENT + streetLampNumber, AID.ISLOCALNAME));
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


		ClockSimulation.init();
		AgentContainer kontener = getContainerController();
		for (StreetLampModel streetLamp : streetLamps) {
			try {
				AgentController illuminanceControler = kontener.createNewAgent(
						IlluminanceAgent.PREFIX_AGENT + streetLamp.id, "agents.lowlevel.IlluminanceAgent",
						new Object[] { new Illuminance(streetLamp.id) });
				illuminanceControler.start();
				/*
				 * AgentController movementKontroler = kontener.createNewAgent(
				 * MovementAgent.PREFIX_MOVEMENT_AGENT + streetLamp.id,
				 * "drivers.agents.MovementAgent", new Object[] { getAID(), new
				 * Movement(streetLamp.id) }); movementKontroler.start();
				 */

				AgentController streetLampKontroler = kontener.createNewAgent(
						StreetLampAgent.PREFIX_AGENT + streetLamp.id, "agents.lowlevel.StreetLampAgent",
						new Object[] { new drivers.StreetLamp(streetLamp) });
				streetLampKontroler.start();

			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}

		createAgentsWithIDCheck(Configuration.DECISION_AGENT_COUNT, DecisionAgent.PREFIX_AGENT,
				"agents.highlevel.DecisionAgent", streetLamps);
		createAgentsWithIDCheck(Configuration.EXECUTION_AGENT_COUNT, ExecutionAgent.PREFIX_AGENT,
				"agents.highlevel.ExecutionAgent", streetLamps);
		createAgentsWithIDCheck(Configuration.ENVIRONMENT_ANALYZER_AGENT_COUNT, EnvironmentAnalyzerAgent.PREFIX_AGENT,
				"agents.analytic.EnvironmentAnalyzerAgent", streetLamps);
		// createAgentsWithIDCheck(DECISION_AGENT_COUNT,
		// DecisionAgent.PREFIX_AGENT, "agents.highlevel.DecisionAgent",
		// streetLamps);
		// createAgentsWithIDCheck(DECISION_AGENT_COUNT,
		// DecisionAgent.PREFIX_AGENT, "agents.highlevel.DecisionAgent",
		// streetLamps);
		/*
		 * ArrayList<String>[] decisionAgentsArgs = new
		 * ArrayList[DECISION_AGENT_COUNT]; ArrayList<String>[]
		 * executionAgentsArgs = new ArrayList[EXECUTION_AGENT_COUNT];
		 * 
		 * for (int i = 0; i < DECISION_AGENT_COUNT; i++) {
		 * decisionAgentsArgs[i] = new ArrayList<>(); }
		 * 
		 * for (int i = 0; i < EXECUTION_AGENT_COUNT; i++) {
		 * executionAgentsArgs[i] = new ArrayList<>(); }
		 * 
		 * int decCounter = 0; int exeCounter = 0;
		 * 
		 * for (StreetLampModel streetLamp : streetLamps) {
		 * decisionAgentsArgs[decCounter].add(streetLamp.id); decCounter++; if
		 * (decCounter >= DECISION_AGENT_COUNT) decCounter = 0;
		 * 
		 * executionAgentsArgs[exeCounter].add(streetLamp.id); exeCounter++; if
		 * (exeCounter >= EXECUTION_AGENT_COUNT) exeCounter = 0; }
		 * 
		 * 
		 * for (int i = 0; i < DECISION_AGENT_COUNT; i++) { try {
		 * AgentController envControler =
		 * kontener.createNewAgent(DecisionAgent.PREFIX_AGENT + (i + 1),
		 * "agents.highlevel.DecisionAgent", new Object[] {
		 * decisionAgentsArgs[i] }); envControler.start(); } catch
		 * (StaleProxyException e) { e.printStackTrace(); } }
		 * 
		 * for (int i = 0; i < EXECUTION_AGENT_COUNT; i++) { try {
		 * AgentController envControler =
		 * kontener.createNewAgent(ExecutionAgent.PREFIX_AGENT + (i + 1),
		 * "agents.highlevel.ExecutionAgent", new Object[] {
		 * executionAgentsArgs[i] }); envControler.start(); } catch
		 * (StaleProxyException e) { e.printStackTrace(); } }
		 * 
		 * for (int i = 0; i < EXECUTION_AGENT_COUNT; i++) { try {
		 * AgentController envControler =
		 * kontener.createNewAgent(ExecutionAgent.PREFIX_AGENT + (i + 1),
		 * "agents.highlevel.ExecutionAgent", new Object[] {
		 * executionAgentsArgs[i] }); envControler.start(); } catch
		 * (StaleProxyException e) { e.printStackTrace(); } }
		 */
		try {
			AgentController clkControler = kontener.createNewAgent(AstronomicalClockAgent.PREFIX_AGENT + 1,
					"agents.lowlevel.AstronomicalClockAgent", new Object[] { new AstronomicalClock("-1") });
			clkControler.start();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createAgentsWithIDCheck(int count, String prefix, String className,
			ArrayList<StreetLampModel> streetLamps) {
		ArrayList<String>[] agentsArg = new ArrayList[count];
		for (int i = 0; i < count; i++) {
			agentsArg[i] = new ArrayList<>();
		}

		int counter = 0;

		for (StreetLampModel streetLamp : streetLamps) {
			agentsArg[counter].add(streetLamp.id);
			counter++;
			if (counter >= count)
				counter = 0;
		}

		AgentContainer kontener = getContainerController();

		for (int i = 0; i < count; i++) {
			try {
				AgentController envControler = kontener.createNewAgent(prefix + (i + 1), className,
						new Object[] { agentsArg[i] });
				envControler.start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * @Override public void onCreateActors(ArrayList<ActorModel> actors) { //
	 * ActorsSimulation actorsSimulation = new ActorsSimulation(actors); //
	 * actorsSimulation.start(); }
	 */
	public static void registerService(Agent agent, DFAgentDescription dfd) {
		try {
			DFService.register(agent, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreateActors(ArrayList<ActorModel> actors) {
		// TODO Auto-generated method stub
		//ActorsSimulation actorsSimulation = new ActorsSimulation(actors); //
		//actorsSimulation.start();
	}
}
