package com.gy11233.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gy11233.model.domain.Chat;
import com.gy11233.model.domain.User;
import com.gy11233.model.request.ChatRequest;
import com.gy11233.model.vo.ChatMessageVO;
import java.util.Date;
import java.util.List;


public interface ChatService extends IService<Chat> {
    /**
     * 获取私人聊天
     *
     * @param chatRequest 聊天请求
     * @param chatType    聊天类型
     * @param loginUser   登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser);

    /**
     * 获取缓存
     *
     * @param redisKey redis键
     * @param id       id
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getCache(String redisKey, String id);

    /**
     * 保存缓存
     *
     * @param redisKey       redis键
     * @param id             id
     * @param chatMessageVos 聊天消息vos
     */
    void saveCache(String redisKey, String id, List<ChatMessageVO> chatMessageVos);

    /**
     * 聊天结果
     *
     * @param userId     用户id
     * @param toId       到id
     * @param text       文本
     * @param chatType   聊天类型
     * @param createTime 创建时间
     * @return {@link ChatMessageVO}
     */
    ChatMessageVO chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime);

    /**
     * 删除密钥
     *
     * @param key 钥匙
     * @param id  id
     */
    void deleteKey(String key, String id);

    /**
     * 获取团队聊天
     *
     * @param chatRequest 聊天请求
     * @param teamChat    团队聊天
     * @param loginUser   登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getTeamChat(ChatRequest chatRequest, int teamChat, User loginUser);

    /**
     * 获得大厅聊天
     *
     * @param chatType  聊天类型
     * @param loginUser 登录用户
     * @return {@link List}<{@link ChatMessageVO}>
     */
    List<ChatMessageVO> getHallChat(int chatType, User loginUser);
}
