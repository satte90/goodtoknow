package com.teliacompany.tiberius.base.test.utils.approvals;

import com.spun.util.StringUtils;
import com.spun.util.tests.StackTraceReflectionResult;
import com.spun.util.tests.TestUtils;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.namer.AttributeStackSelector;
import org.approvaltests.namer.NamerFactory;

import java.io.File;

class ResourceNamer implements ApprovalNamer {
    private StackTraceReflectionResult info = TestUtils.getCurrentFileForMethod(new AttributeStackSelector());
    private final String srcFilePath;

    public ResourceNamer() {
        String sub = NamerFactory.getSubdirectory();
        String subdirectory = StringUtils.isEmpty(sub) ? "" : sub + File.separator;
        String baseDir = this.getBaseDirectory();
        srcFilePath = baseDir + File.separator + subdirectory;
    }

    public ResourceNamer(String srcFilePath) {
        this.srcFilePath = srcFilePath;
    }

    @Override
    public String getApprovalName() {
        return String.format("%s.%s%s", this.info.getClassName(), this.info.getMethodName(), NamerFactory.getAndClearAdditionalInformation());
    }

    @Override
    public String getSourceFilePath() {
        return srcFilePath;
    }

    @Override
    public File getApprovedFile(String extensionWithDot) {
        return new File(String.format("%s/%s.approved%s", getSourceFilePath(), getApprovalName(), extensionWithDot));
    }

    @Override
    public File getReceivedFile(String extensionWithDot) {
        return new File(String.format("%s/%s.received%s", getSourceFilePath(), getApprovalName(), extensionWithDot));
    }

    private String getBaseDirectory() {

        File file = new File("src/test/resources");
        String testResourcePath = file.getAbsolutePath();

        String packageName = this.info.getFullClassName().substring(0, this.info.getFullClassName().lastIndexOf("."));
        String packagePath = packageName.replace('.', File.separatorChar);

        return testResourcePath + File.separatorChar + "approvals" + File.separatorChar + packagePath;
    }
}
