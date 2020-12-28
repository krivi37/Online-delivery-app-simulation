package operations;

import usluge.DB;
import usluge.Dogadjaj;

import javax.xml.transform.Result;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OrderOp implements  OrderOperations {

    Connection con;

    @Override
    public int addArticle(int orderId, int articleId, int count) {
        String query1 = "SELECT Id, Kolicina, Vrijednost FROM Por_Artikli WHERE IdP = ? AND IdA = ?";//provjera da li je artikal vec porucen kao i dohvatanja ID - a stavke i kolicine
        String query2 = "SELECT IdG FROM Prodavnica P, Artikal A WHERE A.Id = ? AND A.IdP = P.Id";//dohvatanje ID - a grada artikla u porudzbini
        String query3 = "INSERT INTO Por_Artikli Values (?, ? , ?, ?, ?, ?)";//ubacivanje novog artikla u porudzbinu
        String query4 = "UPDATE Por_Artikli SET Kolicina = ?, Vrijednost = ?, Vrijednost_puna = ? WHERE Id = ?";//azuriranje kolicine artikla koji je vec u porudzbini
        String query5 = "SELECT Kolicina, Cijena FROM Artikal WHERE Id = ?";//dohvatanje kolicine i cijene artikala u prodavnici
        String query6 = "UPDATE Artikal SET Kolicina = ? WHERE Id = ?";//azuriranje kolicine artikala u prodavnici
        String query7 = "SELECT Popust FROM Prodavnica P, Artikal A WHERE A.Id = ? AND A.IdP = P.Id";//dohvatanje popusta iz prodavnice
        int kolicina = 0;
        int pret_kol = 0;
        double vrijednost = 0;
        double vrijednost_bez_popusta = 0;
        double cijena_artikla = 0;
        double popust = 0;
        int id_g = 0;
        int id = -1;
        try {
            PreparedStatement ps = con.prepareStatement(query1);
            ps.setInt(1, orderId);
            ps.setInt(2, articleId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){//ako je artikal vec u porudzbini
                id = rs.getInt(1);
                kolicina = rs.getInt(2);
                vrijednost = rs.getDouble(3);
                pret_kol = kolicina;
                kolicina += count;

                ps = con.prepareStatement(query5);//provjera dostupne kolicine artikla
                ps.setInt(1, articleId);
                ResultSet rs1 = ps.executeQuery();
                if(rs1.next()){
                    if(kolicina > rs1.getInt(1)){
                        System.out.println("Porucena kolicina je veca od trenutno dostupne u prodavnici!");
                        return -1;
                    }
                    cijena_artikla = rs.getDouble(2);
                }


                ps = con.prepareStatement(query7);//dohvatanje popusta
                ps.setInt(1, articleId);
                rs1 = ps.executeQuery();
                if(rs1.next()) popust = rs1.getDouble(1);
                if(popust != 0)popust = 1 - popust/100;
                else popust = 1;

                vrijednost_bez_popusta = vrijednost_bez_popusta + kolicina*cijena_artikla;
                vrijednost = vrijednost + kolicina*popust*cijena_artikla;

                ps = con.prepareStatement(query4);//azuriranje kolicine porucenog artikla
                ps.setInt(1, kolicina);
                ps.setInt(4, id);
                ps.setDouble(3, vrijednost_bez_popusta);
                ps.setDouble(2, vrijednost);
                ps.executeUpdate();



                pret_kol -= count;//azuriranje kolicine presotalih artikala u prodavnici
                ps = con.prepareStatement(query6);
                ps.setInt(1, pret_kol);
                ps.setInt(2, id);
                ps.executeUpdate();

                return id;
            }
            else {//u slucaju da dodajemo novi artikal u porudzbinu
                ps = con.prepareStatement(query5);//dohvatanje kolicine artikala u prodavnici
                ps.setInt(1, articleId);
                rs = ps.executeQuery();
                if(rs.next()) {
                    kolicina = rs.getInt(1);
                    cijena_artikla = rs.getDouble(2);
                }

                if(kolicina < count){
                    System.out.println("Porucena kolicina je veca od trenutno dostupne u prodavnici!");
                    return -1;
                }

                ps = con.prepareStatement(query2);//dohvatanje grada prodavnice i postavljanje grada za artikal
                ps.setInt(1, articleId);
                rs = ps.executeQuery();
                int id_grada = 0;
                if(rs.next()) id_grada = rs.getInt(1);


                ps = con.prepareStatement(query7);//dohvatanje popust
                ps.setInt(1, articleId);
                rs = ps.executeQuery();
                if(rs.next()) popust = rs.getDouble(1);
                if(popust != 0)popust = 1 - popust/100;
                else popust = 1;

                vrijednost_bez_popusta = vrijednost_bez_popusta + count*cijena_artikla;
                vrijednost = count*popust*cijena_artikla;

                ps = con.prepareStatement(query3);//ubacivanje artikla u porudzbinu
                ps.setInt(1, orderId);
                ps.setInt(2, articleId);
                ps.setInt(3, count);
                ps.setInt(4, id_grada);
                ps.setDouble(5, vrijednost);
                ps.setDouble(6, vrijednost_bez_popusta);
                ps.executeUpdate();

                int nova_kol = kolicina - count;//azuriranje kolicine artikala u prodavnici
                ps = con.prepareStatement(query6);
                ps.setInt(1, nova_kol);
                ps.setInt(2, articleId);
                ps.executeUpdate();

                BigDecimal cijena = getFinalPrice(orderId);
                String up_cijenu = "UPDATE Porudzbina SET Cijena = ? WHERE Id = ?";
                ps = con.prepareStatement(up_cijenu);
                ps.setBigDecimal(1, cijena);
                ps.setInt(2,orderId);
                ps.executeUpdate();


                ps = con.prepareStatement(query1);//dohvatanje ID - a stavke u porudzbini
                ps.setInt(1, orderId);
                ps.setInt(2, articleId);
                rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt(1);
                }

            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;

    }

    @Override
    public int removeArticle(int orderId, int articleId) {
        String query1 = "SELECT Id, Kolicina FROM Por_Artikli WHERE IdP = ? AND IdA = ?";
        String query2 = "DELETE FROM Por_Artikli WHERE Id = ?";
        String query5 = "SELECT Kolicina FROM Artikal WHERE Id = ?";
        String query6 = "UPDATE Artikal SET Kolicina = ? WHERE Id = ?";

        try {
            PreparedStatement ps = con.prepareStatement(query1);
            ps.setInt(1, orderId);
            ps.setInt(2, articleId);
            ResultSet rs = ps.executeQuery();
            int id = 0;
            int kolicina = 0;
            int pret_kol = 0;

            if (rs.next()){
                id = rs.getInt(1);
                kolicina = rs.getInt(2);
            }

            else return -1;//ako nema porucenog artikla, tj ako je doslo do greske

            ps = con.prepareStatement(query5);//dohvatanje kolicine artikala iz prodavnice
            ps.setInt(1, articleId);
            rs = ps.executeQuery();
            if(rs.next()) pret_kol = rs.getInt(1);

            kolicina += pret_kol;

            ps = con.prepareStatement(query6);//uvecavanje kolicine artikala u prodavnici za kolicinu sa porudzbine
            ps.setInt(1, kolicina);
            ps.setInt(2, articleId);
            ps.executeUpdate();

            ps = con.prepareStatement(query2);//brisanje artikla sa porudzbine
            ps.setInt(1, id);
            ps.executeUpdate();

            return 1;



        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public List<Integer> getItems(int orderId) {

        String query1 = "SELECT Id FROM Por_Artikli WHERE IdP = ?";
        List<Integer> ret = new LinkedList<Integer>();
        try {
            PreparedStatement ps = con.prepareStatement(query1);
            ps.setInt(1,orderId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                ret.add(rs.getInt(1));
            }
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int completeOrder(int orderId) {

        int max = -1;//distanca ne moze biti manja od nule
        int min = Integer.MAX_VALUE;
        int grad = 0;
        int grad_artikla = 0;//id svakog grada je za jedan manji
        int grad_porudzbine = 0;
        int [][] matrica_puta = CityOp.getMatricaPuta();

        Calendar cal = Calendar.getInstance();
        cal.setTime(GeneralOp.getCurrTime().getTime());

        String gradovi = "SELECT IdG FROM Por_Artikli WHERE IdP = ?";
        String grad_korisnika = "SELECT K.IdG FROM Kupac K, Porudzbina P WHERE P.Id = ? AND P.IdK = K.Id";
        String query = "UPDATE Porudzbina SET Datum_kreiranja = ?, Datum_slanja = ?, Datum_primljeno = ?, Stanje = 'sent', IdG = ?, Cijena = ? WHERE Id = ?";
        String query1 = "Select Datum FROM Transakcije where IdK = ? AND Vrijednost > 10000";
        String query2 = "Select IdK FROM Porudzbina WHERE Id = ?";
        try {

            PreparedStatement ps = con.prepareStatement(query2);
            ps.setInt(1,orderId);
            int buyerId = 0;
            ResultSet rs = ps.executeQuery();
            if(rs.next()) buyerId = rs.getInt(1);

            String dod_popust = "F";
            java.util.Date datum2 = GeneralOp.getCurrTime().getTime();
            java.sql.Date dat2 = new java.sql.Date(datum2.getTime());

            ps = con.prepareStatement(query1);
            ps.setInt(1, buyerId);
            rs = ps.executeQuery();
            while(rs.next()){
                long diff = dat2.getTime() - rs.getDate(1).getTime();
                long razlika = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                if (razlika <= 30){
                    dod_popust = "T";
                    break;
                }
            }

            String upd = "UPDATE Porudzbina set Popust = ? WHERE Id = ?";
            ps = con.prepareStatement(upd);
            ps.setString(1,dod_popust);
            ps.setInt(2, orderId);
            ps.executeUpdate();

            ps = con.prepareStatement(grad_korisnika);//dohvatanje grada korisnika
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            if(rs.next()){
                grad = rs.getInt(1) - 1;//zbog indeksa u matrici koji pocinju od nule, smanjuje se id grada za 1
            }

            ps = con.prepareStatement(gradovi);//dohvatanje gradova u kojima se nalaze poruceni artikli
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            while (rs.next()){//najmanja udaljenost porucenog artikla i grada kupca odredjuje grad porudzbine
                grad_artikla = rs.getInt(1) - 1;
                if (min > matrica_puta[grad][grad_artikla]) {
                    min = matrica_puta[grad][grad_artikla];
                    grad_porudzbine = grad_artikla;
                }
            }

            ps = con.prepareStatement(gradovi);//dohvatanje gradova u kojima se nalaze poruceni artikli
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            while (rs.next()){//najveca udaljenost porucenog artikla i grada porudzbine
                grad_artikla = rs.getInt(1) - 1;
                if (max < matrica_puta[grad_porudzbine][grad_artikla]) {
                    max = matrica_puta[grad_porudzbine][grad_artikla];
                }
            }


            cal.add(Calendar.DAY_OF_MONTH, max);//maksimalna udaljenost odredjuje vrijeme koje je potrebno da svi artikli dodju u grad porudzbine
            java.util.Date dat = cal.getTime();
            java.sql.Date datum = new java.sql.Date(dat.getTime());

            Dogadjaj dog = new Dogadjaj(grad_porudzbine + 1 , orderId, "sent", cal);
            GeneralOp.dodajDogadjaj(dog);

            Calendar kal = Calendar.getInstance();
            kal.setTime(cal.getTime());

            List<Integer> put = CityOp.Path(grad_porudzbine, grad);//rekreiranje puta
            for (int i = 0; i < put.size(); i++){
                if (i + 1 <put.size()){//ako nismo dosli do poslednjeg elementa u nizu
                    int dani = matrica_puta[put.get(i)][put.get(i+1)];//u matrici su svi indeksi za jedan manji od stvarnih ID iz baze
                    String stat = "sent";
                    Calendar kal1 = Calendar.getInstance();
                    kal1.setTime(kal.getTime());
                    kal1.add(Calendar.DAY_OF_MONTH, dani);
                    kal.add(Calendar.DAY_OF_MONTH, dani);
                    Dogadjaj d = new Dogadjaj(put.get(i + 1) + 1, orderId, stat, kal1);
                    GeneralOp.dodajDogadjaj(d);
                }
                else{
                    int dani = matrica_puta[put.get(i)][grad];//ako smo u poslednjem mjestu na putu nadji broj dana potreban izmedju tog grada i grada kupca
                    String stat = "arrived";
                    Calendar kal1 = Calendar.getInstance();
                    kal1.setTime(kal.getTime());
                    kal1.add(Calendar.DAY_OF_MONTH, dani);
                    kal.add(Calendar.DAY_OF_MONTH, dani);
                    Dogadjaj d = new Dogadjaj(grad + 1, orderId, stat, kal1);
                    GeneralOp.dodajDogadjaj(d);
                }
            }

            cal = Calendar.getInstance();
            cal.setTime(datum);
            int dani = matrica_puta[grad_porudzbine][grad];
            cal.add(Calendar.DAY_OF_MONTH, dani);
            java.util.Date dat1 = cal.getTime();
            java.sql.Date datum1 = new java.sql.Date(dat1.getTime());

            dat1 = GeneralOp.getCurrTime().getTime();
            java.sql.Date datum_kreiranja = new java.sql.Date(dat1.getTime());

            BigDecimal cijena = getFinalPrice(orderId);


            ps = con.prepareStatement(query);
            ps.setDate(1, datum_kreiranja);
            ps.setDate(2, datum);
            ps.setDate(3, datum1);
            ps.setInt(4, grad_porudzbine+1);
            ps.setBigDecimal(5, cijena);
            ps.setInt(6, orderId);
            ps.executeUpdate();
            return 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public BigDecimal getFinalPrice(int orderId) {
        String call = "{call SP_FINAL_PRICE (?, ?)}";
        try {

            // CallableStatement stmt = con.prepareCall(call);
            CallableStatement stmt = con.prepareCall(call);
            stmt.setInt(1, orderId);
            stmt.registerOutParameter(2, Types.DECIMAL);
            stmt.execute();
            BigDecimal ret = stmt.getBigDecimal(2);
            ret = ret.setScale(3);
            return ret;



        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public BigDecimal getDiscountSum(int orderId) {
        String query = "SELECT Vrijednost_puna, Vrijednost FROM Por_Artikli WHERE IdP = ?";
        BigDecimal ret = BigDecimal.ZERO;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,orderId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                BigDecimal vr1 = rs.getBigDecimal(1);
                BigDecimal vr2 = rs.getBigDecimal(2);
                vr1 = vr1.subtract(vr2);
                ret = ret.add(vr1);
            }
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getState(int orderId) {
        String query = "SELECT Stanje FROM Porudzbina WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,orderId);
            ResultSet rs = ps.executeQuery();
            String ret ="";
            if(rs.next())ret = rs.getString(1);
            ret = ret.replaceAll("\\s+","");
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Calendar getSentTime(int orderId) {
        String query = "SELECT Datum_kreiranja FROM Porudzbina WHERE Id = ?";
        java.sql.Date datum = null;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) datum = rs.getDate(1);
            if(datum != null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(datum);
            return cal;
            }
            else return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Calendar getRecievedTime(int orderId) {
        String query = "SELECT Datum_primljeno FROM Porudzbina WHERE Id = ?";
        java.sql.Date datum = null;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) datum = rs.getDate(1);

            Calendar cal = Calendar.getInstance();
            cal.setTime(datum);
            if (cal.after(GeneralOp.getCurrTime()))return null;
            return cal;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getBuyer(int orderId) {
        String query = "Select IdK FROM Porudzbina WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            int ret = 0;
            if(rs.next()) ret = rs.getInt(1);
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getLocation(int orderId) {
        String query = "Select IdG FROM Porudzbina WHERE Id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next())return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public OrderOp(){
        con = DB.getInstance().getConnection();
    }
}
