package drivers.agents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import javafx.scene.Node;
import smart_lighting.Simulation.ActorModel;

@SuppressWarnings("serial")
public class ActorsAgent extends Agent {

	public static String PREFIX_ACTORS_AGENT = "ACTORS_";

	ArrayList<ActorModel> actors;

	@SuppressWarnings("unchecked")
	@Override
	protected void setup() {
		super.setup();
		actors = (ArrayList<ActorModel>) getArguments()[0];
		addBehaviour(new SendResult(this, 500));
	}

	@SuppressWarnings("serial")
	class SendResult extends TickerBehaviour {

		public SendResult(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			for (ActorModel actor : actors) {
				Thread t = new Thread() {
					@Override
					public void run() {
						updateView(actor);
					}

					private void updateView(ActorModel actor) {
						String url = "http://localhost:8080/actors/" + actor.id;

						try {
							URL obj = new URL(url);
							HttpURLConnection con = (HttpURLConnection) obj.openConnection();
							con.setRequestMethod("GET");
							con.setRequestProperty("User-Agent", "Mozilla/5.0");
							BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
							String inputLine;
							StringBuffer response = new StringBuffer();
							while ((inputLine = in.readLine()) != null) {
								response.append(inputLine);
							}
							in.close();
							JSONObject myResponse = new JSONObject(response.toString());
							double x = myResponse.getJSONObject("currentCoords").getDouble("x");
							double y = myResponse.getJSONObject("currentCoords").getDouble("y");

							actor.node.setTranslateX(x);
							actor.node.setTranslateY(y);
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				t.start();
			}
		}

	}

}
