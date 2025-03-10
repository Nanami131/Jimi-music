package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.common.R;
import com.example.jimi.controller.MinioUploadController;
import com.example.jimi.mapper.SongMapper;
import com.example.jimi.model.domain.Singer;
import com.example.jimi.model.domain.Song;
import com.example.jimi.model.request.SongRequest;
import com.example.jimi.service.SongService;
import com.example.jimi.utils.FileNameUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    @Autowired
    private SongMapper songMapper;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Autowired
    MinioClient minioClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public R allSong() {
        return R.success(null, songMapper.selectList(null));
    }

    @Override
    public R addSong(SongRequest addSongRequest, MultipartFile lrcfile, MultipartFile mpfile) {

        if (addSongRequest.getName() == null || "".equals(addSongRequest.getName())) {
            return R.error("必须指定歌曲名");
        }
        String introduction = addSongRequest.getIntroduction();
        if (introduction != null && introduction.length() > 250) {
            addSongRequest.setIntroduction(introduction.substring(0, 250));
        }
        Song song = new Song();
        BeanUtils.copyProperties(addSongRequest, song);
        String pic = "/img/songPic/tubiao.jpg";
        String fileName = mpfile.getOriginalFilename();
        String s = MinioUploadController.uploadFile(mpfile);
        String storeUrlPath = "/" + bucketName + "/" + fileName;
        song.setCreateTime(new Date());
        song.setUpdateTime(new Date());
        song.setPic(pic);
        song.setUrl(storeUrlPath);

        if (lrcfile != null && (song.getLyric().equals("[00:00:00]暂无歌词"))) {
            byte[] fileContent = new byte[0];
            try {
                fileContent = lrcfile.getBytes();
                String content = new String(fileContent, "GB2312");
                song.setLyric(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (s.equals("File uploaded successfully!") && songMapper.insert(song) > 0) {
            return R.success("上传成功", storeUrlPath);
        } else {
            return R.error("上传失败");
        }
    }

    @Override
    public R updateSongMsg(SongRequest updateSongRequest) {
        String introduction = updateSongRequest.getIntroduction();
        if (introduction != null && introduction.length() > 250) {
            updateSongRequest.setIntroduction(introduction.substring(0, 250));
        }
        Song song = new Song();
        BeanUtils.copyProperties(updateSongRequest, song);
        if (songMapper.updateById(song) > 0) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    @Override
    public R updateSongUrl(MultipartFile urlFile, int id) {
        Song song = songMapper.selectById(id);
        String path = song.getUrl();
        String[] parts = path.split("/");
        String fileName = parts[parts.length - 1];

        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build();
        fileName = urlFile.getOriginalFilename();
        String s = MinioUploadController.uploadFile(urlFile);
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (InternalException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (ServerException e) {
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            throw new RuntimeException(e);
        }
        String storeUrlPath = "/" + bucketName + "/" + fileName;
        song.setId(id);
        song.setUrl(storeUrlPath);
        song.setName(fileName);
        if (s.equals("File uploaded successfully!") && songMapper.updateById(song) > 0) {
            return R.success("更新成功", storeUrlPath);
        } else {
            return R.error("更新失败");
        }
    }

    @Override
    public R updateSongPic(MultipartFile urlFile, int id) {
        String originalFilename = urlFile.getOriginalFilename();
        String fileName = FileNameUtils.defineNamePath(originalFilename, "/singer/song/", id);
        Song song = new Song();
        song.setId(id);
        song.setPic("/user01" + fileName);
        String s = MinioUploadController.uploadImgFile(urlFile, fileName);
        if (s.equals("File uploaded successfully!") && songMapper.updateById(song) > 0) {
            QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
            queryWrapper
                    .select("singer_id") // 只查询 singer_id 字段
                    .eq("id", id);       // 条件：id 等于传入的 id
            //还有歌手部分歌曲的图片缓存也要删除
            stringRedisTemplate.delete("song:singerId:" + songMapper.selectOne(queryWrapper).getSingerId());
            stringRedisTemplate.delete("song:id:" + id);
            return R.success("上传成功", fileName);
        } else {
            return R.error("上传失败");
        }
    }

    @Override
    public R deleteSong(Integer id) {
        Song song = songMapper.selectById(id);
        String path = song.getUrl();
        String[] parts = path.split("/");
        String fileName = parts[parts.length - 1];
        System.out.println(fileName);
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build();
        if (songMapper.deleteById(id) > 0) {
            try {
                minioClient.removeObject(removeObjectArgs);
            } catch (ErrorResponseException e) {
                throw new RuntimeException(e);
            } catch (InsufficientDataException e) {
                throw new RuntimeException(e);
            } catch (InternalException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (InvalidResponseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (ServerException e) {
                throw new RuntimeException(e);
            } catch (XmlParserException e) {
                throw new RuntimeException(e);
            }
            stringRedisTemplate.delete("song:singerId:" + id);
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R songOfSingerId(Integer singerId) {
        String key = stringRedisTemplate.opsForValue().get("song:singerId:" + singerId);
        if (key != null) {
            try {
                List<Song> songs = objectMapper.readValue(key, new TypeReference<List<Song>>() {
                });
                return R.success("走了缓存", songs);
            } catch (JsonProcessingException e) {
                return R.error(e.getMessage());
            }
        }
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("singer_id", singerId);
        List<Song> songs = songMapper.selectList(queryWrapper);
        try {
            stringRedisTemplate.opsForValue().set("song:singerId:" + singerId,
                    objectMapper.writeValueAsString(songs), 5, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存建立失败: {}", e);
        }
        return R.success("没有走缓存", songs);
    }

    @Override
    public R songOfId(Integer id) {
        String key = stringRedisTemplate.opsForValue().get("song:id:" + id);
        if (key != null) {
            try {
                List<Song> songs = objectMapper.readValue(key, new TypeReference<List<Song>>() {
                });
                return R.success("走了缓存", songs);
            } catch (JsonProcessingException e) {
                return R.error(e.getMessage());
            }
        }
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        List<Song> songs = songMapper.selectList(queryWrapper);
        try {
            stringRedisTemplate.opsForValue().set("song:id:" + id,
                    objectMapper.writeValueAsString(songs), 5, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存建立失败: {}", e);
        }
        return R.success("没有走缓存", songs);
    }

    @Override
    public R songOfSingerName(String name) {
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        List<Song> songs = songMapper.selectList(queryWrapper);
        if (songs.isEmpty()) {
            return R.error("添加失败，没有找到该歌,无法加入该歌单");
        }

        return R.success(null, songMapper.selectList(queryWrapper));
    }

    @Override
    public R updateSongLrc(MultipartFile lrcFile, int id) {
        Song song = songMapper.selectById(id);
        byte[] fileContent = new byte[0];
        try {
            fileContent = lrcFile.getBytes();
            String content = new String(fileContent, StandardCharsets.UTF_8);
            song.setLyric(content);
            System.out.println("1234"+content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (songMapper.updateById(song) > 0) {
            QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
            queryWrapper
                    .select("singer_id") // 只查询 singer_id 字段
                    .eq("id", id);       // 条件：id 等于传入的 id
            //还有歌手部分歌曲的图片缓存也要删除
            stringRedisTemplate.delete("song:singerId:" + songMapper.selectOne(queryWrapper).getSingerId());
            stringRedisTemplate.delete("song:id:" + id);
            return R.success("更新成功");
        } else {
            return R.error("更新失败");
        }
    }
}
