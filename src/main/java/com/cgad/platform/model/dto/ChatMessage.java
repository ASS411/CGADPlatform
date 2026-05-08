package com.cgad.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息 DTO
 *
 * 聊天消息的角色分类：
 *   - user：用户发送的消息
 *   - assistant：AI 助手的回复
 *   - system：系统指令（设定 AI 的行为）
 *
 * 大模型对话是"消息列表"模式：
 *   - 每次请求发送完整对话历史，模型根据上下文生成回复
 *   - 这就是"无状态"API —— 服务器不保存对话状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /** 消息角色：user / assistant */
    private String role;

    /** 消息内容 */
    private String content;
}
