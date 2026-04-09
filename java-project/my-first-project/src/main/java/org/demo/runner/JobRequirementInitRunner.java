package org.demo.runner;

import org.demo.dao.JobRequirementRepository;
import org.demo.entity.JobRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobRequirementInitRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRequirementInitRunner.class);

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    @Override
    public void run(String... args) throws Exception {
        if (jobRequirementRepository.count() > 0) {
            log.info("岗位要求数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化岗位要求数据...");

        // 1. Java后端岗位
        JobRequirement javaBackend = new JobRequirement();
        javaBackend.setJobPosition("backend");
        javaBackend.setPositionName("Java后端开发工程师");
        javaBackend.setRequiredSkills("[\"Java\", \"Spring Boot\", \"MySQL\", \"Git\", \"REST API\"]");
        javaBackend.setPreferredSkills("[\"Redis\", \"消息队列\", \"微服务\", \"Docker\"]");
        javaBackend.setEducationRequirement("本科及以上，计算机相关专业");
        javaBackend.setExperienceRequirement("1-3年");
        javaBackend.setDescription("负责后端服务开发，参与系统设计和数据库设计");
        jobRequirementRepository.save(javaBackend);

        // 2. Web前端岗位
        JobRequirement webFrontend = new JobRequirement();
        webFrontend.setJobPosition("frontend");
        webFrontend.setPositionName("Web前端开发工程师");
        webFrontend.setRequiredSkills("[\"HTML/CSS\", \"JavaScript\", \"Vue.js\", \"Git\"]");
        webFrontend.setPreferredSkills("[\"React\", \"TypeScript\", \"Webpack\", \"小程序开发\"]");
        webFrontend.setEducationRequirement("本科及以上，计算机相关专业");
        webFrontend.setExperienceRequirement("1-3年");
        webFrontend.setDescription("负责前端页面开发，与后端协作完成功能实现");
        jobRequirementRepository.save(webFrontend);

        // 3. Python算法岗位
        JobRequirement pythonAlgo = new JobRequirement();
        pythonAlgo.setJobPosition("algo");
        pythonAlgo.setPositionName("Python算法工程师");
        pythonAlgo.setRequiredSkills("[\"Python\", \"数据结构\", \"算法\", \"机器学习基础\", \"Git\"]");
        pythonAlgo.setPreferredSkills("[\"NumPy/Pandas\", \"PyTorch/TensorFlow\", \"SQL\", \"数据可视化\"]");
        pythonAlgo.setEducationRequirement("硕士及以上，计算机/数学/统计相关专业");
        pythonAlgo.setExperienceRequirement("不限");
        pythonAlgo.setDescription("负责算法模型开发、数据处理和效果优化");
        jobRequirementRepository.save(pythonAlgo);

        // 4. 测试工程师岗位
        JobRequirement testEngineer = new JobRequirement();
        testEngineer.setJobPosition("test");
        testEngineer.setPositionName("测试工程师");
        testEngineer.setRequiredSkills("[\"测试理论\", \"测试用例设计\", \"缺陷管理\", \"SQL\"]");
        testEngineer.setPreferredSkills("[\"自动化测试\", \"性能测试\", \"接口测试\", \"JMeter/Selenium\"]");
        testEngineer.setEducationRequirement("本科及以上，计算机相关专业");
        testEngineer.setExperienceRequirement("不限");
        testEngineer.setDescription("负责功能测试、接口测试，参与测试流程优化");
        jobRequirementRepository.save(testEngineer);

        log.info("岗位要求数据初始化完成，共导入 {} 条", jobRequirementRepository.count());
    }
}