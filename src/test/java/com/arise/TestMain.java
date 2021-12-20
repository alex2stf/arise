package com.arise;

import com.arise.core.serializers.parser.GrootTest;
import com.arise.core.tools.AppCacheTest;
import com.arise.core.tools.StringUtilTest;
import com.arise.tests.HttpBoundaryTest;
import com.arise.weland.dto.ContentInfoTest;

public class TestMain {


    public static void main(String[] args) throws Exception {
        ContentInfoTest.main(args);
        HttpBoundaryTest.main(args);
        AppCacheTest.main(args);
        StringUtilTest.main(args);
        GrootTest.main(args);
    }
}
