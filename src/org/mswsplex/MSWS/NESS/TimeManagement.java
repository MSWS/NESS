package org.mswsplex.MSWS.NESS;

import java.util.concurrent.TimeUnit;

import org.bukkit.OfflinePlayer;

public class TimeManagement {
	public static String getTime(Long seconds) {
		double time = seconds;
		double dec;
		double res = time;
		dec = 0;
		String type = "Seconds";
		if (res >= 60) {
			res = TimeUnit.MINUTES.convert((long) time, TimeUnit.SECONDS);
			type = "Minutes";
			dec = (time % 60) / 60;
			if (res >= 60) {
				res = TimeUnit.HOURS.convert((long) time, TimeUnit.SECONDS);
				type = "Hours";
				dec = (time % 60) / 60;
				if (res >= 24) {
					res = TimeUnit.DAYS.convert((long) time, TimeUnit.SECONDS);
					type = "Days";
					dec = (time % 1440) / 1440;
				}
			}
		}
		return (Math.round(res) + ((dec + "").substring(1, 3))) + " " + type;
	}

	public static String lastOn(OfflinePlayer target) {
		return getTime((long) (System.currentTimeMillis() - target.getLastPlayed()) / 1000 / 60);
	}

	public static String getTime(Integer minutes) {
		double time = minutes;
		double dec;
		double res = time;
		dec = 0;
		String type = "Minutes";
		if (res >= 60) {
			res = TimeUnit.HOURS.convert((long) time, TimeUnit.MINUTES);
			type = "Hours";
			dec = (time % 60) / 60;
			if (res >= 24) {
				res = TimeUnit.DAYS.convert((long) time, TimeUnit.MINUTES);
				type = "Days";
				dec = (time % 1440) / 1440;
			}
		}
		return (Math.round(res) + ((dec + "").substring(1, 3))) + " " + type;
	}

	public static String getTime(Double mils) {
		boolean isNegative = mils < 0;
		double mil = Math.abs(mils);
		String names[] = { "milliseconds", "seconds", "minutes", "hours", "days", "weeks", "months", "years", "decades",
				"centuries" };
		String sNames[] = { "millisecond", "second", "minute", "hour", "day", "week", "month", "year", "decade",
				"century" };
		Double length[] = { 1.0, 1000.0, 60000.0, 3.6e+6, 8.64e+7, 6.048e+8, 2.628e+9, 3.154e+10, 3.154e+11,
				3.154e+12 };
		String suff = "";
		for (int i = length.length - 1; i >= 0; i--) {
			if (mil >= length[i]) {
				if (suff.equals(""))
					suff = names[i];
				mil = mil / length[i];
				if (mil == 1) {
					suff = sNames[i];
					// suff = suff.substring(0, suff.length() - 1);
				}
				break;
			}
		}
		String name = mil + "";
		if (Math.round(mil) == mil) {
			name = (int) Math.round(mil) + "";
		}
		if (name.contains(".")) {
			if (name.split("\\.")[1].length() > 2) {
				name = name.split("\\.")[0] + "."
						+ name.split("\\.")[1].substring(0, Math.min(name.split("\\.")[1].length(), 2));
			}
		}
		if (isNegative)
			name = "-" + name;
		return name + " " + suff;
	}
}
