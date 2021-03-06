<%
    /*
     * Copyright (c) 2015-2016 LabKey Corporation
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
%>
<%@ page import="org.labkey.api.util.UniqueID" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.mpower.MPowerController" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("Ext4");
        dependencies.add("survey");
        dependencies.add("mpower/SurveyPanel.js");
    }
%>
<%
    JspView<MPowerController.CreateSurvey> me = (JspView<MPowerController.CreateSurvey>) HttpView.currentView();
    MPowerController.CreateSurvey bean = me.getModelBean();

    String formRenderId = "survey-form-panel-" + UniqueID.getRequestScopedUID(HttpView.currentRequest());
%>
<div id=<%=q(formRenderId)%>></div>

<script type="text/javascript">

    Ext4.onReady(function(){

        var panel = Ext4.create('LABKEY.ext4.MPowerSurveyPanel', {
            cls             : 'lk-survey-panel themed-panel',
            responsesPk     : <%=q(bean.getToken())%>,
            surveyDesignId  : <%=q(bean.getDesignId())%>,
            renderTo        : <%=q(formRenderId)%>
        });
    });

</script>
