package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.common.R;
import com.example.jimi.mapper.RankListMapper;
import com.example.jimi.model.domain.RankList;
import com.example.jimi.model.request.RankListRequest;
import com.example.jimi.service.RankListService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RankListServiceImpl extends ServiceImpl<RankListMapper, RankList> implements RankListService {


    @Autowired
    private RankListMapper rankMapper;

    @Override
    public R addRank(RankListRequest rankListAddRequest) {
        try {
            RankList rankList = new RankList();
            BeanUtils.copyProperties(rankListAddRequest, rankList);

            QueryWrapper<RankList> wrapper = new QueryWrapper<>();
            wrapper.eq("consumer_id", rankList.getConsumerId())
                    .eq("song_list_id", rankList.getSongListId());

            // 尝试更新，若无记录则插入
            if (rankMapper.update(rankList, wrapper) == 0) {
                rankMapper.insert(rankList);
            }
            return R.success("评价成功");
        } catch (Exception e) {
            return R.error("评价失败: " + e.getMessage());
        }
    }


    @Override
    public R rankOfSongListId(Long songListId) {
        // 评分总人数如果为 0，则返回0；否则返回计算出的结果
        QueryWrapper<RankList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("song_list_id",songListId);
        Long rankNum = rankMapper.selectCount(queryWrapper);
        return R.success(null, (rankNum <= 0) ? 0 : rankMapper.selectScoreSum(songListId) / rankNum);
    }

    @Override
    public R getUserRank(Long consumerId, Long songListId) {
        Integer i = rankMapper.selectUserRank(consumerId, songListId);
        return R.success(null, i);
    }
}
