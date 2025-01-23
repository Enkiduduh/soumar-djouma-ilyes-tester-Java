package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAO {

  private static final Logger logger = LogManager.getLogger(TicketDAO.class);

  public DataBaseConfig dataBaseConfig = new DataBaseConfig();

  public boolean saveTicket(Ticket ticket) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = dataBaseConfig.getConnection();
      ps = con.prepareStatement(DBConstants.SAVE_TICKET);
      // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
      // ps.setInt(1,ticket.getId());
      ps.setInt(1, ticket.getParkingSpot().getId());
      ps.setString(2, ticket.getVehicleRegNumber());
      ps.setDouble(3, ticket.getPrice());
      ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
      ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
      logger.info("Executing query: " + ps);
      int rowsAffected = ps.executeUpdate();
      if (rowsAffected > 0) {
        logger.info("Ticket saved successfully");
        return true;
      } else {
        logger.error("No rows affected, ticket not saved");
        return false;
      }
    } catch (Exception ex) {
      logger.error("Error saving ticket", ex);
      return false;
    } finally {
      dataBaseConfig.closePreparedStatement(ps);
      dataBaseConfig.closeConnection(con);
    }
  }

  public Ticket getTicket(String vehicleRegNumber) {
    Connection con = null;
    Ticket ticket = null;
    try {
      con = dataBaseConfig.getConnection();
      //A test
      System.out.println("connecting DB");
      PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
      // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
      ps.setString(1, vehicleRegNumber);
      ResultSet rs = ps.executeQuery();
      //B test
      System.out.println("connected DB");
      if (rs.next()) {
        ticket = new Ticket();
        // C test
        System.out.println("Creating ticket");
        ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setId(rs.getInt(2));
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setPrice(rs.getDouble(3));
        ticket.setInTime(rs.getTimestamp(4));
        ticket.setOutTime(rs.getTimestamp(5));
        //D test
        System.out.println("All infos for ticket are ok");
      }
      System.out.println("Ended connection DB");
      dataBaseConfig.closeResultSet(rs);
      dataBaseConfig.closePreparedStatement(ps);
      return ticket;
    } catch (Exception ex) {
      logger.error("Error fetching next available slot oups!", ex);
      return null;
    } finally {
      dataBaseConfig.closeConnection(con);
    }
  }

  public boolean updateTicket(Ticket ticket) {
    Connection con = null;
    try {
      con = dataBaseConfig.getConnection();
      PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
      ps.setDouble(1, ticket.getPrice());
      ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
      ps.setInt(3, ticket.getId());
      ps.execute();
      return true;
    } catch (Exception ex) {
      logger.error("Error saving ticket info", ex);
    } finally {
      dataBaseConfig.closeConnection(con);
    }
    return false;
  }

  public int getNbTicket(String vehicleRegNumber) {
    Connection con = null;
    int nbTickets = 0;
    try {
      con = dataBaseConfig.getConnection();
      PreparedStatement ps = con.prepareStatement(DBConstants.COUNT_TICKETS);
      ps.setString(1, vehicleRegNumber);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        nbTickets = rs.getInt(1); // Récupère le résultat du COUNT
      }
      dataBaseConfig.closeResultSet(rs);
      dataBaseConfig.closePreparedStatement(ps);
    } catch (Exception ex) {
      logger.error("Error fetching next available slot", ex);
    } finally {
      dataBaseConfig.closeConnection(con);
    }
    return nbTickets;
  }
}
