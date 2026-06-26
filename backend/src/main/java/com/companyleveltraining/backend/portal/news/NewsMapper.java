package com.companyleveltraining.backend.portal.news;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 校园资讯读查询 MyBatis Mapper。 */
@Mapper
public interface NewsMapper {

    String BASE_SELECT = """
        SELECT n.id, n.news_no AS newsNo, n.title, n.summary, n.content,
               n.category_id AS categoryId, n.category_code AS categoryCode,
               c.category_name AS categoryName, n.cover_image AS coverImage,
               n.author, n.status, n.is_top AS top, n.publisher_id AS publisherId,
               n.view_count AS viewCount,
               DATE_FORMAT(n.publish_time, '%Y-%m-%d %H:%i:%s') AS publishTime,
               DATE_FORMAT(n.created_at, '%Y-%m-%d %H:%i:%s') AS createTime
        FROM news n
        LEFT JOIN news_category c ON c.id = n.category_id
        """;

    @Select("""
        SELECT id, category_name AS categoryName, category_code AS categoryCode, sort_order AS sortOrder, status
        FROM news_category
        WHERE status = 1
        ORDER BY sort_order ASC, id ASC
        """)
    List<Map<String, Object>> categories();

    @Select("""
        <script>
        """ + BASE_SELECT + """
        WHERE n.status = 'published' AND n.deleted_at IS NULL
        <if test="categoryId != null">
          AND n.category_id = #{categoryId}
        </if>
        <if test="keyword != null">
          AND n.title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        ORDER BY n.is_top DESC, n.publish_time DESC, n.id DESC
        </script>
        """)
    List<Map<String, Object>> findPublished(@Param("categoryId") Long categoryId,
                                            @Param("keyword") String keyword);

    @Select("""
        """ + BASE_SELECT + """
        WHERE n.status = 'published' AND n.deleted_at IS NULL
        ORDER BY n.is_top DESC, n.publish_time DESC, n.id DESC
        LIMIT #{limit}
        """)
    List<Map<String, Object>> findLatest(@Param("limit") int limit);

    @Select("""
        """ + BASE_SELECT + """
        WHERE n.id = #{id} AND n.status = 'published' AND n.deleted_at IS NULL
        LIMIT 1
        """)
    Map<String, Object> findPublishedById(@Param("id") Long id);

    @Select("""
        <script>
        """ + BASE_SELECT + """
        WHERE n.deleted_at IS NULL
        <if test="status != null">
          AND n.status = #{status}
        </if>
        <if test="categoryId != null">
          AND n.category_id = #{categoryId}
        </if>
        <if test="keyword != null">
          AND n.title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        ORDER BY n.is_top DESC, n.created_at DESC
        </script>
        """)
    List<Map<String, Object>> findAllForAdmin(@Param("status") String status,
                                              @Param("categoryId") Long categoryId,
                                              @Param("keyword") String keyword);

    @Select("""
        """ + BASE_SELECT + """
        WHERE n.id = #{id} AND n.deleted_at IS NULL
        LIMIT 1
        """)
    Map<String, Object> findByIdForAdmin(@Param("id") Long id);

    @Select("SELECT category_code FROM news_category WHERE id = #{categoryId}")
    String categoryCodeOf(@Param("categoryId") Long categoryId);
}
