package com.scanner.scannerAPI;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class DatabaseConnector {
  // public static void main(String[] args) throws SQLException {
  //   String data = getWeeklyData("RP");
  //   System.out.println(data);

  //   data = getHourlyCallData("SHOOTING");
  //   System.out.println(data);

  //   data = getTotalCalls();
  //   System.out.println(data);

  //   data = getRecentCallsAgency("RP");
  //   System.out.println(data);

  //   data = getRecencyCallsType("SHOOTING");
  //   System.out.println(data);
  // }

  public static String getWeeklyData(String TestAgency) throws SQLException {
    LocalDate date = LocalDate.now().minusDays(100); // TODO: update when database is live

    ArrayList<String> dateSet = new ArrayList<>();
    for (int i = 1; i < 8; i++) {
      date = date.minusDays(1);
      dateSet.add(date.format(DateTimeFormatter.ofPattern("M/d/yyyy")));
      // System.out.println(date.format(DateTimeFormatter.ofPattern("M/d/yyyy")));
    }

    String query = "select "
        + "calltype, "
        + "count(*) AS countno "
        + "from scans "
        + " where "
        + "substr(calltime, 0, instr(calltime, ',')) in ("
        + "\'" + dateSet.get(0) + "\',"
        + "\'" + dateSet.get(1) + "\',"
        + "\'" + dateSet.get(2) + "\',"
        + "\'" + dateSet.get(3) + "\',"
        + "\'" + dateSet.get(4) + "\',"
        + "\'" + dateSet.get(5) + "\',"
        + "\'" + dateSet.get(6) + "\') "
        + " and agency = \'" + TestAgency + "\' "
        + "group by "
        + " calltype";
    // System.out.println(query);

    return executeQuery(query);
  }

  public static String getHourlyCallData(String CallType) throws SQLException {
    String query = "SELECT"
        + " CAST(substr(CallTime, instr(CallTime, ', ')+2, instr(CallTime, ':')-instr(CallTime,', ')-2) AS INT) AS hour, "
        + " substr(CallTime, instr(CallTime, 'M')+1, -2) AS am_pm, "
        + " COUNT(*) as countno"
        + " FROM scans "
        + "  WHERE"
        + " CallType = " + "\'" + CallType + "\'"
        + " GROUP BY"
        + "  1, 2"
        + " ORDER BY"
        + " 2, 1;";
    return executeQuery(query);
  }

  public static String getTotalCalls() throws SQLException {
    String query = "SELECT COUNT(*) as countno FROM scans;";
    return executeQuery(query);
  }

  public static String getRecentCallsAgency(String AgencyName) throws SQLException {
    String query = "SELECT * FROM scans WHERE agency = " 
    + "\'" + AgencyName + "\'"
    + " ORDER BY callTime DESC LIMIT 25;"; // TODO: make sure string date sort the right way
                                           // Break out year, month, date, then time
    return executeQuery(query);
  }

  public static String getRecencyCallsType(String callType) throws SQLException {
    String query = "SELECT * FROM scans WHERE callType = " 
    + "\'" + callType + "\'"
    + " ORDER BY callTime DESC LIMIT 25;"; // TODO: make sure string date sort the right way
    return executeQuery(query);
  }

  public static String executeQuery(String query) throws SQLException {

    String resultsJSON = null;
    Connection connection = null;
    try {
      // create a database connection
      connection = DriverManager.getConnection("jdbc:sqlite:data/valley_scanner.db");
      resultsJSON = resultSetToJson(connection, query);

      // Statement statement = connection.createStatement();
      // statement.setQueryTimeout(30); // set timeout to 30 sec.

      // rs = statement.executeQuery(query);

      // while (rs.next()) {
      // Map<String, Object> record = new HashMap<String, Object>();
      // record.put("calltype", rs.getString("calltype"));
      // record.put("countno", rs.getInt("countno"));
      // results.add(record);
      // }

    } catch (SQLException e) {
      // if the error message is "out of memory",
      // it probably means no database file is found
      System.err.println(e.getMessage());
    } finally {
      try {
        if (connection != null)
          connection.close();
      } catch (SQLException e) {
        // connection close failed.
        System.err.println(e.getMessage());
      }
    }

    return resultsJSON;
  }

  public static String resultSetToJson(Connection connection, String query) {
    // https://stackoverflow.com/questions/35134337/general-java-method-that-queries-db-and-returns-results-in-json-format/35136697#35136697
    List<Map<String, Object>> listOfMaps = null;
    try {
      QueryRunner queryRunner = new QueryRunner();
      listOfMaps = queryRunner.query(connection, query, new MapListHandler());
    } catch (SQLException se) {
      throw new RuntimeException("Couldn't query the database.", se);
    } finally {
      DbUtils.closeQuietly(connection);
    }
    return new Gson().toJson(listOfMaps);
  }
}
