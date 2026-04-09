<template>
  <div class="question-list-container">
    <div class="page-header">
      <div class="header-top">
        <div>
          <h2>{{ title }}</h2>
          <p>每个岗位按“技术知识 / 项目经历 / 场景题 / 行为题”提供了可筛选题库，支持查看参考答案。</p>
        </div>
        <el-button class="back-btn" plain @click="goBackToBank">返回岗位题库</el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-select
        v-model="category"
        placeholder="请选择题目类别"
        clearable
        @change="handleCategoryChange"
      >
        <el-option
          v-for="item in categoryOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      stripe
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="displayId" label="ID" width="80" />
      <el-table-column prop="title" label="题目" min-width="320" />
      <el-table-column prop="category" label="类别" width="120" />
      <el-table-column prop="content" label="内容" min-width="360" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && list.length === 0" class="empty-state">
      当前岗位下暂无题目数据
    </div>

    <div class="pagination-container">
      <el-pagination
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog v-model="detailVisible" :title="selectedQuestion?.title || '题目详情'" width="820px">
      <div v-if="selectedQuestion" class="detail-body">
        <div class="detail-tag-row">
          <span class="detail-tag">{{ selectedQuestion.category }}</span>
          <span class="detail-tag detail-tag-light">序号 {{ selectedQuestion.displayId }}</span>
        </div>
        <section class="detail-section">
          <h3>题目内容</h3>
          <p>{{ selectedQuestion.content }}</p>
        </section>
        <section class="detail-section">
          <h3>参考答案</h3>
          <p class="answer-text">{{ selectedQuestion.answer || '当前题目暂未提供参考答案。' }}</p>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getJavaQuestion, getPythonQuestion, getTestQuestion, getWebQuestion } from '@/api/question.js'

const props = defineProps({
  module: {
    type: String,
    required: true
  },
  title: {
    type: String,
    default: '岗位题库'
  }
})

const router = useRouter()

const categoryOptions = [
  { label: '技术知识', value: '技术知识' },
  { label: '项目经历', value: '项目经历' },
  { label: '场景题', value: '场景题' },
  { label: '行为题', value: '行为题' }
]

const apiMap = {
  Java: getJavaQuestion,
  Web: getWebQuestion,
  Python: getPythonQuestion,
  Test: getTestQuestion
}

const knowledgeVariants = [
  '请解释 {topic} 的核心原理。',
  '在面试里你会如何系统回答 {topic}？',
  '围绕 {topic}，你通常会重点展开哪些知识点？',
  '请说明 {topic} 在实际工程中的应用方式。',
  '面试官追问 {topic} 时，你会从哪些维度作答？',
  '请对 {topic} 做一次从概念到落地的完整说明。',
  '如果要讲清楚 {topic}，你会如何组织回答结构？',
  '请说明 {topic} 常见问题以及优化思路。',
  '请结合真实业务场景谈谈你对 {topic} 的理解。',
  '当面试官问到 {topic}，你会怎样给出有层次的回答？'
]

const projectVariants = [
  '请结合一个真实项目，重点说明 {topic}。',
  '围绕 {topic}，请介绍你的项目背景、职责和成果。',
  '在项目经历题中，如果问到 {topic}，你会如何回答？',
  '请分享一个与你的 {topic} 相关的项目案例。',
  '面试官要求你展开讲 {topic}，你会如何组织内容？',
  '请说明你在项目中处理 {topic} 的思路和结果。',
  '如果围绕 {topic} 做项目复盘，你会重点讲哪些点？',
  '请从问题、方案、执行和结果四个角度谈谈 {topic}。',
  '请介绍你在项目里和 {topic} 相关的实践经验。',
  '请举例说明你在项目中如何完成 {topic}。'
]

const scenarioVariants = [
  '如果遇到 {topic}，你会如何排查和处理？',
  '面试官给出“{topic}”这个场景时，你会怎样应对？',
  '请说明 {topic} 的处理步骤和优先级。',
  '当线上出现 {topic}，你会怎么推进解决？',
  '如果业务现场发生 {topic}，你的排查思路是什么？',
  '请结合工程实践说明 {topic} 的解决方案。',
  '针对 {topic}，你会如何定位根因并验证修复？',
  '请谈谈 {topic} 这类问题的常见原因与改进策略。',
  '如果让你处理 {topic}，你会怎么安排操作顺序？',
  '面对 {topic}，你会怎样兼顾效率、风险和沟通？'
]

const behaviorVariants = [
  '当你遇到 {topic} 时，你通常怎么做？',
  '行为面试里如果问到 {topic}，你会如何回答？',
  '请分享一次你处理 {topic} 的经历。',
  '面对 {topic}，你会怎样与团队协作？',
  '如果工作中出现 {topic}，你的应对原则是什么？',
  '请结合真实经历谈谈你如何处理 {topic}。',
  '当面试官追问 {topic} 时，你会如何体现你的思考方式？',
  '围绕 {topic}，你会如何说明自己的沟通和决策能力？',
  '如果需要你复盘一次 {topic}，你会重点讲哪些内容？',
  '请说明你面对 {topic} 时的处理流程和结果。'
]

