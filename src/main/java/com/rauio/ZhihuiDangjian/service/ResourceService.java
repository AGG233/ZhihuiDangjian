package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.dto.ContentBlockDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ResourceService {

    CompletableFuture<Map<String, String>> saveFile(MultipartFile file, User user) throws IOException, NoSuchAlgorithmException;
    Map<String, String> saveFileBatch(List<MultipartFile> files) throws IOException, NoSuchAlgorithmException;

    Boolean saveBlock(ContentBlockDto block);

    URL getByHash(String Hash);
    URL getById(String id);
    List<String> getBatchWithId(List<String> idList);
    List<String> getBatchWithHash(List<String> hashList);


    boolean delete(String hash);
    boolean delete(String[] hashList);
}