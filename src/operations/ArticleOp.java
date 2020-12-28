package operations;

import usluge.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArticleOp implements ArticleOperations {

    public int createArticle(int shopId, String articleName, int articlePrice){
        String query = "INSERT INTO Artikal VALUES(?, ?, NULL, ?)";
        String query2 = "SELECT Id FROM Artikal WHERE IdP = ? AND Ime = ?";
        try{
            Connection con = DB.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1,articleName);
            ps.setInt(2,articlePrice);
            ps.setInt(3,shopId);
            ps.executeUpdate();
            ps = con.prepareStatement(query2);
            ps.setInt(1,shopId);
            ps.setString(2, articleName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }
}
