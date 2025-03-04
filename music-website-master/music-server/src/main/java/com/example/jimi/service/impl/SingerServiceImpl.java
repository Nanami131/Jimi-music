package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.common.R;
import com.example.jimi.controller.MinioUploadController;
import com.example.jimi.mapper.SingerMapper;
import com.example.jimi.mapper.SongMapper;
import com.example.jimi.model.domain.Singer;
import com.example.jimi.model.domain.Song;
import com.example.jimi.model.request.SingerRequest;
import com.example.jimi.service.SingerService;
import com.example.jimi.service.SongService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class SingerServiceImpl extends ServiceImpl<SingerMapper, Singer> implements SingerService {

    @Autowired
    private SingerMapper singerMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private SongService songService;
    @Override
    public R updateSingerMsg(SingerRequest updateSingerRequest) {
        String introduction = updateSingerRequest.getIntroduction();
        if(introduction!=null&&introduction.length()>250){
            updateSingerRequest.setIntroduction(introduction.substring(0,250));
        }
        Singer singer = new Singer();
        BeanUtils.copyProperties(updateSingerRequest, singer);
        if (singerMapper.updateById(singer) > 0) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    @Override
    public R updateSingerPic(MultipartFile avatorFile, int id) {
        String fileName =  avatorFile.getOriginalFilename();
        MinioUploadController.uploadImgFile(avatorFile);
        String imgPath = "/user01/singer/img/" + fileName;
        Singer singer = new Singer();
        singer.setId(id);
        singer.setPic(imgPath);
        if (singerMapper.updateById(singer) > 0) {
            return R.success("上传成功", imgPath);
        } else {
            return R.error("上传失败");
        }
    }


    @Transactional
    @Override
    public R deleteSinger(Integer id) {
        QueryWrapper<Song> relatedsongs = new QueryWrapper<>();
        relatedsongs.eq("singer_id",id);
        List<Song> songs = songMapper.selectList(relatedsongs);
        for (Song song:songs) {
            songService.deleteSong(song.getId());
        }
        songMapper.delete(relatedsongs);
        if (singerMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R allSinger() {
        return R.success(null, singerMapper.selectList(null));
    }

    @Override
    public R addSinger(SingerRequest addSingerRequest) {
        String introduction = addSingerRequest.getIntroduction();
        if(introduction!=null&&introduction.length()>250){
            addSingerRequest.setIntroduction(introduction.substring(0,250));
        }
        Singer singer = new Singer();
        BeanUtils.copyProperties(addSingerRequest, singer);
        String pic = "/img/avatorImages/user.jpg";
        singer.setPic(pic);
        if (singerMapper.insert(singer) > 0) {
            return R.success("添加成功");
        } else {
            return R.error("添加失败");
        }
    }

    @Override
    public R singerOfName(String name) {
        QueryWrapper<Singer> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        return R.success(null, singerMapper.selectList(queryWrapper));
    }

    @Override
    public R singerOfSex(Integer sex) {
        QueryWrapper<Singer> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("sex", sex);
        return R.success(null, singerMapper.selectList(queryWrapper));
    }
}
