package com.arise.canter;


public class CommandProcessor {

    public void replay(){

    }

    public CommandProcessor play(String id, String ... args){

        return this;
    }

    public void stop(){

    }


    public void pause(){

    }

    private Macro currentMacro;
    public Macro from(String id){
        currentMacro = new Macro(this);
        return currentMacro;
    }

    public class Macro {

        private CommandProcessor processor;

        private Macro(CommandProcessor processor){
            this.processor = processor;
        }

        public Limit to(String id){
            return new Limit(this);
        }
    }

     public class Limit {
         private Macro macro;

         private Limit(Macro macro){
             this.macro = macro;
         }

        public void replay(){
            macro.processor.replay();
        }
    }
}
