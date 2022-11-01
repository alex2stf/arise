package com.company.todos;

import org.apache.kafka.common.protocol.types.Field;

import java.util.Scanner;
import static java.lang.System.exit;
import static java.lang.System.out;

public class ThingsToRealize {

    private static final String I_WILL_LOOK_FOR_ANOTHER_JOB = "I'll look for another job.";
    private static final int EMPLOYEE_DEFAULT_RESISTANCE = 100;
    static String whatYouWantAsAProjectManager;

    public static void main(String[] args) {
        if (args.length == 0 || null == args[0]){
            throw new RuntimeException("Please restart program! Meanwhile, " + I_WILL_LOOK_FOR_ANOTHER_JOB);
        }
        whatYouWantAsAProjectManager = args[0];
        if ("I need you to deliver me something".equals(whatYouWantAsAProjectManager)){
            Scanner s = new Scanner(System.in);
            whatYouWantAsAProjectManager = s.nextLine();
            if ("I know exactly what I want".equals(whatYouWantAsAProjectManager)){
                out.println("I will do it and everybody will be happy.");
                exit(0);
            }

            int meFindingSolutions = 1;
            while ("We need to talk".equals(whatYouWantAsAProjectManager)){
                out.println("Look, I found solution " + meFindingSolutions + (meFindingSolutions >  EMPLOYEE_DEFAULT_RESISTANCE ? ". Man you tired me. " + I_WILL_LOOK_FOR_ANOTHER_JOB : ".") );
                whatYouWantAsAProjectManager = s.nextLine();
                meFindingSolutions++;
            }
            out.println(whatYouWantAsAProjectManager + " is implemented!");
            exit(0);
        } else {
            out.println("Let me know what you want. Meanwhile, " + I_WILL_LOOK_FOR_ANOTHER_JOB);
            exit(-1);
        }
    }
}
