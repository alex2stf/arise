package com.company.todos;


import java.util.Scanner;

import static java.lang.System.exit;
import static java.lang.System.out;

public class ThingsToRealize {

    private static final String I_WILL_LOOK_FOR_ANOTHER_JOB = "I'll look for another job.";
    static String whatYouWantAsAProjectManager;

    public static void main(String[] args) {

        if (args.length == 0 || null == args[0]){
            throw new RuntimeException("Please restart program! Meanwhile, " + I_WILL_LOOK_FOR_ANOTHER_JOB);
        }
        whatYouWantAsAProjectManager = args[0];
        if ("I need you to deliver me something".equals(whatYouWantAsAProjectManager)){
            Scanner s = new Scanner(System.in);

            out.println("Let me know how I can help you:");
            whatYouWantAsAProjectManager = s.nextLine();

            String feedback;
            while (!"This project is finished. Thanks!".equals(whatYouWantAsAProjectManager)){
                if (whatYouWantAsAProjectManager.startsWith("Please")){
                    feedback = "Is there anything else you want me to do?";
                } else {
                    feedback = ":|";
                }
                out.println("<" + whatYouWantAsAProjectManager + "> is implemented. " + feedback);
                whatYouWantAsAProjectManager = s.nextLine();
            }
            out.println("My job here is done! " + I_WILL_LOOK_FOR_ANOTHER_JOB);
            exit(0);
        } else {
            out.println("Let me know what you want. Meanwhile, " + I_WILL_LOOK_FOR_ANOTHER_JOB);
            exit(-1);
        }
    }
}
