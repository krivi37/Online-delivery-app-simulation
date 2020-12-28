package operations;

import usluge.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import usluge.Dogadjaj;

public class GeneralOp implements GeneralOperations {

    //private Calendar calendar;
    static Calendar cal = null;
    static Vector<Dogadjaj> dogadjaji = null;
    static Connection con = null;

    public void setInitialTime(Calendar time){
        //calendar = time;
        GeneralOp.cal = Calendar.getInstance();
        cal.setTime(time.getTime());
    }

    public Calendar time(int days){
        //calendar.add(Calendar.DAY_OF_MONTH, days);
        GeneralOp.cal.add(Calendar.DAY_OF_MONTH, days);
        provjera_dogadjaja();
        return GeneralOp.cal;
    }

    public Calendar getCurrentTime(){
        return GeneralOp.cal;
    }

    public void eraseAll() {
        GeneralOp.dogadjaji = null;
        CityOp.matrica_puta = null;
        CityOp.matrica_prethodnika = null;
        String batch = "EXEC sp_MSForEachTable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'";
        String batch1 = "EXEC sp_MSForEachTable 'DELETE FROM ?'";
        String batch2 = "EXEC sp_MSForEachTable 'ALTER TABLE ? CHECK CONSTRAINT ALL'";
        String reset_seed = "DBCC CHECKIDENT ('Kupac', RESEED, 0);";
        String reset_seed1 = "DBCC CHECKIDENT ('Prodavnica', RESEED, 0);";
        String reset_seed2 = "DBCC CHECKIDENT ('Grad', RESEED, 0);";
        String reset_seed3 = "DBCC CHECKIDENT ('Transakcije', RESEED, 0);";
        String reset_seed4 = "DBCC CHECKIDENT ('Por_Artikli', RESEED, 0);";
        String reset_seed5 = "DBCC CHECKIDENT ('Artikal', RESEED, 0);";
        String reset_seed6 = "DBCC CHECKIDENT ('Porudzbina', RESEED, 0);";

        Connection con = DB.getInstance().getConnection();
        try {
            Statement stmt = con.createStatement();
            stmt.execute(batch);
            stmt.execute(batch1);
            stmt.execute(batch2);
            stmt.execute(reset_seed);
            stmt.execute(reset_seed1);
            stmt.execute(reset_seed2);
            stmt.execute(reset_seed3);
            stmt.execute(reset_seed4);
            stmt.execute(reset_seed5);
            stmt.execute(reset_seed6);


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void dodajDogadjaj(Dogadjaj dog){
        if(dogadjaji == null)dogadjaji = new Vector<Dogadjaj>();
        int t = 0;
        for (int i = 0; i < dogadjaji.size(); i++){
                if(dogadjaji.elementAt(i).vrijeme.after(dog.vrijeme)){//dogadjaji se odrzavaju sortiranim po vremenu da bi se prilikom azuriranja baze proslo kroz manje dogadjaja
                    dogadjaji.insertElementAt(dog, i);
                    return;
                }
            }
        dogadjaji.add(dog);//ako nije izasao iz funkcije, znaci da su svi dogadjaji prije ovog sto se dodaje, pa zato trenutni dogadjaj dodajemo na kraj liste
    }

    public static void provjera_dogadjaja(){
        Calendar cal = GeneralOp.getCurrTime();
        int j = 0;
        if(dogadjaji == null) return;
        if(dogadjaji.size() > 0){
            for (int i = 0; i < dogadjaji.size(); i++){
                if((dogadjaji.elementAt(i).vrijeme.before(cal)) || (dogadjaji.elementAt(i).vrijeme.compareTo(cal)==0)){
                    j++;
                    String upd = "UPDATE Porudzbina SET IdG = ?, Stanje = ? WHERE Id = ?";
                    int id_g = dogadjaji.elementAt(i).idGrada;
                    String status = dogadjaji.elementAt(i).status;
                    int id_p = dogadjaji.elementAt(i).idPor;
                    try {
                        PreparedStatement ps = con.prepareStatement(upd);
                        ps.setInt(1,id_g);
                        ps.setInt(3, id_p);
                        ps.setString(2, status);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                else break;
            }

            for (int i =0; i < j; i++){
                dogadjaji.remove(i);//ukloni dogadjaj koji se desio
            }
        }
    }

    public static Calendar getCurrTime(){
        return GeneralOp.cal;
    }

    public GeneralOp(){
        con = DB.getInstance().getConnection();
    }


}
