package com.gradle;

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;
import com.gradle.scan.plugin.BuildScanExtension;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;

public class CommonCustomUserDataGradlePlugin implements Plugin<Object> {

    private final ProviderFactory providers;

    @Inject
    public CommonCustomUserDataGradlePlugin(ProviderFactory providers) {
        this.providers = providers;
    }

    public void apply(Object target) {
        if (target instanceof Settings) {
            if (!isGradle6OrNewer()) {
                throw new GradleException("For Gradle versions prior to 6.0, common-custom-user-data-gradle-plugin must be applied to the Root project");
            }
            applySettingsPlugin((Settings) target);
        } else if (target instanceof Project) {
            if (isGradle6OrNewer()) {
                throw new GradleException("For Gradle versions 6.0 and newer, common-custom-user-data-gradle-plugin must be applied to Settings");
            }
            applyProjectPlugin((Project) target);
        }
    }

    private void applySettingsPlugin(Settings settings) {
        settings.getPluginManager().withPlugin("com.gradle.enterprise", __ -> {
            // configuration changes applied here will override configuration settings set in the settings.gradle(.kts)
            // unwrap this block to instead allow the project's settings.gradle(.kts) to override the configuration settings set by this plugin
            settings.getGradle().settingsEvaluated(___ -> {
                GradleEnterpriseExtension gradleEnterprise = settings.getExtensions().getByType(GradleEnterpriseExtension.class);
                CustomGradleEnterpriseConfig.configureGradleEnterprise(gradleEnterprise, providers);

                BuildScanExtension buildScan = gradleEnterprise.getBuildScan();
                CustomGradleEnterpriseConfig.configureBuildScanPublishing(buildScan, providers);
                CustomBuildScanEnhancements.configureBuildScan(buildScan, settings.getGradle());

                BuildCacheConfiguration buildCache = settings.getBuildCache();
                CustomGradleEnterpriseConfig.configureBuildCache(buildCache, providers);
            });
        });
    }

    private void applyProjectPlugin(Project project) {
        if (!project.equals(project.getRootProject())) {
            throw new GradleException("Common custom user data plugin may only be applied to root project");
        }
        project.getPluginManager().withPlugin("com.gradle.build-scan", __ -> {
            // configuration changes applied here will override configuration settings set in the root project's build.gradle(.kts)
            // unwrap this block to instead allow the root project's build.gradle(.kts) to override the configuration settings set by this plugin
            project.afterEvaluate(___ -> {
                GradleEnterpriseExtension gradleEnterprise = project.getExtensions().getByType(GradleEnterpriseExtension.class);
                CustomGradleEnterpriseConfig.configureGradleEnterprise(gradleEnterprise, providers);

                BuildScanExtension buildScan = gradleEnterprise.getBuildScan();
                CustomGradleEnterpriseConfig.configureBuildScanPublishing(buildScan, providers);
                CustomBuildScanEnhancements.configureBuildScan(buildScan, project.getGradle());

                // Build cache configuration cannot be accessed from a project plugin
            });
        });
    }

    private static boolean isGradle6OrNewer() {
        return GradleVersion.current().compareTo(GradleVersion.version("6.0")) >= 0;
    }

}
