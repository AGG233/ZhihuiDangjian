package com.rauio.smartdangjian.server.ai.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * 语音转写服务：基于 DashScope FunASR（paraformer-v2）将音频文件非流式转写为文本。
 */
@Slf4j
@Service
public class SpeechService {

    /** FunASR 非流式语音识别模型 */
    private static final String MODEL_PARA_FORMER_V2 = "paraformer-v2";

    /** 采样率（Hz），paraformer-v2 固定 16k */
    private static final int SAMPLE_RATE = 16000;

    /** 转写超时时间（秒） */
    private static final long TIMEOUT_SECONDS = 60;

    private final String apiKey;
    private final Recognition recognition;
    private final ExecutorService executor;

    @Autowired
    public SpeechService(@Value("${spring.ai.dashscope.api-key}") String apiKey) {
        this(apiKey, new Recognition());
    }

    /**
     * 供测试注入 mock Recognition 使用。
     */
    SpeechService(String apiKey, Recognition recognition) {
        this.apiKey = apiKey;
        this.recognition = recognition;
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * 将音频文件转写为文本。支持 wav/mp3/pcm 格式，采样率固定 16000。
     *
     * @param audio 上传的音频文件
     * @return 识别出的文本
     */
    public String transcribe(MultipartFile audio) {
        if (audio == null) {
            throw new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "音频文件不能为空");
        }
        String format = resolveFormat(audio.getOriginalFilename());
        byte[] audioBytes = readBytes(audio);
        if (audioBytes == null || audioBytes.length == 0) {
            throw new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "音频文件内容为空");
        }

        RecognitionParam param = RecognitionParam.builder()
                .model(MODEL_PARA_FORMER_V2)
                .format(format)
                .sampleRate(SAMPLE_RATE)
                .apiKey(apiKey)
                .build();

        // FunASR 非流式 call(RecognitionParam, File) 仅接收文件，需将字节暂存到临时文件，调用完即删除
        File tempFile = null;
        try {
            tempFile = File.createTempFile("smartdangjian-asr-", "." + format);
            Files.write(tempFile.toPath(), audioBytes);
            File audioFile = tempFile;
            Future<String> future = executor.submit(() -> recognition.call(param, audioFile));
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("语音转写超时，超过 {} 秒", TIMEOUT_SECONDS, e);
            throw new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "语音转写超时，请稍后重试");
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("语音转写失败", e);
            throw new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "语音转写失败，请稍后重试");
        } catch (IOException e) {
            log.error("语音转写临时文件读写失败", e);
            throw new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "语音转写失败，请稍后重试");
        } finally {
            if (tempFile != null && !tempFile.delete()) {
                tempFile.deleteOnExit();
            }
        }
    }

    private static String resolveFormat(String originalFilename) {
        if (originalFilename == null) {
            return "wav";
        }
        String name = originalFilename.toLowerCase(Locale.ROOT);
        if (name.endsWith(".mp3")) {
            return "mp3";
        }
        if (name.endsWith(".pcm")) {
            return "pcm";
        }
        return "wav";
    }

    private static byte[] readBytes(MultipartFile audio) {
        try {
            return audio.getBytes();
        } catch (IOException e) {
            log.error("读取音频文件失败", e);
            throw new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "读取音频文件失败");
        }
    }
}
