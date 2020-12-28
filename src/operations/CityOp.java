package operations;

import usluge.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

public class CityOp implements CityOperations {

    private Connection con;
    static int[][] matrica_puta = null;
    static int[][] matrica_prethodnika = null;

    @Override
    public int createCity(String name) {
        String provjera = "SELECT * FROM Grad WHERE Ime = ?";
        String query = "INSERT INTO GRAD VALUES (?)";
        String query2 = "SELECT Id FROM GRAD WHERE Ime = ?";
        String dohvati_br_gradova = "SELECT Count(*) FROM Grad";
        int ret = 0;
        try {
            PreparedStatement ps = con.prepareStatement(provjera);
            ps.setString(1,name);
            ps.executeQuery();
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Grad je vec u bazi");
                return rs.getInt(1);
            }
            ps = con.prepareStatement(query);
            ps.setString(1, name);
            ps.executeUpdate();

            ps = con.prepareStatement(query2);
            ps.setString(1,name);
            rs = ps.executeQuery();

            if(rs.next())ret = rs.getInt(1);

            ps = con.prepareStatement(dohvati_br_gradova);
            rs = ps.executeQuery();
            int br_gradova = 0;
            if(rs.next()) br_gradova = rs.getInt(1);

            if(CityOp.matrica_puta == null)CityOp.matrica_puta = new int[br_gradova][br_gradova];
            else{//uradjeno u slucaju da se povezu neki gradovi pa da se doda novi grad. Na ovaj nacin pamtimo staru matricu puta i prosirujemo je
                int[][] stara_matrica = new int[br_gradova - 1][br_gradova - 1];
                for (int i = 0;i <br_gradova - 1;i++)
                    for(int j =0; j<br_gradova -1;j++)
                        stara_matrica[i][j]=CityOp.matrica_puta[i][j];


                CityOp.matrica_puta = new int[br_gradova][br_gradova];
                for (int i = 0;i <br_gradova - 1;i++)
                    for(int j =0; j<br_gradova - 1; j++)
                        CityOp.matrica_puta[i][j] = stara_matrica[i][j];

                for (int i =br_gradova -1; i<br_gradova;i++)
                    for (int j = 0; j<br_gradova;j++)
                        if(i == j)CityOp.matrica_puta[i][j] = 0;
                        else {
                            CityOp.matrica_puta[i][j] = Integer.MAX_VALUE;
                        }

                for (int j =br_gradova -1; j<br_gradova;j++)
                    for (int i = 0; i<br_gradova;i++)
                        if(i == j)CityOp.matrica_puta[i][j] = 0;
                        else CityOp.matrica_puta[i][j] = Integer.MAX_VALUE;

            }
            if(CityOp.matrica_prethodnika == null)CityOp.matrica_prethodnika = new int[br_gradova][br_gradova];
            else{
                int[][] stara_matrica = new int[br_gradova - 1][br_gradova - 1];
                for (int i = 0;i <br_gradova - 1;i++)
                    for(int j =0; j<br_gradova -1;j++)
                        stara_matrica[i][j]=CityOp.matrica_prethodnika[i][j];

                CityOp.matrica_prethodnika = new int[br_gradova][br_gradova];
                for (int i = 0;i <br_gradova - 1;i++)
                    for(int j =0; j<br_gradova -1;j++)
                        CityOp.matrica_prethodnika[i][j] = stara_matrica[i][j];

                for (int i =br_gradova -1; i<br_gradova;i++)
                    for (int j = 0; j<br_gradova;j++)
                        CityOp.matrica_prethodnika[i][j] = 0;

                for (int j =br_gradova -1; j<br_gradova;j++)
                    for (int i = 0; i<br_gradova;i++)
                       CityOp.matrica_prethodnika[i][j] = 0;
            }

            return ret;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;

    }

