package com.arise.corona.impl;

import com.arise.corona.dto.ContentInfo;
import org.junit.Test;

public class SuggestionServiceTest {


    @Test
    public void test(){
        ContentInfo mediaInfo = new ContentInfo().setPath("path/whatever/bruce springsteen - X unknown tour");
        SuggestionService suggestionService = new SuggestionService()
                .load("corona/config/commons/suggestions.json")
//                .(mediaInfo, null)
                ;
    }

}
