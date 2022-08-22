package com.runsidekick.broker.util;

import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class ApplicationAwareProbeQueryFilter {
    private StringBuilder filtersExpr;
    private List<String> args;

    public ApplicationAwareProbeQueryFilter(StringBuilder filtersExpr, List<String> args) {
        this.filtersExpr = filtersExpr;
        this.args = args;
    }
}