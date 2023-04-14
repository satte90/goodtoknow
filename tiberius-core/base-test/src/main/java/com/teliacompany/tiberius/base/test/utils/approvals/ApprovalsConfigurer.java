package com.teliacompany.tiberius.base.test.utils.approvals;

import org.apache.commons.lang3.StringUtils;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.EnvironmentAwareReporter;
import org.approvaltests.reporters.FirstWorkingReporter;
import org.approvaltests.reporters.JunitReporter;
import org.approvaltests.reporters.linux.LinuxDiffReporter;
import org.approvaltests.reporters.macosx.MacDiffReporter;
import org.approvaltests.reporters.windows.WindowsDiffReporter;
import org.approvaltests.writers.ApprovalWriterFactory;

public final class ApprovalsConfigurer {

    public static final String APPROVALS_CUSTOM_DIFF_INFO = "APPROVALS_CUSTOM_DIFF_INFO";
    public static final String APPROVALS_DISABLE_OS_DIFF_INFO = "APPROVALS_DISABLE_OS_DIFF_INFO";

    private ApprovalsConfigurer() {
    }

    public static void configure() {
        org.approvaltests.Approvals.namerCreater = ResourceNamer::new;
        configureRest();
    }

    public static void configure(String srcPath) {
        org.approvaltests.Approvals.namerCreater = () -> new ResourceNamer(srcPath);
        configureRest();
    }

    private static void configureRest() {
        // Very hacky. Replaces the default JUnitReporter with one that also supports JUnit5
        EnvironmentAwareReporter[] reporters = DiffReporter.INSTANCE.getReporters();
        for (int i = 0; i < reporters.length; i++) {
            if (reporters[i] instanceof JunitReporter) {
                reporters[i] = new FirstWorkingReporter(reporters[i], JUnit5Reporter.INSTANCE);
            }
        }

        // More hacks. Checks for a custom diff info. This way any program or script can be configured.
        // Runs the supplied diff info as command. Replaces two %s with the approved and received file paths.
        // Example values "program %s %s", "/script.sh %s %s"
        String customDiffInfo = System.getenv(APPROVALS_CUSTOM_DIFF_INFO);
        if (customDiffInfo != null) {
            if (StringUtils.countMatches(customDiffInfo, "%s") != 2) {
                throw new RuntimeException("Detected custom approvals with bad format '" + customDiffInfo + "', must contains two '%s' as placeholders for the two diff file paths.");
            }
            reporters[0] = new EnvConfiguredReporter(customDiffInfo);
        }

        // Disables the standard external diff reporters.
        String approvalsDisableDiffInfo = System.getenv(APPROVALS_DISABLE_OS_DIFF_INFO);
        if (approvalsDisableDiffInfo == null || "true".equalsIgnoreCase(approvalsDisableDiffInfo)) {
            for (int i = 0; i < reporters.length; i++) {
                if (isOsAwareReporter(reporters[i])) {
                    reporters[i] = new DisabledReporter();
                }
            }
        }
    }

    private static boolean isOsAwareReporter(EnvironmentAwareReporter reporter) {
        return reporter instanceof WindowsDiffReporter
                || reporter instanceof LinuxDiffReporter
                || reporter instanceof MacDiffReporter;
    }

}
