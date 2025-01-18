package com.parkit.parkingsystem;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

  private static ParkingService parkingService;

  @Mock
  private static InputReaderUtil inputReaderUtil;
  @Mock
  private static ParkingSpotDAO parkingSpotDAO;
  @Mock
  private static TicketDAO ticketDAO;

  @BeforeEach
  private void setUpPerTest() {
    try {
      Mockito.lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
      Ticket ticket = new Ticket();
      ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
      ticket.setParkingSpot(parkingSpot);
      ticket.setVehicleRegNumber("ABCDEF");
      Mockito.lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
      Mockito.lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

      Mockito.lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

      parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
  }

  @Test
  public void processExitingVehicleTest() {
    // Mock de getNbTicket() pour simuler un utilisateur récurrent
    when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
    // Appel de la méthode à tester
    parkingService.processExitingVehicle();
    // Vérifiez que la méthode updateParking a été appelée
    verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    // Vérifiez que la méthode getNbTicket a été appelée une fois avec le numéro
    // d'immatriculation
    verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
  }

  @Test
  public void testProcessIncomingVehicle() {
    try {
      // Mock de l'entrée utilisateur pour le choix du type de véhicule
      when(inputReaderUtil.readSelection()).thenReturn(1); // 1 pour CAR
      // Mock de la récupération du numéro de plaque
      Mockito.lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
      // Mock de l'attribution d'une place de parking disponible
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
      when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
      // Mock de la sauvegarde du ticket
      when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
      // Appel de la méthode à tester
      parkingService.processIncomingVehicle("ABCDEF");
      // Vérification que `getNextAvailableSlot` a été appelée une fois
      verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
      // Vérification que `updateParking` a été appelée pour marquer la place comme
      // occupée
      verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
      // Vérification que le ticket a été sauvegardé
      verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    } catch (Exception e) {
      e.printStackTrace();
      fail("Test échoué à cause d'une exception inattendue");
    }
  }

  @Test
  public void processExitingVehicleTestUnableUpdate() {
    try {
      // Mock de création d'un ticket
      Ticket ticket = new Ticket();
      ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
      ticket.setVehicleRegNumber("ABCDEF");
      ticket.setInTime(new Date());

      // Mock des appels
      when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
      when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
      when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
      when(parkingService.getVehichleRegNumber()).thenReturn("ABCDEF");

      // Appel de la méthode à tester
      parkingService.processExitingVehicle();

      // Vérifiez que `updateTicket` a été appelé
      verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));

      // Vérifiez que `updateParking` n'a pas été appelé
      verify(parkingSpotDAO, Mockito.never()).updateParking(any(ParkingSpot.class));

    } catch (Exception e) {
      e.printStackTrace();
      fail("Test échoué à cause d'une exception inattendue");
    }
  }

  @Test
  public void testGetNextParkingNumberIfAvailable() {
    try {
      // Mock de la méthode `getVehichleType` pour retourner un type de véhicule
      // valide
      ParkingType parkingType = ParkingType.CAR;
      Mockito.when(inputReaderUtil.readSelection()).thenReturn(1);
      // Mock de `getNextAvailableSlot` pour retourner un spot disponible avec ID = 1
      Mockito.when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(1);
      // Appel de la méthode à tester
      ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

      // Vérification du résultat
      assertNotNull(parkingSpot); // Vérifie que le spot retourné n'est pas null
      assertEquals(1, parkingSpot.getId()); // Vérifie que l'ID du spot est bien 1
      assertTrue(parkingSpot.isAvailable()); // Vérifie que le spot est bien marqué comme disponible
      assertEquals(parkingType, parkingSpot.getParkingType()); // Vérifie que le type de véhicule est correct
      // Vérifie que `getNextAvailableSlot` a été appelé une fois
      verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(parkingType);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Test échoué à cause d'une exception inattendue");
    }
  }

  @Test
  public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
    try {
      // Mock de la méthode `getVehichleType` pour retourner un type de véhicule
      // valide
      ParkingType parkingType = ParkingType.CAR;
      Mockito.when(inputReaderUtil.readSelection()).thenReturn(1);
      // Mock de `getNextAvailableSlot` pour retourner spot non disponible
      Mockito.when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(0);
      // Appel de la méthode à tester
      ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

      // Vérification du résultat
      assertNull(parkingSpot); // Vérifie qu'il n'y a pas de place disponible
      // Vérifie que `getNextAvailableSlot` a été appelé une fois
      verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(parkingType);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Test échoué à cause d'une exception inattendue");
    }
  }

  @Test
  public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    try {
      // Mock de la méthode `getVehichleType` pour retourner un type de véhicule
      // invalide
      Mockito.when(inputReaderUtil.readSelection()).thenReturn(3);
      // Appel de la méthode à tester
      ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

      // Vérification du résultat
      assertNull(parkingSpot); // Vérifie que la méthode retourne `null`
      // Vérifie que `getNextAvailableSlot` n'a jamais été appelé
      verify(parkingSpotDAO, Mockito.never()).getNextAvailableSlot(any(ParkingType.class));
    } catch (Exception e) {
      e.printStackTrace();
      fail("Test échoué à cause d'une exception inattendue");
    }
  }
}