const moduleQuestionConfigs = {
  Web: {
    技术知识: [
      { topic: 'Vue3 响应式原理', focus: 'Proxy、依赖收集、触发更新和性能优化', answer: '回答时可以先讲 Vue2 的 Object.defineProperty 局限，再说明 Vue3 用 Proxy 拦截更多操作，最后补充 effect、track、trigger 这些核心机制以及它们对性能和可维护性的提升。' },
      { topic: '虚拟 DOM 和 Diff', focus: '虚拟节点、同层比较、key 的作用和更新代价', answer: '先解释虚拟 DOM 是对真实 DOM 的抽象，再说明 Diff 的目标是减少真实 DOM 操作；重点强调同层比较、key 对复用和稳定性的影响，以及在列表更新中的实际表现。' },
      { topic: '浏览器缓存机制', focus: '强缓存、协商缓存、Cache-Control 和 ETag', answer: '可以按“是否发请求”来区分强缓存和协商缓存，说明 Cache-Control、Expires、ETag、Last-Modified 的作用，并补充静态资源版本控制的实践。' },
      { topic: 'Event Loop', focus: '宏任务、微任务、渲染时机和异步队列', answer: '先定义调用栈和任务队列，再区分宏任务与微任务，最后结合 Promise、setTimeout、DOM 渲染和 requestAnimationFrame 举例，让回答更落地。' },
      { topic: 'CSS 布局体系', focus: 'Flex、Grid、定位和响应式布局', answer: '建议从常见布局方式的适用场景切入，说明 Flex 适合一维布局、Grid 适合二维布局，再补充定位、媒体查询和移动端适配。' },
      { topic: '跨域与 CORS', focus: '同源策略、预检请求、代理转发和安全性', answer: '回答时先解释同源策略，再说明 CORS 响应头和预检请求，最后补充前端代理、Nginx 转发、JSONP 历史方案以及安全边界。' },
      { topic: 'HTTP 与 HTTPS', focus: '三次握手、TLS、状态码和常见请求头', answer: '可以先说明 HTTP 报文结构和常见状态码，再讲 HTTPS 在加密、证书和身份校验上的增强，以及它对安全和性能的影响。' },
      { topic: '前端性能优化', focus: '首屏加载、资源体积、长任务和渲染性能', answer: '建议从资源优化、渲染优化和工程优化三层展开，提到压缩、懒加载、缓存、虚拟列表、代码分包和性能监控，形成完整回答结构。' },
      { topic: '前端工程化', focus: '构建、代码规范、CI/CD 和环境管理', answer: '可以从构建工具、代码质量、自动化测试和发布流程讲起，说明工程化的目标是提升协作效率、可维护性和交付稳定性。' },
      { topic: '状态管理设计', focus: '局部状态、全局状态、数据流和副作用管理', answer: '回答时要说明什么状态适合本地组件、什么状态应放在全局仓库，再补充模块拆分、异步状态流转和副作用处理方式。' }
    ],
    项目经历: [
      { topic: '项目背景与业务价值', focus: '业务目标、用户对象和上线成果', answer: '建议按 STAR 结构回答，先交代项目目标和场景，再说你的职责，重点讲一个高价值成果，例如转化率、性能指标或交付效率的提升。' },
      { topic: '组件设计与复用', focus: '组件边界、抽象能力和维护成本', answer: '可以从组件拆分原则讲起，再说明如何沉淀通用组件、业务组件和 hooks，以及如何避免过度抽象。' },
      { topic: '权限系统设计', focus: '路由权限、按钮权限和角色控制', answer: '回答时建议说明权限模型、前后端职责分工、动态路由和异常兜底处理，并补充如何防止仅靠前端控制的风险。' },
      { topic: '大型表单或配置平台', focus: '动态渲染、校验规则和用户体验', answer: '先讲场景复杂度，再说明如何做表单 schema、联动校验、性能优化和保存草稿等能力，体现系统化设计。' },
      { topic: '数据可视化项目', focus: '大屏渲染、图表性能和交互体验', answer: '可以描述图表库选型、数据刷新策略、屏幕适配、动画节奏和性能优化，重点讲你解决了什么难点。' },
      { topic: '前端监控与埋点', focus: '错误监控、性能采集和数据分析闭环', answer: '建议说明埋点规范、异常收集、性能指标和看板应用，再讲监控数据如何反过来推动优化。' },
      { topic: '微前端实践', focus: '拆分边界、主子应用通信和样式隔离', answer: '回答时可说明为什么要做微前端、如何做应用拆分、路由通信、共享依赖和隔离策略，并客观看待其成本。' },
      { topic: '首屏性能优化项目', focus: '指标定义、优化方案和收益验证', answer: '重点讲如何确定 LCP、FCP 等指标，采取了哪些优化措施，以及最终是否通过监控验证了优化收益。' },
      { topic: '复杂联调问题处理', focus: '接口规范、Mock、灰度验证和日志排查', answer: '可以说明你如何与后端统一协议、怎样利用 Mock 或测试环境推进联调，以及出现问题时如何快速定位。' },
      { topic: '低代码或搭建平台', focus: 'DSL、配置驱动和渲染引擎', answer: '回答时可重点讲配置结构设计、组件映射、渲染性能和扩展性，体现架构思维。' }
    ],
    场景题: [
      { topic: '页面白屏', focus: '控制台报错、资源加载、路由与环境变量', answer: '可以按“先看报错，再看网络，再看环境和发布内容”的顺序排查，强调 source map、监控告警和灰度回滚的重要性。' },
      { topic: '大列表卡顿', focus: '渲染数量、虚拟列表、分页和缓存', answer: '应先确认卡顿发生在渲染还是数据处理，再采用虚拟列表、分片渲染、分页加载和减少不必要响应式开销等方案。' },
      { topic: '首屏加载过慢', focus: '包体积、关键资源、接口串行和渲染阻塞', answer: '可以从资源分析入手，检查 chunk、图片、字体和接口依赖，再通过懒加载、预加载、SSR/骨架屏等方式优化体验。' },
      { topic: '线上 JavaScript 报错飙升', focus: '错误聚合、版本定位和热修复策略', answer: '先通过监控平台确认版本和影响范围，再定位具体代码路径，必要时回滚或热修复，并补充如何防止同类问题再次出现。' },
      { topic: '接口偶发超时', focus: '前端重试、超时提示和降级体验', answer: '回答时要兼顾技术和用户体验，说明超时策略、重试次数、幂等要求、兜底提示和日志上报。' },
      { topic: '登录态频繁失效', focus: 'token 刷新、过期处理和多端一致性', answer: '建议说明 token 生命周期、续签机制、并发刷新问题和退出登录兜底处理。' },
      { topic: '跨域请求失败', focus: '预检请求、代理配置和后端响应头', answer: '可从请求头、Method、代理与后端 CORS 设置三方面定位，并说明生产环境通常通过网关或 Nginx 统一处理。' },
      { topic: '样式在不同浏览器不一致', focus: '兼容性、标准化和回归验证', answer: '回答时可提到浏览器兼容矩阵、CSS reset、自动前缀、功能降级和针对性回归测试。' },
      { topic: '打包后资源 404', focus: 'publicPath、部署路径和缓存刷新', answer: '重点说明静态资源路径、路由 history 模式、Nginx 配置和 CDN 缓存造成的影响。' },
      { topic: '页面交互抖动或闪烁', focus: '重复渲染、异步状态切换和骨架屏策略', answer: '可以从状态更新频率、动画与重绘、接口数据切换和占位策略来分析，给出减少闪烁的具体措施。' }
    ],
    行为题: [
      { topic: '需求频繁变更', focus: '优先级判断、影响评估和沟通机制', answer: '建议说明你如何先确认变更背景，再量化影响、同步风险和更新时间，并通过评审文档减少重复沟通。' },
      { topic: '与产品有分歧', focus: '目标对齐、方案比较和结果导向', answer: '回答时应体现你不是直接反对，而是基于用户价值、开发成本和上线风险给出替代方案。' },
      { topic: '与设计稿存在实现冲突', focus: '体验、成本与技术约束平衡', answer: '可以强调先理解设计目标，再说明实现限制，最后通过降级方案或阶段性方案达成一致。' },
      { topic: '和后端联调出现争议', focus: '接口文档、日志证据和协作效率', answer: '重点体现你会用接口文档、请求样例和日志数据说话，而不是单纯口头争执。' },
      { topic: '排期被压缩', focus: '任务拆分、风险管理和优先级控制', answer: '回答时可以说明如何区分必须做和可延后项，并及时同步风险，确保团队对结果有一致预期。' },
      { topic: '带教新人', focus: '知识传递、代码评审和成长反馈', answer: '建议强调方法论，比如任务拆解、结对编程、review 反馈和逐步放权，而不是单纯替对方做完。' },
      { topic: '线上事故复盘', focus: '问题复现、责任边界和改进闭环', answer: '优秀回答应体现你关注根因和机制优化，而不是甩锅，最后落到流程、监控或测试补强。' },
      { topic: '技术方案推进受阻', focus: '收益证明、试点验证和共识建立', answer: '可以讲先做小范围试点，用数据证明收益，再逐步推进，而不是一开始就要求全量切换。' },
      { topic: '跨团队协作', focus: '目标拆解、同步机制和预期管理', answer: '重点体现你如何把口头沟通变成清晰的任务和时间点，并在过程中持续同步风险。' },
      { topic: '高压下保证交付质量', focus: '节奏管理、关键路径和兜底方案', answer: '回答时应强调在高压环境下更需要主次分明、风险透明和质量底线，而不是盲目加班。' }
    ]
  },
  Java: {
    技术知识: [
      { topic: 'HashMap 与 ConcurrentHashMap', focus: '线程安全、底层实现和使用场景', answer: '建议先讲 HashMap 的非线程安全，再说明 ConcurrentHashMap 在不同 JDK 版本中的实现演进，以及为什么它更适合高并发读写场景。' },
      { topic: 'Spring Bean 生命周期', focus: '实例化、注入、初始化和销毁', answer: '回答时可按容器启动时间线说明，并补充 BeanPostProcessor、Aware 接口和初始化回调，让回答更完整。' },
      { topic: '事务传播行为', focus: 'REQUIRED、REQUIRES_NEW、NESTED 和回滚边界', answer: '重点解释传播行为决定方法之间如何共享事务，再结合真实业务说明不同传播行为的风险和使用场景。' },
      { topic: 'JVM 内存模型', focus: '堆、栈、方法区和对象生命周期', answer: '建议从线程私有与线程共享内存区切入，再补充对象创建、垃圾回收和常见内存溢出问题。' },
      { topic: '垃圾回收机制', focus: '可达性分析、GC 算法和调优思路', answer: '回答时可以先说明对象何时可回收，再讲常见算法和收集器，最后补充生产环境如何通过日志和监控调优。' },
      { topic: '线程池设计', focus: '核心参数、拒绝策略和使用风险', answer: '要说明 corePoolSize、maximumPoolSize、队列和拒绝策略之间的关系，并提醒线程池不能只会背参数，更要结合业务负载选型。' },
      { topic: 'MySQL 索引与查询优化', focus: '索引结构、覆盖索引和慢 SQL 排查', answer: '建议从 B+ 树原理切入，再说明联合索引最左匹配、回表、覆盖索引和 explain 的使用。' },
      { topic: 'Redis 使用场景', focus: '缓存、分布式锁、过期策略和一致性', answer: '回答时可以区分高频读缓存、排行榜、分布式协调等场景，并说明缓存穿透、击穿和雪崩的防护方案。' },
      { topic: '消息队列原理', focus: '削峰、异步解耦、重试和幂等', answer: '先说明为什么引入消息队列，再补充消费失败、重复消费和顺序性处理，让回答更贴近工程。' },
      { topic: '分布式系统一致性', focus: 'CAP、最终一致性和事务方案', answer: '回答时可以先讲 CAP 的约束，再说明常见的一致性策略，比如本地消息表、可靠消息和补偿机制。' }
    ],
    项目经历: [
      { topic: '系统架构设计', focus: '模块拆分、服务边界和演进过程', answer: '建议回答时突出你为什么这么拆、有哪些关键权衡，以及架构调整后带来的收益。' },
      { topic: '高并发接口优化', focus: '缓存、限流、异步化和数据库减压', answer: '可以围绕性能瓶颈、优化方案和结果指标来回答，让项目经历更有说服力。' },
      { topic: '数据库性能优化项目', focus: '慢 SQL、索引重构和读写分离', answer: '重点说明问题如何被发现、你采取了哪些方案，以及最终响应时间或吞吐量的改善。' },
      { topic: '缓存体系建设', focus: '热点数据、更新策略和一致性保障', answer: '可从缓存分层、TTL、主动失效和双删策略等实践来回答。' },
      { topic: '订单或支付系统', focus: '一致性、幂等和异常补偿', answer: '回答时可强调关键交易链路中的一致性控制，以及如何处理重复请求和失败回滚。' },
      { topic: '权限与用户体系', focus: '认证、鉴权、角色模型和数据隔离', answer: '建议说明模型设计、接口权限校验和后台管理能力，并结合实际业务举例。' },
      { topic: '日志监控平台接入', focus: '链路追踪、指标监控和问题定位效率', answer: '可以说明埋点、traceId、报警规则和问题发现效率提升，让项目更有工程味道。' },
      { topic: '接口治理与网关实践', focus: '限流、熔断、灰度和统一鉴权', answer: '可从网关为什么存在、承担什么职责以及上线效果来回答。' },
      { topic: '重构历史系统', focus: '技术债清理、稳定迁移和风险控制', answer: '重点讲如何保证业务连续性、怎么分阶段替换，以及如何降低重构风险。' },
      { topic: '数据报表或 BI 系统', focus: '数据同步、批处理和查询性能', answer: '可以围绕报表查询慢、数据口径不一致等难点来讲解决方案。' }
    ],
    场景题: [
      { topic: '线上接口响应变慢', focus: '监控、线程池、SQL 和缓存排查', answer: '建议按“先看监控全局，再看单点瓶颈”的顺序分析，快速判断是应用、数据库还是外部依赖问题。' },
      { topic: 'Redis 缓存穿透', focus: '空值缓存、布隆过滤器和热点保护', answer: '回答时先定义问题，再补充常见治理方案，并说明不同方案的适用边界。' },
      { topic: '数据库死锁', focus: '事务顺序、锁粒度和重试机制', answer: '应说明如何借助日志和数据库监控定位死锁语句，并从代码和事务设计上减少再次发生。' },
      { topic: '消息重复消费', focus: '幂等设计、状态机和唯一键约束', answer: '关键是把“重复处理不会出错”讲清楚，可以用业务唯一号、去重表或幂等状态控制来落地。' },
      { topic: '服务雪崩', focus: '降级、熔断、限流和依赖隔离', answer: '回答要体现系统性思考，先止损，再定位，再补充架构层面的预防措施。' },
      { topic: 'JVM 内存溢出', focus: '堆 dump、GC 日志和对象分析', answer: '可以说明如何先收集现场证据，再通过分析工具定位问题对象，并结合代码或缓存策略修复。' },
      { topic: '接口幂等失效', focus: '重复提交、重试请求和唯一约束', answer: '建议从入口防重、业务防重和数据层防重三层说明。' },
      { topic: '发布后大量 500 错误', focus: '快速回滚、日志定位和灰度机制', answer: '回答时可强调先恢复服务，再定位具体原因，最后通过灰度和发布检查项降低再次发生概率。' },
      { topic: '慢 SQL 激增', focus: '索引失效、执行计划和流量变化', answer: '重点说明如何通过 explain、慢日志和业务访问模式分析原因，并制定短期和长期优化方案。' },
      { topic: '分布式事务失败', focus: '补偿机制、消息可靠性和状态一致性', answer: '可以从最终一致性思路出发，说明失败后的补偿、重试和人工兜底流程。' }
    ],
    行为题: [
      { topic: '和产品讨论需求优先级', focus: '业务价值、开发成本和上线风险', answer: '好的回答应体现你不是机械执行，而是基于业务目标给出专业判断，同时尊重决策流程。' },
      { topic: '和前端联调分歧', focus: '接口契约、日志证据和协同效率', answer: '要体现你善于用事实和文档推动协作，而不是陷入互相指责。' },
      { topic: '高风险需求压期上线', focus: '风险透明、兜底方案和责任边界', answer: '建议说明你如何把风险说清楚、给出替代方案，并协助团队做出可落地决定。' },
      { topic: '推动代码规范落地', focus: '示范、自动化和团队共识', answer: '重点体现“机制化”而不是“口头要求”，例如通过 lint、review 和模板沉淀推动执行。' },
      { topic: '带新人做后端开发', focus: '任务拆分、知识传递和成长节奏', answer: '回答时可以说明如何从小任务开始、通过 review 提升质量，并逐步让新人独立负责模块。' },
      { topic: '线上事故责任压力', focus: '应急、复盘和机制改进', answer: '好的回答不应只强调情绪，而要体现你在压力下仍然能快速组织信息、推动修复并完善机制。' },
      { topic: '技术方案被质疑', focus: '收益证明、试点验证和持续沟通', answer: '建议强调先做验证，再用数据和结果说服团队，而不是一开始强推。' },
      { topic: '跨团队推进项目', focus: '目标拆解、同步机制和依赖管理', answer: '回答时应体现你有项目推进意识，知道如何识别阻塞项并持续跟进。' },
      { topic: '面对不熟悉的业务领域', focus: '快速学习、业务抽象和风险识别', answer: '重点体现你的学习路径和拆解能力，以及你如何在陌生领域中降低试错成本。' },
      { topic: '平衡稳定性和开发效率', focus: '技术债、交付节奏和质量底线', answer: '优秀回答需要体现取舍能力，说明哪些事情可以延期，哪些质量底线不能退。' }
    ]
  },
  Python: {
    技术知识: [
      { topic: '深拷贝与浅拷贝', focus: '引用关系、可变对象和 copy 模块', answer: '建议从对象引用说起，再解释浅拷贝只复制一层、深拷贝递归复制全部嵌套结构，并举列表或字典例子。' },
      { topic: '常见排序与查找算法', focus: '时间复杂度、空间复杂度和适用场景', answer: '回答时可以按“冒泡/快排/归并/二分”组织，并强调不同算法的复杂度和数据特性要求。' },
      { topic: '生成器与迭代器', focus: '迭代协议、yield 和节省内存', answer: '先说明迭代器协议，再解释生成器是特殊迭代器，通过 yield 按需产生数据，非常适合大数据量流式处理。' },
      { topic: '多进程与多线程', focus: 'GIL、IO 密集和 CPU 密集', answer: '回答要讲清楚 GIL 对并发模型的影响，说明 IO 密集适合线程，CPU 密集更适合多进程或借助其他技术方案。' },
      { topic: '装饰器原理', focus: '闭包、函数包装和场景应用', answer: '可以从函数是一等公民切入，再讲闭包如何保存上下文，最后结合日志、鉴权和缓存装饰器举例。' },
      { topic: 'NumPy 与 Pandas 基础', focus: '向量化、DataFrame 和性能优势', answer: '重点说明为什么它们比纯 Python 循环更高效，以及在数据处理中的典型使用方式。' },
      { topic: '机器学习模型评估', focus: '准确率、召回率、F1 和 AUC', answer: '建议解释不同指标适合什么场景，特别是类别不平衡时为什么不能只看准确率。' },
      { topic: '特征工程方法', focus: '缺失值处理、编码、标准化和特征选择', answer: '回答时可强调数据清洗、变量变换和特征筛选的重要性，并说明这些步骤如何影响模型效果。' },
      { topic: '深度学习训练流程', focus: '数据集划分、损失函数和优化器', answer: '可以从数据准备、模型搭建、训练验证、调参与部署几个阶段组织回答。' },
      { topic: 'Python 性能优化', focus: '算法复杂度、向量化、缓存和剖析工具', answer: '重点说明先定位瓶颈，再有针对性优化，例如减少 Python 层循环、利用缓存或采用更高效的数据结构。' }
    ],
    项目经历: [
      { topic: '算法建模项目', focus: '问题定义、特征设计和指标结果', answer: '建议从业务问题切入，再说明你如何构建特征、选择模型和验证效果，最后用结果数据收尾。' },
      { topic: '数据清洗项目', focus: '脏数据处理、口径统一和质量控制', answer: '可重点描述数据来源复杂度、清洗规则设计和如何保证结果可复现。' },
      { topic: '推荐系统实践', focus: '召回、排序和离线在线评估', answer: '回答时可以从架构流程、特征设计和效果指标展开，体现系统性思考。' },
      { topic: '风控或预测模型项目', focus: '标签定义、样本平衡和特征解释', answer: '重点说明业务目标、样本构建、模型选择以及如何让业务理解模型结论。' },
      { topic: '自动化脚本平台', focus: '效率提升、调度执行和异常处理', answer: '可以讲脚本如何替代重复劳动、如何调度、记录日志和处理失败重试。' },
      { topic: '数据可视化分析项目', focus: '指标体系、图表设计和业务洞察', answer: '建议说明数据怎么来、图表怎么选、最终帮助业务做了什么决策。' },
      { topic: 'NLP 项目实践', focus: '文本预处理、向量化和效果评估', answer: '可从语料处理、模型或特征选择以及线上落地效果来回答。' },
      { topic: '爬虫与数据采集项目', focus: '反爬处理、数据质量和调度', answer: '回答时要兼顾技术实现与合法合规边界，说明如何保障采集稳定性和数据质量。' },
      { topic: '模型部署与服务化', focus: '接口封装、性能和版本管理', answer: '重点说明如何把离线模型变成可调用服务，以及如何做灰度和监控。' },
      { topic: '实验平台或训练流水线', focus: '参数管理、结果追踪和复现实验', answer: '建议强调工程能力，说明如何让模型迭代更可控、可复现、可协作。' }
    ],
    场景题: [
      { topic: '模型效果不达预期', focus: '数据、特征、模型和误差分析', answer: '可以按“先查数据，再看特征，再调模型，最后做误差分析”的顺序回答，体现系统排查能力。' },
      { topic: '线上数据漂移', focus: '监控、告警、回滚和重训策略', answer: '重点说明如何监控输入分布和效果指标，一旦发现漂移如何做回滚、规则兜底或重新训练。' },
      { topic: '训练耗时过长', focus: '采样、并行化、特征优化和资源利用', answer: '回答时可以先定位瓶颈，再说明是否通过减少无效特征、向量化和分布式训练来优化。' },
      { topic: '数据缺失严重', focus: '缺失模式、补全策略和业务含义', answer: '要体现你不会机械填补，而是先区分随机缺失和业务缺失，再选择删除、填充或建模处理。' },
      { topic: '模型过拟合', focus: '正则化、交叉验证和特征约束', answer: '建议从训练集和验证集表现差异切入，再说明常见缓解手段及其适用条件。' },
      { topic: '线上接口延迟过高', focus: '模型简化、缓存和推理加速', answer: '可以讲如何在效果和性能之间做平衡，必要时采取模型蒸馏、特征缓存或异步计算。' },
      { topic: '异常值很多', focus: '检测方法、业务判断和数据修正', answer: '回答时应体现统计方法和业务理解并重，说明异常值不能一刀切删除。' },
      { topic: '结果和业务预期冲突', focus: '解释性、验证实验和沟通方式', answer: '重点说明如何先确认数据和模型可靠，再向业务解释假设与局限，并提出验证方案。' },
      { topic: '脚本线上失败', focus: '日志、重试、幂等和告警机制', answer: '可以从可观测性和恢复能力两个维度说明，体现工程思维。' },
      { topic: '特征泄漏', focus: '特征来源、时间穿越和验证方法', answer: '建议解释为什么泄漏会让离线指标虚高，以及如何通过特征审查和时间切分避免。' }
    ],
    行为题: [
      { topic: '和业务沟通模型结论', focus: '解释性、可信度和可执行建议', answer: '回答要体现你能把技术结果翻译成业务语言，并给出下一步行动建议，而不是只汇报指标。' },
      { topic: '面对不确定性需求', focus: '快速验证、分阶段交付和预期管理', answer: '建议说明你如何先做最小验证，再逐步扩大投入，避免一次性重成本试错。' },
      { topic: '与同事对模型路线有分歧', focus: '实验对比、数据说话和协作共识', answer: '优秀回答应强调用实验和指标对比，而不是凭经验拍板。' },
      { topic: '数据质量不理想但时间紧', focus: '关键指标优先和风险透明', answer: '重点体现你会先保障核心链路，再向团队透明同步剩余风险和限制。' },
      { topic: '跨团队推进分析项目', focus: '数据口径、责任边界和同步节奏', answer: '回答时要体现你如何推动不同团队对齐口径、共享数据和形成闭环。' },
      { topic: '带新人做数据分析', focus: '思路训练、工具使用和复盘反馈', answer: '建议强调培养分析框架和业务理解，而不仅是教命令和工具。' },
      { topic: '结果被质疑', focus: '证据链、假设边界和复核机制', answer: '要体现你欢迎质疑并善于建立证据链，通过复核和实验提升结论可信度。' },
      { topic: '上线后效果不稳定', focus: '监控、复盘和持续迭代', answer: '可以强调你会建立效果追踪机制，快速识别问题并迭代，而不是上线后就不再关注。' },
      { topic: '高压下处理分析需求', focus: '优先级、自动化和交付节奏', answer: '回答时体现你会先抓关键问题，同时通过自动化和模板化提高效率。' },
      { topic: '陌生领域快速学习', focus: '业务拆解、资料收集和验证闭环', answer: '好的回答应该体现学习不是被动读资料，而是结合问题、数据和反馈快速形成理解。' }
    ]
  },
  Test: {
    技术知识: [
      { topic: '登录功能测试用例设计', focus: '正常流程、异常流程、边界值和安全性', answer: '回答时建议从功能、异常、边界、安全和兼容五个维度展开，体现测试覆盖思维。' },
      { topic: '接口测试与 UI 测试', focus: '定位差异、维护成本和适用场景', answer: '可以说明接口测试更稳定高效，UI 测试更贴近用户路径，两者需要组合使用而不是替代关系。' },
      { topic: '回归测试策略', focus: '触发时机、范围选择和风险优先级', answer: '建议从版本迭代、缺陷修复和高风险模块变更出发，说明如何确定回归范围。' },
      { topic: '缺陷生命周期', focus: '提交、跟踪、验证和关闭标准', answer: '回答时要强调缺陷管理不仅是提单，更包括优先级评估、沟通跟进和结果验证。' },
      { topic: '自动化测试框架', focus: '分层设计、脚本维护和执行报告', answer: '可以从用例分层、数据驱动、日志截图和持续集成接入几个方面回答。' },
      { topic: '性能测试核心指标', focus: 'QPS、RT、吞吐量和资源利用率', answer: '建议解释指标含义、测试场景设计和结果分析方式，而不是只背名词。' },
      { topic: 'SQL 与数据校验', focus: '查询验证、数据比对和口径一致性', answer: '回答时可说明如何通过 SQL 验证接口结果、报表数据和批处理结果。' },
      { topic: 'Linux 常用排查命令', focus: '日志查看、进程监控和网络诊断', answer: '可以结合 tail、grep、top、ps、netstat 或 ss 等命令谈排查思路。' },
      { topic: '接口自动化实践', focus: '鉴权、依赖管理和环境切换', answer: '重点说明参数化、断言设计、公共方法封装和环境稳定性处理。' },
      { topic: '安全测试基础', focus: '输入校验、鉴权绕过和敏感信息保护', answer: '建议说明最常见的安全测试点，例如越权、注入、弱口令和敏感信息暴露。' }
    ],
    项目经历: [
      { topic: '核心业务测试项目', focus: '测试范围、策略制定和风险控制', answer: '建议从项目背景、测试对象、测试重点和发现的关键问题来讲，突出你的价值。' },
      { topic: '自动化落地项目', focus: '选型、覆盖范围和维护收益', answer: '回答时可以先讲为什么做自动化，再讲从哪些场景切入，以及最终给团队带来了什么提升。' },
      { topic: '性能测试项目', focus: '场景构建、压测执行和结果分析', answer: '重点说明你如何设计用户模型、发现瓶颈并推动开发一起解决问题。' },
      { topic: '接口测试平台建设', focus: '用例管理、执行能力和报告沉淀', answer: '可以描述平台化建设如何提升执行效率、复用能力和可视化程度。' },
      { topic: '线上质量治理项目', focus: '缺陷复盘、指标看板和流程优化', answer: '建议说明你如何从个别缺陷出发，沉淀出可持续的质量治理机制。' },
      { topic: '兼容性测试项目', focus: '设备矩阵、浏览器版本和回归成本', answer: '回答时可说明如何选择关键设备组合以及如何平衡覆盖率与测试成本。' },
      { topic: '数据校验项目', focus: '批处理结果、报表口径和比对机制', answer: '重点体现你在复杂数据链路中的验证思路和工具使用能力。' },
      { topic: '缺陷管理与推动', focus: '缺陷分级、优先级和协同修复', answer: '可以讲你如何确保关键问题被及时处理，而不是提了 bug 就结束。' },
      { topic: '测试流程优化', focus: '模板化、标准化和质量门禁', answer: '建议突出你如何把经验沉淀为流程或规范，提升团队整体效率。' },
      { topic: '发布前质量保障', focus: '冒烟、回归和上线检查清单', answer: '回答时可以说明你如何组织发布前验证，降低线上风险。' }
    ],
    场景题: [
      { topic: '线上严重缺陷', focus: '问题分级、复现、回归和复盘', answer: '可以按“先止损、再定位、再验证、最后复盘”的顺序回答，体现应急处理能力。' },
      { topic: '测试环境与生产环境不一致', focus: '差异识别、风险评估和补充验证', answer: '回答时要体现你能识别关键差异，并通过预发、灰度和监控来弥补环境差距。' },
      { topic: '开发认为不是 bug', focus: '需求对齐、证据准备和沟通方式', answer: '重点体现你会用需求、日志、复现步骤和影响范围说话，推动团队达成一致。' },
      { topic: '自动化脚本大面积失败', focus: '环境依赖、公共模块和排查优先级', answer: '应先判断是环境问题还是业务变更，再从公共能力层开始排查，快速缩小范围。' },
      { topic: '性能压测结果异常', focus: '压测模型、监控指标和瓶颈定位', answer: '建议说明如何确认压测场景是否合理，再通过应用、数据库和系统指标综合分析。' },
      { topic: '接口结果偶发错误', focus: '数据依赖、并发条件和日志追踪', answer: '可以说明如何做重复验证、加日志、保留上下文并与开发一起定位。' },
      { topic: '需求临时变更临近上线', focus: '回归范围、风险评估和优先级判断', answer: '回答时要体现测试不是简单说“来不及”，而是要快速识别关键路径并同步风险。' },
      { topic: '批量数据校验不一致', focus: '抽样策略、SQL 比对和链路分析', answer: '重点说明如何从数据源、处理中间层和展示层逐步定位问题。' },
      { topic: '用户反馈问题无法复现', focus: '信息收集、日志比对和环境重建', answer: '可以讲你如何收集版本、设备、时间点、操作路径，并借助日志和监控重建现场。' },
      { topic: '上线后回滚决策', focus: '影响范围、修复成本和业务时机', answer: '要体现你能参与风险判断，帮助团队基于事实选择回滚、热修或临时降级。' }
    ],
    行为题: [
      { topic: '与开发沟通缺陷', focus: '事实表达、优先级判断和协作氛围', answer: '回答时应体现你既坚持质量标准，也尊重协作关系，通过事实推动问题解决。' },
      { topic: '测试时间不足', focus: '风险取舍、主流程优先和透明沟通', answer: '建议说明你会先保核心链路，再把剩余风险显式同步，让团队共同承担决策。' },
      { topic: '推动自动化改进', focus: '试点切入、收益证明和持续维护', answer: '重点体现你会先从高收益场景试点，再通过结果推动团队接受，而不是空谈自动化价值。' },
      { topic: '带新人熟悉测试工作', focus: '方法训练、用例思维和反馈机制', answer: '可以说明如何先教框架和思路，再通过实战和 review 帮助对方快速成长。' },
      { topic: '面对重复性工作', focus: '自动化意识和流程优化', answer: '好的回答应体现你不只是抱怨重复，而是主动思考如何沉淀工具和模板。' },
      { topic: '跨团队推进质量问题', focus: '事实依据、同步节奏和责任闭环', answer: '建议强调如何通过数据和风险等级争取资源，而不是单纯“提醒别人注意”。' },
      { topic: '线上事故后的复盘', focus: '根因分析、流程改进和预防机制', answer: '回答时可说明你如何从具体事故提炼出测试、流程或监控层面的改进措施。' },
      { topic: '需求描述不清晰', focus: '澄清问题、补充场景和提前暴露风险', answer: '重点体现你会主动补充边界条件和异常场景，而不是等开发完成后再被动测试。' },
      { topic: '坚持质量底线', focus: '风险认知、沟通勇气和替代方案', answer: '可以说明在关键质量问题上你会坚持原则，但同时给出可执行的折中方案。' },
      { topic: '高压发布节奏', focus: '节奏管理、检查清单和协作效率', answer: '优秀回答应体现你在高压场景下更依赖流程化和标准化，而不是完全靠个人硬扛。' }
    ]
  }
}

