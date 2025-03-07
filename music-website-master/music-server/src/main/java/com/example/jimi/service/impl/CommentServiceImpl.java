package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.annotation.AutoFill;
import com.example.jimi.annotation.UserPermissionCheck;
import com.example.jimi.common.R;
import com.example.jimi.enumeration.OperationType;
import com.example.jimi.mapper.CommentMapper;
import com.example.jimi.model.domain.Comment;
import com.example.jimi.model.domain.CommentSupport;
import com.example.jimi.model.domain.Singer;
import com.example.jimi.model.request.CommentRequest;
import com.example.jimi.service.CommentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @AutoFill(OperationType.INSERT)
    @UserPermissionCheck(fieldName = "userId")
    @Override
    public R addComment(CommentRequest addCommentRequest) {
        String content = addCommentRequest.getContent();
        if(content!=null&&content.length()>250){
            addCommentRequest.setContent(content.substring(0,250));
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(addCommentRequest, comment);
        comment.setType(addCommentRequest.getNowType());
        if (commentMapper.insert(comment) > 0) {
            return R.success("评论成功");
        } else {
            return R.error("评论失败");
        }
    }

    @Override
    public R updateCommentMsg(CommentRequest addCommentRequest) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(addCommentRequest, comment);
        if (commentMapper.updateById(comment) > 0) {
            return R.success("点赞成功");
        } else {
            return R.error("点赞失败");
        }
    }

    //    删除评论
    @Override
    @UserPermissionCheck(fieldName = "userId")
    public R deleteComment(Integer id) {
        if (commentMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R commentOfSongListId(Integer songListId, Integer pageNum, Integer pageSize) {
        String key =stringRedisTemplate.opsForValue().get("comment:songlist:"+songListId+":"+pageNum);
        if(key!=null){
            try {
                IPage<Comment> commentIPage=objectMapper.readValue(key, new TypeReference<Page<Comment>>(){});
                return R.success("走了缓存",commentIPage);
            } catch (JsonProcessingException e) {
                return R.error(e.getMessage());
            }
        }
        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> commentIPage = (Page<Comment>) commentMapper.selectCommentsWithUser(page, songListId);
        try {
            stringRedisTemplate.opsForValue().set("comment:songlist:"+songListId+":"+pageNum,
                    objectMapper.writeValueAsString(commentIPage),20, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存建立失败: {}",  e);
        }
        return R.success("没有走缓存",commentIPage);
    }

    @Override
    public R commentOfSongId(Integer songId, Integer pageNum, Integer pageSize) {
        String key =stringRedisTemplate.opsForValue().get("comment:song:"+songId+":"+pageNum);
        if(key!=null){
            try {
                IPage<Comment> commentIPage=objectMapper.readValue(key, new TypeReference<Page<Comment>>(){});
                return R.success("走了缓存",commentIPage);
            } catch (JsonProcessingException e) {
                return R.error(e.getMessage());
            }
        }
        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> commentIPage = (Page<Comment>) commentMapper.selectCommentsWithUserBySongId(page, songId);
        try {
            stringRedisTemplate.opsForValue().set("comment:song:"+songId+":"+pageNum,
                    objectMapper.writeValueAsString(commentIPage),20, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存建立失败: {}",  e);
        }
        return R.success("没有走缓存", commentIPage);
    }

    @Override
    public R getCommentSupports(List<Integer> commentIds) {
        List<CommentSupport> supports = commentMapper.selectSupportsByIds(commentIds);
        return R.success("查询点赞信息成功", supports);
    }
}
