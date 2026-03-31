package com.rauio.smartdangjian.server.quiz.controller.user;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz/answers")
@RequiredArgsConstructor
@PermissionAccess(UserType.STUDENT)
public class UserQuizAnswerController {


    private final UserQuizAnswerService userQuizAnswerService;

    /*
     * 用户考试信息
     * */
    @GetMapping("/users/{id}")
    @ResourceAccess(id = "#id")
    public Result<List<UserQuizAnswer>> getByUserIdQuizAnswers(@PathVariable String id){
        List<UserQuizAnswer> result = userQuizAnswerService.getByUserId(id);
        return Result.ok(result);
    }

    @GetMapping("/users/{id}/quizzes/{quizId}")
    @ResourceAccess(id = "#id")
    public Result<List<UserQuizAnswer>> getByQuizIdQuizAnswers(@PathVariable String id, @PathVariable String quizId){
        List<UserQuizAnswer> result = userQuizAnswerService.getByUserIdAndQuizId(id, quizId);
        return Result.ok(result);
    }

    @GetMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @ResourceAccess(id = "#id")
    public Result<UserQuizAnswer> getByUserIdAndQuizIdAndOptionIdQuizAnswer(@PathVariable String id,
                                                                            @PathVariable String quizId,
                                                                            @PathVariable String optionId){
        UserQuizAnswer result = userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(id, quizId, optionId);
        return Result.ok(result);
    }

    @PostMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @ResourceAccess(id = "#id")
    public Result<Boolean> createQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        Boolean result = userQuizAnswerService.create(userQuizAnswer);

        return Result.ok(result);
    }

    @PutMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @ResourceAccess(id = "#id")
    public Result<Boolean> updateQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        Boolean result = userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(userQuizAnswer);
        return Result.ok(result);
    }
}
