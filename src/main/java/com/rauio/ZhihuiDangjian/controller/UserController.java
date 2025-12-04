package com.rauio.ZhihuiDangjian.controller;

import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.UserQuizAnswer;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;
import com.rauio.ZhihuiDangjian.service.CourseService;
import com.rauio.ZhihuiDangjian.service.UserQuizAnswerService;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="用户管理接口", description = "提供用户信息操作")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CourseService courseService;
    private final UserQuizAnswerService userQuizAnswerService;

    @Operation(
            summary = "获取用户信息",
            description = "通过ID获取用户信息")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> get(@Parameter(description = "用户ID") @PathVariable Long id){
        UserVO user = userService.getUserByID(id);
        return ApiResponse.ok(user);
    }

    @Operation(
            summary = "更新用户信息",
            description = "通过ID更新用户信息"
    )
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(
            @PathVariable Long id,
            @RequestBody User user
    ){
        Boolean result = userService.update(id,user);
        return ApiResponse.ok(result);
    }

    @Operation(
            summary = "删除用户（已经弃用）",
            description = "通过ID删除用户"
    )
    @DeleteMapping("/{id}")
    @Deprecated
    public ApiResponse<Object> delete(@Parameter(description = "用户ID") @PathVariable Long id){
        Boolean result = userService.delete(id);
        return ApiResponse.ok("404","接口已经弃用",null);
    }

    @GetMapping("/course/{id}")
    public ApiResponse<List<Course>> getAllCoursesOfUser(@PathVariable String id){
        List<Course> result = courseService.getAllCoursesOfUser(id);
        return ApiResponse.ok(result);
    }

    /*
    * 用户考试信息
    * */
    @GetMapping("/{id}/quiz")
    public ApiResponse<List<UserQuizAnswer>> getAllQuizAnswerOfUser(@PathVariable String id){
        List<UserQuizAnswer> result = userQuizAnswerService.selectByUserId(id);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}/quiz/{quizId}")
    public ApiResponse<List<UserQuizAnswer>> getQuizAnswerOfQuiz(@PathVariable String quizId){
        List<UserQuizAnswer> result = userQuizAnswerService.selectByQuizId(quizId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}/quiz/{quizId}/{optionId}")
    public ApiResponse<UserQuizAnswer> getQuizAnswerOfOption(@PathVariable String optionId){
        UserQuizAnswer result = userQuizAnswerService.selectByOptionId(optionId);
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/quiz/{quizId}/{optionId}")
    public ApiResponse<Boolean> createQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        Boolean result = userQuizAnswerService.insert(userQuizAnswer);

        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}/quiz/{quizId}/{optionId}")
    public ApiResponse<Boolean> updateQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        Boolean result = userQuizAnswerService.update(userQuizAnswer);
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{id}/quiz/{quizId}/{optionId}")
    @PermissionAccess(UserType.MANAGER)
    public ApiResponse<Boolean> deleteQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        Boolean result = userQuizAnswerService.delete(quizId);
        return ApiResponse.ok(result);
    }
}