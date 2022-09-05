package com.runsidekick.broker.util;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.ProbeConfig;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author yasin.kalafat
 */
public final class ProbeUtil {

    private ProbeUtil() {

    }

    public static <T extends ProbeConfig> Collection<T> filterProbes(Collection<T> probeConfigs,
                                                                     ApplicationFilter filter) {
        Collection<T> filteredProbes = new ArrayList<>();
        for (T probeConfig : probeConfigs) {
            List<ApplicationFilter> responseFilters = probeConfig.getApplicationFilters();
            for (ApplicationFilter responseFilter : responseFilters) {
                boolean filtered = true;
                if (responseFilter.getName() != null) {
                    if (StringUtils.isEmpty(filter.getName())) {
                        filtered = false;
                    } else {
                        filtered = responseFilter.getName().equals(filter.getName());
                    }
                }
                if (filtered && responseFilter.getStage() != null) {
                    filtered = responseFilter.getStage().equals(filter.getStage());
                }
                if (filtered && responseFilter.getVersion() != null) {
                    filtered = responseFilter.getVersion().equals(filter.getVersion());
                }
                if (filtered && responseFilter.getCustomTags() != null && !responseFilter.getCustomTags().isEmpty()) {
                    Map<String, String> customTagMap = filter.getCustomTags();
                    for (Map.Entry<String, String> entry : responseFilter.getCustomTags().entrySet()) {
                        if (customTagMap != null && customTagMap.containsKey(entry.getKey())) {
                            String tagValue = customTagMap.get(entry.getKey());
                            if (!(entry.getValue() != null && entry.getValue().equals(tagValue))) {
                                filtered = false;
                                break;
                            }
                        } else {
                            filtered = false;
                            break;
                        }
                    }
                }
                if (filtered) {
                    filteredProbes.add(probeConfig);
                    break;
                }
            }
        }
        return filteredProbes;
    }

    public static ApplicationAwareProbeQueryFilter probeQueryFilter(
            String workspaceId, ApplicationFilter applicationFilter) {
        List<String> args = new ArrayList<>();
        args.add(workspaceId);
        return probeQueryFilter(args, applicationFilter);
    }

    public static ApplicationAwareProbeQueryFilter probeQueryFilter(
            String workspaceId, String probeId, ApplicationFilter applicationFilter) {
        List<String> args = new ArrayList<>();
        args.add(workspaceId);
        args.add(probeId);
        return probeQueryFilter(args, applicationFilter);
    }

    private static ApplicationAwareProbeQueryFilter probeQueryFilter(
            List<String> args, ApplicationFilter applicationFilter) {
        StringBuilder filtersExpr = new StringBuilder();
        if (args == null) {
            args = new ArrayList<>();
        }
        if (StringUtils.hasText(applicationFilter.getName())) {
            filtersExpr.append(" AND ");
            filtersExpr.append("(");
            filtersExpr.append("JSON_EXTRACT(application_filters , '$.name') = ?");
            filtersExpr.append(" OR ");
            filtersExpr.append("JSON_EXTRACT(application_filters , '$.name') IS NULL");
            filtersExpr.append(")");
            args.add(applicationFilter.getName());
        }
        if (StringUtils.hasText(applicationFilter.getStage())) {
            filtersExpr.append(" AND ");
            filtersExpr.append("(");
            filtersExpr.append("JSON_EXTRACT(application_filters , '$.stage') = ?");
            filtersExpr.append(" OR ");
            filtersExpr.append("JSON_EXTRACT(application_filters , '$.stage') IS NULL");
            filtersExpr.append(")");
            args.add(applicationFilter.getStage());
        }
        if (StringUtils.hasText(applicationFilter.getVersion())) {
            filtersExpr.append(" AND ");
            filtersExpr.append("(");
            filtersExpr.append("JSON_EXTRACT(application_filters , '$.version') = ?");
            filtersExpr.append(" OR ");
            filtersExpr.append("JSON_EXTRACT(application_filters , '$.version') IS NULL");
            filtersExpr.append(")");
            args.add(applicationFilter.getVersion());
        }
        return new ApplicationAwareProbeQueryFilter(filtersExpr, args);
    }

}
