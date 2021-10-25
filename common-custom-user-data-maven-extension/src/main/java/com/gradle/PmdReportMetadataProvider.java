package com.gradle;

import com.gradle.maven.extension.api.cache.MojoMetadataProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PmdReportMetadataProvider implements MojoMetadataProvider {

    @Override
    public boolean supports(Context context) {
        return "org.apache.maven.plugins.pmd.PmdReport".equals(context.getMojo().getClass().getName());
    }

    @Override
    public void provide(Context context, Metadata metadata) {
        Path targetDir = Paths.get(context.getProject().getBuild().getDirectory());
        metadata
            .inputs(inputs -> inputs
                .fileSet("compileSourceRoots", fileSet -> fileSet
                    .includesProperty("includes")
                    .excludesProperty("excludes"))
                .fileSet("testSourceRoots", fileSet -> fileSet
                    .includesProperty("includes")
                    .excludesProperty("excludes"))
                .fileSet("excludeFromFailureFile", fileSet -> fileSet
                    .normalization("NAME_ONLY")
                )
                .properties(
                    "skip",
                    "format",
                    "linkXRef",
                    "includeTests",
                    "aggregate",
                    "sourceEncoding",
                    "includeXmlInSite",
                    "skipEmptyReport",
                    "jdkToolchain",
                    "targetJdk",
                    "language",
                    "minimumPriority",
                    "rulesets",
                    "typeResolution",
                    "benchmark",
                    "suppressMarker",
                    "skipPmdError",
                    "renderProcessingErrors",
                    "renderRuleViolationPriority",
                    "renderViolationsByPriority"
                )
                .ignoredProperties(
                    "showPmdLog",
                    "session",
                    "rulesetsTargetDirectory",
                    "reactorProjects",
                    "excludeRoots",
                    "xrefLocation",
                    "xrefTestLocation",
                    "targetDirectory",
                    "analysisCache",
                    "analysisCacheLocation"
                )
            )
            .outputs(outputs -> outputs
                .files(files -> files
                    .file("xmlReport", targetDir.resolve("pmd.xml"))
                    .file("txtReport", targetDir.resolve("pmd.txt"))
                    .file("csvReport", targetDir.resolve("pmd.csv"))
                    .file("benchmarkOutputFilename")
                )
                .cacheableBecause("generates consistent outputs for declared inputs")
            );
    }
}
