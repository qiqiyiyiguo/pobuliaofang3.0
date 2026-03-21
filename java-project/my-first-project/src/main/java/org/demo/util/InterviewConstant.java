package org.demo.util;

/**
 * 面试相关常量定义
 * 集中管理所有面试模块使用的常量，避免硬编码
 */
public class InterviewConstant {

    // ==================== 面试状态常量 ====================

    /**
     * 面试进行中
     */
    public static final String STATUS_IN_PROGRESS = "进行中";

    /**
     * 面试已结束
     */
    public static final String STATUS_FINISHED = "已结束";

    // ==================== 面试风格常量 ====================

    /**
     * 友好风格
     * 面试官语气温和、鼓励性强
     */
    public static final String STYLE_FRIENDLY = "友好";

    /**
     * 严格风格
     * 面试官语气严肃、要求高、喜欢追问细节
     */
    public static final String STYLE_STRICT = "严格";

    /**
     * 温和风格
     * 面试官语气平和、中规中矩
     */
    public static final String STYLE_MILD = "温和";

    /**
     * 所有风格的列表，用于校验
     */
    public static final String[] ALL_STYLES = {STYLE_FRIENDLY, STYLE_STRICT, STYLE_MILD};

    // ==================== AI决策动作常量 ====================

    /**
     * AI决定继续追问
     * 表示当前回答不够深入，需要进一步追问
     */
    public static final String ACTION_FOLLOW_UP = "追问";

    /**
     * AI决定切换到下一题
     * 表示当前问题回答充分，可以进入下一个问题
     */
    public static final String ACTION_NEXT = "下一题";

    /**
     * AI决定结束面试
     * 表示面试已完成，可以结束
     */
    public static final String ACTION_FINISH = "结束";

    /**
     * 所有动作的列表，用于校验
     */
    public static final String[] ALL_ACTIONS = {ACTION_FOLLOW_UP, ACTION_NEXT, ACTION_FINISH};

    // ==================== 面试官角色提示词模板 ====================

    /**
     * 面试官角色提示词模板
     * 包含岗位、风格、问题、回答等占位符
     * AI需要严格按照JSON格式返回
     */
    public static final String INTERVIEW_PROMPT_TEMPLATE =
            "你是一个专业的{jobPosition}面试官，正在进行模拟面试。\n" +
                    "面试风格：{style}\n" +
                    "当前问题：{question}\n" +
                    "用户回答：{answer}\n\n" +
                    "请分析这个回答，并严格按照以下JSON格式返回（不要添加任何额外文字）：\n" +
                    "{\n" +
                    "  \"evaluation\": \"对回答的简短点评（30-50字，专业、具体）\",\n" +
                    "  \"action\": \"追问/下一题/结束\",\n" +
                    "  \"nextQuestion\": \"如果是追问，这里写追问问题；如果是下一题或结束，这里留空字符串\",\n" +
                    "  \"summary\": \"如果是结束，这里写面试总结（50字左右）；其他情况留空字符串\"\n" +
                    "}\n\n" +
                    "要求：\n" +
                    "1. 评价要专业、具体，指出回答的优点和不足\n" +
                    "2. 追问要深入且有针对性，挖掘更深层次的知识\n" +
                    "3. 如果回答充分且完整，可以切换到下一题\n" +
                    "4. 如果已经完成3-5个问题的问答，且表现充分，可以结束面试\n" +
                    "5. 不同面试风格的语气要有所区别：\n" +
                    "   - 友好风格：鼓励为主，语气温和\n" +
                    "   - 严格风格：要求严格，喜欢追问细节\n" +
                    "   - 温和风格：中规中矩，客观公正";

    /**
     * 简短版提示词模板（用于快速测试）
     */
    public static final String INTERVIEW_PROMPT_TEMPLATE_SHORT =
            "你是一个{jobPosition}面试官，风格{style}。\n" +
                    "问题：{question}\n" +
                    "回答：{answer}\n\n" +
                    "请返回JSON：{\"evaluation\":\"点评\",\"action\":\"追问/下一题/结束\",\"nextQuestion\":\"追问问题\",\"summary\":\"总结\"}";

    // ==================== 错误提示常量 ====================

    /**
     * AI服务不可用时的默认提示
     */
    public static final String AI_SERVICE_UNAVAILABLE = "AI面试官服务暂时不可用，请稍后重试。";

    /**
     * 会话不存在提示
     */
    public static final String SESSION_NOT_FOUND = "面试会话不存在，请重新开始面试。";

    /**
     * 题库为空提示
     */
    public static final String QUESTION_BANK_EMPTY = "该岗位暂无题库，请先添加题目。";

    // ==================== 默认值常量 ====================

    /**
     * 默认面试风格
     */
    public static final String DEFAULT_STYLE = STYLE_FRIENDLY;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 默认最大追问次数
     */
    public static final int DEFAULT_MAX_FOLLOW_UP = 3;

    // ==================== 辅助方法 ====================

    /**
     * 判断是否是有效的面试风格
     * @param style 待检查的风格
     * @return 是否有效
     */
    public static boolean isValidStyle(String style) {
        if (style == null) return false;
        for (String s : ALL_STYLES) {
            if (s.equals(style)) return true;
        }
        return false;
    }

    /**
     * 判断是否是有效的AI动作
     * @param action 待检查的动作
     * @return 是否有效
     */
    public static boolean isValidAction(String action) {
        if (action == null) return false;
        for (String a : ALL_ACTIONS) {
            if (a.equals(action)) return true;
        }
        return false;
    }

    /**
     * 获取风格描述
     * @param style 风格
     * @return 风格描述
     */
    public static String getStyleDescription(String style) {
        switch (style) {
            case STYLE_FRIENDLY:
                return "友好型面试官：语气温和，鼓励为主，会给出建设性意见";
            case STYLE_STRICT:
                return "严格型面试官：要求高，喜欢追问细节，评价客观直接";
            case STYLE_MILD:
                return "温和型面试官：中规中矩，客观公正，不偏不倚";
            default:
                return "未知风格";
        }
    }
}