/*
 * Copyright (c) 2016 LabKey Corporation
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
package org.labkey.remoteapi.security;

import org.json.simple.JSONObject;
import org.labkey.remoteapi.CommandResponse;
import org.labkey.remoteapi.PostCommand;

public class DeletePolicyCommand extends PostCommand<CommandResponse>
{
    String resourceId;

    public DeletePolicyCommand(String resourceId)
    {
        super("security", "deletePolicy");
        this.resourceId = resourceId;
    }

    @Override
    public JSONObject getJsonObject()
    {
        JSONObject result = new JSONObject();

        result.put("resourceId", resourceId);
        return result;
    }

    public DeletePolicyCommand(PostCommand source)
    {
        super(source);
    }
}