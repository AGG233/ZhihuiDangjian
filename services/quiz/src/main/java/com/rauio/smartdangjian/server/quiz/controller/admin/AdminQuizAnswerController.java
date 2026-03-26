package com.rauio.smartdangjian.server.quiz.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/quiz/answers")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminQuizAnswerController {

    private final UserQuizAnswerService userQuizAnswerService;

    @DeleteMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    public Result<Boolean> deleteQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        return Result.ok(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(id, quizId, optionId));
    }
}
