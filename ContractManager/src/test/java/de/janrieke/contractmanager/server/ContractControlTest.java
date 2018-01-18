package de.janrieke.contractmanager.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.janrieke.contractmanager.rmi.Contract.IntervalType;
import de.janrieke.contractmanager.util.CalendarBuilder;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Config;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Application.class, Config.class })
public class ContractControlTest {

	@InjectMocks
	private ContractImpl contract;

	@Mock(name = "types")
	private HashMap<String, String> types;

	// Otherwise, Mockito gets confused in which field to inject.
	@Mock
	private HashMap<String, String> whatever;

	@Mock
	private CalendarBuilder calendarBuilder;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(types.size()).thenReturn(1);

		mockStatic(Application.class);
		Config config = mock(Config.class);
		when(config.getLocale()).thenReturn(Locale.getDefault());
		when(Application.getConfig()).thenReturn(config);

		// Mock the date retrieval to a fixed date.
		when(calendarBuilder.getInstance()).then(a -> {
			Calendar now = Calendar.getInstance();
			now.set(2016, 9, 28, 12, 0, 0);
			return now;
		});
	}

	@Test
	public void testGetNextTermBegin() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		contract.setStartDate(startDate);
		contract.setFirstMinRuntimeCount(6);
		contract.setFirstMinRuntimeType(IntervalType.MONTHS);
		contract.setFollowingMinRuntimeCount(3);
		contract.setFollowingMinRuntimeType(IntervalType.MONTHS);
		Date actual = contract.getNextTermBegin();
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
		contract.setStartDate(startDate);
		contract.setFixedTerms(true);
		contract.setFirstMinRuntimeCount(6);
		contract.setFirstMinRuntimeType(IntervalType.MONTHS);
		contract.setFollowingMinRuntimeCount(3);
		contract.setFollowingMinRuntimeType(IntervalType.MONTHS);
		Date actual = contract.getNextTermBegin();
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
		contract.setStartDate(startDate);
		contract.setFixedTerms(true);
		contract.setFirstMinRuntimeCount(1);
		contract.setFirstMinRuntimeType(IntervalType.YEARS);
		contract.setFollowingMinRuntimeCount(6);
		contract.setFollowingMinRuntimeType(IntervalType.MONTHS);
		Date actual = contract.getNextTermBegin();
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
		contract.setStartDate(startDate);
		contract.setFirstMinRuntimeCount(6);
		contract.setFirstMinRuntimeType(IntervalType.MONTHS);
		contract.setFollowingMinRuntimeCount(3);
		contract.setFollowingMinRuntimeType(IntervalType.MONTHS);
		Date actual = contract.getNextTermBegin();
		actual = contract.getNextTermBeginAfter(actual);
		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 6, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}

	@Test
	public void testGetNextCancellationDeadline() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		contract.setStartDate(startDate);
		contract.setFirstMinRuntimeCount(6);
		contract.setFirstMinRuntimeType(IntervalType.MONTHS);
		contract.setFollowingMinRuntimeCount(3);
		contract.setFollowingMinRuntimeType(IntervalType.MONTHS);
		contract.setCancelationPeriodCount(3);
		contract.setCancelationPeriodType(IntervalType.DAYS);
		Date actual = contract.getNextCancellationDeadline();
		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 3, 19).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}

	@Test
	public void testGetNextCancellationDeadline_FixedTerms_Months() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		contract.setStartDate(startDate);
		contract.setFixedTerms(true);
		contract.setFirstMinRuntimeCount(6);
		contract.setFirstMinRuntimeType(IntervalType.MONTHS);
		contract.setFollowingMinRuntimeCount(3);
		contract.setFollowingMinRuntimeType(IntervalType.MONTHS);
		contract.setCancelationPeriodCount(3);
		contract.setCancelationPeriodType(IntervalType.DAYS);
		Date actual = contract.getNextCancellationDeadline();
		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 3, 28).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}

	@Test
	public void testGetNextCancellationDeadline_FixedTerms_Years() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		contract.setStartDate(startDate);
		contract.setFixedTerms(true);
		contract.setFirstMinRuntimeCount(1);
		contract.setFirstMinRuntimeType(IntervalType.YEARS);
		contract.setFollowingMinRuntimeCount(6);
		contract.setFollowingMinRuntimeType(IntervalType.MONTHS);
		contract.setCancelationPeriodCount(3);
		contract.setCancelationPeriodType(IntervalType.DAYS);
		Date actual = contract.getNextCancellationDeadline();
		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 12, 28).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}

	@Test
	public void testGetNextCancellationDeadlineAfter() throws RemoteException {
		Date startDate = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2016, 9, 23).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		contract.setStartDate(startDate);
		contract.setFirstMinRuntimeCount(6);
		contract.setFirstMinRuntimeType(IntervalType.MONTHS);
		contract.setFollowingMinRuntimeCount(3);
		contract.setFollowingMinRuntimeType(IntervalType.MONTHS);
		contract.setCancelationPeriodCount(3);
		contract.setCancelationPeriodType(IntervalType.DAYS);
		Date actual = contract.getNextCancellationDeadline();
		// getNextCancellationDeadline() uses a "not before" logic, not "after".
		// Thus, we have to add one day.
		actual = Date.from(actual.toInstant().plus(1, ChronoUnit.DAYS));
		actual = contract.getNextCancellationDeadline(actual);
		Date expected = Date.from(ZonedDateTime
				.ofLocal(LocalDate.of(2017, 6, 19).atStartOfDay(), ZoneId.systemDefault(), null)
				.toInstant());
		assertEquals(expected, actual);
	}
}
