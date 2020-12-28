package operations;

import usluge.DB;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ShopOp implements ShopOperations {

   Connection con;

    @Override
    public int createShop(String name, String cityName){
        String query = "INSERT INTO PRODAVNICA VALUES (0,0,?,?)";
        String query2 = "SELECT Id FROM GRAD WHERE Ime = ?";
        String query3 = "SELECT Id FROM PRODAVNICA WHERE Ime = ? AND IdG = ?";
        String provjera ="SELECT P.Id FROM Prodavnica P, Grad G WHERE P.Ime = ? AND P.IdG = G.Id AND G.Ime = ?";
        try{
            int id_grada;
            PreparedStatement ps = con.prepareStatement(provjera);
            ps.setString(1, name);
            ps.setString(2, cityName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Prodavnica vec postoji!");
                return rs.getInt(1);
            }

            ps = con.prepareStatement(query2);
            ps.setString(1,cityName);
            rs = ps.executeQuery();
            if(rs.next()){
                id_grada = rs.getInt(1);
                ps = con.prepareStatement(query);
                ps.setString(2,name);
                ps.setInt(1,rs.getInt(1));
                ps.executeUpdate();
            }
            else return -1;

            ps = con.prepareStatement(query3);
            ps.setString(1,name);
            ps.setInt(2, id_grada);
            rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
            else return -1;

        }catch(SQLException e){
            e.printStackTrace();
            return -1;
       }
    }

    @Override
    public int setCity(int shopId, String cityName) {
        String query = "UPDATE PRODAVNICA SET IdG = ? WHERE Id = ?";
        String query2 = "SELECT Id FROM GRAD WHERE Ime = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query2);
            ps.setString(1,cityName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ps = con.prepareStatement(query);
                ps.setInt(2,shopId);
                ps.setInt(1,rs.getInt(1));
                ps.executeUpdate();
                return 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getCity(int shopId) {
        String query = "SELECT IdG FROM PRODAVNICA WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,shopId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int setDiscount(int shopId, int discountPercentage) {
        String query = "UPDATE PRODAVNICA SET Popust = ? WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,discountPercentage);
            ps.setInt(2,shopId);
            ps.executeUpdate();
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int increaseArticleCount(int articleId, int increment) {
        String query = "SELECT Kolicina FROM Artikal WHERE Id = ?";
        String query2 = "UPDATE Artikal SET Kolicina = ? WHERE Id = ?";
        int kolicina = 0;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,articleId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                kolicina = rs.getInt(1);
            }
            else return -1;

            kolicina += increment;

            ps = con.prepareStatement(query2);
            ps.setInt(1,kolicina);
            ps.setInt(2,articleId);
            ps.executeUpdate();
            return kolicina;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;

    }

    @Override
    public int getArticleCount(int articleId) {
        String query = "SELECT Kolicina FROM Artikal WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,articleId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
            else return -1;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getArticles(int shopId) {
        String query = "SELECT Id FROM Artikal WHERE IdP = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            List<Integer> ret = new LinkedList<Integer>();
            while(rs.next()){
                ret.add(rs.getInt(1));
            }
            return ret;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getDiscount(int shopId) {
        String query = "SELECT Popust FROM Prodavnica WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public ShopOp(){
       con = DB.getInstance().getConnection();
   }
}
