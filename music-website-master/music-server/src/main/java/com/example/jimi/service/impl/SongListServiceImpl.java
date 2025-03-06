package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.annotation.AutoFill;
import com.example.jimi.common.R;
import com.example.jimi.controller.MinioUploadController;
import com.example.jimi.enumeration.OperationType;
import com.example.jimi.mapper.BannerMapper;
import com.example.jimi.mapper.SongListMapper;
import com.example.jimi.model.domain.Banner;
import com.example.jimi.model.domain.SongList;
import com.example.jimi.model.request.SongListRequest;
import com.example.jimi.service.SongListService;
import com.example.jimi.utils.FileNameUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class SongListServiceImpl extends ServiceImpl<SongListMapper, SongList> implements SongListService {

    @Autowired
    private SongListMapper songListMapper;

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @AutoFill(OperationType.UPDATE)
    @Override
    public R updateSongListMsg(SongListRequest updateSongListRequest) {
        String introduction = updateSongListRequest.getIntroduction();
        if(introduction!=null&&introduction.length()>250){
            updateSongListRequest.setIntroduction(introduction.substring(0,250));
        }
        SongList songList = new SongList();
        BeanUtils.copyProperties(updateSongListRequest, songList);
        if (songListMapper.updateById(songList) > 0) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    @Override
    public R deleteSongList(Integer id) {
        if (songListMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R allSongList() {
        return R.success(null, songListMapper.selectList(null));
    }

    @Override
    public List<SongList> findAllSong() {
        List<SongList> songLists = songListMapper.selectList(null);
        return songLists;
    }


    @Override
    public R likeTitle(String title) {
        QueryWrapper<SongList> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title",title);
        return R.success(null, songListMapper.selectList(queryWrapper));
    }

    @Override
    public R likeStyle(String style) {
        QueryWrapper<SongList> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("style",style);
        return R.success(null, songListMapper.selectList(queryWrapper));
    }

    @Override
    public R getSongListById(int id) {
        QueryWrapper<SongList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        return R.success(null, songListMapper.selectList(queryWrapper));
    }

    @Override
    public R addSongList(SongListRequest addSongListRequest) {
        SongList songList = new SongList();
        BeanUtils.copyProperties(addSongListRequest, songList);
        String pic = "/img/songListPic/123.jpg";
        songList.setPic(pic);
        if (songListMapper.insert(songList) > 0) {
            return R.success("添加成功");
        } else {
            return R.error("添加失败");
        }
    }

    @Override
    public R updateSongListImg(MultipartFile avatorFile, @RequestParam("id") int id) {
        String originalFilename =avatorFile.getOriginalFilename();
        String fileName = FileNameUtils.defineNamePath(originalFilename,"/songlist/",id);

        SongList songList = new SongList();
        songList.setId(id);
        songList.setPic("/user01"+fileName);

        String s = MinioUploadController.uploadImgFile(avatorFile,fileName);
        if (s.equals("File uploaded successfully!") && songListMapper.updateById(songList) > 0) {
            //由于歌单上传功能和图片上传是分离的。而一般来说旧歌单不会更新图片。所以将歌单图片更新视为上传了新歌单
            //据此把新歌单作为Banner更新
            QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pic","/user01"+fileName).or().eq("url","/song-sheet-detail/"+id);
            if (bannerMapper.selectList(queryWrapper).isEmpty()){
                int idx=(int) (Math.random() * 8) + 1;
                Banner banner=new Banner().setPic("/user01"+fileName).setUrl("/song-sheet-detail/"+id).setId(idx);
                bannerMapper.updateById(banner);
                //清理缓存
                stringRedisTemplate.delete("banner:all");
            }

            return R.success("上传成功", fileName);
        } else {
            return R.error("上传失败");
        }
    }
}
