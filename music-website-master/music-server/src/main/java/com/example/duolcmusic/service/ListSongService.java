package com.example.duolcmusic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.duolcmusic.common.R;
import com.example.duolcmusic.model.domain.ListSong;
import com.example.duolcmusic.model.request.ListSongRequest;

import java.util.List;

public interface ListSongService extends IService<ListSong> {

    R addListSong(ListSongRequest addListSongRequest);

    R updateListSongMsg(ListSongRequest updateListSongRequest);

    R deleteListSong(Integer songId);

    //看看这啥
    List<ListSong> allListSong();

    R listSongOfSongId(Integer songListId);
}
