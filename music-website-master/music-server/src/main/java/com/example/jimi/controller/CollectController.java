package com.example.jimi.controller;

import com.example.jimi.common.R;
import com.example.jimi.model.request.CollectRequest;
import com.example.jimi.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CollectController {

    @Autowired
    private CollectService collectService;


    // 添加收藏的歌曲
    //前台界面逻辑
    @PostMapping("/collection/add")
    public R addCollection(@RequestBody CollectRequest addCollectRequest) {
        return collectService.addCollection(addCollectRequest);
    }

    //TODO  这些其实有点偏简单的逻辑  所以就一点 所以放在外面  拿到里面
    // 取消收藏的歌曲
    @DeleteMapping("/collection/delete")
    public R deleteCollection(@RequestParam Integer userId, @RequestParam Integer songId) {
        return collectService.deleteCollect(userId, songId);
    }

    // 是否收藏歌曲
    @PostMapping("/collection/status")
    public R isCollection(@RequestBody CollectRequest isCollectRequest) {
        return collectService.existSongId(isCollectRequest);

    }

    // 返回的指定用户 ID 收藏的列表
    @GetMapping("/collection/detail")
    public R collectionOfUser(@RequestParam Integer userId) {
        return collectService.collectionOfUser(userId);
    }
}
