/*
 * Copyright (c) 2012-2014 LabKey Corporation
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
package org.labkey.wnprc_ehr.table;

import org.labkey.api.data.AbstractTableInfo;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.WrappedColumn;
import org.labkey.api.ehr.EHRService;
import org.labkey.api.ldk.table.AbstractTableCustomizer;
import org.labkey.api.query.QueryForeignKey;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.util.StringExpressionFactory;

/**
 * User: bimber
 * Date: 12/7/12
 * Time: 2:22 PM
 */
public class WNPRC_EHRCustomizer extends AbstractTableCustomizer
{
    public WNPRC_EHRCustomizer()
    {

    }

    public void customize(TableInfo table)
    {
        if (table instanceof AbstractTableInfo)
        {
            customizeColumns((AbstractTableInfo)table);

            if (table.getName().equalsIgnoreCase("Animal") && table.getSchema().getName().equalsIgnoreCase("study"))
                customizeAnimalTable((AbstractTableInfo) table);
            else if (table.getName().equalsIgnoreCase("Birth") && table.getSchema().getName().equalsIgnoreCase("study"))
                customizeBirthTable((AbstractTableInfo) table);
            else if (table.getName().equalsIgnoreCase("protocol") && table.getSchema().getName().equalsIgnoreCase("ehr"))
                customizeProtocolTable((AbstractTableInfo)table);
        }
    }

    private void customizeColumns(AbstractTableInfo ti)
    {
        ColumnInfo project = ti.getColumn("project");
        if (project != null)
        {
            project.setFormat("00000000");
        }

        customizeRoomCol(ti, "room");
        customizeRoomCol(ti, "room1");
        customizeRoomCol(ti, "room2");
    }

    private void customizeRoomCol(AbstractTableInfo ti, String columnName)
    {
        ColumnInfo room = ti.getColumn(columnName);
        if (room != null)
        {
            if (!ti.getName().equalsIgnoreCase("rooms"))
            {
                // Configure the FK to show the raw value to improve performance, since the database can avoid
                // doing the join in many cases
                Container ehrContainer = EHRService.get().getEHRStudyContainer(ti.getUserSchema().getContainer());
                if (ehrContainer != null)
                {
                    UserSchema us = getUserSchema(ti, "ehr_lookups", ehrContainer);
                    if (us != null)
                    {
                        room.setFk(new QueryForeignKey(us, ehrContainer, "rooms", "room", "room", true));
                    }
                }
            }
        }
    }

    private void customizeBirthTable(AbstractTableInfo ti)
    {
        ColumnInfo cond = ti.getColumn("cond");
        if (cond != null)
        {
            cond.setLabel("Housing Condition");
            UserSchema us = getUserSchema(ti, "ehr_lookups");
            if (us != null)
            {
                cond.setFk(new QueryForeignKey(us, null, "housing_condition_codes", "value", "title"));
            }
        }

    }

