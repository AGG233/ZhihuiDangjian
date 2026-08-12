package com.rauio.smartdangjian.server.quiz.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;

import cn.dev33.satoken.annotation.SaCheckRole;

@Service
@Transactional
public class UserQuizAnswerService extends ServiceImpl<UserQuizAnswerMapper, UserQuizAnswer> {

    /** 主观题类型：服务端不自动判分 */
    private static final String QUESTION_TYPE_SHORT_ANSWER = "short_answer";
    /** 多选题类型：按命中比例判分 */
    private static final String QUESTION_TYPE_MULTIPLE_CHOICE = "multiple_choice";

    /** 答题结果：错误 */
    private static final int ANSWER_WRONG = 0;
    /** 答题结果：完全正确 */
    private static final int ANSWER_CORRECT = 1;
    /** 答题结果：部分正确 */
    private static final int ANSWER_PARTIAL = 2;

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    /**
     * 通过构造器注入判分所需的题目与选项服务。
     *
     * <p>QuizOptionService 反向依赖本服务（学生防泄题校验），使用 {@code @Lazy}
     * 打破构造器循环依赖：本服务持有一个懒代理，首次调用时才真正解析 QuizOptionService。
     */
    public UserQuizAnswerService(QuizService quizService, @Lazy QuizOptionService quizOptionService) {
        this.quizService = quizService;
        this.quizOptionService = quizOptionService;
    }

    /**
     * 创建用户答题记录并服务端自动判分。
     *
     * <p>判分基于服务端查询的题目与选项数据，绝不信任客户端传入的 isCorrect/score：
     * <ul>
     *   <li>single_choice / true_false：所选选项正确 → isCorrect=1、scoreObtained=题目分值；错误 → isCorrect=0、0 分</li>
     *   <li>multiple_choice：累计所选全部命中正确选项且无多选 → isCorrect=1 满分；部分命中 → isCorrect=2、按命中比例四舍五入；含错误选项 → isCorrect=0</li>
     *   <li>short_answer：服务端不自动判分，isCorrect / scoreObtained 置 null，留待人工/AI 评审</li>
     * </ul>
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否创建成功
     */
    public Boolean create(UserQuizAnswer userQuizAnswer) {
        Quiz quiz = quizService.get(userQuizAnswer.getQuizId());
        if (quiz == null) {
            throw new BusinessException(QuizErrorConstants.QUIZ_NOT_FOUND, "题目不存在");
        }

        // 主观题：服务端不自动判分，留待人工/AI 评审
        if (QUESTION_TYPE_SHORT_ANSWER.equals(quiz.getQuestionType())) {
            userQuizAnswer.setIsCorrect(null);
            userQuizAnswer.setScoreObtained(null);
            return this.save(userQuizAnswer);
        }

        // 客观题：判分必须基于服务端查询的选项数据，选项不存在或不属于该题时拒绝
        QuizOption option = quizOptionService.getById(userQuizAnswer.getOptionId());
        if (option == null || !Objects.equals(option.getQuizId(), quiz.getId())) {
            throw new BusinessException(QuizErrorConstants.QUIZ_OPTION_NOT_FOUND, "选项不存在");
        }

        if (QUESTION_TYPE_MULTIPLE_CHOICE.equals(quiz.getQuestionType())) {
            scoreMultipleChoice(userQuizAnswer, quiz, option);
        } else {
            scoreSingleChoice(userQuizAnswer, quiz, option);
        }
        return this.save(userQuizAnswer);
    }

    /**
     * 单选/判断题判分：所选选项即最终答案，答对得全分，答错 0 分。
     */
    private void scoreSingleChoice(UserQuizAnswer answer, Quiz quiz, QuizOption option) {
        if (Boolean.TRUE.equals(option.getIsCorrect())) {
            answer.setIsCorrect(ANSWER_CORRECT);
            answer.setScoreObtained(resolveScore(quiz));
        } else {
            answer.setIsCorrect(ANSWER_WRONG);
            answer.setScoreObtained(0);
        }
    }