const variantMap = {
  技术知识: knowledgeVariants,
  项目经历: projectVariants,
  场景题: scenarioVariants,
  行为题: behaviorVariants
}

const category = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const detailVisible = ref(false)
const selectedQuestion = ref(null)

const currentApi = computed(() => apiMap[props.module])

function goBackToBank() {
  router.push({ path: '/ai-interview', query: { view: 'question', tab: 'bank' } })
}

function openDetail(row) {
  selectedQuestion.value = row
  detailVisible.value = true
}

function buildGeneratedQuestions(module) {
  const config = moduleQuestionConfigs[module] || {}

  return Object.entries(config).flatMap(([questionCategory, topics]) => {
    const variants = variantMap[questionCategory] || []
    return topics.flatMap((topicItem, topicIndex) =>
      variants.map((variant, variantIndex) => ({
        id: `${module}-${questionCategory}-${topicIndex + 1}-${variantIndex + 1}`,
        title: variant.replace('{topic}', topicItem.topic),
        category: questionCategory,
        content: `请重点围绕 ${topicItem.focus} 展开，尽量结合真实经历或具体工程细节，不要只停留在概念定义。`,
        answer: `参考回答：可以先说明你对“${topicItem.topic}”的定义和背景，再结合 ${topicItem.focus} 分层展开。${topicItem.answer} 最后补充一个实际项目或工作场景中的例子，会让回答更完整、更像真实面试表现。`
      }))
    )
  })
}

