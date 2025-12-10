package com.rauio.ZhihuiDangjian.controller;


import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.Quiz;
import com.rauio.ZhihuiDangjian.pojo.QuizOption;
import com.rauio.ZhihuiDangjian.pojo.response.Result;
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
@PermissionAccess(UserType.SCHOOL)
public class QuizController {

    private final QuizService quizService;
    private final QuizOptionService  quizOptionService;

    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    public Result<Quiz> getQuiz(@PathVariable Long id) {
        Quiz quiz = quizService.get(id);
        return Result.ok(quiz);
    }

    @GetMapping("/{chapterId}")
    @PermissionAccess(UserType.STUDENT)
    public Result<List<Quiz>> getQuizOfChapter(@PathVariable String chapterId) {
        List<Quiz> quizList = quizService.getByChapterId(chapterId);
        return Result.ok(quizList);
    }

    @PostMapping("/")
    public Result<Boolean> createQuiz(@RequestBody Quiz quiz) {
        Boolean createdQuiz = quizService.insert(quiz);
        return Result.ok(createdQuiz);
    }

    @PutMapping("/")
    public Result<Boolean> updateQuiz(@RequestBody Quiz quiz) {
        Boolean updatedQuiz = quizService.update(quiz);
        return Result.ok(updatedQuiz);
    }

    @DeleteMapping("/")
    public Result<Boolean> deleteQuiz(@RequestBody Quiz quiz) {
        Boolean deletedQuiz = quizService.delete(quiz.getId());
        return Result.ok(deletedQuiz);
    }

    /*
    * 问题选项接口
    * */

    @GetMapping("/{id}/option")
    @PermissionAccess(UserType.STUDENT)
    public Result<List<QuizOption>> getQuizOption(@PathVariable Long id) {
        List<QuizOption> quizOptionList = quizOptionService.getByQuizId(id);
        return Result.ok(quizOptionList);
    }

    @GetMapping("/{id}/option/{optionId}")
    public Result<QuizOption> getByOptionId(@PathVariable Long optionId) {
        QuizOption quizOption = quizOptionService.get(optionId);
        return Result.ok(quizOption);
    }

    @PostMapping("/{id}/option")
    public Result<Boolean> createQuizOption(@RequestBody QuizOption quizOption) {
        Boolean createdQuizOption = quizOptionService.insert(quizOption.getQuizId(), quizOption);
        return Result.ok(createdQuizOption);
    }

    @PutMapping("/{id}/option")
    public Result<Boolean> updateQuizOption(@RequestBody QuizOption quizOption) {
        Boolean updatedQuizOption = quizOptionService.update(quizOption.getQuizId(), quizOption);
        return Result.ok(updatedQuizOption);
    }

    @DeleteMapping("/{id}/option/{optionId}")
    public Result<Boolean> deleteQuizOption(@PathVariable Long optionId, @PathVariable String id) {
        Boolean deletedQuizOption = quizOptionService.delete(optionId);
        return Result.ok(deletedQuizOption);
    }
}
