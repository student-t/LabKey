package org.labkey.jbrowse.button;

import org.labkey.api.ldk.table.SimpleButtonConfigFactory;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.view.template.ClientDependency;
import org.labkey.jbrowse.JBrowseModule;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * User: bimber
 * Date: 7/16/2014
 * Time: 5:37 PM
 */
public class AddLibraryButton extends SimpleButtonConfigFactory
{
    public AddLibraryButton()
    {
        super(ModuleLoader.getInstance().getModule(JBrowseModule.class), "Create JBrowse Session", "JBrowse.window.DatabaseWindow.libraryHandler(dataRegionName);", new LinkedHashSet<>(Arrays.asList(ClientDependency.fromPath("jbrowse/window/DatabaseWindow.js"))));
    }
}
