/*
 * Copyright (c) 2010-2017 LabKey Corporation
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
package org.labkey.experiment.api;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.dialect.SqlDialect;
import org.labkey.api.exp.query.ExpDataInputTable;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.UserSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jeckels
 * Date: Jan 4, 2010
 */
public class ExpDataInputTableImpl extends ExpInputTableImpl<ExpDataInputTable.Column> implements ExpDataInputTable
{
    public ExpDataInputTableImpl(String name, UserSchema schema)
    {
        super(name, ExperimentServiceImpl.get().getTinfoDataInput(), schema, null);
    }

    public ColumnInfo createColumn(String alias, ExpDataInputTable.Column column)
    {
        switch (column)
        {
            case Data:
            {
                ColumnInfo result = wrapColumn(alias, _rootTable.getColumn("DataId"));
                result.setFk(getExpSchema().getDataIdForeignKey());
                return result;
            }
            case Role:
                return wrapColumn(alias, _rootTable.getColumn("Role"));
            case TargetProtocolApplication:
                ColumnInfo result = wrapColumn(alias, _rootTable.getColumn("TargetApplicationId"));
                result.setFk(getExpSchema().getProtocolApplicationForeignKey());
                return result;

            case LSID:
            {
                final SqlDialect dialect = getSqlDialect();
                SQLFragment sql = new SQLFragment("" +
                        dialect.concatenate(
                                "'" + DataInput.lsidPrefix() + "'",
                                "CAST(" + ExprColumn.STR_TABLE_ALIAS + ".dataId AS VARCHAR)",
                                "'.'",
                                "CAST(" + ExprColumn.STR_TABLE_ALIAS + ".targetApplicationId AS VARCHAR)"));

                ColumnInfo col = new ExprColumn(this, alias, sql, JdbcType.VARCHAR);
                col.setHidden(true);
                col.setCalculated(true);
                col.setUserEditable(false);
                col.setReadOnly(true);
                return col;
            }

            default:
                throw new IllegalArgumentException("Unsupported column: " + column);
        }
    }

    @Override
    protected ColumnInfo getLSIDColumn()
    {
        return getColumn(Column.LSID);
    }

    public void populate()
    {
        addColumn(Column.Data);
        addColumn(Column.TargetProtocolApplication);
        addColumn(Column.Role);
        addColumn(Column.LSID);

        List<FieldKey> defaultCols = new ArrayList<>();
        defaultCols.add(FieldKey.fromParts(Column.Data));
        defaultCols.add(FieldKey.fromParts(Column.Role));
        setDefaultVisibleColumns(defaultCols);
    }

}