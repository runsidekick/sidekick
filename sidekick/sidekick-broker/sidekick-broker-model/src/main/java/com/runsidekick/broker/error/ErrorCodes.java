package com.runsidekick.broker.error;

/**
 * @author serkan.ozal
 */
public interface ErrorCodes {

    CodedError UNKNOWN = new CodedError(0, "An error occurred during the operation");

    CodedError TARGET_APPLICATION_NOT_AVAILABLE =
            new CodedError(100, "Target application (instance id=%s) is not available");

    CodedError TRACEPOINT_ALREADY_EXIST =
            new CodedError(
                    2000,
                    "Tracepoint has been already added in file %s on line %d from client %s");

    CodedError PUT_TRACEPOINT_FAILED =
            new CodedError(
                    2050,
                    "Error occurred while putting tracepoint to file %s on line %d from client %s: %s");

    CodedError LOGPOINT_ALREADY_EXIST =
            new CodedError(
                    3000,
                    "Logpoint has been already added in class %s on line %d from client %s");

    CodedError PUT_LOGPOINT_FAILED =
            new CodedError(
                    3050,
                    "Error occurred while putting logpoint to class %s on line %d from client %s: %s");

    CodedError PUT_REFERENCE_EVENT_FAILED =
            new CodedError(
                    4000,
                    "Error occurred while putting reference event to probe %s with type %s from client %s: %s");

    CodedError ATTACH_FAILED =
            new CodedError(
                    5000,
                    "Error occurred while attaching agents from client %s: %s");

    CodedError DETACH_FAILED =
            new CodedError(
                    5001,
                    "Error occurred while detaching agents from client %s: %s");

}
