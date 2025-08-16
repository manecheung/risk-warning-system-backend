package org.example.riskwarningsystembackend.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一结果码枚举类
 * <p>
 * 定义系统中常用的HTTP状态码和对应的描述信息，用于统一响应格式中的状态码表示
 * </p>
 */
@Getter
public enum ResultCode {
    /**
     * 操作成功状态码
     */
    SUCCESS(200, "操作成功"),

    /**
     * 创建成功状态码
     */
    CREATED(201, "创建成功"),

    /**
     * 操作失败状态码
     */
    FAILURE(500, "操作失败"),

    /**
     * 未授权状态码
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 资源未找到状态码
     */
    NOT_FOUND(404, "资源未找到"),

    /**
     * 错误的请求状态码
     */
    BAD_REQUEST(400, "错误的请求");

    /**
     * 状态码值
     */
    private final int code;

    /**
     * 状态码描述信息
     */
    private final String message;

    // 静态映射表，提高查找效率
    private static final Map<Integer, ResultCode> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(ResultCode::getCode, e -> e));

    static {
        // 校验 code 是否唯一
        if (CODE_MAP.size() != values().length) {
            throw new IllegalStateException("ResultCode 中存在重复的 code 值");
        }
    }

    /**
     * 构造函数，初始化状态码和描述信息
     *
     * @param code    HTTP状态码
     * @param message 状态码对应的描述信息
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据 code 查找对应的 ResultCode 枚举项
     *
     * @param code 状态码
     * @return 对应的 ResultCode 枚举项，若未找到则返回 null
     */
    public static ResultCode fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
