package usluge;

import java.util.Calendar;

public class Dogadjaj{
    public int idGrada;
    public int idPor;
    public String status;
    public Calendar vrijeme;

    public Dogadjaj(int idG, int idP, String status, Calendar tren){
        idGrada = idG;
        idPor = idP;
        this.status = status;
        vrijeme = tren;
    }

}