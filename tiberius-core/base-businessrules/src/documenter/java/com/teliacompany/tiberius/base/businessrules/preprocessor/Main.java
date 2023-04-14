package com.teliacompany.tiberius.base.businessrules.preprocessor;

import com.teliacompany.tiberius.base.businessrules.BusinessRule;
import com.teliacompany.tiberius.base.businessrules.BusinessRuleComponent;
import com.teliacompany.tiberius.base.businessrules.BusinessRuleMetaData.Entry;
import com.teliacompany.tiberius.base.businessrules.EndPhaseBusinessRule;
import com.teliacompany.tiberius.base.businessrules.InitialBusinessRule;
import com.teliacompany.tiberius.base.businessrules.MainBusinessRule;
import com.teliacompany.tiberius.base.businessrules.PrepareBusinessRule;
import com.teliacompany.tiberius.base.businessrules.RequestTypeEnum;
import com.teliacompany.tiberius.base.businessrules.SetupBusinessRule;
import com.teliacompany.tiberius.base.businessrules.preprocessor.exception.BusinessRulesPreProcessorException;
import com.teliacompany.tiberius.base.businessrules.preprocessor.logger.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.teliacompany.tiberius.base.businessrules.preprocessor.MarkdownCreator.createMd;
import static com.teliacompany.tiberius.base.businessrules.preprocessor.PlantUmlCreator.createPlantUml;

public class Main {
    private static final Logger LOGO_LOGGER = LoggerFactory.getLogoLogger();
    private static final Logger LOG = LoggerFactory.getMainLogger();

    public static void main(String[] args) {
        LOGO_LOGGER.info("\n" +
                "\u001b[1;33m" +
                "   ___           _                  ___       __      \n" +
                "  / _ )__ _____ (_)__  ___ ___ ___ / _ \\__ __/ /__    \n" +
                " / _  / // (_-</ / _ \\/ -_|_-<(_-</ , _/ // / / -_)   \n" +
                "/____/\\_,_/___/_/_//_/\\__/___/___/_/|_|\\_,_/_/\\__/    \n" +
                "  / _ \\_______ / _ \\_______  _______ ___ ___ ___  ____\n" +
                " / ___/ __/ -_) ___/ __/ _ \\/ __/ -_|_-<(_-</ _ \\/ __/\n" +
                "/_/  /_/  \\__/_/  /_/  \\___/\\__/\\__/___/___/\\___/_/   \n" +
                "\u001b[0m");

        if(args.length < 1) {
            LOG.severe("Invalid number of arguments, cannot resolve path. Should be the first argument");
            System.exit(0);
        }
        final String pathArg = args[0];
        final Path path = Path.of(StringUtils.removeEnd(pathArg, "server"));
        LOG.fine("path=" + path);

        Reflections reflections = new Reflections("com.teliacompany.tiberius");
        List<Class<? extends BusinessRule>> businessRuleClasses = reflections.getSubTypesOf(BusinessRule.class).stream()
                .filter(c -> !c.isInterface())
                .collect(Collectors.toList());

        LOG.info("Found " + businessRuleClasses.size() + " business rules");
        businessRuleClasses.forEach(br -> LOG.info(" " + br.getSimpleName()));
        List<BusinessRuleDocument> rules = getBusinessRuleDocuments(businessRuleClasses);

        LOG.info("Validating annotations");
        List<Class<? extends RequestTypeEnum>> requestTypeEnums = reflections.getSubTypesOf(RequestTypeEnum.class).stream()
                .filter(c -> !c.isInterface())
                .collect(Collectors.toList());

        if(!requestTypeEnums.isEmpty()) {
            if(requestTypeEnums.size() > 1) {
                LOG.warning("\u001b[1;31m Only one implementation of RequestTypeEnum should be used, found " + requestTypeEnums.size() + " \u001b[0m");
            }
            List<String> validNames = Arrays.stream(requestTypeEnums.get(0).getEnumConstants()).map(RequestTypeEnum::name).collect(Collectors.toList());
            LOG.info("Validating request names used in annotations are valid...");
            int failures = 0;
            for(BusinessRuleDocument rule : rules) {
                if(!validNames.containsAll(rule.getApplyOnlyForRequests())) {
                    LOG.severe("\u001b[31m Rule " + rule.getName() + " uses invalid request names for 'applyOnlyForRequests'. \u001b[0m");
                    failures++;
                }
                if(!validNames.containsAll(rule.getDontApplyForRequests())) {
                    LOG.severe("\u001b[31m Rule " + rule.getName() + " uses invalid request names for 'dontApplyForRequests'. \u001b[0m");
                    failures++;
                }
            }
            if(failures > 0) {
                LOG.info("Valid names are: " + String.join(", ", validNames));
                throw new BusinessRulesPreProcessorException("Invalid request names used");
            }
        }

        //Generate documentation
        LOG.info("Generating documentation...");
        String plantUml = createPlantUml(rules);
        LOG.info("Generating businessRules.puml... ");
        writeFile(plantUml, path, "businessRules.puml");
        LOG.info("Generating businessRules.md... ");
        writeFile(createMd(rules), path, "businessRules.md");
        LOG.info("\u001b[1;92mBusiness rules documentation successfully generated\u001b[0m");

        LOG.info("\n\u001b[1;33m****************************************************\u001b[0m\n");
    }

