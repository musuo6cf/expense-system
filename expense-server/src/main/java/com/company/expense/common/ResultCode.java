package com.company.expense.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请重新登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "请求资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务异常
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USERNAME_EXISTS(1004, "用户名已存在"),
    OLD_PASSWORD_ERROR(1005, "原密码错误"),
    TOKEN_EXPIRED(1006, "Token已过期"),
    TOKEN_INVALID(1007, "Token无效"),
    EXPENSE_NOT_FOUND(2001, "报销单不存在"),
    EXPENSE_STATUS_ERROR(2002, "报销单状态异常"),
    APPROVAL_NOT_FOUND(3001, "审批记录不存在"),
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_NOT_FOUND(4002, "文件不存在");

    private final int code;
    private final String message;
}
