package com.companyleveltraining.backend.portal.notice;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 通知公告读查询 MyBatis Mapper。 */
@Mapper
public interface NoticeMapper {

    String PORTAL_SELECT = """
        SELECT n.id, n.notice_no AS noticeNo, n.title, n.content, n.notice_type AS noticeType,
               n.publish_scope AS publishScope, n.target_roles AS targetRoles, n.is_top AS top,
               n.status, n.publisher_id AS publisherId, n.publisher_name AS publisherName,
               n.view_count AS viewCount,
               DATE_FORMAT(n.publish_time, '%Y-%m-%d %H:%i:%s') AS publishTime,
               DATE_FORMAT(n.created_at, '%Y-%m-%d %H:%i:%s') AS createTime,
               (r.id IS NOT NULL) AS readFlag
        FROM notice n
        LEFT JOIN notice_read r ON r.notice_id = n.id AND r.user_id = #{userId}
        """;

    String ADMIN_SELECT = """
        SELECT n.id, n.notice_no AS noticeNo, n.title, n.content, n.notice_type AS noticeType,
               n.publish_scope AS publishScope, n.target_roles AS targetRoles, n.is_top AS top,
               n.status, n.publisher_id AS publisherId, n.publisher_name AS publisherName,
               n.view_count AS viewCount,
               DATE_FORMAT(n.publish_time, '%Y-%m-%d %H:%i:%s') AS publishTime,
               DATE_FORMAT(n.created_at, '%Y-%m-%d %H:%i:%s') AS createTime,
               0 AS readFlag
        FROM notice n
        """;

    @Select("""
        <script>
        """ + PORTAL_SELECT + """
        WHERE n.status = 'published' AND n.deleted_at IS NULL
          AND (n.publish_scope = 'all' OR FIND_IN_SET(#{role}, n.target_roles) > 0)
        <if test="keyword != null">
          AND n.title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        ORDER BY n.is_top DESC, n.publish_time DESC, n.id DESC
        </script>
        """)
    List<Map<String, Object>> findPublishedForRole(@Param("role") String role,
                                                   @Param("userId") Long userId,
                                                   @Param("keyword") String keyword);

    @Select("""
        """ + PORTAL_SELECT + """
        WHERE n.status = 'published' AND n.deleted_at IS NULL
          AND (n.publish_scope = 'all' OR FIND_IN_SET(#{role}, n.target_roles) > 0)
        ORDER BY n.is_top DESC, n.publish_time DESC, n.id DESC
        LIMIT #{limit}
        """)
    List<Map<String, Object>> findLatestForRole(@Param("role") String role,
                                                @Param("userId") Long userId,
                                                @Param("limit") int limit);

    @Select("""
        """ + PORTAL_SELECT + """
        WHERE n.id = #{id} AND n.status = 'published' AND n.deleted_at IS NULL
          AND (n.publish_scope = 'all' OR FIND_IN_SET(#{role}, n.target_roles) > 0)
        LIMIT 1
        """)
    Map<String, Object> findPublishedById(@Param("role") String role,
                                          @Param("userId") Long userId,
                                          @Param("id") Long id);

    @Select("""
        <script>
        """ + ADMIN_SELECT + """
        WHERE n.deleted_at IS NULL
        <if test="status != null">
          AND n.status = #{status}
        </if>
        <if test="keyword != null">
          AND n.title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        ORDER BY n.is_top DESC, n.created_at DESC
        </script>
        """)
    List<Map<String, Object>> findAllForAdmin(@Param("status") String status,
                                              @Param("keyword") String keyword);

    @Select("""
        """ + ADMIN_SELECT + """
        WHERE n.id = #{id} AND n.deleted_at IS NULL
        LIMIT 1
        """)
    Map<String, Object> findByIdForAdmin(@Param("id") Long id);
}
