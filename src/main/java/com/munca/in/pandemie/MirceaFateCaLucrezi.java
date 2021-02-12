


package com.munca.in.pandemie;

import java.awt.*;
import java.math.BigDecimal;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.*;
import java.util.*;

public class MirceaFateCaLucrezi {





    public static void main(String[] args) throws Throwable {
            try {
               for (NetworkInterface networkInterface:   Collections.list(NetworkInterface.getNetworkInterfaces()) ){
                   System.out.println(networkInterface.getDisplayName());
               };
            } catch (SocketException e) {
                e.printStackTrace();
            }

        Robot r = new Robot(); int i = 0;

        System.out.println(new BigDecimal("\n \t0000040 ".trim()).toPlainString());
        DateFormatSymbols f = new DateFormatSymbols(Locale.getDefault());
        f.setWeekdays(new String[]{
                "ce kkt de zi e asta?", "Duminica", "Luni", "Marti",
                "Miercuri", "Joi", "Vineri", "Sâmbătă",
        });
        SimpleDateFormat z = new SimpleDateFormat("EEEEE dd MM yyyy 'la ora' HH:mm:ss '_|_'", f);
        String t = "ne-am facut ca muncim %s";

        while (true){
            System.out.println(String.format(t, z.format(new Date())));
            r.mouseMove(i++, ++i);
            Thread.sleep(1000 * 60 * 3);
        }
    }
}