function addDisplayIds(items) {
  return items.map((item, index) => ({
    ...item,
    displayId: (page.value - 1) * pageSize.value + index + 1
  }))
}

function getFallbackData() {
  const source = buildGeneratedQuestions(props.module)
  const filtered = category.value
    ? source.filter(item => item.category === category.value)
    : source

  const start = (page.value - 1) * pageSize.value
  const end = start + pageSize.value

  list.value = addDisplayIds(filtered.slice(start, end))
  total.value = filtered.length
}

async function fetchData() {
  if (!currentApi.value) {
    getFallbackData()
    return
  }

  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }
    if (category.value) {
      params.category = category.value
    }

    const data = await currentApi.value(params)
    if (data?.list?.length) {
      list.value = addDisplayIds(
        data.list.map(item => ({
          ...item,
          answer: item.answer || item.referenceAnswer || '当前接口暂未返回参考答案，已使用详情面板承接后续扩展。'
        }))
      )
      total.value = data.total || data.list.length
    } else {
      getFallbackData()
    }
  } catch (error) {
    console.error('获取题目列表失败', error)
    getFallbackData()
  } finally {
    loading.value = false
  }
}

function handleCategoryChange() {
  page.value = 1
  fetchData()
}

function handleSizeChange(size) {
  pageSize.value = size
  page.value = 1
  fetchData()
}

