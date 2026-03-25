
![Vue](https://img.shields.io/badge/Vue.js-3.0-4FC08D?style=flat-square&logo=vue.js) ![Vite](https://img.shields.io/badge/Vite-4.0-646CFF?style=flat-square&logo=vite) ![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-3.0-38B2AC?style=flat-square&logo=tailwind-css) ![SpringBoot](https://img.shields.io/badge/Spring_Boot-2.x-6DB33F?style=flat-square&logo=spring-boot) ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql) ![Redis](https://img.shields.io/badge/Redis-6.0-DC382D?style=flat-square&logo=redis)

DataBox 是一个网盘。

---
## ✨ 核心特性
- ⚡ **高性能文件引擎**：支持大文件**分片上传、断点续传**；引入 MD5 算法实现**文件秒传**。 
- 📖 **全格式沉浸式预览**：支持视频（画中画/HLS）、流式音频解析、代码高亮；引入 LibreOffice 引擎，实现复杂 Office 文档的**高保真服务端 PDF 转换预览**。 
- 🤖 **AI Copilot (智能摘要)**：集成大语言模型，针对长文档一键生成智能摘要，并具备平滑的每日额度实时扣减反馈。
- 🚀 **极致优化的批量操作**：重构 MyBatis N+1 查询问题，千级别文件批量收藏/删除实现**毫秒级无感 DOM 状态更新**。 
- 🛡️ **双轨制安全防刷网关**：分享违规举报系统采用 `用户ID状态机 + 游客IP24小时静默` 双轨拦截，配合 Redis 与滑动验证码，提高系统安全性。 
- 🔔 **高并发系统消息中心**：采用 Redis + MySQL 读写分离架构，轻松扛住高频红点未读数轮询。
---
## 🛠️ 技术栈

### 前端 (Frontend)
- **核心框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI 框架 / 样式**: Element-Plus + Tailwind CSS + Scss
- **第三方集成**: `vue-router`, `axios`, `pdf.js`, `aplayer`, `highlight.js`

### 后端 (Backend)
- **核心框架**: Java 8+ / Spring Boot 2.x
- **持久层 / 数据库**: MyBatis / MySQL 8.x
- **缓存 / 并发控制**: Redis
- **文档渲染引擎**: JODConverter + LibreOffice (Headless)
- **其他**: 切面 AOP (操作日志记录)、`IP2Region` (离线IP解析)、MD5 文件哈希计算

---

## 🚀 快速开始 (部署指南)

### 环境准备
在开始运行之前，请确保您的开发环境已安装以下软件：
- **Node.js**: v16+ (推荐 v18)
- **Java**: JDK 8 或 JDK 11
- **MySQL**: v8.0+
- **Redis**: v5.0+
- **[可选但强烈推荐] LibreOffice**: 用于 Office 文档的高保真在线预览 (请记住安装路径)。

---

### 1. 后端部署 (DataBox-server)

1. **初始化数据库**：
   - 登录您的 MySQL 数据库。
   - 执行项目根目录 sql/databox_init.sql 脚本，自动创建数据库和所有表结构。

2. **修改配置文件**：
   打开 `DataBox-server/src/main/resources/application.properties`，修改以下关键配置：
	- **DB_USERNAME**：你的数据库用户名
	- **DB_PASSWORD**：你的数据库密码
	- **MAIL_USERNAME**：配置用户发送邮箱验证码的邮箱
	- **MAIL_PASSWORD**：邮箱授权码
	- **REDIS_HOST**：Redis主机地址
	- **PROJECT_FOLDER**：用于存放云盘文件的目录
	- **ADMIN_EMAILS**：设置的管理员邮箱
	- **DEEPSEEK_API_KEY**：DeepSeek的API key
	- **OFFICE_HOME**：LibreOffice 实际安装路径
3. **启动后端**：
	- 在你的代码编辑器中打开 `DataBox-server` 目录。
	- 等待 Maven 下载依赖。
	- 运行主启动类`com.databox.DataBoxApplication.java`。
	- 默认端口为`7090`。
---
### 2. 前端部署 (Databox-front)
1. **进入前端目录并安装依赖**：
```
cd Databox-front
npm install
```
2. **配置反向代理 (解决跨域)**：
开发环境下，Vite 已配置好 proxy 代理。打开 `vite.config.js`，确保代理指向后端的真实地址：
```
proxy: {
    "/api": {
        target: "http://localhost:7090", // 指向 Spring Boot 后端端口
        changeOrigin: true,
        pathRewrite: {
            "^api": "/api"
        }
    }
}
```
3. **启动开发服务器**：
```
npm run dev
```
启动成功后，浏览器访问终端中输出的地址即可进入云盘首页。