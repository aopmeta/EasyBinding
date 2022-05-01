package com.aopmeta.easybinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 包含有错误码和错误日志的状态类
 */
public class ResourceStatus {
    @NonNull
    public final Status status;
    public final int errCode;
    @Nullable
    public final String message;

    ResourceStatus(@NonNull Status status, @Nullable String message) {
        this(status, 0, message);
    }

    ResourceStatus(@NonNull Status status, int errCode, @Nullable String message) {
        this.status = status;
        this.errCode = errCode;
        this.message = message;
    }

    public boolean isOk() {
        return status == Status.SUCCESS || status == Status.FINISH;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isFinish() {
        return status == Status.FINISH;
    }

    public boolean isCancel() {
        return status == Status.CANCEL;
    }

    public boolean isCache() {
        return status == Status.CACHE;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isEnd() {
        return isOk() || isError() || isCancel();
    }

    public static ResourceStatus parse(ResourceStatus resourceStatus) {
        return new ResourceStatus(resourceStatus.status, resourceStatus.errCode, resourceStatus.message);
    }

    public static ResourceStatus parse(Resource resource) {
        return new ResourceStatus(resource.status, resource.errCode, resource.message);
    }

    public static ResourceStatus prepare() {
        return new ResourceStatus(Status.PREPARE, null);
    }

    public static ResourceStatus cache() {
        return new ResourceStatus(Status.CACHE, null);
    }

    public static ResourceStatus loading() {
        return new ResourceStatus(Status.LOADING, null);
    }

    public static ResourceStatus success() {
        return new ResourceStatus(Status.SUCCESS, null);
    }

    public static ResourceStatus cancel() {
        return new ResourceStatus(Status.CANCEL, null);
    }

    public static ResourceStatus error(String msg) {
        return new ResourceStatus(Status.ERROR, msg);
    }

    public static ResourceStatus error(String msg, int errCode) {
        return new ResourceStatus(Status.ERROR, errCode, msg);
    }

    public static ResourceStatus finish() {
        return new ResourceStatus(Status.FINISH, null);
    }
}
