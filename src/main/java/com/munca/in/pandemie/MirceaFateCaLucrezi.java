


package com.munca.in.pandemie;

import java.awt.*;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MirceaFateCaLucrezi {


    public static void main(String[] args) throws Throwable {

        Robot r = new Robot(); int i = 0;

        System.out.println("Salut " + System.getProperty("user.name") + " ... hai sa vedem ce am facut azi!");
        DateFormatSymbols f = new DateFormatSymbols(Locale.getDefault());
        f.setWeekdays(new String[]{
                "ce kkt de zi e asta?", "Duminica", "Luni", "Marti",
                "Miercuri", "Joi", "Vineri", "Sâmbătă",
        });
        SimpleDateFormat z = new SimpleDateFormat("EEEEE dd MM yyyy 'la ora' HH:mm:ss", f);
        String t = "%s ne-am facut ca muncim";


        while (true){
            System.out.println(String.format(t, z.format(new Date())));
            r.mouseMove(i++, ++i);
            Thread.sleep(1000 * 60 * 3);
        }
    }
}
