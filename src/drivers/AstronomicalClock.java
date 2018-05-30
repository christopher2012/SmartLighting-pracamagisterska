package drivers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.shredzone.commons.suncalc.SunTimes;

import drivers.interfaces.AstronomicalClockRemote;
import smart_lighting.ClockSimulation;

// output format SUNRISE;SUNSET;TIME

public class AstronomicalClock implements AstronomicalClockRemote {

	final double longitude = 50.061914;
	final double latitude = 19.938248;

	public final static String TIME_FORMAT = "HH:mm:ss";

	@Override
	public String getData() {
		Date date = ClockSimulation.getInstance().getCalendar().getTime();
		SunTimes times = SunTimes.compute().on(date).at(longitude, latitude).execute();

		return times.getRise() + ";" + times.getSet() + ";" + date.toGMTString();
	}

}
