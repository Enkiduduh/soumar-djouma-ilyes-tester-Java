package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;

public class ParkingSpotDAO {
  private static final Logger logger = LogManager.getLogger("ParkingSpotDAO");

  public DataBaseConfig dataBaseConfig = new DataBaseConfig();

  public int getNextAvailableSlot(ParkingType parkingType) {
    Connection con = null;
    int result = -1;
    try {
      con = dataBaseConfig.getConnection();
      PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
      ps.setString(1, parkingType.toString());
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        result = rs.getInt(1);
      }
      dataBaseConfig.closeResultSet(rs);
      dataBaseConfig.closePreparedStatement(ps);
    } catch (Exception ex) {
      logger.error("Error fetching next available slot!", ex);
    } finally {
      dataBaseConfig.closeConnection(con);
    }
    return result;
  }

  public boolean updateParking(ParkingSpot parkingSpot) {
    // update the availability fo that parking slot
    Connection con = null;
    try {
      con = dataBaseConfig.getConnection();
      PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
      ps.setBoolean(1, parkingSpot.isAvailable());
      ps.setInt(2, parkingSpot.getId());
      int updateRowCount = ps.executeUpdate();
      dataBaseConfig.closePreparedStatement(ps);
      return (updateRowCount == 1);
    } catch (Exception ex) {
      logger.error("Error updating parking info", ex);
      return false;
    } finally {
      dataBaseConfig.closeConnection(con);
    }
  }

  public ParkingSpot getParkingSpot(int id) {
    Connection con = null;
    ParkingSpot parkingSpot = null;
    try {
      con = dataBaseConfig.getConnection();
      PreparedStatement ps = con.prepareStatement("SELECT * FROM parking WHERE PARKING_NUMBER = ?");
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        parkingSpot = new ParkingSpot(id, ParkingType.valueOf(rs.getString("TYPE")), rs.getBoolean("AVAILABLE"));
      }
      dataBaseConfig.closeResultSet(rs);
      dataBaseConfig.closePreparedStatement(ps);
    } catch (Exception ex) {
      logger.error("Error fetching parking spot", ex);
    } finally {
      dataBaseConfig.closeConnection(con);
    }
    return parkingSpot;
  }

}
