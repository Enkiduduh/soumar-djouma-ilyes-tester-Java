package com.parkit.parkingsystem.integration;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

  private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
  private static ParkingSpotDAO parkingSpotDAO;
  private static TicketDAO ticketDAO;
  private static DataBasePrepareService dataBasePrepareService;

  @Mock
  private static InputReaderUtil inputReaderUtil;

  @BeforeAll
  private static void setUp() throws Exception {
    parkingSpotDAO = new ParkingSpotDAO();
    parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
    ticketDAO = new TicketDAO();
    ticketDAO.dataBaseConfig = dataBaseTestConfig;
    dataBasePrepareService = new DataBasePrepareService();
  }

  @BeforeEach
  private void setUpPerTest() throws Exception {
    lenient().when(inputReaderUtil.readSelection()).thenReturn(1);
    lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    dataBasePrepareService.clearDataBaseEntries();
  }

  @AfterAll
  private static void tearDown() {

  }

  @Test
  public void testParkingACar() {
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    //Avant action - Le ticket ne doit pas encore être présent dans la DB
    // assertNull(ticketDAO.getTicket("ABCDEF"));

    //Action voiture arrivant au parking
    parkingService.processIncomingVehicle("ABCDEF");

    //Après action - Le ticket doit maintenant être présent dans la DB
    Ticket ticket = ticketDAO.getTicket("ABCDEF");
    // assertNotNull(ticketDAO.getTicket("ABCDEF"));
    assertNotNull(ticket, "Le ticket devrait être enregistré dans la base de données.");
    assertEquals("ABCDEF", ticket.getVehicleRegNumber(), "Le numéro d'immatriculation du ticket ne correspond pas.");
    assertNotNull(ticket.getInTime(), "L'heure d'entrée devrait être renseignée.");
    assertNotNull(ticket.getParkingSpot(), "Une place de parking devrait être associée au ticket.");
    assertFalse(ticket.getParkingSpot().isAvailable(),
        "La place de parking devrait être marquée comme non disponible.");

    // Vérification que la place de parking est mise à jour
    ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
    assertNotNull(parkingSpot, "La place de parking associée au ticket devrait exister dans la base.");
    assertFalse(parkingSpot.isAvailable(), "La place de parking devrait être mise à jour comme non disponible.");
  }

  // @Test
  // public void testParkingLotExit() {
  //   testParkingACar();
  //   ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
  //   parkingService.processExitingVehicle();

  //   // Vérification des données dans la base de données
  //   Ticket ticket = ticketDAO.getTicket("ABCDEF");
  //   assertNotNull(ticket, "Le ticket devrait être récupéré de la base de données.");
  //   assertNotNull(ticket.getOutTime(), "L'heure de sortie devrait être enregistrée dans le ticket.");
  //   assertTrue(ticket.getOutTime().after(ticket.getInTime()),
  //       "L'heure de sortie devrait être postérieure à l'heure d'entrée.");
  //   assertTrue(ticket.getPrice() > 0, "Le tarif devrait être calculé et supérieur à 0.");

  //   // Utilisation de getNextAvailableSlot pour obtenir l'ID d'une place de parking
  //   // disponible
  //   ParkingType parkingType = ticket.getParkingSpot().getParkingType();
  //   int nextAvailableSlotId = parkingSpotDAO.getNextAvailableSlot(parkingType);

  //   // Vérification que l'ID de la place de parking récupérée est valide
  //   assertTrue(nextAvailableSlotId > 0, "L'ID de la place de parking suivante devrait être valide.");

  //   // Vérification de la place de parking
  //   ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
  //   assertNotNull(parkingSpot, "La place de parking associée devrait être récupérée.");
  //   assertTrue(parkingSpot.isAvailable(),
  //       "La place de parking devrait être marquée comme disponible après la sortie du véhicule.");
  // }

  // @Test
  // public void testParkingLotExitRecurringUser() {
  //   testParkingACar();
  //   ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
  //   parkingService.processIncomingVehicle();
  //   parkingService.processExitingVehicle();
  //   // TODO: check that the remise is applying on recurrent users
  // }
}