    /**
     * 多选题判分：按该用户在此题下累计提交的选项集合（含本次）整体判定。
     * 集合含错误选项 → 0 分；集合恰好等于全部正确选项 → 满分；否则按命中比例四舍五入取整。
     */
    private void scoreMultipleChoice(UserQuizAnswer answer, Quiz quiz, QuizOption option) {
        // 该用户在此题下已提交的选项集合，并入本次提交
        List<UserQuizAnswer> existing = getByUserIdAndQuizId(answer.getUserId(), answer.getQuizId());
        Set<Long> selected = existing.stream().map(UserQuizAnswer::getOptionId).collect(Collectors.toSet());
        selected.add(option.getId());

        // 服务端查询全部选项，得出正确选项集合
        List<QuizOption> allOptions = quizOptionService.getByQuizId(quiz.getId());
        Set<Long> correctOptionIds = allOptions.stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                .map(QuizOption::getId)
                .collect(Collectors.toSet());

        // 含错误选项 → 0 分
        if (!correctOptionIds.containsAll(selected)) {
            answer.setIsCorrect(ANSWER_WRONG);
            answer.setScoreObtained(0);
            return;
        }

        int score = resolveScore(quiz);
        // 全部正确选项均已选择且无多余选择 → 满分
        if (selected.size() == correctOptionIds.size()) {
            answer.setIsCorrect(ANSWER_CORRECT);
            answer.setScoreObtained(score);
            return;
        }

        // 部分命中 → 按命中比例四舍五入
        answer.setIsCorrect(ANSWER_PARTIAL);
        answer.setScoreObtained((int) Math.round(score * selected.size() / (float) correctOptionIds.size()));
    }

    /**
     * 题目分值兜底：数据库未配置分值按 0 分处理。
     */
    private int resolveScore(Quiz quiz) {
        return quiz.getScore() == null ? 0 : quiz.getScore();
    }

    /**
     * 根据主键更新用户答题记录。
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否更新成功
     */
    @SaCheckRole("SCHOOL")
    public Boolean update(UserQuizAnswer userQuizAnswer) {
        return this.updateById(userQuizAnswer);
    }

    /**
     * 根据用户、测验和选项组合更新答题记录。
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否更新成功
     */
    @SaCheckRole("SCHOOL")
    public Boolean updateByUserIdAndQuizIdAndOptionId(UserQuizAnswer userQuizAnswer) {
        UserQuizAnswer existing = getByUserIdAndQuizIdAndOptionId(
                userQuizAnswer.getUserId(), userQuizAnswer.getQuizId(), userQuizAnswer.getOptionId());
        if (existing == null) {
            return false;
        }
        userQuizAnswer.setId(existing.getId());
        return this.updateById(userQuizAnswer);
    }

    /**
     * 根据记录 ID 删除答题记录。
     *
     * @param id 记录 ID
     * @return 是否删除成功
     */
    @SaCheckRole("MANAGER")
    public Boolean delete(Long id) {
        return this.removeById(id);
    }

    /**
     * 根据用户、测验和选项组合删除答题记录。
     *
     * @param userId 用户 ID
     * @param quizId 测验 ID
     * @param optionId 选项 ID
     * @return 是否删除成功
     */
    @SaCheckRole("MANAGER")
    public Boolean deleteByUserIdAndQuizIdAndOptionId(Long userId, Long quizId, Long optionId) {
        UserQuizAnswer existing = getByUserIdAndQuizIdAndOptionId(userId, quizId, optionId);
        if (existing == null) {
            return false;
        }
        return this.removeById(existing.getId());
    }

    /**
     * 查询测验下的答题记录。
     *
     * @param quizId 测验 ID
     * @return 答题记录列表
     */
    public List<UserQuizAnswer> getByQuizId(Long quizId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getQuizId, quizId);
        return this.list(wrapper);
    }

    /**
     * 根据选项 ID 查询答题记录。
     *
     * @param optionId 选项 ID
     * @return 答题记录
     */
    public UserQuizAnswer getByOptionId(Long optionId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getOptionId, optionId);
        return this.getOne(wrapper);
    }

    /**
     * 查询用户全部答题记录。
     *
     * @param userId 用户 ID
     * @return 答题记录列表
     */
    public List<UserQuizAnswer> getByUserId(Long userId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        return this.list(wrapper);
    }

    /**
     * 查询用户在指定测验下的答题记录。
     *
     * @param userId 用户 ID
     * @param quizId 测验 ID
     * @return 答题记录列表
     */
    public List<UserQuizAnswer> getByUserIdAndQuizId(Long userId, Long quizId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        wrapper.eq(UserQuizAnswer::getQuizId, quizId);
        return this.list(wrapper);
    }

    /**
     * 根据用户、测验和选项组合查询答题记录。
     *
     * @param userId 用户 ID
     * @param quizId 测验 ID
     * @param optionId 选项 ID
     * @return 答题记录
     */
    public UserQuizAnswer getByUserIdAndQuizIdAndOptionId(Long userId, Long quizId, Long optionId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        wrapper.eq(UserQuizAnswer::getQuizId, quizId);
        wrapper.eq(UserQuizAnswer::getOptionId, optionId);
        return this.getOne(wrapper);
    }
}