    private static void writeFile(String content, Path path, final String filename) {
//        final File f = new File(Documenter.class.getProtectionDomain().getCodeSource().getLocation().getPath());
//        Path path = f.toPath().getParent().getParent(); // Move out of target/classes

        File file = new File(path + "/documents/" + filename);
        boolean k = file.getParentFile().mkdirs();
        if(k) {
            LOG.info("Creating documents directory in repo");
        }

        LOG.fine("Looking for existing file: " + file.getAbsolutePath());

        try {
            if(file.createNewFile()) {
                LOG.fine("New file created");
            } else {
                LOG.fine("File exist");
            }
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
                LOG.fine("Wrote to file");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    private static <I, T> List<BusinessRuleDocument> getBusinessRuleDocuments(List<Class<? extends BusinessRule>> businessRuleClasses) {
        List<BusinessRule> rules = businessRuleClasses.stream()
                .map(clazz -> {
                    try {
                        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                        if(constructors[0].getParameterCount() == 0) {
                            return (BusinessRule) constructors[0].newInstance();
                        }
                        Object[] objArray = IntStream.of(constructors[0].getParameterCount())
                                .mapToObj(i -> null)
                                .toArray();
                        return (BusinessRule) constructors[0].newInstance(objArray);
                    } catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new BusinessRulesPreProcessorException("Could not instantiate business rule, no default constructor found");
                    }
                })
                .collect(Collectors.toList());

        DocumenterBusinessRuleEnforcer<I, T> enforcer = new DocumenterBusinessRuleEnforcer<>(rules);

        final List<BusinessRule> list = new ArrayList<>();
        list.add(enforcer.getInitialBusinessRule());
        list.add(enforcer.getPrepareBusinessRule());
        list.addAll(enforcer.getSetupBusinessRules());
        list.addAll(enforcer.getMainBusinessRules());
        list.add(enforcer.getEndPhaseBusinessRule());

        return list.stream()
                .map(businessRule -> {
                    Class<? extends BusinessRule> clazz = businessRule.getClass();

                    Class<? extends BusinessRule> phaseInterface = findPhaseBusinessRuleInterface(clazz);


                    String brInterface = Arrays.stream(clazz.getInterfaces())
                            .filter(ix -> !ix.equals(BusinessRule.class))
                            .filter(ix -> Arrays.stream(ix.getInterfaces()).anyMatch(BusinessRule.class::isAssignableFrom))
                            .findFirst()
                            .map(Class::getSimpleName)
                            .orElse(null);

                    String prefixInterface = "";
                    if(!phaseInterface.getSimpleName().equals(brInterface)) {
                        prefixInterface = phaseInterface.getSimpleName() + ".";
                    }

                    Entry metaData = enforcer.getBusinessRuleMetaData().get(businessRule.getClass());
                    Set<String> dependencies = metaData.getDependencies().stream()
                            .map(c -> {
                                if(!c.isInterface()) {
                                    return StringUtils.removeEnd(c.getSimpleName(), "BusinessRule");
                                }
                                return c.getSimpleName();
                            })
                            .collect(Collectors.toSet());

                    return new BusinessRuleDocument()
                            .setName(StringUtils.removeEnd(clazz.getSimpleName(), "BusinessRule"))
                            .setSummary(metaData.getSummary())
                            .setApplyWhen(metaData.getAppliedWhenInfo())
                            .setDescription(metaData.getDescription())
                            .setInterfaceName(prefixInterface + brInterface)
                            .setDependencies(new ArrayList<>(dependencies))
                            .setApplyOnlyForRequests(metaData.getAllowList())
                            .setDontApplyForRequests(metaData.getDenyList());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends BusinessRule> findPhaseBusinessRuleInterface(Class<? extends BusinessRule> businessRuleClass) {
        if(Arrays.asList(businessRuleClass.getInterfaces()).contains(BusinessRule.class)) {
            //Found it!
            return businessRuleClass;
        }
        return Arrays.stream(businessRuleClass.getInterfaces())
                .filter(BusinessRule.class::isAssignableFrom)
                .map(ix -> findPhaseBusinessRuleInterface((Class<? extends BusinessRule>) ix))
                .findFirst()
                .orElse(null);
    }

    private static List<Class<? extends BusinessRule>> sortClasses(List<Class<? extends BusinessRule>> businessRulesQueue) {
        final List<Class<? extends BusinessRule>> sortedRules = new ArrayList<>();
        int index = 0;
        final int maxIterations = businessRulesQueue.size() * 2 + 1;

        while(!businessRulesQueue.isEmpty()) {
            Class<? extends BusinessRule> ruleClass = businessRulesQueue.remove(0);
            BusinessRuleComponent annotation = ruleClass.getAnnotation(BusinessRuleComponent.class);
            List<Class<? extends BusinessRule>> dependencies = Arrays.asList(annotation.dependsOn());

            if(dependencies.stream().allMatch(dep -> sortedRules.stream().anyMatch(sortedRule -> sortedRule.isAssignableFrom(dep)))) {
                sortedRules.add(ruleClass);
                index = 0;
            } else {
                businessRulesQueue.add(ruleClass);
                index++;
            }

            // Hacky, not totally reliable and lazy way of detecting dependency cycles :)
            if(index > maxIterations) {
                throw new BusinessRulesPreProcessorException("Probable business rule dependency cycle detected!");
            }
        }
        return sortedRules;
    }
}
