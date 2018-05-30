package smart_lighting;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.Node;

public class ClockSimulation {

	private final static String TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
	private Node node;
	Calendar cal;
	SimpleDateFormat simpleFormatter;

	private static ClockSimulation clockSimulation;

	public static void init() {
		clockSimulation = new ClockSimulation();
		Timer timer = new Timer("current-time");
		clockSimulation.cal = Calendar.getInstance();
		timer.schedule(clockSimulation.new UpdateTimeTask(), 1000, 1000);
		clockSimulation.simpleFormatter = new SimpleDateFormat(TIME_FORMAT);

	}

	public static ClockSimulation getInstance() {
		return clockSimulation;
	}
	
	public Calendar getCalendar(){
		return cal;
	}

	public class UpdateTimeTask extends TimerTask {

		@Override
		public void run() {
			cal.add(Calendar.SECOND, 1);
			String currentTime = simpleFormatter.format(cal.getTime());
			GuiGenerator.instance().setCurrentTime(currentTime);
		}
	}
	
	
}
