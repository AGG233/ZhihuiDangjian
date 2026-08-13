package com.rauio.smartdangjian.server.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.exception.ApiException;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpeechService 语音转写服务")
class SpeechServiceTest {

    @Mock
    private Recognition recognition;

    private SpeechService speechService;

    @BeforeEach
    void setUp() {
        speechService = new SpeechService("test-api-key", recognition);
    }

    @Test
    @DisplayName("transcribe 返回 FunASR 识别文本")
    void transcribeReturnsRecognizedText() {
        when(recognition.call(any(RecognitionParam.class), any(File.class))).thenReturn("党的纪律建设是党的生命线");

        MockMultipartFile audio = new MockMultipartFile("file", "voice.wav", "audio/wav", new byte[] {1, 2, 3});

        assertThat(speechService.transcribe(audio)).isEqualTo("党的纪律建设是党的生命线");
    }

    @Test
    @DisplayName("transcribe 按扩展名推断 format 并固定 paraformer-v2 / 16000 采样率")
    void transcribeInfersMp3FormatAndFixesModelAndSampleRate() {
        when(recognition.call(any(RecognitionParam.class), any(File.class))).thenReturn("转写结果");

        MockMultipartFile audio = new MockMultipartFile("file", "voice.mp3", "audio/mpeg", new byte[] {1, 2, 3});
        speechService.transcribe(audio);

        ArgumentCaptor<RecognitionParam> paramCaptor = ArgumentCaptor.forClass(RecognitionParam.class);
        verify(recognition).call(paramCaptor.capture(), any(File.class));
        assertThat(paramCaptor.getValue().getFormat()).isEqualTo("mp3");
        assertThat(paramCaptor.getValue().getModel()).isEqualTo("paraformer-v2");
        assertThat(paramCaptor.getValue().getSampleRate()).isEqualTo(16000);
    }

    @Test
    @DisplayName("transcribe 默认 wav 格式")
    void transcribeDefaultsToWav() {
        when(recognition.call(any(RecognitionParam.class), any(File.class))).thenReturn("转写结果");

        MockMultipartFile audio =
                new MockMultipartFile("file", "voice", "application/octet-stream", new byte[] {1, 2, 3});
        speechService.transcribe(audio);

        ArgumentCaptor<RecognitionParam> paramCaptor = ArgumentCaptor.forClass(RecognitionParam.class);
        verify(recognition).call(paramCaptor.capture(), any(File.class));
        assertThat(paramCaptor.getValue().getFormat()).isEqualTo("wav");
    }

    @Test
    @DisplayName("FunASR 调用抛异常时转为 BusinessException 8005")
    void transcribeThrowsBusinessExceptionOnFunAsrFailure() {
        when(recognition.call(any(RecognitionParam.class), any(File.class)))
                .thenThrow(new ApiException(new RuntimeException("ASR service down")));

        MockMultipartFile audio = new MockMultipartFile("file", "voice.wav", "audio/wav", new byte[] {1, 2, 3});

        assertThatThrownBy(() -> speechService.transcribe(audio))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(AiErrorConstants.VOICE_TRANSCRIBE_FAILED));
    }

    @Test
    @DisplayName("空音频文件抛业务错误")
    void transcribeRejectsEmptyAudio() {
        MockMultipartFile audio = new MockMultipartFile("file", "voice.wav", "audio/wav", new byte[0]);

        assertThatThrownBy(() -> speechService.transcribe(audio))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(AiErrorConstants.VOICE_TRANSCRIBE_FAILED));
    }

    @Test
    @DisplayName("null 音频抛业务错误")
    void transcribeRejectsNullAudio() {
        assertThatThrownBy(() -> speechService.transcribe(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(AiErrorConstants.VOICE_TRANSCRIBE_FAILED));
    }
}
