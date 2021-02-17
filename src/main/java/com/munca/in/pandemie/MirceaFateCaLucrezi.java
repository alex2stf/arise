


package com.munca.in.pandemie;

import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.WelandAPI;
import com.arise.weland.WelandClient;

import java.awt.*;
import java.math.BigDecimal;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.text.*;
import java.util.*;

public class MirceaFateCaLucrezi {





    public static void main(String[] args) throws Throwable {
        WelandClient.openFile("http://localhost:8221/",
                "https://www.youtube.com/watch?v=Uuc7Md2adSU&autoplay=1",
                new CompleteHandler() {
                    @Override
                    public void onComplete(Object data) {
                        System.out.println(data);
                    }
                });
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