    @Override
    public List<Integer> getCities() {
        String query = "SELECT Id FROM GRAD";
        List<Integer> ret = new LinkedList<Integer>();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) ret.add(rs.getInt(1));
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    int disconnectCities(int cityId1, int cityId2){
        String query = "DELETE FROM Susjedi WHERE IdG1 = ? AND IdG2 = ?";
        String provjera = "SELECT * FROM Susjedi WHERE IdG1 = ? AND IdG2 = ?";
        try {
            PreparedStatement ps = con.prepareStatement(provjera);
            ps.setInt(1,cityId1);
            ps.setInt(2,cityId2);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                System.out.println("Vec su povezani gradovi!");
                return 1;
            }

            ps = con.prepareStatement(query);
            ps.setInt(1, cityId1);
            ps.setInt(2, cityId2);
            ps.executeUpdate();
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int connectCities(int cityId1, int cityId2, int distance) {
        String query = "INSERT INTO Susjedi VALUES (?, ?, ?)";
        String provjera = "SELECT * FROM Susjedi WHERE IdG1 = ? AND IdG2 = ?";
        String dohvati_susjede = "SELECT * FROM Susjedi";
        String dohvati_br_gradova = "SELECT Count(*) FROM Grad";
        try {
            PreparedStatement ps = con.prepareStatement(provjera);
            ps.setInt(1,cityId1);
            ps.setInt(2,cityId2);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return 1;
            }

            ps = con.prepareStatement(query);
            ps.setInt(1, cityId1);
            ps.setInt(2, cityId2);
            ps.setInt(3, distance);
            ps.executeUpdate();

            ps = con.prepareStatement(dohvati_br_gradova);
            rs = ps.executeQuery();
            int br_gradova = 0;
            if(rs.next()) br_gradova = rs.getInt(1);



            ps = con.prepareStatement(dohvati_susjede);
            rs = ps.executeQuery();
            while(rs.next()){//ili napraviti hes mapu sa ID gradova ili svaki put pri brisanju resetovati identity column
                int i = rs.getInt(1) - 1;//u tabeli ID pocinju od jedinice
                int j = rs.getInt(2) - 1;
                int d = rs.getInt(3);
                CityOp.matrica_puta[i][j] = d;
                CityOp.matrica_puta[j][i] = d;//putevi su u oba smjera
                CityOp.matrica_prethodnika[i][j] = i;
                CityOp.matrica_prethodnika[j][i] = j;//prethodnik gradu j kad se ide u grad i je sam grad i ako su susjedi, bar u pocetku dok je to jedini put
            }

            Floyd(br_gradova);


            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Integer> getConnectedCities(int cityId) {
        String query = "SELECT IdG2 FROM Susjedi WHERE IdG1 = ?";
        String query2 = "SELECT IdG1 FROM Susjedi WHERE IdG2 = ?";
        List<Integer> ret = new LinkedList<Integer>();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,cityId);
            ResultSet rs = ps.executeQuery();
            while(rs.next())ret.add(rs.getInt(1));
            ps = con.prepareStatement(query2);
            ps.setInt(1,cityId);
            rs = ps.executeQuery();
            while(rs.next())ret.add(rs.getInt(1));

            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Integer> getShops(int cityId) {
        String query = "SELECT Id FROM Prodavnica WHERE IdG = ?";
        List<Integer> ret = new LinkedList<Integer>();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, cityId);
            ResultSet rs = ps.executeQuery();
            while(rs.next())ret.add(rs.getInt(1));
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int[][] getMatricaPuta(){
        return CityOp.matrica_puta;
    }

    public static int[][] getMatricaPrethodnika(){
        return CityOp.matrica_prethodnika;
    }

    private static void Floyd(int br_gradova){
        for (int k = 0; k < br_gradova; k++)
            for (int i = 0; i < br_gradova; i++)
                for (int j =0; j < br_gradova; j++){
                    try {
                        if (CityOp.matrica_puta[i][j] > Math.addExact(CityOp.matrica_puta[i][k], CityOp.matrica_puta[k][j])) {// CityOp.matrica_puta[i][k] + CityOp.matrica_puta[k][j])
                            CityOp.matrica_prethodnika[i][j] = CityOp.matrica_prethodnika[k][j];
                            CityOp.matrica_puta[i][j] = Math.addExact(CityOp.matrica_puta[i][k], CityOp.matrica_puta[k][j]);
                        }
                    }
                    catch(ArithmeticException e){
                        continue;//ako dodje do prekoracenja u sabiranju
                    }
                }
    }

    public static List<Integer> Path(int idPolaznogGrada, int idOdrGrada) {//nalazenje puta preko matrice prethodnika
        List<Integer> ret = new ArrayList<Integer>();

        if (idPolaznogGrada == idOdrGrada) {
            ret.add(idPolaznogGrada);
            return ret;
        }
        ret.add(idPolaznogGrada);
        int i = idPolaznogGrada;
        int j = idOdrGrada;
        while (i != matrica_prethodnika[i][j]) {//dodajemo u put sledeci cvor na putu, i taj sledeci cvor postavljamo za pocetni, kada pocetni cvor na putu postane isto sto i sledbenik do krajnjeg cvora, prekidamo iteraciju
            ret = Path(matrica_prethodnika[i][j], j);
            i = j;
            ret.add(j);
        }
        return ret;
    }

    public CityOp(){
        con = DB.getInstance().getConnection();
    }
}
