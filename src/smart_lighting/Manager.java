package smart_lighting;

import java.io.IOException;
import java.util.ArrayList;

import org.neo4j.driver.v1.types.Node;

import agents.analytic.EnvironmentAnalyzerAgent;
import agents.analytic.ImageAnalyzerAgent;
import agents.analytic.MovementAnalyzerAgent;
import agents.highlevel.DecisionAgent;
import agents.highlevel.ExecutionAgent;
import agents.lowlevel.AstronomicalClockAgent;
import agents.lowlevel.CameraAgent;
import agents.lowlevel.IlluminanceAgent;
import agents.lowlevel.MovementAgent;
import agents.lowlevel.StreetLampAgent;
import agents.lowlevel.VelocityAgent;
import drivers.AstronomicalClock;
import drivers.Camera;
import drivers.Illuminance;
import drivers.Movement;
import drivers.Velocity;
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
		onDataLoadListener = Manager.this;
		onCreateActorsListener = Manager.this;
/*
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
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

		AgentContainer container = getContainerController();
		for (StreetLampModel streetLamp : streetLamps) {
			try {
				AgentController illuminanceControler = container.createNewAgent(
						IlluminanceAgent.PREFIX_AGENT + streetLamp.id, "agents.lowlevel.IlluminanceAgent",
						new Object[] { new Illuminance(streetLamp.id) });
				illuminanceControler.start();

				AgentController streetLampControler = container.createNewAgent(
						StreetLampAgent.PREFIX_AGENT + streetLamp.id, "agents.lowlevel.StreetLampAgent",
						new Object[] { new drivers.StreetLamp(streetLamp) });
				streetLampControler.start();

				AgentController cameraControler = container.createNewAgent(CameraAgent.PREFIX_AGENT + streetLamp.id,
						"agents.lowlevel.CameraAgent", new Object[] { new Camera(streetLamp.id) });
				cameraControler.start();

				AgentController movementControler = container.createNewAgent(MovementAgent.PREFIX_AGENT + streetLamp.id,
						"agents.lowlevel.MovementAgent", new Object[] { new Movement(streetLamp.id) });
				movementControler.start();

				AgentController velocityControler = container.createNewAgent(VelocityAgent.PREFIX_AGENT + streetLamp.id,
						"agents.lowlevel.VelocityAgent", new Object[] { new Velocity(streetLamp.id) });
				velocityControler.start();

			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}

		try {

			AgentController clkControler = container.createNewAgent(AstronomicalClockAgent.PREFIX_AGENT + 1,
					"agents.lowlevel.AstronomicalClockAgent", new Object[] { new AstronomicalClock("-1") });
			clkControler.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		createAgentsWithIDCheck(Configuration.DECISION_AGENT_COUNT, DecisionAgent.PREFIX_AGENT,
				"agents.highlevel.DecisionAgent", streetLamps);

		createAgentsWithIDCheck(Configuration.EXECUTION_AGENT_COUNT, ExecutionAgent.PREFIX_AGENT,
				"agents.highlevel.ExecutionAgent", streetLamps);

		createAgentsWithIDCheck(Configuration.ENVIRONMENT_ANALYZER_AGENT_COUNT, EnvironmentAnalyzerAgent.PREFIX_AGENT,
				"agents.analytic.EnvironmentAnalyzerAgent", streetLamps);

		createAgentsWithIDCheck(Configuration.IMAGE_ANALYZER_AGENT_COUNT, ImageAnalyzerAgent.PREFIX_AGENT,
				"agents.analytic.ImageAnalyzerAgent", streetLamps);

		createAgentsWithIDCheck(Configuration.MOVEMENT_ANALYZER_AGENT_COUNT, MovementAnalyzerAgent.PREFIX_AGENT,
				"agents.analytic.MovementAnalyzerAgent", streetLamps);

	}

	@SuppressWarnings("unchecked")
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

	public static void registerService(Agent agent, DFAgentDescription dfd) {
		try {
			DFService.register(agent, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreateActors(ArrayList<ActorModel> actors) {

		ActorsSimulation actorsSimulation = new ActorsSimulation(actors); //
		actorsSimulation.start();
	}
}
