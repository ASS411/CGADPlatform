package com.cgad.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装
 *
 * 【知识点】统一响应格式的重要性：
 *   - 前端可以根据 code 字段判断请求是否成功
 *   - message 字段提供错误描述
 *   - data 字段携带实际数据
 *   - 所有接口返回相同结构，前端可以统一处理
 *
 * 【知识点】泛型 <T>：
 *   - T 是类型参数，使用时指定具体类型
 *   - 例如 ApiResponse<DocumentAnalysisResult> 表示 data 字段是 DocumentAnalysisResult 类型
 *   - 好处：编译时类型检查，避免运行时类型错误
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** 状态码：200 成功，400 客户端错误，500 服务端错误 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据，类型由泛型 T 决定 */
    private T data;

    /** 快速创建成功响应 */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /** 快速创建错误响应 */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }
}
