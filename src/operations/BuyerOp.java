package operations;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import usluge.DB;

import javax.xml.transform.Result;

public class BuyerOp implements BuyerOperations {
    Connection con;

    public int createBuyer(String name, int cityId){
        String query = "INSERT INTO KUPAC VALUES(?, 0, ?)";
        String provjera = "SELECT * FROM KUPAC WHERE Ime = ?";
        String id = "SELECT Id FROM KUPAC WHERE Ime = ?";

        try{
            PreparedStatement ps = con.prepareStatement(provjera);
            ps.setString(1,name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                System.out.println("Kupac vec postoji!");
                return 1;
            }

            ps = con.prepareStatement(query);
            ps.setString(1, name);
            ps.setInt(2, cityId);
            ps.executeUpdate();

            ps = con.prepareStatement(id);
            ps.setString(1, name);
            rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }

    public int setCity(int buyerId, int cityId){
        String query = "UPDATE KUPAC SET IdG = ? WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, cityId);
            ps.setInt(2, buyerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    public int getCity(int buyerId){
        String query = "SELECT IdG FROM KUPAC WHERE ID = ?";
        try{
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getInt(1);
            }
            else return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public BigDecimal increaseCredit(int buyerId, BigDecimal credit){
        String query = "UPDATE KUPAC SET Stanje = Stanje + ? WHERE Id = ?";
        String query2 = "SELECT Stanje FROM KUPAC WHERE Id = ?";
        try{
            PreparedStatement ps = con.prepareStatement(query);
            ps.setBigDecimal(1,credit);
            ps.setInt(2, buyerId);
            ps.executeUpdate();
            ps = con.prepareStatement(query2);
            ps.setInt(1,buyerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getBigDecimal(1);
            }
            else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int createOrder(int buyerId){
        String query = "INSERT INTO PORUDZBINA VALUES (NULL, NULL, NULL, NULL, NULL, ?, ?, ?)";
        String query2 = "SELECT Max(Id) FROM PORUDZBINA";//poslednja kreirana ce imati najveci id
        try{


            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, buyerId);
            ps.setString(2,"created");
            ps.setString(3, "F");
            ps.executeUpdate();
            ps = con.prepareStatement(query2);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
            else return -1;

        }catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<Integer> getOrders(int buyerId){
        String query = "SELECT Id FROM PORUDZBINA WHERE IdK = ?";
        List<Integer> ret = new LinkedList<>();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,buyerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                ret.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public BigDecimal getCredit(int buyerId){
        String query = "SELECT Stanje FROM KUPAC WHERE Id = ?";
        BigDecimal ret = null;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,buyerId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ret = rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public List<Integer> getAllBuyers(){
        String query = "SELECT Id FROM KUPAC";
        List<Integer> ret = new LinkedList<>();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                ret.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public BuyerOp(){
        con = DB.getInstance().getConnection();

    }

}
