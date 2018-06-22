package smart_lighting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import agents.highlevel.DecisionAgent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.MicroEnvironment;

public class ResultsGenerator {

	private static ResultsGenerator resultsGenerator = null;
	BufferedWriter writer;
	Timer timer;
	String path = null;

	public Map<String, MicroEnvironment> microEnvironments;

	public static ResultsGenerator instance() {

		if (resultsGenerator == null)
			resultsGenerator = new ResultsGenerator();

		return resultsGenerator;
	}

	public void generate() {
		if (timer != null)
			timer.cancel();
		// TODO Auto-generated method stub
		stop();

		try {
			writer = new BufferedWriter(new FileWriter("results.data"));
			File f = new File("results.data");
			path = f.getAbsolutePath();

			Alert alert = new Alert(AlertType.CONFIRMATION,
					"Wygenerowany plik zostanie zapisany do lokalizacji " + path);
			alert.setTitle("Komunikat");
			alert.setHeaderText("Uruchomiono symulacje");
			alert.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timer = new Timer("result-timer");
		timer.schedule(resultsGenerator.new UpdateResults(), 1000, 1000);
	}

	public void stop() {
		try {
			if (timer != null)
				timer.cancel();
			if (writer != null)
				writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class UpdateResults extends TimerTask {

		boolean isFirstRun = true;

		@Override
		public void run() {
			String dataRow = "";
			Iterator<Entry<String, MicroEnvironment>> it = DecisionAgent.microEnvironments.entrySet().iterator();
			if (isFirstRun) {
				dataRow += "Czas;";
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					dataRow += "ID_" + pair.getKey() + ";";

				}

				if (dataRow != null && dataRow.length() > 0 && dataRow.charAt(dataRow.length() - 1) == ';')
					dataRow = dataRow.substring(0, dataRow.length() - 1);
			}

			Date date = ClockSimulation.getInstance().getCalendar().getTime();
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			dataRow += String.format("%n") + df.format(date) + ";";
			it = DecisionAgent.microEnvironments.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				dataRow += ((MicroEnvironment) pair.getValue()).getPower() + ";";

			}

			if (dataRow != null && dataRow.length() > 0 && dataRow.charAt(dataRow.length() - 1) == ';')
				dataRow = dataRow.substring(0, dataRow.length() - 1);

			try {
				dataRow = dataRow.replace(".", ",");
				writer.write(dataRow);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			isFirstRun = false;
		}
	}
}
