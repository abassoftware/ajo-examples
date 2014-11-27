package de.abas.examples.utilities;

import de.abas.erp.api.gui.TextBox;
import de.abas.erp.common.type.AbasDate;
import de.abas.erp.common.type.AbasDateTime;
import de.abas.erp.common.type.AbasDuration;
import de.abas.erp.common.type.AbasTime;
import de.abas.erp.db.DbContext;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;
import de.abas.jfop.base.buffer.UserTextBuffer;

/**
 * Utility class to add durations of the type AbasDuration to an AbasDate object.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
public class AbasDateUtilities {
	/**
	 * Class variables
	 */
	private final String XGP4DURATION = "xgp4duration";
	private final String XGD14RESULT = "xgd14result";
	private final String XGD19RESULT = "xgd19result";
	private final String XGD19DATE = "xgd19date";
	private final String XGD2DATECALC = "xgd2datecalc";
	private final String XWCALENDARWEEK = "xwcalendarweek";
	private final String XIRESULT = "xiresult";
	private final String XZEND_TIME = "xzstopZeit";
	private final String G_DAY_CORRECT = "dayCorrect";

	private final String XZRESULT_TIME = "xzresultZeit";
	private final String XZSTART_TIME = "xzstartZeit";
	private final String XGD8RESULT = "xgd8result";
	private final String XGD2RESULT = "xgd2result";
	private final String XGD2DATE = "xgd2date";

	// only one object of UserTextBuffer and GlobalTextBuffer necessary
	private UserTextBuffer userTextBuffer;
	private GlobalTextBuffer globalTextBuffer;

	private final String TYPE_GD2 = "GD2";
	private final String TYPE_GD8 = "GD8";
	private final String TYPE_TIME = "Time";
	private final String TYPE_I2 = "I2";
	private final String TYPE_WEEK = "Week";
	private final String TYPE_GD14 = "GD14";
	private final String TYPE_GD19 = "GD19";
	private final String TYPE_GP4 = "GP4";

	private final String TEXTBOX_HEADLINE = "Attention";
	private final String TEXTBOX_LINES = "day correction";

	private final int DAY_COUNT = 7;

	/**
	 * Uses an instance of the BufferFactory to get an object of UserTextBuffer. Only one UserTextBuffer object is necessary.
	 * 
	 * @return The instance of UserTextBuffer is returned.
	 */
	private UserTextBuffer getUserTextBuffer() {
		if (userTextBuffer == null) {
			userTextBuffer = BufferFactory.newInstance(true).getUserTextBuffer();
		}
		return userTextBuffer;
	}

	/**
	 * Uses an instance of the BufferFactory to get an object of GlobalTextBuffer. Only one GlobalTextBuffer obejct is necessary.
	 * 
	 * @return The instance of GlobalTextBuffer is returned.
	 */
	private GlobalTextBuffer getGlobalTextBuffer() {
		if (globalTextBuffer == null) {
			globalTextBuffer = BufferFactory.newInstance(true).getGlobalTextBuffer();
		}
		return globalTextBuffer;
	}

	/**
	 * Adds a number of days to an AbasDate only considering working days.
	 * 
	 * @param dbContext The database context.
	 * @param date The AbasDate to add the days to.
	 * @param days The amount of days to add.
	 * @return The AbasDate with the days added.
	 */
	public AbasDate addWorkingDays(DbContext dbContext, AbasDate date, int days) {
		// declares variables as in FOP
		if (!getUserTextBuffer().isVarDefined(XGD2DATE)) {
			getUserTextBuffer().defineVar(TYPE_GD2, XGD2DATE);
		}
		if (!getUserTextBuffer().isVarDefined(XGD2RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD2, XGD2RESULT);
		}
		if (!getUserTextBuffer().isVarDefined(XGD8RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD8, XGD8RESULT);
		}

		// initializes variables and calculates new date
		getUserTextBuffer().assign(XGD2DATE, date);
		getUserTextBuffer().formulaField(XGD2RESULT, userTextBuffer, XGD2DATE + " &" + days);
		getUserTextBuffer().formulaValue(XGD8RESULT, userTextBuffer, XGD2RESULT);

		// gets the new date from UserTextBuffer and returns it
		AbasDate abasDate = getUserTextBuffer().getAbasDateValue(XGD8RESULT);
		return abasDate;
	}

	/**
	 * Subtracts a number of days from an AbasDate only considering working days.
	 * 
	 * @param dbContext The database context.
	 * @param date The AbasDate to subtract the days from.
	 * @param days The amount of days to subtract.
	 * @return The AbasDate with the days subtracted.
	 */
	public AbasDate subWorkingDays(DbContext dbContext, AbasDate date, int days) {
		return addWorkingDays(dbContext, date, -days);
	}

	/**
	 * Adds a number of days to an AbasDate.
	 * 
	 * @param dbContext The database context.
	 * @param date The AbasDate to add the days to.
	 * @param days The amount of days to add.
	 * @return The AbasDate with the days added.
	 */
	public AbasDate addCalendarDays(DbContext dbContext, AbasDate date, int days) {
		// declares variables as in FOP
		if (!getUserTextBuffer().isVarDefined(XGD2DATE)) {
			getUserTextBuffer().defineVar(TYPE_GD2, XGD2DATE);
		}
		if (!getUserTextBuffer().isVarDefined(XGD2RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD2, XGD2RESULT);
		}
		if (!getUserTextBuffer().isVarDefined(XGD8RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD8, XGD8RESULT);
		}

		// initializes variables and calculates new date
		getUserTextBuffer().assign(XGD2DATE, date);
		getUserTextBuffer().formulaField(XGD2RESULT, userTextBuffer, XGD2DATE + " +" + days);
		getUserTextBuffer().formulaValue(XGD8RESULT, userTextBuffer, XGD2RESULT);

		// gets the new date from UserTextBuffer and returns it
		AbasDate abasDate = getUserTextBuffer().getAbasDateValue(XGD8RESULT);
		return abasDate;
	}

	/**
	 * Subtracts a number of days from an AbasDate
	 * 
	 * @param dbContext The database context
	 * @param date The AbasDate to add the days to.
	 * @param days The amount of days to subtract.
	 * @return The AbasDate with the days subtracted.
	 */
	public AbasDate subCalendarDays(DbContext dbContext, AbasDate date, int days) {
		return addCalendarDays(dbContext, date, -days);
	}

	/**
	 * Adds hours to an AbasTime correcting the day if necessary.
	 * 
	 * @param dbContext The database context.
	 * @param time The AbasTime to add the hours to.
	 * @param hours The hours to add.
	 * @return The AbasTime with the hours added.
	 */
	public AbasTime addHours(DbContext dbContext, AbasTime time, double hours) {
		// declares variables as in FOP
		if (!getUserTextBuffer().isVarDefined(XZSTART_TIME)) {
			getUserTextBuffer().defineVar(TYPE_TIME, XZSTART_TIME);
		}
		if (!getUserTextBuffer().isVarDefined(XZRESULT_TIME)) {
			getUserTextBuffer().defineVar(TYPE_TIME, XZRESULT_TIME);
		}

		// initializes variables and calculates new time
		getUserTextBuffer().assign(XZSTART_TIME, time);
		getUserTextBuffer().formulaField(XZRESULT_TIME, userTextBuffer, XZSTART_TIME + " +" + hours);

		// corrects the day if necessary
		int dayCorrect = getGlobalTextBuffer().getIntegerValue(G_DAY_CORRECT);
		if (dayCorrect > 0) {
			TextBox textBox = new TextBox(dbContext, TEXTBOX_HEADLINE, TEXTBOX_LINES);
			textBox.show();
		}
		
		// gets the new time from UserTextBuffer and returns it
		String stringValue = getUserTextBuffer().getStringValue(XZRESULT_TIME);
		return AbasTime.valueOf(stringValue);
	}

	/**
	 * Subtracts hours from an AbasTime correcting the day if necessary.
	 * 
	 * @param dbContext The database context.
	 * @param time The AbasTime to subtract the hours from.
	 * @param hours The hours to subtract.
	 * @return The AbasTime with the hours subtracted.
	 */
	public AbasTime subHours(DbContext dbContext, AbasTime time, double hours) {
		return addHours(dbContext, time, (hours * -1));
	}

	/**
	 * Adds minutes to an AbasTime correcting the day if necessary.
	 * 
	 * @param dbContext The database context.
	 * @param time The AbasTime to add the minutes to.
	 * @param minutes The minutes to add.
	 * @return The AbasTime with the minutes added.
	 */
	public AbasTime addMinutes(DbContext dbContext, AbasTime time, int minutes) {
		// declares variables as in FOP
		if (!getUserTextBuffer().isVarDefined(XZSTART_TIME)) {
			getUserTextBuffer().defineVar(TYPE_TIME, XZSTART_TIME);
		}
		if (!getUserTextBuffer().isVarDefined(XZRESULT_TIME)) {
			getUserTextBuffer().defineVar(TYPE_TIME, XZRESULT_TIME);
		}

		// initializes variables and calculates new time
		getUserTextBuffer().assign(XZSTART_TIME, time);
		getUserTextBuffer().formulaField(XZRESULT_TIME, userTextBuffer, XZSTART_TIME + " + " + minutes);

		// corrects the day if necessary
		int dayCorrect = getGlobalTextBuffer().getIntegerValue(G_DAY_CORRECT);
		if (dayCorrect > 0) {
			TextBox textBox = new TextBox(dbContext, TEXTBOX_HEADLINE, TEXTBOX_LINES);
			textBox.show();
		}
		
		// gets the new time from UserTextBuffer and returns it
		String stringValue = getUserTextBuffer().getStringValue(XZRESULT_TIME);
		return AbasTime.valueOf(stringValue);
	}

	/**
	 * Subtracts minutes from an AbasTime correcting the day if necessary.
	 * 
	 * @param dbContext The database context.
	 * @param time The AbasTime to subtract the minutes from.
	 * @param minutes The minutes to subtract.
	 * @return The AbasTime with the minutes subtracted.
	 */
	public AbasTime subMinutes(DbContext dbContext, AbasTime time, int minutes) {
		return addMinutes(dbContext, time, (minutes * -1));
	}

	/**
	 * Calculates the duration between two AbasTime instances in minutes.
	 * 
	 * @param dbContext The database context.
	 * @param beginTime The beginning time for the calculation.
	 * @param endTime The end time for the calculation.
	 * @return The amount of minutes between beginTime and endTime as Integer.
	 */
	public int getTimeDiffMinutes(DbContext dbContext, AbasTime beginTime, AbasTime endTime) {
		// declares variables as in FOP
		if (!getUserTextBuffer().isVarDefined(XZSTART_TIME)) {
			getUserTextBuffer().defineVar(TYPE_TIME, XZSTART_TIME);
		}
		if (!getUserTextBuffer().isVarDefined(XZEND_TIME)) {
			getUserTextBuffer().defineVar(TYPE_TIME, XZEND_TIME);
		}
		if (!getUserTextBuffer().isVarDefined(XIRESULT)) {
			getUserTextBuffer().defineVar(TYPE_I2, XIRESULT);
		}

		// initializes variables
		getUserTextBuffer().assign(XZSTART_TIME, beginTime);
		getUserTextBuffer().assign(XZEND_TIME, endTime);

		// calculates the duration
		getUserTextBuffer().formulaField(XIRESULT, userTextBuffer, XZSTART_TIME + " - " + XZEND_TIME);
		int integerValue = getUserTextBuffer().getIntegerValue(XIRESULT);
		return integerValue;
	}

	/**
	 * Gets the calendar week of an AbasDate instance.
	 * 
	 * @param dbContext The database context.
	 * @param date The AbasDate of which to find out the calendar week.
	 * @return The calendar week as String.
	 */
	public String getCalendarWeek(DbContext dbContext, AbasDate date) {

		String stringValue = "";
		if (!getUserTextBuffer().isVarDefined(XWCALENDARWEEK)) {
			getUserTextBuffer().defineVar(TYPE_WEEK, XWCALENDARWEEK);
		}
		String dateString = date.toString();
		getUserTextBuffer().assign(XWCALENDARWEEK, dateString);
		stringValue = getUserTextBuffer().getStringValue(XWCALENDARWEEK);

		return stringValue;
	}

	/**
	 * Adds weeks to an AbasDate instance.
	 * 
	 * @param dbContext The database context.
	 * @param date The AbasDate instance to add the weeks to.
	 * @param weeks The number of weeks to add.
	 * @return The AbasDate with the weeks added.
	 */
	public AbasDate addWeeks(DbContext dbContext, AbasDate date, int weeks) {

		if (!getUserTextBuffer().isVarDefined(XGD2DATE)) {
			getUserTextBuffer().defineVar(TYPE_GD2, XGD2DATE);
		}
		if (!getUserTextBuffer().isVarDefined(XGD2DATECALC)) {
			getUserTextBuffer().defineVar(TYPE_GD2, XGD2DATECALC);
		}
		if (!getUserTextBuffer().isVarDefined(XGD8RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD8, XGD8RESULT);
		}

		getUserTextBuffer().assign(XGD2DATE, date);

		AbasDate addCalendarDays = this.addCalendarDays(dbContext, date, (DAY_COUNT * weeks));
		return addCalendarDays;
	}

	/**
	 * Subtracts weeks from an AbasDate instance.
	 * 
	 * @param dbContext The database context.
	 * @param date The AbasDate instance to subtract the weeks from.
	 * @param weeks The number of weeks to subtract.
	 * @return The AbasDate with the weeks subtracted.
	 */
	public AbasDate subWeeks(DbContext dbContext, AbasDate date, int weeks) {
		return addWeeks(dbContext, date, (weeks * -1));
	}

	/**
	 * Adds an AbasDuration (e.g. 3D01h12m09s) to an AbasDate.
	 * 
	 * Attention: Date calculations with seconds can only be done using AbasDuration.
	 * 
	 * @param dbContext The database context.
	 * @param date The AbasDateTime instance to add the AbasDuration instance to.
	 * @param duration The AbasDuration instance to add.
	 * @return The AbasDate instance with the duration added.
	 */
	public AbasDate addDuration(DbContext dbContext, AbasDateTime date, AbasDuration duration) {
		// declares variables as in FOP
		if (!getUserTextBuffer().isVarDefined(XGD19DATE)) {
			getUserTextBuffer().defineVar(TYPE_GD19, XGD19DATE);
		}
		if (!getUserTextBuffer().isVarDefined(XGD19RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD19, XGD19RESULT);
		}
		if (!getUserTextBuffer().isVarDefined(XGD14RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD14, XGD14RESULT);
		}
		if (!getUserTextBuffer().isVarDefined(XGP4DURATION)) {
			getUserTextBuffer().defineVar(TYPE_GP4, XGP4DURATION);
		}

		// initializes variables
		getUserTextBuffer().assign(XGD19DATE, date);
		getUserTextBuffer().assign(XGP4DURATION, duration);

		// calculates new AbasDate by adding the AbasDuration instance to the AbasDateTime instance.
		getUserTextBuffer().formulaField(XGD14RESULT, userTextBuffer, XGD19DATE + " + " + XGP4DURATION);
		
		// gets the new date from UserTextBuffer and returns it
		AbasDate abasDate = getUserTextBuffer().getAbasDateValue(XGD14RESULT);
		return abasDate;
	}

	/**
	 * Subtracts an AbasDuration (e.g. 3D01h12m09s) to an AbasDate.
	 * 
	 * Attention: Date calculations with seconds can only be done using AbasDuration.
	 * 
	 * @param dbContext The database context.
	 * @param date The AbasDateTime instance to subtract the AbasDuration from.
	 * @param duration The AbasDuration instance to subtract.
	 * @return The AbasDate instance with the duration subtracted.
	 */
	public AbasDate subDuration(DbContext dbContext, AbasDateTime date, AbasDuration duration) {
		// declares variables as in FOP
		if (!getUserTextBuffer().isVarDefined(XGD19DATE)) {
			getUserTextBuffer().defineVar(TYPE_GD19, XGD19DATE);
		}
		if (!getUserTextBuffer().isVarDefined(XGD19RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD19, XGD19RESULT);
		}
		if (!getUserTextBuffer().isVarDefined(XGD14RESULT)) {
			getUserTextBuffer().defineVar(TYPE_GD14, XGD14RESULT);
		}
		if (!getUserTextBuffer().isVarDefined(XGP4DURATION)) {
			getUserTextBuffer().defineVar(TYPE_GP4, XGP4DURATION);
		}

		// initializes variables
		getUserTextBuffer().assign(XGD19DATE, date);
		getUserTextBuffer().assign(XGP4DURATION, duration);

		// calculates new AbasDate by subtracting the AbasDuration instance from the AbasDateTime instance
		getUserTextBuffer().formulaField(XGD14RESULT, userTextBuffer, XGD19DATE + " - " + XGP4DURATION);
		
		// gets the new date from UserTextBuffer and returns it
		AbasDate abasDate = getUserTextBuffer().getAbasDateValue(XGD14RESULT);
		return abasDate;
	}
}
