package com.companyleveltraining.backend.ai;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** AI 助手会话与消息数据访问。 */
@Repository
public class AiRepository {

    private final JdbcTemplate jdbcTemplate;

    public AiRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createConversation(Long userId, String title) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                "INSERT INTO ai_conversation (user_id, title) VALUES (?, ?)", new String[] {"id"});
            ps.setObject(1, userId);
            ps.setString(2, title);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Optional<Long> findOwnedConversation(Long conversationId, Long userId) {
        List<Long> ids = jdbcTemplate.query(
            "SELECT id FROM ai_conversation WHERE id = ? AND user_id = ?",
            (rs, i) -> rs.getLong("id"), conversationId, userId);
        return ids.stream().findFirst();
    }

    public void addMessage(Long conversationId, String role, String content) {
        jdbcTemplate.update(
            "INSERT INTO ai_message (conversation_id, role, content) VALUES (?, ?, ?)",
            conversationId, role, content);
        jdbcTemplate.update(
            "UPDATE ai_conversation SET updated_at = CURRENT_TIMESTAMP(3) WHERE id = ?", conversationId);
    }

    public List<Map<String, Object>> listConversations(Long userId) {
        return jdbcTemplate.queryForList("""
            SELECT id, title,
                   DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS createTime,
                   DATE_FORMAT(updated_at, '%Y-%m-%d %H:%i:%s') AS updateTime
            FROM ai_conversation
            WHERE user_id = ?
            ORDER BY updated_at DESC
            """, userId);
    }

    public List<Map<String, Object>> listMessages(Long conversationId) {
        return jdbcTemplate.queryForList("""
            SELECT id, role, content,
                   DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS createTime
            FROM ai_message
            WHERE conversation_id = ?
            ORDER BY created_at ASC, id ASC
            """, conversationId);
    }
}
