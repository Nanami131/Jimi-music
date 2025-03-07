package com.example.jimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jimi.model.domain.Comment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.metadata.IPage;
@Repository
public interface CommentMapper extends BaseMapper<Comment> {

    // 自定义分页查询，联表获取用户信息
    IPage<Comment> selectCommentsWithUser(Page<Comment> page, @Param("songListId") Integer songListId);

    IPage<Comment> selectCommentsWithUserBySongId(Page<Comment> page, @Param("songId") Integer songId);
}
