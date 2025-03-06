package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.common.R;
import com.example.jimi.controller.MinioUploadController;
import com.example.jimi.mapper.SingerMapper;
import com.example.jimi.mapper.SongMapper;
import com.example.jimi.model.domain.Singer;
import com.example.jimi.model.domain.Song;
import com.example.jimi.model.domain.SongList;
import com.example.jimi.model.request.SingerRequest;
import com.example.jimi.service.SingerService;
import com.example.jimi.service.SongService;
import com.example.jimi.utils.FileNameUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SingerServiceImpl extends ServiceImpl<SingerMapper, Singer> implements SingerService {

    @Autowired
    private SingerMapper singerMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private SongService songService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
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
        String originalFilename = avatorFile.getOriginalFilename();
        String fileName= FileNameUtils.defineNamePath(originalFilename,"/singer/img/",id);

        Singer singer = new Singer();
        singer.setId(id);
        singer.setPic("/user01"+fileName);
        String s= MinioUploadController.uploadImgFile(avatorFile,fileName);
        if (s.equals("File uploaded successfully!") && singerMapper.updateById(singer) > 0) {
            return R.success("上传成功", fileName);
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
        String key =stringRedisTemplate.opsForValue().get("singer:name:"+name);
        if(key!=null){
            try {
                List<Singer> singers=objectMapper.readValue(key, new TypeReference<List<Singer>>(){});
                return R.success("走了缓存",singers);
            } catch (JsonProcessingException e) {
                return R.error(e.getMessage());
            }
        }
        QueryWrapper<Singer> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        List<Singer> singers = singerMapper.selectList(queryWrapper);
        try {
            stringRedisTemplate.opsForValue().set("singer:name:"+name,
                    objectMapper.writeValueAsString(singers),5, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存建立失败: {}",  e);
        }
        return R.success(null,singers);
    }

    @Override
    public R singerOfSex(Integer sex) {
        String key =stringRedisTemplate.opsForValue().get("singer:sex:"+sex);
        if(key!=null){
            try {
                List<Singer> singers=objectMapper.readValue(key, new TypeReference<List<Singer>>(){});
                return R.success("走了缓存",singers);
            } catch (JsonProcessingException e) {
                return R.error(e.getMessage());
            }
        }
        QueryWrapper<Singer> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("sex", sex);
        List<Singer> singers = singerMapper.selectList(queryWrapper);
        try {
            stringRedisTemplate.opsForValue().set("singer:sex:"+sex,
                    objectMapper.writeValueAsString(singers),5, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存建立失败: {}",  e);
        }
        return R.success(null, singers);
    }
}
