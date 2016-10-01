package de.janrieke.contractmanager.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.janrieke.contractmanager.rmi.Contract.IntervalType;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Config;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Application.class, Config.class })
public class ContractControlTest {

	@InjectMocks
	private ContractImpl contract;

	@Mock(name = "types")
	private HashMap<String, String> types;

	@Mock
	private HashMap<String, String> whatever; // Otherwise, Mockito gets confused in which field to inject.

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(types.size()).thenReturn(1);

		mockStatic(Application.class);
		Config config = mock(Config.class);
		when(config.getLocale()).thenReturn(Locale.getDefault());
		when(Application.getConfig()).thenReturn(config);
	}

	@Test
	public void testGetNextTermBegin() throws RemoteException, ParseException {
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
	public void testGetNextTermBeginAfter() throws RemoteException, ParseException {
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
	@Ignore
	public void testGetNextCancellationDeadline() {
		fail("Not yet implemented");
	}
}
