package com.runsidekick.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yasin.kalafat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbeTag {

    private String id;
    private String workspaceId;
    private String tag;

}
