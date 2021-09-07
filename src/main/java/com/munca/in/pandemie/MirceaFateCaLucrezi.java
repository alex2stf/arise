


package com.munca.in.pandemie;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class MirceaFateCaLucrezi {


    public static String unhash(int target) {
        StringBuilder answer = new StringBuilder();
        if (target < 0) {
            // String with hash of Integer.MIN_VALUE, 0x80000000
            answer.append("\\u0915\\u0009\\u001e\\u000c\\u0002");
            if (target == Integer.MIN_VALUE)
                return answer.toString();
            // Find target without sign bit set
            target = target & Integer.MAX_VALUE;
        }
        unhash0(answer, target);
        return answer.toString();
    }

    private static void unhash0(StringBuilder partial, int target) {
        int div = target / 31;
        int rem = target % 31;
        if (div <= Character.MAX_VALUE) {
            if (div != 0)
                partial.append((char)div);
            partial.append((char)rem);
        } else {
            unhash0(partial, div);
            partial.append((char)rem);
        }
    }

    public static void main(String[] args) throws Throwable {


        System.out.println(
               new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date())
        );


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
