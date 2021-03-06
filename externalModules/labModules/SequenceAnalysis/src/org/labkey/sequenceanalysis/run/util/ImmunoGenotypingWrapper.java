package org.labkey.sequenceanalysis.run.util;

import org.apache.log4j.Logger;
import org.labkey.api.pipeline.PipelineJobException;
import org.labkey.api.sequenceanalysis.pipeline.SequencePipelineService;
import org.labkey.api.sequenceanalysis.run.AbstractGatkWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bimber on 6/19/2017.
 */
public class ImmunoGenotypingWrapper extends AbstractGatkWrapper
{
    public ImmunoGenotypingWrapper(Logger log)
    {
        super(log);
    }

    @Override
    protected String getJarName()
    {
        return "DISCVRSeq.jar";
    }

    public File execute(File inputBam, File referenceFasta, File outputPrefix, List<String> options) throws PipelineJobException
    {
        List<String> args = new ArrayList<>();
        args.add(SequencePipelineService.get().getJavaFilepath());
        args.addAll(SequencePipelineService.get().getJavaOpts());
        args.add("-jar");
        args.add(getJAR().getPath());
        args.add("ImmunoGenotyper");
        args.add("-R");
        args.add(referenceFasta.getPath());
        args.add("-I");
        args.add(inputBam.getPath());
        args.add("-O");
        args.add(outputPrefix.getPath());
        if (options != null)
        {
            args.addAll(options);
        }

        execute(args);

        File outputTxt = new File(outputPrefix.getPath() + ".genotypes.txt");
        if (!outputTxt.exists())
        {
            throw new PipelineJobException("Expected output not found: " + outputTxt.getPath());
        }

        return outputTxt;
    }
}
