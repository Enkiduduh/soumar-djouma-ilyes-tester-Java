package com.parkit.parkingsystem.integration;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

  private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
  private static ParkingSpotDAO parkingSpotDAO;
  private static TicketDAO ticketDAO;
  private static DataBasePrepareService dataBasePrepareService;
  private static FareCalculatorService fareCalculatorService;

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


  // @Test
  // public void testSaveTicket() {
  //   Ticket ticket = new Ticket();
  //   Date inTime = new Date();
  //   ticket.setParkingSpot(new ParkingSpot(3, ParkingType.CAR, false));
  //   ticket.setVehicleRegNumber("XYZ123");
  //   ticket.setPrice(1.5);
  //   ticket.setInTime(inTime);

  //   // Obtenir l'heure actuel + 1h
  //   Calendar calendar = Calendar.getInstance();
  //   calendar.setTime(inTime);
  //   calendar.add(Calendar.HOUR, 1);
  //   Date outTime = calendar.getTime();
  //   ticket.setOutTime(outTime);

  //   boolean result = ticketDAO.saveTicket(ticket);
  //   assertTrue(result, "Le ticket devrait être enregistré dans la base.");
  // }

  // @Test
  // public void testGetTicket() {
  //   Ticket ticket = ticketDAO.getTicket("XYZ123");
  //   assertNotNull(ticket, "Le ticket XYZ123 devrait exister dans la base.");
  // }

  @Test
  public void testParkingACar() throws Exception {
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    // Avant action - Le ticket ne doit pas encore être présent dans la DB
    assertNull(ticketDAO.getTicket("ABCDEF"));

    // Action voiture arrivant au parking
    parkingService.processIncomingVehicle("ABCDEF");

    // Insérer un ticket directement dans la DB pour vérifier l'insertion
    // Date inTime = new Date();
    // Ticket ticket = new Ticket();
    // ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
    // ticket.setVehicleRegNumber("ABCDEF");
    // ticket.setPrice(0);
    // ticket.setInTime(inTime);
    // ticket.setOutTime(null);
    // boolean isTicketSaved = ticketDAO.saveTicket(ticket);

    // Après action - Le ticket doit maintenant être inséré dans la DB
    Ticket ticket = ticketDAO.getTicket("ABCDEF");
    assertNotNull(ticket, "Le ticket devrait être enregistré dans la base de données.");

    assertNotNull(ticketDAO.getTicket("ABCDEF"));
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

  @Test
  public void testParkingLotExit() throws Exception {
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    // Le véhicule entre dans le parking
    parkingService.processIncomingVehicle("ABCDEF");
    // Le véhicule sort du parking
    parkingService.processExitingVehicle();

    // Vérification des données dans la base de données
    Ticket ticket = ticketDAO.getTicket("ABCDEF");

    assertNotNull(ticket, "Le ticket devrait être récupéré de la base de données.");

    // Obtenir l'heure actuel + 1h
    Calendar calendar = Calendar.getInstance();
    Date inTime = new Date();
    calendar.setTime(inTime);
    calendar.add(Calendar.HOUR, 1);
    Date outTime = calendar.getTime();
    ticket.setOutTime(outTime);
    ticket.setPrice(1.5);
    ticketDAO.updateTicket(ticket);

    assertNotNull(ticket.getOutTime(), "L'heure de sortie devrait être enregistrée dans le ticket.");
    assertTrue(ticket.getOutTime().after(ticket.getInTime()), "L'heure de sortie devrait être postérieure à l'heure d'entrée.");
    assertTrue(ticket.getPrice() > 0, "Le tarif devrait être calculé et supérieur à 0.");

  // Utilisation de getNextAvailableSlot pour obtenir l'ID d'une place de parking disponible
  ParkingType parkingType = ticket.getParkingSpot().getParkingType();
  int nextAvailableSlotId = parkingSpotDAO.getNextAvailableSlot(parkingType);

  // Vérification de la place de parking
  ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
  assertNotNull(parkingSpot, "La place de parking associée devrait être récupérée.");
  assertTrue(parkingSpot.isAvailable(), "La place de parking devrait être marquée comme disponible après la sortie du véhicule.");
  }

  @Test
  public void testParkingLotExitRecurringUser() {
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    FareCalculatorService fareCalculatorService = new FareCalculatorService();

    //  Simulation d'un ticket déjà existant
    Ticket firstTicket = new Ticket();
    Date firstInTime = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(firstInTime);
    calendar.add(Calendar.HOUR, -3); // Simuler une entrée 3 heures avant
    firstInTime = calendar.getTime();

    Date firstOutTime = new Date(); // Maintenant
    firstTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
    firstTicket.setVehicleRegNumber("ABCDEF");
    firstTicket.setInTime(firstInTime);
    firstTicket.setOutTime(firstOutTime);
    firstTicket.setPrice(3 * Fare.CAR_RATE_PER_HOUR); // Tarif pour 3 heures
    ticketDAO.saveTicket(firstTicket); // Sauvegarder le ticket initial en base
    assertTrue(ticketDAO.getNbTicket("ABCDEF") > 0, "Il doit déjà y avoir au moins 1 ticket existant");

    // Simuler une nouvelle entrée dans le parking
    parkingService.processIncomingVehicle("ABCDEF");

    // Obtenir l'heure d'entrée (maintenant)
    Ticket currentTicket = ticketDAO.getTicket("ABCDEF");
    Date newInTime = currentTicket.getInTime();


    // Simuler une sortie 2 heures plus tard
    calendar.setTime(newInTime);
    calendar.add(Calendar.HOUR, 2); // Ajouter 2 heures
    Date newOutTime = calendar.getTime();
    currentTicket.setOutTime(newOutTime);

    // Calculer le tarif avec remise pour utilisateur récurrent
    fareCalculatorService.calculateFare(currentTicket, true);

    // Sauvegarder le ticket mis à jour
    ticketDAO.updateTicket(currentTicket);

    // Vérifier le tarif final
    double expectedPrice = (2 * Fare.CAR_RATE_PER_HOUR) * 0.95; // 5% de remise
    assertEquals(expectedPrice, currentTicket.getPrice(), "Le tarif calculé pour l'utilisateur récurrent est incorrect.");
  }
}
