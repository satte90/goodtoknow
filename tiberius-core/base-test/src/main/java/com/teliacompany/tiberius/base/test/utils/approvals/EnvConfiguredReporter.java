package com.teliacompany.tiberius.base.test.utils.approvals;

import com.spun.util.ObjectUtils;
import com.spun.util.SystemUtils;
import com.spun.util.io.FileUtils;
import org.approvaltests.reporters.DiffInfo;
import org.approvaltests.reporters.GenericDiffReporter;

import java.io.File;

public class EnvConfiguredReporter extends GenericDiffReporter {
    public EnvConfiguredReporter(String customDiffInfo) {
        super(getDiffInfo(customDiffInfo));
    }

    private static DiffInfo getDiffInfo(String customDiffInfo) {
        int paramSplit = customDiffInfo.indexOf(" ");
        String program = customDiffInfo.substring(0, paramSplit);
        String params = customDiffInfo.substring(paramSplit + 1);
        return new DiffInfo(program, params, GenericDiffReporter.TEXT_FILE_EXTENSIONS);
    }

    @Override
    public void report(String received, String approved) {
        if (!isWorkingInThisEnvironment(received)) {
            throw new RuntimeException(diffProgramNotFoundMessage);
        }
        FileUtils.createIfNeeded(approved);
        launch(received, approved);
    }

    private void launch(String received, String approved) {
        try {
            ProcessBuilder builder = new ProcessBuilder(getCommandLine(received, approved));
            preventProcessFromClosing(builder);
            builder.start().waitFor();
        } catch (Exception e) {
            throw ObjectUtils.throwAsError(e);
        }
    }

    private void preventProcessFromClosing(ProcessBuilder builder) {
        if (!SystemUtils.isWindowsEnviroment()) {
            File output = new File("/dev/null");
            builder.redirectError(output).redirectOutput(output);
        }
    }
}
