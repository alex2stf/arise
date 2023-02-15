package com.arise;

import com.arise.canter.CommandRegistryTest;
import com.arise.canter.CronusTest;
import com.arise.cargo.management.DependencyManager;
import com.arise.cargo.management.DependencyManagerTest;
import com.arise.core.serializers.parser.GrootTest;
import com.arise.core.tools.AppCacheTest;
import com.arise.core.tools.Assert;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.CollectionUtilTest;
import com.arise.core.tools.StringUtilTest;
import com.arise.tests.HttpBoundaryTest;
import com.arise.weland.dto.ContentInfoTest;
import com.arise.weland.impl.RadioPlayerTest;

import static com.arise.core.tools.Assert.assertTrue;

public class TestMain {


    public static void main(String[] args) throws Exception {
        ContentInfoTest.main(args);
        HttpBoundaryTest.main(args);
        AppCacheTest.main(args);
        StringUtilTest.main(args);
        GrootTest.main(args);
        CronusTest.main(args);
        CommandRegistryTest.main(args);
        DependencyManagerTest.main(args);
        CollectionUtilTest.main(args);
        RadioPlayerTest.main(args);
        assertTrue(true);
    }
}
