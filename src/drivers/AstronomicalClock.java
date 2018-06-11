package drivers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.shredzone.commons.suncalc.SunTimes;

import agents.lowlevel.AstronomicalClockAgent;
import drivers.remote.AstronomicalClockRemote;
import smart_lighting.ClockSimulation;

// output format SUNRISE;SUNSET;TIME

public class AstronomicalClock extends Driver implements AstronomicalClockRemote {

	public AstronomicalClock(String id) {
		super(id);
	}

	final double longitude = 50.061914;
	final double latitude = 19.938248;

	@Override
	public String getData() {
		Date date = ClockSimulation.getInstance().getCalendar().getTime();
		SunTimes times = SunTimes.compute().on(date).at(longitude, latitude).execute();

		DateFormat df = new SimpleDateFormat(AstronomicalClockAgent.TIME_FORMAT);
		return df.format(times.getRise()) + ";" + df.format(times.getSet()) + ";" + df.format(date);
	}

}
