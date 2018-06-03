package agents.lowlevel;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import agents.analytic.EnvironmentAnalyzerAgent;
import drivers.remote.AstronomicalClockRemote;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import utils.JSONKey;

public class AstronomicalClockAgent extends Agent {

	private final static int PERIOD_BEHAVIOUR = 1000 * 60;

	public final static String PREFIX_AGENT = "ASTRONOMICAL_CLOCK_";

	public final static String TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

	public final static String ID = "-1";
		
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
				Map<String, String> map = new HashMap<String, String>();
				map.put(JSONKey.LAMP_ID, astronomicalClockDriver.getID());
				map.put(JSONKey.SUNRISE, data.split(";")[0]);
				map.put(JSONKey.SUNSET, data.split(";")[1]);
				map.put(JSONKey.TIME, data.split(";")[2]);
				JSONObject jsonObject = new JSONObject(map);

				ACLMessage wiadomosc = new ACLMessage(ACLMessage.INFORM);
				for (int i = 0; i < result.length; i++) 
					wiadomosc.addReceiver(result[i].getName());

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
