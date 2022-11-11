package com.runsidekick.service.impl;

import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.event.impl.BaseEvent;
import com.runsidekick.broker.model.event.impl.ErrorStackSnapshotEvent;
import com.runsidekick.broker.model.event.impl.LogPointEvent;
import com.runsidekick.broker.model.event.impl.TracePointSnapshotEvent;
import com.runsidekick.broker.service.ApplicationService;
import com.runsidekick.helper.EventHistoryHelper;
import com.runsidekick.model.EventHistory;
import com.runsidekick.model.EventHitCount;
import com.runsidekick.model.request.EventHistoryRequest;
import com.runsidekick.repository.EventHistoryRepository;
import com.runsidekick.service.EventHistoryService;
import io.thundra.swark.utils.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.runsidekick.model.EventType.ERRORSNAPSHOT;
import static com.runsidekick.model.EventType.LOGPOINT;
import static com.runsidekick.model.EventType.TRACEPOINT;

/**
 * @author yasin.kalafat
 */
@Service
public class EventHistoryServiceImpl implements EventHistoryService {

    @Value("${event.history.writer.thread.count:50}")
    private int eventHistoryWriterThreadCount;

    @Autowired
    protected ApplicationService applicationService;

    @Autowired
    private EventHistoryRepository eventHistoryRepository;

    @Autowired
    private EventHistoryHelper eventHistoryHelper;

    private ExecutorService executorService;

    @PostConstruct
    void initExecutor() {
        executorService = Executors.newFixedThreadPool(eventHistoryWriterThreadCount);
    }

    @Override
    public void addTracePointEventHistory(String workspaceId, TracePointSnapshotEvent event, TracePoint tracePoint,
                                          String rawMessage) {
        executorService.submit(() -> saveEventHistory(EventHistory.builder()
                .id(UUIDUtils.generateId())
                .workspaceId(workspaceId)
                .applicationFilter(getApplicationFilter(workspaceId, event))
                .fileName(StringUtils.hasText(event.getFileName()) ? event.getFileName() : event.getClassName())
                .lineNo(event.getLineNo())
                .client(event.getClient())
                .eventData(rawMessage)
                .type(TRACEPOINT)
                .probeName(tracePoint.getProbeName())
                .probeTags(tracePoint.getTags())
                .build()));
    }

    @Override
    public void addLogPointEventHistory(String workspaceId, LogPointEvent event, LogPoint logPoint, String rawMessage) {
        executorService.submit(() -> saveEventHistory(EventHistory.builder()
                .id(UUIDUtils.generateId())
                .workspaceId(workspaceId)
                .applicationFilter(getApplicationFilter(workspaceId, event))
                .fileName(StringUtils.hasText(event.getFileName()) ? event.getFileName() : event.getClassName())
                .lineNo(event.getLineNo())
                .client(event.getClient())
                .eventData(rawMessage)
                .type(LOGPOINT)
                .probeName(logPoint.getProbeName())
                .probeTags(logPoint.getTags())
                .build()));
    }

    @Override
    public void addErrorSnapshotEventHistory(String workspaceId, ErrorStackSnapshotEvent event, String rawMessage) {
        executorService.submit(() -> saveEventHistory(EventHistory.builder()
                .id(UUIDUtils.generateId())
                .workspaceId(workspaceId)
                .applicationFilter(getApplicationFilter(workspaceId, event))
                .fileName(StringUtils.hasText(event.getFileName()) ? event.getFileName() : event.getClassName())
                .lineNo(event.getLineNo())
                .client(event.getClient())
                .eventData(rawMessage)
                .type(ERRORSNAPSHOT)
                .build()));
    }

    @Override
    public List<EventHistory> queryEventHistory(EventHistoryRequest request, int page, int size) {
        return eventHistoryRepository.queryEventHistory(request, page, size);
    }

    @Override
    public List<EventHitCount> getCountsGroupedByDate(EventHistoryRequest request) {
        return eventHistoryRepository.countsGroupedByDate(request);
    }

    private void saveEventHistory(EventHistory eventHistory) {
        if (eventHistoryHelper.isEventHistoryEnabled(eventHistory)) {
            eventHistoryRepository.save(eventHistory);
        }
    }

    private ApplicationFilter getApplicationFilter(String workspaceId, BaseEvent event) {
        Application application = applicationService.getApplication(workspaceId, event.getApplicationInstanceId());
        ApplicationFilter applicationFilter = new ApplicationFilter();
        applicationFilter.setName(application.getName());
        applicationFilter.setVersion(application.getVersion());
        applicationFilter.setStage(application.getStage());
        applicationFilter.setCustomTags(application.getCustomTags().stream()
                .collect(Collectors.toMap(
                        Application.CustomTag::getTagName,
                        Application.CustomTag::getTagValue, (a, b) -> b)));
        return applicationFilter;
    }
}
