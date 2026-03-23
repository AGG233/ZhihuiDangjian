package com.rauio.smartdangjian.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.content.pojo.Course;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.pojo.vo.UserVO;
import com.rauio.smartdangjian.content.service.CourseService;
import com.rauio.smartdangjian;
import com.rauio.smartdangjian.user.service.UserService;
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
    public Result<UserVO> get(@Parameter(description = "用户ID") @PathVariable String id){
        UserVO user = userService.get(id);
        return Result.ok(user);
    }

    @Operation(
            summary = "获取用户信息",
            description = "通过用户请求体的信息模糊查询条件匹配的用户"
    )
    @PostMapping("/search/{pageNum}/{pageSize}")
    public Result<Page<User>> getPage(@RequestBody UserDto  userDto, @PathVariable int pageNum, @PathVariable int pageSize){
        Page<User> user = userService.getPage(userDto,pageNum,pageSize);
        return Result.ok(user);
    }

    @Operation(
            summary = "更新用户信息",
            description = "通过ID更新用户信息"
    )
    @PutMapping("/{id}")
    public Result<Boolean> update(
            @PathVariable String id,
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
    public Result<Object> delete(@Parameter(description = "用户ID") @PathVariable String id){
        Boolean result = userService.delete(id);
        return Result.ok("404","接口已经弃用",null);
    }

    @GetMapping("/course/{id}")
    public Result<List<Course>> getByUserIdCourses(@PathVariable String id){
        List<Course> result = courseService.getByUserId(id);
        return Result.ok(result);
    }

    /*
    * 用户考试信息
    * */
    @GetMapping("/{id}/quiz")
    public Result<List<UserQuizAnswer>> getByUserIdQuizAnswers(@PathVariable String id){
        List<UserQuizAnswer> result = userQuizAnswerService.getByUserId(id);
        return Result.ok(result);
    }

    @GetMapping("/{id}/quiz/{quizId}")
    public Result<List<UserQuizAnswer>> getByQuizIdQuizAnswers(@PathVariable String quizId){
        List<UserQuizAnswer> result = userQuizAnswerService.getByQuizId(quizId);
        return Result.ok(result);
    }

    @GetMapping("/{id}/quiz/{quizId}/{optionId}")
    public Result<UserQuizAnswer> getByUserIdAndQuizIdAndOptionIdQuizAnswer(@PathVariable String id,
                                                                             @PathVariable String quizId,
                                                                             @PathVariable String optionId){
        UserQuizAnswer result = userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(id, quizId, optionId);
        return Result.ok(result);
    }

    @PostMapping("/{id}/quiz/{quizId}/{optionId}")
    public Result<Boolean> createQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        Boolean result = userQuizAnswerService.create(userQuizAnswer);

        return Result.ok(result);
    }

    @PutMapping("/{id}/quiz/{quizId}/{optionId}")
    public Result<Boolean> updateQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        Boolean result = userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(userQuizAnswer);
        return Result.ok(result);
    }

    @DeleteMapping("/{id}/quiz/{quizId}/{optionId}")
    @PermissionAccess(UserType.MANAGER)
    public Result<Boolean> deleteQuizAnswer(@PathVariable String id, @PathVariable String quizId, @PathVariable String optionId){
        Boolean result = userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(id, quizId, optionId);
        return Result.ok(result);
    }
}
