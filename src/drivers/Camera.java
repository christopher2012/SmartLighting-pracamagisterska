package drivers;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;

import drivers.remote.CameraRemote;
import utils.JSONKey;
import utils.ApiConnector;

public class Camera extends Driver implements CameraRemote {

	public Camera(String id) {
		super(id);
	}

	public final static String DATA_URL = "http://localhost:8080/cameras/";

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
