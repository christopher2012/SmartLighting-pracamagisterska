package drivers;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;

import drivers.Movement.OnResult;
import drivers.remote.VelocityRemote;
import utils.JSONKey;
import utils.ApiConnector;

public class Velocity extends Driver implements VelocityRemote {

	public static final String DATA_URL = "http://localhost:8080/vel-dir-sensors/";

	public Velocity(String id) {
		super(id);
	}

	@Override
	public void getData(OnResult onResult) {

		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient httpClient = new HttpClient(connectionManager);

		new ApiConnector(httpClient, new GetMethod(DATA_URL + getID()), new ApiConnector.OnResponseListener() {

			@Override
			public void onResponse(JSONObject jsonObject) {
				try {
					onResult.onResult(jsonObject.put(JSONKey.LAMP_ID, getID()).toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public interface OnResult {
		void onResult(String data);
	}

}
