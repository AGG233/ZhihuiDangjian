package com.rauio.smartdangjian.server.ai.controller.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiChatResponse;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.ai.service.SpeechService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Tag(name = "AI语音问答接口", description = "提供语音转写与语音问答功能")
@RestController
@RequestMapping("/api/ai/voice")
@RequiredArgsConstructor
@SaCheckRole("STUDENT")
public class VoiceChatController {

    private final SpeechService speechService;
    private final LLMService llmService;

    @Operation(
            summary = "语音问答接口",
            description = "上传音频文件（wav/mp3/pcm），FunASR 转写为文本后走统一 AI 对话，SSE 流式返回。sessionId 可选，省略时自动生成新会话。")
    @PostMapping(
            value = "/chat",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> chat(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "sessionId", required = false) String sessionId) {
        String text = speechService.transcribe(file);
        if (text == null || text.isBlank()) {
            throw new BusinessException(AiErrorConstants.VOICE_TRANSCRIBE_FAILED, "未识别到语音内容，请重新录制");
        }
        return llmService.chat(new AiChatRequest(sessionId, text));
    }
}
