package operations;

import sun.awt.image.ImageWatched;
import usluge.DB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class TransactionOp implements TransactionOperations {

    Connection con;

    @Override
    public BigDecimal getBuyerTransactionsAmmount(int buyerId) {
        String query = "SELECT Vrijednost FROM Transakcije WHERE IdK = ?";
        BigDecimal ret = BigDecimal.ZERO;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                ret = ret.add(rs.getBigDecimal(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int shopId) {
        String query = "SELECT Vr_Prod FROM Transakcije WHERE IdPr = ?";
        BigDecimal ret = BigDecimal.ZERO.setScale(3);
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                ret = ret.add(rs.getBigDecimal(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public List<Integer> getTransationsForBuyer(int buyerId) {
        String query = "SELECT Id FROM Transakcije WHERE IdK = ?";
        List<Integer> ret = new LinkedList<Integer>();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                ret.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    public int getTransactionForBuyersOrder(int orderId) {
        String query = "SELECT Id FROM Transakcije WHERE IdPor = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getTransactionForShopAndOrder(int orderId, int shopId) {
        String query = "SELECT Id FROM Transakcije WHERE IdPr = ? AND IdPor = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, shopId);
            ps.setInt(2, orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getTransationsForShop(int shopId) {
        List<Integer> ret = new LinkedList<Integer>();
        String query = "SELECT Id FROM Transakcije WHERE IdPr = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
                ret.add(rs.getInt(1));
            if(ret.size() > 0)return ret;
            else return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Calendar getTimeOfExecution(int transactionId) {
        Calendar cal = Calendar.getInstance();
        String query = "SELECT Datum FROM Transakcije WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            java.sql.Date dat = null;
            if(rs.next()) dat = rs.getDate(1);
            cal.setTime(dat);
            return cal;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int orderId) {
        String query = "SELECT Vrijednost FROM Transakcije WHERE IdPor = ?";
        BigDecimal ret = BigDecimal.ZERO.setScale(3);
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) ret = rs.getBigDecimal(1);
            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int shopId, int orderId) {
        String query = "SELECT Vr_Prod FROM Transakcije WHERE IdPr = ? AND IdPor = ?";
        BigDecimal ret = BigDecimal.ZERO.setScale(3);
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, shopId);
            ps.setInt(2, orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) ret = rs.getBigDecimal(1);
            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public BigDecimal getTransactionAmount(int transactionId) {
        String query = "SELECT Vrijednost FROM Transakcije WHERE Id = ?";
        BigDecimal ret = BigDecimal.ZERO.setScale(3);
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,transactionId);
            ResultSet rs = ps.executeQuery();
            if(rs.next())ret = rs.getBigDecimal(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return ret;
    }

    @Override
    public BigDecimal getSystemProfit() {
        String query = "SELECT Profit FROM Transakcije";
        BigDecimal ret = BigDecimal.ZERO.setScale(3);
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                ret = ret.add(rs.getBigDecimal(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public TransactionOp(){
        con = DB.getInstance().getConnection();
    }

}
