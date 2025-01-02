package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

  public void calculateFare(Ticket ticket) {
    if ((ticket.getInTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
      throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
    }

    // Temps d'entrée et de sortie en millisecondes
    long inTimeMillis = ticket.getInTime().getTime();
    long outTimeMillis = ticket.getOutTime().getTime();

    // TODO: Some tests are failing here. Need to check if this logic is correct
    if (outTimeMillis <= inTimeMillis) {
      throw new IllegalArgumentException("Out time must be after in time");
    }

    // Durée en heures (avec fractions)

    // double duration = (outTimeMillis - inTimeMillis) / (double) (1000 * 60 * 60);
          // ou
    double duration = (outTimeMillis - inTimeMillis) / (1000.0 * 60 * 60);

    if (duration <= 0) {
      throw new IllegalArgumentException("Parking time must be positive");
    }

    // Déterminer le tarif par type de véhicule
    double price = 0;
    switch (ticket.getParkingSpot().getParkingType()) {
      case CAR:
        price = duration * Fare.CAR_RATE_PER_HOUR;
        break;
      case BIKE:
        price = duration * Fare.BIKE_RATE_PER_HOUR;
        break;
      default:
        throw new IllegalArgumentException("Unkown Parking Type");
    }

    ticket.setPrice(price);
  }
}
