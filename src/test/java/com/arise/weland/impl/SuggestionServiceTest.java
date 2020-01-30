package com.arise.weland.impl;

import com.arise.weland.dto.ContentInfo;
import org.junit.Test;

public class SuggestionServiceTest {


    @Test
    public void test(){
        ContentInfo mediaInfo = new ContentInfo().setPath("path/whatever/bruce springsteen - X unknown tour");
        SuggestionService suggestionService = new SuggestionService()
                .load("weland/config/commons/suggestions.json")
//                .(mediaInfo, null)
                ;
    }

}
