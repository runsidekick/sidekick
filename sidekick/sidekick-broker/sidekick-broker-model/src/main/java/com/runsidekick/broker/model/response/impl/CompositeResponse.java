package com.runsidekick.broker.model.response.impl;

import com.runsidekick.broker.model.response.Response;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
@RequiredArgsConstructor
public class CompositeResponse<R extends Response> extends BaseResponse {
    private final List<R> responses;
}
