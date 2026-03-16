package com.rauio.smartdangjian.service.quiz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.mapper.QuizMapper;
import com.rauio.smartdangjian.pojo.Quiz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizMapper quizMapper;
    public Quiz get(Long quizId) {
        return quizMapper.selectById(quizId);
    }
    public List<Quiz> getByChapterId(String chapterId) {
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Quiz::getChapterId, chapterId);
        return quizMapper.selectList(wrapper);
    }
    public Boolean update(Quiz quiz) {
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Quiz::getId, quiz.getId());
        return quizMapper.update(quiz, wrapper) > 0;
    }
    public Boolean insert(Quiz quiz) {
        return quizMapper.insert(quiz) > 0;
    }
    public Boolean delete(Long quizId) {
        return quizMapper.deleteById(quizId) > 0;
    }
}