function handleCurrentChange(current) {
  page.value = current
  fetchData()
}

watch(
  () => props.module,
  () => {
    category.value = ''
    page.value = 1
    fetchData()
  },
  { immediate: true }
)
</script>

<style scoped>
.question-list-container {
  padding: 20px;
  color: #f6f8ff;
}

.page-header {
  margin-bottom: 20px;
  text-align: left;
}

.header-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 8px;
  color: #ffffff;
}

.page-header p {
  margin: 0;
  color: rgba(223, 230, 255, 0.74);
  line-height: 1.6;
}

.back-btn {
  flex-shrink: 0;
  border-radius: 999px;
}

.filter-bar {
  display: flex;
  justify-content: flex-start;
}

.empty-state {
  margin-top: 16px;
  padding: 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(223, 230, 255, 0.72);
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-body {
  display: grid;
  gap: 18px;
}

.detail-tag-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-tag {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(101, 115, 255, 0.16);
  color: #dce2ff;
  font-size: 13px;
}

.detail-tag-light {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(223, 230, 255, 0.72);
}

.detail-section h3 {
  margin: 0 0 10px;
  color: #ffffff;
}

.detail-section p {
  margin: 0;
  line-height: 1.8;
  color: rgba(223, 230, 255, 0.76);
  white-space: pre-wrap;
}

.answer-text {
  background: rgba(255, 255, 255, 0.07);
  border-radius: 14px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

@media (max-width: 900px) {
  .header-top {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
