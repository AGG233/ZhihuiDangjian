package com.rauio.ZhihuiDangjian.controller;


import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.Quiz;
import com.rauio.ZhihuiDangjian.pojo.QuizOption;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.service.QuizOptionService;
import com.rauio.ZhihuiDangjian.service.QuizService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
@Tag(name = "试题接口")
@PermissionAccess(UserType.TEACHER)
public class QuizController {

    private final QuizService quizService;
    private final QuizOptionService  quizOptionService;

    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<Quiz> getQuiz(@PathVariable Long id) {
        Quiz quiz = quizService.get(id);
        return ApiResponse.ok(quiz);
    }

    @GetMapping("/{chapterId}")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<List<Quiz>> getQuizOfChapter(@PathVariable String chapterId) {
        List<Quiz> quizList = quizService.getByChapterId(chapterId);
        return ApiResponse.ok(quizList);
    }

    @PostMapping("/")
    public ApiResponse<Boolean> createQuiz(@RequestBody Quiz quiz) {
        Boolean createdQuiz = quizService.insert(quiz);
        return ApiResponse.ok(createdQuiz);
    }

    @PutMapping("/")
    public ApiResponse<Boolean> updateQuiz(@RequestBody Quiz quiz) {
        Boolean updatedQuiz = quizService.update(quiz);
        return ApiResponse.ok(updatedQuiz);
    }

    @DeleteMapping("/")
    public ApiResponse<Boolean> deleteQuiz(@RequestBody Quiz quiz) {
        Boolean deletedQuiz = quizService.delete(quiz.getId());
        return ApiResponse.ok(deletedQuiz);
    }

    /*
    * 问题选项接口
    * */

    @GetMapping("/{id}/option")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<List<QuizOption>> getQuizOption(@PathVariable String id) {
        List<QuizOption> quizOptionList = quizOptionService.getByQuizId(id);
        return ApiResponse.ok(quizOptionList);
    }

    @GetMapping("/{id}/option/{optionId}")
    public ApiResponse<QuizOption> getByOptionId(@PathVariable Long optionId) {
        QuizOption quizOption = quizOptionService.get(optionId);
        return ApiResponse.ok(quizOption);
    }

    @PostMapping("/{id}/option")
    public ApiResponse<Boolean> createQuizOption(@RequestBody QuizOption quizOption) {
        Boolean createdQuizOption = quizOptionService.insert(quizOption.getQuizId(), quizOption);
        return ApiResponse.ok(createdQuizOption);
    }

    @PutMapping("/{id}/option")
    public ApiResponse<Boolean> updateQuizOption(@RequestBody QuizOption quizOption) {
        Boolean updatedQuizOption = quizOptionService.update(quizOption.getQuizId(), quizOption);
        return ApiResponse.ok(updatedQuizOption);
    }

    @DeleteMapping("/{id}/option/{optionId}")
    public ApiResponse<Boolean> deleteQuizOption(@PathVariable Long optionId, @PathVariable String id) {
        Boolean deletedQuizOption = quizOptionService.delete(optionId);
        return ApiResponse.ok(deletedQuizOption);
    }
}
