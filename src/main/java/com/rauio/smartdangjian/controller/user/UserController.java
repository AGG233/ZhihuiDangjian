package com.rauio.smartdangjian.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.Course;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.UserQuizAnswer;
import com.rauio.smartdangjian.pojo.dto.UserDto;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.pojo.vo.UserVO;
import com.rauio.smartdangjian.service.content.CourseService;
import com.rauio.smartdangjian.service.user.UserQuizAnswerService;
import com.rauio.smartdangjian.service.user.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
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
    public Result<UserVO> get(@Parameter(description = "用户ID") @PathVariable Long id){
        UserVO user = userService.getUserByID(id);
        return Result.ok(user);
    }

    @Operation(
            summary = "获取用户信息",
            description = "通过用户请求体的信息模糊查询条件匹配的用户"
    )
    @PostMapping("/search/{pageNum}/{pageSize}")
    public Result<Page<User>> getByDto(@RequestBody UserDto  userDto, @PathVariable int pageNum, @PathVariable int pageSize){
        Page<User> user = userService.getUser(userDto,pageNum,pageSize);
        return Result.ok(user);
    }

    @Operation(
            summary = "更新用户信息",
            description = "通过ID更新用户信息"
    )
    @PutMapping("/{id}")
    public Result<Boolean> update(
            @PathVariable Long id,
            @RequestBody User user
    ){
        Boolean result = userService.update(id,user);
        return Result.ok(result);
    }

    @Operation(
            summary = "删除用户（已经弃用）",
            description = "通过ID删除用户"
    )
    @DeleteMapping("/{id}")
    public Result<Object> delete(@Parameter(description = "用户ID") @PathVariable Long id){
        Boolean result = userService.delete(id);
        return Result.ok("404","接口已经弃用",null);
    }

    @GetMapping("/course/{id}")
    public Result<List<Course>> getAllCoursesOfUser(@PathVariable String id){
        List<Course> result = courseService.getAllCoursesOfUser(id);
        return Result.ok(result);
    }

    /*
    * 用户考试信息
    * */
    @GetMapping("/{id}/quiz")
    public Result<List<UserQuizAnswer>> getAllQuizAnswerOfUser(@PathVariable Long id){
        List<UserQuizAnswer> result = userQuizAnswerService.selectByUserId(id);
        return Result.ok(result);
    }

    @GetMapping("/{id}/quiz/{quizId}")
    public Result<List<UserQuizAnswer>> getQuizAnswerOfQuiz(@PathVariable Long quizId){
        List<UserQuizAnswer> result = userQuizAnswerService.selectByQuizId(quizId);
        return Result.ok(result);
    }

    @GetMapping("/{id}/quiz/{quizId}/{optionId}")
    public Result<UserQuizAnswer> getQuizAnswerOfOption(@PathVariable Long optionId){
        UserQuizAnswer result = userQuizAnswerService.selectByOptionId(optionId);
        return Result.ok(result);
    }

    @PostMapping("/{id}/quiz/{quizId}/{optionId}")
    public Result<Boolean> createQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        Boolean result = userQuizAnswerService.insert(userQuizAnswer);

        return Result.ok(result);
    }

    @PutMapping("/{id}/quiz/{quizId}/{optionId}")
    public Result<Boolean> updateQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        Boolean result = userQuizAnswerService.update(userQuizAnswer);
        return Result.ok(result);
    }

    @DeleteMapping("/{id}/quiz/{quizId}/{optionId}")
    @PermissionAccess(UserType.MANAGER)
    public Result<Boolean> deleteQuizAnswer(@PathVariable Long id, @PathVariable Long quizId, @PathVariable String optionId){
        Boolean result = userQuizAnswerService.delete(quizId);
        return Result.ok(result);
    }
}