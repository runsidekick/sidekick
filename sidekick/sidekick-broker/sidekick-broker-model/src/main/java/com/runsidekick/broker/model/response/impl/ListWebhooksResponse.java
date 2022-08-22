package com.runsidekick.broker.model.response.impl;

import com.runsidekick.model.dto.WebhookDto;
import lombok.Data;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
public class ListWebhooksResponse extends BaseResponse {

    private List<WebhookDto> webhooks;

}