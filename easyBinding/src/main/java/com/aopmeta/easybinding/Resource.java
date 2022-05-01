package com.aopmeta.easybinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 包含状态的数据
 */
public class Resource<T> extends ResourceStatus {
    @Nullable
    private T data;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this(status, data, 0, message);
    }

    private Resource(@NonNull Status status, @Nullable T data, int errCode, @Nullable String message) {
        super(status, errCode, message);
        this.data = data;
    }

    @Nullable
    public T getData() {
        return data;
    }

    public void setData(@Nullable T data) {
        this.data = data;
    }

    public static <T> Resource<T> parse(Resource<?> resource, T data) {
        return new Resource<>(resource.status, data, resource.errCode, resource.message);
    }

    public static <T> Resource<T> prepare(T data) {
        return new Resource<>(Status.PREPARE, data, null);
    }

    public static <T> Resource<T> cache(T data) {
        return new Resource<>(Status.CACHE, data, null);
    }

    public static <T> Resource<T> loading(T t) {
        return new Resource<>(Status.LOADING, t, null);
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> cancel(T data) {
        return new Resource<>(Status.CANCEL, data, null);
    }

    public static <T> Resource<T> error(String msg, T t) {
        return new Resource<>(Status.ERROR, t, msg);
    }

    public static <T> Resource<T> error(String msg, int errCode, T t) {
        return new Resource<>(Status.ERROR, t, errCode, msg);
    }

    public static <T> Resource<T> finish(T data) {
        return new Resource<>(Status.FINISH, data, null);
    }
}
