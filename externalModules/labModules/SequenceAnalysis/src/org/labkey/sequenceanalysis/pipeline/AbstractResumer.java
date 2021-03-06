package org.labkey.sequenceanalysis.pipeline;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.labkey.api.pipeline.PipelineJobException;
import org.labkey.api.pipeline.RecordedAction;
import org.labkey.api.writer.PrintWriters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Created by bimber on 4/2/2017.
 */
abstract public class AbstractResumer implements Serializable
{
    transient Logger _log;
    transient static final XStream _xstream = new XStream(new XppDriver());
    transient File _localWorkDir;

    protected TaskFileManagerImpl _fileManager;
    protected LinkedHashSet<RecordedAction> _recordedActions = null;
    protected boolean _isResume = false;
    protected Map<File, File> _copiedInputs = new HashMap<>();

    //for serialization
    protected AbstractResumer()
    {

    }

    protected AbstractResumer(File localWorkDir, Logger log, TaskFileManagerImpl fileManager)
    {
        _localWorkDir = localWorkDir;
        _log = log;
        _fileManager = fileManager;
        _recordedActions = new LinkedHashSet<>();
    }

    protected static File getSerializedXml(File outdir, String xmlName)
    {
        return new File(outdir, xmlName);
    }

    protected static <RESUMER extends AbstractResumer> RESUMER readFromXml(File xml, Class<RESUMER> clazz) throws PipelineJobException
    {
        try (BufferedInputStream bus = new BufferedInputStream(new FileInputStream(xml)))
        {
            return (RESUMER) _xstream.fromXML(bus);
        }
        catch (IOException e)
        {
            throw new PipelineJobException(e);
        }
    }

    protected void writeToXml() throws PipelineJobException
    {
        writeToXml(_localWorkDir);
    }

    abstract protected String getXmlName();

    protected void writeToXml(File outDir) throws PipelineJobException
    {
        _log.debug("saving job checkpoint to file");
        _log.debug("total actions: " + _recordedActions.size());
        if (outDir == null)
        {
            throw new PipelineJobException("output directory was null");
        }

        File output = getSerializedXml(outDir, getXmlName());
        _log.debug("using file: " + output.getPath());
        try (PrintWriter writer = PrintWriters.getPrintWriter(output))
        {
            String xml = _xstream.toXML(this);
            _log.debug("xml length: " + xml.length());
            writer.write(xml);
        }
        catch (Throwable e)
        {
            throw new PipelineJobException(e);
        }
    }

    public void markComplete()
    {
        File xml = getSerializedXml(_localWorkDir, getXmlName());
        if (xml.exists())
        {
            _log.info("closing job resumer");
            xml.delete();
        }
    }

    public void saveState() throws PipelineJobException
    {
        writeToXml();
    }

    public TaskFileManagerImpl getFileManager()
    {
        return _fileManager;
    }

    public void setFileManager(TaskFileManagerImpl fileManager)
    {
        _fileManager = fileManager;
    }

    public boolean isResume()
    {
        return _isResume;
    }

    public void setResume(boolean resume)
    {
        _isResume = resume;
    }

    public LinkedHashSet<RecordedAction> getRecordedActions()
    {
        return _recordedActions;
    }

    public void setRecordedActions(LinkedHashSet<RecordedAction> recordedActions)
    {
        _recordedActions = recordedActions;
    }

    public File getLocalWorkDir()
    {
        return _localWorkDir;
    }

    public void setLocalWorkDir(File localWorkDir)
    {
        _localWorkDir = localWorkDir;
    }

    public Map<File, File> getCopiedInputs()
    {
        return _copiedInputs;
    }

    public void setCopiedInputs(Map<File, File> copiedInputs)
    {
        _copiedInputs = copiedInputs;
    }
}
