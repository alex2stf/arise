package com.arise.cargo;

public enum  RelationType {
    ONE_TO_ONE(new String[]{"one_to_one"}),
    ONE_TO_MANY(new String[]{"one_to_many"}),
    MANY_TO_ONE(new String[]{"many_to_one"}),
    MANY_TO_MANY(new String[]{"many_to_many"});


    private final String[] identifiers;

    RelationType(String [] identifiers){

        this.identifiers = identifiers;
    }

    public static RelationType fromString(String s){
        for (RelationType t: RelationType.values()){
            for (String x: t.identifiers){
                if (x.equalsIgnoreCase(s)){
                    return t;
                }
            }
        }


        return null;
    }
}
