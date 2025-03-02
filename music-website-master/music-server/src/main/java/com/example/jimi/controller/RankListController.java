package com.example.jimi.controller;

import com.example.jimi.common.R;
import com.example.jimi.model.request.RankListRequest;
import com.example.jimi.service.RankListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RankListController {

    @Autowired
    private RankListService rankListService;


    // 提交评分
    @PostMapping("/rankList/add")
    public R addRank(@RequestBody RankListRequest rankListAddRequest) {
        return rankListService.addRank(rankListAddRequest);
    }

    // 获取指定歌单的评分
    @GetMapping("/rankList")
    public R rankOfSongListId(@RequestParam Long songListId) {
        return rankListService.rankOfSongListId(songListId);
    }

    // 获取指定用户的歌单评分
    @GetMapping("/rankList/user")
    public R getUserRank(@RequestParam(required = false) Long consumerId, @RequestParam Long songListId) {
        R userRank = rankListService.getUserRank(consumerId, songListId);
        return R.success("成功",userRank);
    }


}
