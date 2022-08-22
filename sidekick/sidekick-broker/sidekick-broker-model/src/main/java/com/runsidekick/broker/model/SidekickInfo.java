package com.runsidekick.broker.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author yasin.kalafat
 */
@Data
@Builder
@ToString
public class SidekickInfo {
    private String workspaceId;
    private String rootToken;
    private String email;
}
