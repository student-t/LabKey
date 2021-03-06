/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.snprc_ehr;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.TableInfo;
import org.labkey.api.ldk.table.CustomPermissionsTable;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.DeletePermission;
import org.labkey.api.security.permissions.InsertPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.snprc_ehr.security.ManageGroupMembersPermission;
import org.labkey.snprc_ehr.security.ManageRelatedTablesPermission;

/**
 * Created by lkacimi on 5/4/2017.
 */
public class SNPRC_EHRUserSchema extends SimpleUserSchema
{
    public SNPRC_EHRUserSchema(User user, Container container, DbSchema dbschema)
    {
        super(SNPRC_EHRSchema.NAME, null, user, container, dbschema);
    }

    protected TableInfo createWrappedTable(String name, @NotNull TableInfo schemaTable)
    {
        String nameLowercased = name.toLowerCase();
        switch(nameLowercased){
            case SNPRC_EHRSchema.TABLE_VALID_VETS:
            case SNPRC_EHRSchema.TABLE_VALID_BIRTH_CODES:
            case SNPRC_EHRSchema.TABLE_VALID_DEATH_CODES:
            case SNPRC_EHRSchema.TABLE_VALID_INSTITUTIONS:
                return getCustomPermissionTable(createSourceTable(nameLowercased), ManageRelatedTablesPermission.class);
            case SNPRC_EHRSchema.TABLE_GROUP_CATEGORIES:
            case SNPRC_EHRSchema.TABLE_ANIMAL_GROUPS:
                return getCustomPermissionTable(createSourceTable(nameLowercased), ManageGroupMembersPermission.class);
        }

        return super.createWrappedTable(name, schemaTable);
    }


    private TableInfo getCustomPermissionTable(TableInfo schemaTable, Class<? extends Permission> perm)
    {
        CustomPermissionsTable ret = new CustomPermissionsTable(this, schemaTable);
        ret.addPermissionMapping(InsertPermission.class, perm);
        ret.addPermissionMapping(UpdatePermission.class, perm);
        ret.addPermissionMapping(DeletePermission.class, perm);

        return ret.init();
    }

}
