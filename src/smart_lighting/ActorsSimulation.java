package smart_lighting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import smart_lighting.Simulation.ActorModel;

public class ActorsSimulation {

	ArrayList<ActorModel> actors;
	LocationUpdater locationUpdater = new LocationUpdater();
	Timer timer = null;

	public ActorsSimulation(ArrayList<ActorModel> actors) {
		this.actors = actors;
	}

	public void deleteActor(String id) {
		for (ActorModel actorModel : actors) {
			if (actorModel.id.equals(id)) {
				actors.remove(actorModel);
				break;
			}
		}
	}

	class LocationUpdater extends TimerTask {

		@Override
		public void run() {
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

							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									actor.node.setTranslateX(x);
									actor.node.setTranslateY(y);
								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				t.start();

			}

		}
	}

	public void start() {
		if (timer != null)
			timer.cancel();

		timer = new Timer("timer");
		timer.schedule(new LocationUpdater(), 1000, 1000);

	}

	public void stop() {
		if (timer != null)
			timer.cancel();
	}

}
