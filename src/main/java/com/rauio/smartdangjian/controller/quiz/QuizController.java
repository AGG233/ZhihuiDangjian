package com.rauio.smartdangjian.controller.quiz;


import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.service.quiz.QuizOptionService;
import com.rauio.smartdangjian.service.quiz.QuizService;
import com.rauio.smartdangjian.utils.spec.UserType;
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
    public Result<Quiz> getQuiz(@PathVariable String id) {
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
        Boolean createdQuiz = quizService.create(quiz);
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
    public Result<List<QuizOption>> getQuizOption(@PathVariable String id) {
        List<QuizOption> quizOptionList = quizOptionService.getByQuizId(id);
        return Result.ok(quizOptionList);
    }

    @GetMapping("/{id}/option/{optionId}")
    public Result<QuizOption> getByOptionId(@PathVariable String optionId) {
        QuizOption quizOption = quizOptionService.get(optionId);
        return Result.ok(quizOption);
    }

    @PostMapping("/{id}/option")
    public Result<Boolean> createQuizOption(@RequestBody QuizOption quizOption) {
        Boolean createdQuizOption = quizOptionService.create(quizOption.getQuizId(), quizOption);
        return Result.ok(createdQuizOption);
    }

    @PutMapping("/{id}/option")
    public Result<Boolean> updateQuizOption(@RequestBody QuizOption quizOption) {
        Boolean updatedQuizOption = quizOptionService.update(quizOption.getId(), quizOption);
        return Result.ok(updatedQuizOption);
    }

    @DeleteMapping("/{id}/option/{optionId}")
    public Result<Boolean> deleteQuizOption(@PathVariable String optionId, @PathVariable String id) {
        Boolean deletedQuizOption = quizOptionService.delete(optionId);
        return Result.ok(deletedQuizOption);
    }
}
