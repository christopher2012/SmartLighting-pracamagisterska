package drivers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.shredzone.commons.suncalc.SunTimes;

import agents.lowlevel.AstronomicalClockAgent;
import drivers.remote.IlluminanceRemote;
import smart_lighting.ClockSimulation;
import smart_lighting.CloudinessSimulation;

public class Illuminance extends Driver implements IlluminanceRemote {

	final double longitude = 50.061914;
	final double latitude = 19.938248;

	public Illuminance(String id) {
		super(id);
	}

	@Override
	public double getIlluminance() {
		Date date = ClockSimulation.getInstance().getCalendar().getTime();
		SunTimes times = SunTimes.compute().on(new Date(date.getTime())).at(longitude, latitude).execute();
		SunTimes timesM60 = SunTimes.compute().on(new Date(date.getTime() - 60000 * 60)).at(longitude, latitude)
				.execute();

		DateFormat df = new SimpleDateFormat(AstronomicalClockAgent.TIME_FORMAT);
		Date sunrise = times.getRise();
		Date sunset = times.getSet();
		Date suriseM60 = timesM60.getRise();
		Date sunsetM60 = timesM60.getSet();

		int sunriseDiff = (int) Math.abs((date.getTime() - suriseM60.getTime()) / 60000);
		int sunsetDiff = (int) Math.abs((date.getTime() - sunsetM60.getTime()) / 60000);
		boolean isNight = sunrise.getTime() < sunset.getTime();

		float timeOfDayRatio = 0;

		if (sunriseDiff < 60) {
			timeOfDayRatio = isNight ? ((60.0f - sunriseDiff) / 120.0f) : ((60f + sunriseDiff) / 120.0f);
		} else if (sunsetDiff < 60) {
			timeOfDayRatio = (isNight ? ((60.f - sunsetDiff) / 120.0f) : (60.0f + sunsetDiff) / 120.0f);
		} else {
			timeOfDayRatio = isNight ? 0.0f : 1.0f;
		}

		float cloudiness = CloudinessSimulation.getInstance().getCloudinnessItemRatio();
		timeOfDayRatio = timeOfDayRatio * cloudiness;

		return timeOfDayRatio;
	}

}
