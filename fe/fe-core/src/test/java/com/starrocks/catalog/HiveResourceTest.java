// This file is licensed under the Elastic License 2.0. Copyright 2021-present, StarRocks Inc.

package com.starrocks.catalog;

import com.google.common.collect.Maps;
import com.starrocks.analysis.CreateResourceStmt;
import com.starrocks.common.UserException;
import com.starrocks.mysql.privilege.Auth;
import com.starrocks.mysql.privilege.PrivPredicate;
import com.starrocks.persist.gson.GsonUtils;
import com.starrocks.qe.ConnectContext;
import com.starrocks.server.GlobalStateMgr;
import com.starrocks.sql.analyzer.PrivilegeChecker;
import com.starrocks.utframe.UtFrameUtils;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class HiveResourceTest {
    private static ConnectContext connectContext;

    @Before
    public void setUp() throws Exception {
        connectContext = UtFrameUtils.createDefaultCtx();
    }

    @Test
    public void testFromStmt(@Mocked GlobalStateMgr globalStateMgr, @Injectable Auth auth) throws UserException {
        new Expectations() {
            {
                globalStateMgr.getAuth();
                result = auth;
                auth.checkGlobalPriv((ConnectContext) any, PrivPredicate.ADMIN);
                result = true;
            }
        };

        String name = "hive0";
        String type = "hive";
        String metastoreURIs = "thrift://127.0.0.1:9380";
        Map<String, String> properties = Maps.newHashMap();
        properties.put("type", type);
        properties.put("hive.metastore.uris", metastoreURIs);
        CreateResourceStmt stmt = new CreateResourceStmt(true, name, properties);
        com.starrocks.sql.analyzer.Analyzer.analyze(stmt, connectContext);
        PrivilegeChecker.check(stmt, connectContext);
        HiveResource resource = (HiveResource) Resource.fromStmt(stmt);
        Assert.assertEquals("hive0", resource.getName());
        Assert.assertEquals(type, resource.getType().name().toLowerCase());
        Assert.assertEquals(metastoreURIs, resource.getHiveMetastoreURIs());
    }

    @Test
    public void testSerialization() throws Exception {
        Resource resource = new HiveResource("hive0");
        String metastoreURIs = "thrift://127.0.0.1:9380";
        Map<String, String> properties = Maps.newHashMap();
        properties.put("hive.metastore.uris", metastoreURIs);
        resource.setProperties(properties);

        String json = GsonUtils.GSON.toJson(resource);
        Resource resource2 = GsonUtils.GSON.fromJson(json, Resource.class);
        Assert.assertTrue(resource2 instanceof HiveResource);
        Assert.assertEquals(metastoreURIs, ((HiveResource) resource2).getHiveMetastoreURIs());
    }
}
