package org.demo.util;

/**
 * 提示词工具类
 * 负责填充面试官提示词模板，生成完整的AI提示文本
 */
public class PromptUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private PromptUtil() {
        // 工具类不需要实例化
    }

    /**
     * 填充提示词模板（使用完整版模板）
     * @param jobPosition 岗位类型（如：Java后端）
     * @param style 面试风格（友好/严格/温和）
     * @param question 当前问题内容
     * @param answer 用户回答内容
     * @return 完整的提示词文本
     */
    public static String fillPrompt(String jobPosition, String style, String question, String answer) {
        // 参数校验，防止空指针
        jobPosition = jobPosition != null ? jobPosition : "未知岗位";
        style = style != null ? style : InterviewConstant.DEFAULT_STYLE;
        question = question != null ? question : "";
        answer = answer != null ? answer : "";

        // 替换占位符
        String prompt = InterviewConstant.INTERVIEW_PROMPT_TEMPLATE
                .replace("{jobPosition}", jobPosition)
                .replace("{style}", style)
                .replace("{question}", question)
                .replace("{answer}", answer);

        return prompt;
    }

    /**
     * 填充简短版提示词模板
     * 用于快速测试或简单场景
     * @param jobPosition 岗位类型
     * @param style 面试风格
     * @param question 当前问题
     * @param answer 用户回答
     * @return 简短版提示词
     */
    public static String fillShortPrompt(String jobPosition, String style, String question, String answer) {
        // 参数校验
        jobPosition = jobPosition != null ? jobPosition : "未知岗位";
        style = style != null ? style : InterviewConstant.DEFAULT_STYLE;
        question = question != null ? question : "";
        answer = answer != null ? answer : "";

        // 替换占位符
        String prompt = InterviewConstant.INTERVIEW_PROMPT_TEMPLATE_SHORT
                .replace("{jobPosition}", jobPosition)
                .replace("{style}", style)
                .replace("{question}", question)
                .replace("{answer}", answer);

        return prompt;
    }

    /**
     * 填充自定义模板
     * @param template 自定义模板（需包含{jobPosition}等占位符）
     * @param jobPosition 岗位类型
     * @param style 面试风格
     * @param question 当前问题
     * @param answer 用户回答
     * @return 填充后的提示词
     */
    public static String fillCustomPrompt(String template, String jobPosition, String style,
                                          String question, String answer) {
        if (template == null || template.isEmpty()) {
            return fillPrompt(jobPosition, style, question, answer);
        }

        jobPosition = jobPosition != null ? jobPosition : "未知岗位";
        style = style != null ? style : InterviewConstant.DEFAULT_STYLE;
        question = question != null ? question : "";
        answer = answer != null ? answer : "";

        return template
                .replace("{jobPosition}", jobPosition)
                .replace("{style}", style)
                .replace("{question}", question)
                .replace("{answer}", answer);
    }

    /**
     * 带额外参数的自定义填充
     * @param jobPosition 岗位类型
     * @param style 面试风格
     * @param question 当前问题
     * @param answer 用户回答
     * @param extraParams 额外参数Map（如{company}, {years}等）
     * @return 填充后的提示词
     */
    public static String fillPromptWithExtra(String jobPosition, String style, String question,
                                             String answer, java.util.Map<String, String> extraParams) {
        String prompt = fillPrompt(jobPosition, style, question, answer);

        if (extraParams != null && !extraParams.isEmpty()) {
            for (java.util.Map.Entry<String, String> entry : extraParams.entrySet()) {
                String key = "{" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                prompt = prompt.replace(key, value);
            }
        }

        return prompt;
    }

    /**
     * 格式化提示词（添加额外说明）
     * @param basePrompt 基础提示词
     * @param additionalNotes 额外说明
     * @return 格式化后的提示词
     */
    public static String formatPromptWithNotes(String basePrompt, String additionalNotes) {
        if (additionalNotes == null || additionalNotes.isEmpty()) {
            return basePrompt;
        }
        return basePrompt + "\n\n额外要求：\n" + additionalNotes;
    }

    /**
     * 构建系统提示词（用于多轮对话）
     * @param jobPosition 岗位类型
     * @param style 面试风格
     * @return 系统角色提示词
     */
    public static String buildSystemPrompt(String jobPosition, String style) {
        jobPosition = jobPosition != null ? jobPosition : "未知岗位";
        style = style != null ? style : InterviewConstant.DEFAULT_STYLE;

        return String.format(
                "你是一个专业的%s面试官，面试风格为%s。\n" +
                        "你的职责是：\n" +
                        "1. 对面试者的回答进行专业评价\n" +
                        "2. 根据回答质量决定是否追问\n" +
                        "3. 控制面试节奏，保持%s的对话风格\n" +
                        "4. 严格按照JSON格式返回结果",
                jobPosition,
                style,
                style.equals(InterviewConstant.STYLE_FRIENDLY) ? "友好鼓励" :
                        style.equals(InterviewConstant.STYLE_STRICT) ? "严格专业" : "温和客观"
        );
    }

    /**
     * 构建历史对话上下文
     * @param historyQuestions 历史问题列表
     * @param historyAnswers 历史回答列表
     * @param historyEvaluations 历史评价列表
     * @return 格式化的历史上下文
     */
    public static String buildHistoryContext(java.util.List<String> historyQuestions,
                                             java.util.List<String> historyAnswers,
                                             java.util.List<String> historyEvaluations) {
        if (historyQuestions == null || historyQuestions.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("\n历史对话：\n");

        for (int i = 0; i < historyQuestions.size(); i++) {
            context.append("【第").append(i + 1).append("轮】\n");
            context.append("问题：").append(historyQuestions.get(i)).append("\n");

            if (historyAnswers != null && i < historyAnswers.size()) {
                context.append("回答：").append(historyAnswers.get(i)).append("\n");
            }

            if (historyEvaluations != null && i < historyEvaluations.size()) {
                context.append("评价：").append(historyEvaluations.get(i)).append("\n");
            }

            context.append("\n");
        }

        return context.toString();
    }

    /**
     * 构建完整的面试提示词（包含历史上下文）
     * @param jobPosition 岗位类型
     * @param style 面试风格
     * @param currentQuestion 当前问题
     * @param currentAnswer 当前回答
     * @param historyContext 历史对话上下文
     * @return 完整的提示词
     */
    public static String buildFullPromptWithHistory(String jobPosition, String style,
                                                    String currentQuestion, String currentAnswer,
                                                    String historyContext) {
        StringBuilder fullPrompt = new StringBuilder();

        // 添加系统角色
        fullPrompt.append(buildSystemPrompt(jobPosition, style)).append("\n\n");

        // 添加历史对话（如果有）
        if (historyContext != null && !historyContext.isEmpty()) {
            fullPrompt.append(historyContext).append("\n");
        }

        // 添加当前对话
        fullPrompt.append("当前问题：").append(currentQuestion).append("\n");
        fullPrompt.append("用户回答：").append(currentAnswer).append("\n\n");

        // 添加输出格式要求
        fullPrompt.append("请严格按照以下JSON格式返回结果：\n");
        fullPrompt.append("{\n");
        fullPrompt.append("  \"evaluation\": \"对回答的简短点评\",\n");
        fullPrompt.append("  \"action\": \"追问/下一题/结束\",\n");
        fullPrompt.append("  \"nextQuestion\": \"如果是追问，这里写追问问题\",\n");
        fullPrompt.append("  \"summary\": \"如果是结束，这里写面试总结\"\n");
        fullPrompt.append("}\n");

        return fullPrompt.toString();
    }
}