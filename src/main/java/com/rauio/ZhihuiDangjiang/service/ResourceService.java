package com.rauio.ZhihuiDangjiang.service;

import com.rauio.ZhihuiDangjiang.pojo.User;
import com.rauio.ZhihuiDangjiang.pojo.dto.ContentBlockDto;
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

    URL get(String key);
    List<String> getBatch(List<String> objectKeys);
    boolean delete(String key);
    boolean delete(String[] keys);
}