package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiConnector extends Thread {

	HttpClient httpClient;
	GetMethod getMethod;
	OnResponseListener callback;

	public ApiConnector(HttpClient httpClient, GetMethod getMethod, OnResponseListener callback) {
		this.httpClient = httpClient;
		this.getMethod = getMethod;
		this.callback = callback;
	}

	@Override
	public void run() {
		JSONObject jsonObject = null;
		try {
			httpClient.executeMethod(getMethod);
			BufferedReader in = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			jsonObject = new JSONObject(response.toString());
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		callback.onResponse(jsonObject);
	}

	public interface OnResponseListener {
		void onResponse(JSONObject jsonObject);
	}
}
