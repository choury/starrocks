// This file is made available under Elastic License 2.0.
// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/main/java/org/apache/doris/analysis/CreateResourceStmt.java

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.analysis;

import com.starrocks.catalog.Resource.ResourceType;
import com.starrocks.common.util.PrintableMap;
import com.starrocks.sql.ast.AstVisitor;

import java.util.Map;

// CREATE [EXTERNAL] RESOURCE resource_name
// PROPERTIES (key1 = value1, ...)
public class CreateResourceStmt extends DdlStmt {
    private static final String TYPE = "type";

    private final boolean isExternal;
    private final String resourceName;
    private final Map<String, String> properties;
    private ResourceType resourceType;

    public CreateResourceStmt(boolean isExternal, String resourceName, Map<String, String> properties) {
        this.isExternal = isExternal;
        this.resourceName = resourceName;
        this.properties = properties;
        this.resourceType = ResourceType.UNKNOWN;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }
    public boolean isExternal() {
        return isExternal;
    }

    // only used for UT
    public void setResourceType(ResourceType type) {
        this.resourceType = type;
    }

    @Override
    public String toSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ");
        if (isExternal) {
            sb.append("EXTERNAL ");
        }
        sb.append("RESOURCE '").append(resourceName).append("' ");
        sb.append("PROPERTIES(").append(new PrintableMap<>(properties, " = ", true, false)).append(")");
        return sb.toString();
    }
    @Override
    public boolean isSupportNewPlanner() {
        return true;
    }
    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitCreateResourceStatement(this, context);
    }
    @Override
    public String toString() {
        return toSql();
    }
}