    private void customizeAnimalTable(AbstractTableInfo ds)
    {
        if (ds.getColumn("MHCtyping") != null)
            return;

        UserSchema us = getStudyUserSchema(ds);
        if (us == null){
            return;
        }

        if (ds.getColumn("demographicsMHC") == null)
        {
            ColumnInfo col = getWrappedIdCol(us, ds, "MHCtyping", "demographicsMHC");
            col.setLabel("MHC SSP Typing");
            col.setDescription("Summarizes MHC SSP typing results for the common alleles");
            ds.addColumn(col);

            ColumnInfo col2 = getWrappedIdCol(us, ds, "ViralLoad", "demographicsVL");
            col2.setLabel("Viral Loads");
            col2.setDescription("This field calculates the most recent viral load for this animal");
            ds.addColumn(col2);

            ColumnInfo col3 = getWrappedIdCol(us, ds, "ViralStatus", "demographicsViralStatus");
            col3.setLabel("Viral Status");
            col3.setDescription("This calculates the viral status of the animal based on tracking project assignments");
            ds.addColumn(col3);

            ColumnInfo col18 = getWrappedIdCol(us, ds, "Virology", "demographicsVirology");
            col18.setLabel("Virology");
            col18.setDescription("This calculates the distinct pathogens listed as positive for this animal from the virology table");
            ds.addColumn(col18);

            ColumnInfo col4 = getWrappedIdCol(us, ds, "IrregularObs", "demographicsObs");
            col4.setLabel("Irregular Obs");
            col4.setDescription("Shows any irregular observations from each animal today or yesterday.");
            ds.addColumn(col4);

            ColumnInfo col7 = getWrappedIdCol(us, ds, "activeAssignments", "demographicsAssignments");
            col7.setLabel("Assignments - Active");
            col7.setDescription("Contains summaries of the assignments for each animal, including the project numbers, availability codes and a count");
            ds.addColumn(col7);

            ColumnInfo col6 = getWrappedIdCol(us, ds, "totalAssignments", "demographicsAssignmentHistory");
            col6.setLabel("Assignments - Total");
            col6.setDescription("Contains summaries of the total assignments this animal has ever had, including the project numbers and a count");
            ds.addColumn(col6);

            ColumnInfo col5 = getWrappedIdCol(us, ds, "assignmentSummary", "demographicsAssignmentSummary");
            col5.setLabel("Assignments - Detailed");
            col5.setDescription("Contains more detailed summaries of the active assignments for each animal, including a breakdown between research, breeding, training, etc.");
            ds.addColumn(col5);

            ColumnInfo col10 = getWrappedIdCol(us, ds, "DaysAlone", "demographicsDaysAlone");
            col10.setLabel("Days Alone");
            col10.setDescription("Calculates the total number of days each animal has been single housed, if applicable.");
            ds.addColumn(col10);

            ColumnInfo bloodCol = getWrappedIdCol(us, ds, "AvailBlood", "demographicsBloodSummary");
            bloodCol.setLabel("Blood Remaining");
            bloodCol.setDescription("Calculates the total blood draw and remaining, which is determine by weight and blood drawn in the past 30 days.");
            ds.addColumn(bloodCol);

            ColumnInfo col17 = getWrappedIdCol(us, ds, "MostRecentTB", "demographicsMostRecentTBDate");
            col17.setLabel("TB Tests");
            col17.setDescription("Calculates the most recent TB date for this animal, time since TB and the last eye TB tested");
            ds.addColumn(col17);

            ColumnInfo col16 = getWrappedIdCol(us, ds, "Surgery", "demographicsSurgery");
            col16.setLabel("Surgical History");
            col16.setDescription("Calculates whether this animal has ever had any surgery or a surgery flagged as major");
            ds.addColumn(col16);

            ColumnInfo col19 = getWrappedIdCol(us, ds, "MostRecentBehav", "demographicsMostRecentBehav");
            col19.setLabel("Behavior - Current");
            col19.setDescription("This calculates the most recent behavior for the animal, based on the behavior table");
            ds.addColumn(col19);
        }

        if (ds.getColumn("totalOffspring") == null)
        {
            ColumnInfo col15 = getWrappedIdCol(us, ds, "totalOffspring", "demographicsTotalOffspring");
            col15.setLabel("Number of Offspring");
            col15.setDescription("Shows the total offspring of each animal");
            ds.addColumn(col15);
        }
    }

    private void customizeProtocolTable(AbstractTableInfo table)
    {
        ColumnInfo protocolCol = table.getColumn("protocol");
        if (protocolCol != null && table.getColumn("pdf") == null)
        {
            ColumnInfo col = table.addColumn(new WrappedColumn(protocolCol, "pdf"));
            col.setLabel("PDF");
            col.setURL(StringExpressionFactory.create("/query/WNPRC/WNPRC_Units/Animal_Services/Compliance_Training/Private/Protocol_PDFs/executeQuery.view?schemaName=lists&query.queryName=ProtocolPDFs&query.protocol~eq=${pdf}", true));
        }

        if (table.getColumn("totalProjects") == null)
        {
            UserSchema us = getUserSchema(table, "ehr");
            if (us != null)
            {
                ColumnInfo col2 = table.addColumn(new WrappedColumn(protocolCol, "totalProjects"));
                col2.setLabel("Total Projects");
                col2.setUserEditable(false);
                col2.setIsUnselectable(true);
                col2.setFk(new QueryForeignKey(us, null, "protocolTotalProjects", "protocol", "protocol"));
            }
        }
    }

    private ColumnInfo getWrappedIdCol(UserSchema us, AbstractTableInfo ds, String name, String queryName)
    {
        String ID_COL = "Id";
        WrappedColumn col = new WrappedColumn(ds.getColumn(ID_COL), name);
        col.setReadOnly(true);
        col.setIsUnselectable(true);
        col.setUserEditable(false);
        col.setFk(new QueryForeignKey(us, null, queryName, ID_COL, ID_COL));

        return col;
    }

    private UserSchema getStudyUserSchema(AbstractTableInfo ds)
    {
        return getUserSchema(ds, "study");
    }

    public UserSchema getUserSchema(AbstractTableInfo ds, String name)
    {
        UserSchema us = ds.getUserSchema();
        if (us != null)
        {
            if (name.equalsIgnoreCase(us.getName()))
                return us;

            return QueryService.get().getUserSchema(us.getUser(), us.getContainer(), name);
        }

        return null;
    }
}
