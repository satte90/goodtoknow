package com.teliacompany.tiberius.base.test.utils.approvals;

import com.spun.util.ObjectUtils;
import com.spun.util.io.FileUtils;
import org.approvaltests.reporters.ClipboardReporter;
import org.approvaltests.reporters.EnvironmentAwareReporter;
import org.approvaltests.reporters.GenericDiffReporter;
import org.junit.jupiter.api.Assertions;

import java.io.File;

public class JUnit5Reporter implements EnvironmentAwareReporter {
    public static final JUnit5Reporter INSTANCE = new JUnit5Reporter();

    private JUnit5Reporter() {
    }

    @Override
    public void report(String received, String approved) {
        String aText = new File(approved).exists() ? FileUtils.readFile(approved) : "";
        String rText = FileUtils.readFile(received);
        String approveCommand = "To approve run : " + ClipboardReporter.getAcceptApprovalText(received, approved);
        System.out.println(approveCommand);
        Assertions.assertEquals(aText, rText);
    }

    @Override
    public boolean isWorkingInThisEnvironment(String forFile) {
        try {
            ObjectUtils.loadClass("org.junit.jupiter.api.Assertions");
        } catch (Throwable t) {
            return false;
        }
        return GenericDiffReporter.isFileExtensionValid(forFile, GenericDiffReporter.TEXT_FILE_EXTENSIONS);
    }
}
