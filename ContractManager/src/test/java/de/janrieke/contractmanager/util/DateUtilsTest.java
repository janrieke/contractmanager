package de.janrieke.contractmanager.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.janrieke.contractmanager.rmi.Contract.IntervalType;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Config;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Application.class, Config.class })
public class DateUtilsTest {
	private Date now;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		mockStatic(Application.class);
		Config config = mock(Config.class);
		when(config.getLocale()).thenReturn(Locale.getDefault());
		when(Application.getConfig()).thenReturn(config);

		Calendar cal = Calendar.getInstance();
		cal.set(2016, 9, 28, 12, 0, 0);
		now = cal.getTime();
	}

	@Test
	public void testGetNextTermBegin() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());

		ValidRuntimes runtimes = new ValidRuntimes();
		runtimes.firstMinRuntimeCount = 6;
		runtimes.firstMinRuntimeType = IntervalType.MONTHS;
		runtimes.followingMinRuntimeCount = 3;
		runtimes.followingMinRuntimeType = IntervalType.MONTHS;

		Date actual = DateUtils.calculateNextTermBeginAfter(now, startDate, false, runtimes);

		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 3, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}

	@Test
	public void testGetNextTermBegin_FixedTerms_Months() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());

		ValidRuntimes runtimes = new ValidRuntimes();
		runtimes.firstMinRuntimeCount = 6;
		runtimes.firstMinRuntimeType = IntervalType.MONTHS;
		runtimes.followingMinRuntimeCount = 3;
		runtimes.followingMinRuntimeType = IntervalType.MONTHS;
		runtimes.fixedTerms = true;

		Date actual = DateUtils.calculateNextTermBeginAfter(now, startDate, false, runtimes);

		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 4, 1).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}

	@Test
	public void testGetNextTermBegin_FixedTerms_Years() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());

		ValidRuntimes runtimes = new ValidRuntimes();
		runtimes.firstMinRuntimeCount = 1;
		runtimes.firstMinRuntimeType = IntervalType.YEARS;
		runtimes.followingMinRuntimeCount = 6;
		runtimes.followingMinRuntimeType = IntervalType.MONTHS;
		runtimes.fixedTerms = true;

		Date actual = DateUtils.calculateNextTermBeginAfter(now, startDate, false, runtimes);

		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 1, 1).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}

	@Test
	public void testGetNextTermBeginAfter() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());

		ValidRuntimes runtimes = new ValidRuntimes();
		runtimes.firstMinRuntimeCount = 6;
		runtimes.firstMinRuntimeType = IntervalType.MONTHS;
		runtimes.followingMinRuntimeCount = 3;
		runtimes.followingMinRuntimeType = IntervalType.MONTHS;

		Date actual = DateUtils.calculateNextTermBeginAfter(now, startDate, false, runtimes);
		actual = DateUtils.calculateNextTermBeginAfter(actual, startDate, false, runtimes);

		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 6, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}
}
