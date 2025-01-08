package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

  public void calculateFare(Ticket ticket, boolean discount) {
    if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
      throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
    }

    long inTimeMillis = ticket.getInTime().getTime();
    long outTimeMillis = ticket.getOutTime().getTime();

    if (outTimeMillis <= inTimeMillis) {
      throw new IllegalArgumentException("Out time must be after in time");
    }

    // Durée en heures (avec fractions)
    double duration = (outTimeMillis - inTimeMillis) / (1000.0 * 60 * 60);
    // ou
    // double duration = (outTimeMillis - inTimeMillis) / (double) (1000 * 60 * 60);

    if (duration <= 0) {
      throw new IllegalArgumentException("Parking time must be positive");
    }

    // Gratuit pour les parkings de 30 minutes ou moins, sinon appliquer le tarif horaire
    // Durée en heure
    double lessThan30Min = 0.5;


    double price = 0;
    switch (ticket.getParkingSpot().getParkingType()) {
      case CAR:
        if (duration < lessThan30Min) {
          price = 0;
        } else {
          price = duration * Fare.CAR_RATE_PER_HOUR;
        }
        break;
      case BIKE:
      if (duration < lessThan30Min) {
        price = 0;
      } else {
        price = duration * Fare.BIKE_RATE_PER_HOUR;
      }
        break;
      default:
        throw new IllegalArgumentException("Unkown Parking Type");
    }

    //Si discount est truthy, appliquer la réduction de 5%
    if (discount && (duration > lessThan30Min)) {
        price = price * 0.95; // 5% de reduction
    }

    ticket.setPrice(price);
  }
}
