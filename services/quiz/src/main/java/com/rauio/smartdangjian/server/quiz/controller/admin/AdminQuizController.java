package com.rauio.smartdangjian.server.quiz.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/quiz/quizzes")
@RequiredArgsConstructor
@Tag(name = "管理员试题接口")
@PermissionAccess(UserType.SCHOOL)
public class AdminQuizController {

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @PostMapping
    public Result<Boolean> createQuiz(@RequestBody Quiz quiz) {
        Boolean createdQuiz = quizService.create(quiz);
        return Result.ok(createdQuiz);
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateQuiz(@PathVariable String id, @RequestBody Quiz quiz) {
        quiz.setId(id);
        Boolean updatedQuiz = quizService.update(quiz);
        return Result.ok(updatedQuiz);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuiz(@PathVariable String id) {
        Boolean deletedQuiz = quizService.delete(id);
        return Result.ok(deletedQuiz);
    }

    @PostMapping("/{id}/options")
    public Result<Boolean> createQuizOption(@PathVariable String id, @RequestBody QuizOption quizOption) {
        Boolean createdQuizOption = quizOptionService.create(id, quizOption);
        return Result.ok(createdQuizOption);
    }

    @PutMapping("/{quizId}/options/{optionId}")
    public Result<Boolean> updateQuizOption(@PathVariable String optionId, @RequestBody QuizOption quizOption) {
        Boolean updatedQuizOption = quizOptionService.update(optionId, quizOption);
        return Result.ok(updatedQuizOption);
    }

    @DeleteMapping("/{quizId}/options/{optionId}")
    public Result<Boolean> deleteQuizOption(@PathVariable String optionId) {
        Boolean deletedQuizOption = quizOptionService.delete(optionId);
        return Result.ok(deletedQuizOption);
    }
}
