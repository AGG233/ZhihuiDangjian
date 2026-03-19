package com.rauio.smartdangjian.service.quiz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.mapper.QuizOptionMapper;
import com.rauio.smartdangjian.pojo.QuizOption;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.service.user.UserQuizAnswerService;
import com.rauio.smartdangjian.service.user.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class QuizOptionService {

    private final QuizOptionMapper quizOptionMapper;
    private final UserQuizAnswerService userQuizAnswerService;
    private final UserService userService;
    public Boolean update(Long id, QuizOption quizOption) {
        return quizOptionMapper.updateById(quizOption) > 0;
    }
    public Boolean insert(Long quizId, QuizOption option) {
        option.setQuizId(quizId);
        return quizOptionMapper.insert(option) > 0;
    }
    public List<QuizOption> getByQuizId(Long quizId) {
        LambdaQueryWrapper<QuizOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizOption::getQuizId, quizId);
        return quizOptionMapper.selectList(wrapper);
    }
    public QuizOption get(Long id) {
        User user = userService.getUserFromAuthentication();
        QuizOption quizOption = quizOptionMapper.selectById(id);

        if (user.getUserType() == UserType.STUDENT &&
                userQuizAnswerService.selectByUserIdAndQuizId(user.getId(), quizOption.getQuizId()) == null) {
            quizOption.setIsCorrect(null);
        }
        return quizOption;
    }
    public Boolean delete(Long optionId) {
        return quizOptionMapper.deleteById(optionId) > 0;
    }

}
