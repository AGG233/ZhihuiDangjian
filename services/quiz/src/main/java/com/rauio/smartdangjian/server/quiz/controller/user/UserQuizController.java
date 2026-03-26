package com.rauio.smartdangjian.server.quiz.controller.user;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz/quizzes")
@RequiredArgsConstructor
@Tag(name = "用户试题接口")
@PermissionAccess(UserType.STUDENT)
public class UserQuizController {

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @GetMapping("/{id}")
    public Result<Quiz> getQuiz(@PathVariable String id) {
        return Result.ok(quizService.get(id));
    }

    @GetMapping("/by-chapter/{chapterId}")
    public Result<List<Quiz>> getQuizOfChapter(@PathVariable String chapterId) {
        return Result.ok(quizService.getByChapterId(chapterId));
    }

    @GetMapping("/{id}/options")
    public Result<List<QuizOption>> getQuizOption(@PathVariable String id) {
        return Result.ok(quizOptionService.getByQuizId(id));
    }

    @GetMapping("/{id}/options/{optionId}")
    public Result<QuizOption> getByOptionId(@PathVariable String optionId) {
        return Result.ok(quizOptionService.get(optionId));
    }
}
