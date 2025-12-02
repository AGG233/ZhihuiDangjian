package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.constants.ErrorConstants;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.mapper.QuizOptionMapper;
import com.rauio.ZhihuiDangjian.pojo.QuizOption;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.service.QuizOptionService;
import com.rauio.ZhihuiDangjian.service.UserQuizAnswerService;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class QuizOptionServiceImpl implements QuizOptionService {

    private final QuizOptionMapper quizOptionMapper;
    private final UserQuizAnswerService userQuizAnswerService;
    private final UserService userService;


    @Override
    public Boolean update(String id, QuizOption quizOption) {
        return quizOptionMapper.updateById(quizOption) > 0;
    }

    @Override
    public Boolean insert(String quizId, QuizOption option) {
        option.setQuizId(quizId);
        return quizOptionMapper.insert(option) > 0;
    }

    @Override
    public List<QuizOption> getByQuizId(String quizId) {
        LambdaQueryWrapper<QuizOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizOption::getQuizId, quizId);
        return quizOptionMapper.selectList(wrapper);
    }

    @Override
    public QuizOption get(String id) {
        User user = userService.getUserFromAuthentication();
        QuizOption quizOption = quizOptionMapper.selectById(id);

        if (user.getUserType() == UserType.STUDENT &&
                userQuizAnswerService.selectByUserIdAndQuizId(user.getId(), quizOption.getQuizId()) == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "用户无权限");
        }
        return quizOption;
    }

    @Override
    public Boolean delete(String optionId) {
        return quizOptionMapper.deleteById(optionId) > 0;
    }

}
