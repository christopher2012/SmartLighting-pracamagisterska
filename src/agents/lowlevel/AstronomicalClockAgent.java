package agents.lowlevel;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import agents.analytic.EnvironmentAnalyzerAgent;
import drivers.interfaces.AstronomicalClockRemote;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

public class AstronomicalClockAgent extends Agent {

	private final static int PERIOD_BEHAVIOUR = 1000 * 60;

	public final static String PREFIX_AGENT = "ASTRONOMICAL_CLOCK_";
	public final static String SUNRISE = "sunrise";
	public final static String SUNSET = "sunset";
	public final static String TIME = "time";

	private AstronomicalClockRemote astronomicalClockDriver;

	@Override
	protected void setup() {
		super.setup();

		astronomicalClockDriver = (AstronomicalClockRemote) getArguments()[0];
		addBehaviour(new DataService(this, PERIOD_BEHAVIOUR));
	}

	public class DataService extends TickerBehaviour {

		public DataService(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			DFAgentDescription[] result = null;
			try {
				result = EnvironmentAnalyzerAgent.getDFAgents(AstronomicalClockAgent.this);
			} catch (FIPAException e) {
				e.printStackTrace();
			}
			if (result != null & result.length > 0) {

				String data = astronomicalClockDriver.getData();
				System.out.println(data);
				Map<String, String> map = new HashMap<String, String>();
				map.put(SUNRISE, data.split(";")[0]);
				map.put(SUNSET, data.split(";")[1]);
				map.put(TIME, data.split(";")[2]);
				JSONObject jsonObject = new JSONObject(map);
				System.out.println(jsonObject.toString());

				ACLMessage wiadomosc = new ACLMessage(ACLMessage.INFORM);
				wiadomosc.addReceiver(result[0].getName());

				try {
					wiadomosc.setContentObject(jsonObject.toString());
					myAgent.send(wiadomosc);
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}

			}
		}

	}

}
