package com.teliacompany.tiberius.base.server;

import com.teliacompany.tiberius.base.server.config.ApplicationProperties;
import com.teliacompany.tiberius.base.server.config.VersionProperties;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.Arrays;
import java.util.Locale;

public class TiberiusScriba implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger STARTUP_LOG = LoggerFactory.getLogger("com.teliacompany.tiberius.startup.Logger");
    private final ApplicationProperties applicationProperties;
    private final VersionProperties versionProperties;

    private final String a;
    private final String p;
    private final String y;
    private final String y2;
    private final String reset;

    public TiberiusScriba(ApplicationProperties applicationProperties, VersionProperties versionProperties) {
        this.applicationProperties = applicationProperties;
        this.versionProperties = versionProperties;

        // Colors (dont color code if not local)
        if(Collections.containsAny(applicationProperties.getActiveSpringProfiles(), Arrays.asList("local", "componenttest"))) {
            this.a = "\033[1m";
            this.p = "     \033[0;35m";
            this.y = "\033[1;33m     ";
            this.y2 = "\033[1;33m";
            this.reset = "\033[0m";
        } else {
            this.a = "";
            this.p = "     ";
            this.y = "     ";
            this.y2 = "";
            this.reset = "";
        }
    }

    @SuppressWarnings(value = "*")
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        //Very important class, will break 100% if removed. Also constructor needs to be public.
        String vRow1 = paddedAfter(versionProperties.getAppVersion(), 31);
        String vRow2 = paddedAfter(versionProperties.getTiberiusCoreVersion(), 31);
        String vRow3 = paddedAfter(versionProperties.getSpringVersion(), 27);
        String vRow4 = paddedAfter(versionProperties.getSpringBootVersion(), 26);
        String vRow5 = paddedAfter(versionProperties.getRequestWebfluxStarterVersion(), 26);
        String vRow6 = paddedAfter(versionProperties.getLog4jVersion(), 21);

        String tiberiusApplicationName = paddedEqual(applicationProperties.getApplicationName().toUpperCase(Locale.ROOT), 64);


        // @formatter:off
        startupLog(y + a +
                "                                                                                          \n" +
                "                       ,,                                         ,,                      \n" +
                "                  ,,,,,                                             ,,,,,                 \n" +
                "             ,, ,,,,,                                                 ,,,,, ,,            \n" +
                "          .,,,,                                                             ,,,,          \n" +
                "         ,,,, ,,,,                                                       ,,,, ,,,,        \n" +
                "       ,,,,                                                                     *,,.      \n" +
                "      .,,, ,,,,                                                             ,,,, ,,,.     \n" +
                "     ,,                                                                             *,    \n" +
                "    ,,,,   ,,,                                                               ,,,   ,,,,   \n" +
                "    ,,, ,,,,                                                                   ,*,,.,,,   \n" +
                "  ,,        "+p+"████████╗██╗██████╗ ███████╗██████╗ ██╗██╗   ██╗███████╗"+y+"         ,, \n" +
                "  ,,,    ,,,"+p+"╚══██╔══╝██║██╔══██╗██╔════╝██╔══██╗██║██║   ██║██╔════╝"+y+" ,*,    ,,* \n" +
                "  ,,,, ,,,, "+p+"   ██║   ██║██████╔╝█████╗  ██████╔╝██║██║   ██║███████╗"+y+"  ,,,, ,,,, \n" +
                " , ,,,,     "+p+"   ██║   ██║██╔══██╗██╔══╝  ██╔══██╗██║██║   ██║╚════██║"+y+"      ,,,, ,\n" +
                " ,,       ,,"+p+"   ██║   ██║██████╔╝███████╗██║  ██║██║╚██████╔╝███████║"+y+" ,,       ,,\n" +
                " ,,,,, ,,,  "+p+"   ╚═╝   ╚═╝╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝ ╚═════╝ ╚══════╝"+y+"   ,,, ,,,,,\n" +
                "   ,,,       ,                                                               ,       ,,,  \n" +
                " ,,,       ,,,              "+p+"    Version: " + vRow1 + y2 +              ",,,       ,,,\n" +
                "  ,,,,,   ,,,,              "+p+"   Tiberius: " + vRow2 + y2 +              ",,,*   ,,,,* \n" +
                "   ,,,,,, ,,,    ,          "+p+"     Spring: " + vRow3 + y2 +          ",    ,,, ,,,,,,  \n" +
                "    ,,,,,,      *,,         "+p+"Spring Boot: " + vRow4 + y2 +         ",,,      ,,,,,,   \n" +
                "    ,,         ,,,,         "+p+"Req Webflux: " + vRow5 + y2 +         ",,,,         ,,   \n" +
                "     ,,,*,,    ,*,,    ,    "+p+"      Log4J: " + vRow6 + y2 +    ",    *,,,    *,,,*,    \n" +
                "       ,,,,,,,, ,,     ,,,                                       ,,,     ,, ,,,,,,,,      \n" +
                "         ,,,,,,,       ,,,,   ,,                           ,,   *,,,       ,,,,,*,        \n" +
                "                       ,,,,,    *,,,                   ,,,,    ,*,,,                      \n" +
                "            ,,,,*,,,,,  *,,     *,,,,,               ,,,*,,     *,,  ,,,*,,,,,,           \n" +
                "              ,,,,,,,,,,,        ,,,,,,             ,,,,,,        ,,,,,,,,,,,             \n" +
                "                 ,,,,,,,,,,        ,,,,             ,,,,        *,,,,,,,,,                \n" +
                "                         .,,,,,,*,,,,                 ,,,,,,,,,,*.                        \n" +
                "                       ******************         ******************                      \n" +
                "                                                                               .---.      \n" +
                "                                                                              /  .  \\    \n" +
                "                                                                             |\\_/|   |   \n" +
                "                                                                             |   |  /|    \n" +
                "      .----------------------------------------------------------------------------' |    \n" +
                "     /  .-.                                                                          |    \n" +
                "    |  /   \\                                                                         |   \n" +
                "    | |\\_.  |"                 + tiberiusApplicationName +                  "        |   \n" +
                "    |\\|  | /|                                                                        |   \n" +
                "    | `---' |                                                                        /    \n" +
                "    |       |----------------------------------------------------------------------'      \n" +
                "    \\       |                                                                            \n" +
                "     \\     /                                                                             \n" +
                "      `---'                                                                               \n" +
                "");
        // @formatter:on
    }

    private void startupLog(final String message) {
        STARTUP_LOG.info("{}{}", message, this.reset);
    }

    private static String paddedAfter(final String label, final int totalLength) {
        if(label == null) {
            return StringUtils.repeat(" ", totalLength);
        }
        return label + StringUtils.repeat(" ", totalLength - label.length());
    }

    private static String paddedEqual(final String label, final int totalLength) {
        if(label == null) {
            return StringUtils.repeat(" ", totalLength);
        }

        int totalAmountOfSpaces = totalLength - label.length();
        int spacesBefore = (int) (Math.floor(totalAmountOfSpaces / 2D));
        int spacesAfter = (int) (Math.ceil(totalAmountOfSpaces / 2D));

        return StringUtils.repeat(" ", spacesBefore) + label + StringUtils.repeat(" ", spacesAfter);
    }
}
