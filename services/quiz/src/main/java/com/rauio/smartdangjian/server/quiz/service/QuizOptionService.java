package com.rauio.smartdangjian.server.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.quiz.client.dto.CurrentUserDto;
import com.rauio.smartdangjian.server.quiz.mapper.QuizOptionMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class QuizOptionService extends ServiceImpl<QuizOptionMapper, QuizOption> {

    private final UserQuizAnswerService userQuizAnswerService;
    private final UserService userService;

    /**
     * 更新选项信息。
     *
     * @param id 选项 ID
     * @param quizOption 选项实体
     * @return 是否更新成功
     */
    public Boolean update(String id, QuizOption quizOption) {
        quizOption.setId(id);
        return this.updateById(quizOption);
    }

    /**
     * 为指定测验创建选项。
     *
     * @param quizId 测验 ID
     * @param option 选项实体
     * @return 是否创建成功
     */
    public Boolean create(String quizId, QuizOption option) {
        option.setQuizId(quizId);
        return this.save(option);
    }

    /**
     * 查询测验下的全部选项。
     *
     * @param quizId 测验 ID
     * @return 选项列表
     */
    public List<QuizOption> getByQuizId(String quizId) {
        LambdaQueryWrapper<QuizOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizOption::getQuizId, quizId);
        return this.list(wrapper);
    }

    /**
     * 获取选项详情。
     *
     * @param id 选项 ID
     * @return 选项实体；学生未答题时会隐藏正确答案字段
     */
    public QuizOption get(String id) {
        User user = userService.getCurrentUser();
        QuizOption quizOption = this.getById(id);

        if (user.getUserType() == UserType.STUDENT &&
                userQuizAnswerService.getByUserIdAndQuizId(user.getId(), quizOption.getQuizId()).isEmpty()) {
            quizOption.setIsCorrect(null);
        }
        return quizOption;
    }

    /**
     * 删除选项。
     *
     * @param optionId 选项 ID
     * @return 是否删除成功
     */
    public Boolean delete(String optionId) {
        return this.removeById(optionId);
    }

}
