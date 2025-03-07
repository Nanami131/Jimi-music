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
import com.example.jimi.model.request.CommentRequest;
import com.example.jimi.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

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
    public R commentOfSongId(Integer songId, Integer pageNum, Integer pageSize) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("song_id",songId);
        Page<Comment> page =new Page<>(pageNum,pageSize);
        IPage<Comment> commentIPage= commentMapper.selectPage(page,queryWrapper);
        return R.success("分页查询",commentIPage);
    }

    @Override
    public R commentOfSongListId(Integer songListId, Integer pageNum, Integer pageSize) {

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("song_list_id",songListId);
        Page<Comment> page =new Page<>(pageNum,pageSize);

        IPage<Comment> commentIPage= commentMapper.selectPage(page,queryWrapper);
        return R.success("分页查询",commentIPage);
    }
}
