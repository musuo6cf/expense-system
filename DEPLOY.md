# 部署文档

## 1. 环境要求

| 软件 | 最低版本 | 说明 |
|------|----------|------|
| JDK | 17 | Java 运行环境 |
| MySQL | 8.0 | 关系数据库 |
| Maven | 3.9 | 后端构建工具 |
| Node.js | 18 | 前端运行环境 |
| npm | 9 | 前端包管理器 |

**可选：**

| 软件 | 说明 |
|------|------|
| Redis | 缓存服务（当前版本默认不启用） |
| Nginx | 生产环境反向代理 |

**验证环境：**

```bash
java --version    # 应显示 17.x
mysql --version   # 应显示 8.x
node --version    # 应显示 v18+ 或 v20+
mvn --version     # 应显示 3.9+
```

---

## 2. 初始化数据库

### 方式一：命令行导入

```bash
mysql -u root -p < expense-server/src/main/resources/db/init.sql
```

### 方式二：MySQL 客户端

打开 MySQL 客户端（Navicat / DBeaver / MySQL Workbench），执行 `init.sql` 文件内容。

### 初始化内容

脚本会自动完成：
1. 创建数据库 `expense_db`（字符集 utf8mb4）
2. 创建 12 张业务表
3. 插入 4 个默认角色（ADMIN / EMPLOYEE / MANAGER / FINANCE）
4. 插入 10 条默认权限
5. 插入角色权限关联
6. 插入 4 个默认部门
7. 插入默认管理员账号

### 验证

```sql
USE expense_db;
SHOW TABLES;
SELECT username, real_name FROM sys_user;     -- 应显示 admin
SELECT role_name FROM sys_role;               -- 应显示 4 条
```

---

## 3. 启动后端

### 配置数据库连接

编辑 `expense-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root          # 改为你的 MySQL 用户名
    password: your_password # 改为你的 MySQL 密码
```

### 开发环境启动

```bash
cd expense-server
mvn spring-boot:run
```

启动成功日志：
```
Started ExpenseApplication in 2.xxx seconds
```

默认端口：**8080**，上下文路径：**/api**

### 打包部署

```bash
cd expense-server
mvn clean package -DskipTests
java -jar target/expense-server-1.0.0.jar
```

### 验证

```bash
curl http://localhost:8080/api/doc.html
```

---

## 4. 启动前端

### 安装依赖

```bash
cd expense-web
npm install
```

### 配置 API 地址

编辑 `vite.config.ts`，确认代理配置：

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
      changeOrigin: true
    }
  }
}
```

### 开发环境启动

```bash
npm run dev
```

默认端口：**3000**

### 生产构建

```bash
npm run build
```

构建产物在 `dist/` 目录，部署到 Nginx。

### 验证

浏览器访问 `http://localhost:3000`

---

## 5. 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | `http://localhost:3000` | Vue3 开发服务器 |
| 后端 API | `http://localhost:8080/api` | Spring Boot REST API |
| API 文档 | `http://localhost:8080/api/doc.html` | Knife4j 在线调试 |

---

## 6. 默认账号

| 用户名 | 密码 | 角色 | 权限范围 |
|--------|------|------|----------|
| `admin` | `admin123` | ADMIN | 全部功能 |

**首次登录后建议立即修改密码。** 路径：系统设置 → 个人中心 → 修改密码。

---

## 7. 常见问题

### Q1：后端启动报 "Access denied for user"

数据库连接用户名或密码错误。检查 `application.yml` 中的 `datasource.username` 和 `datasource.password`。

### Q2：后端启动报 "Unknown database 'expense_db'"

未执行初始化脚本。先运行 `init.sql` 创建数据库和表。

### Q3：后端启动报 "Unsupported character encoding"

JDBC URL 中的 `characterEncoding` 必须是 `UTF-8`（不能写 `utf8mb4`）。

### Q4：登录报 500 错误

检查 `init.sql` 中管理员账号的 BCrypt 密码哈希是否被正确插入。如果重新生成了密码哈希，需要同步更新 SQL 中的 INSERT 语句。

### Q5：前端请求报 CORS 错误

后端已配置 `CorsConfig` 允许跨域。确保前端代理配置正确，或确认 `application.yml` 中未禁用 CORS。

### Q6：前端页面空白

检查浏览器控制台是否有 JavaScript 错误。确认 `npm install` 成功完成，所有依赖安装正确。

### Q7：附件上传失败

确认 `expense-server` 运行目录下存在 `uploads/` 目录写权限。应用会自动创建 `uploads/yyyy/MM/` 子目录。

### Q8：Token 过期如何处理

Token 有效期 2 小时。过期后前端 Axios 拦截器会自动跳转到登录页，重新登录即可获取新 Token。

### Q9：npm install 报网络错误

国内用户建议配置 npm 镜像：
```bash
npm config set registry https://registry.npmmirror.com
```

### Q10：Maven 下载依赖慢

配置 Maven 阿里云镜像，编辑 `~/.m2/settings.xml`：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

---

## 8. 生产环境部署

### 架构

```
Client (Browser)
    │
    ↓
Nginx (80/443)
    ├── /api/*     → 反向代理到 Spring Boot :8080
    ├── /doc.html  → 反向代理到 Spring Boot :8080
    └── /*         → 静态文件 Vue3 dist/
```

### Nginx 配置

```nginx
server {
    listen       80;
    server_name  your-domain.com;

    # 前端静态文件
    root   /opt/expense-web/dist;
    index  index.html;

    # API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        client_max_body_size 20m;
    }

    # API 文档
    location /doc.html {
        proxy_pass http://127.0.0.1:8080/doc.html;
    }
    location /webjars/ {
        proxy_pass http://127.0.0.1:8080/webjars/;
    }
    location /v3/ {
        proxy_pass http://127.0.0.1:8080/v3/;
    }

    # Vue Router history 模式
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 生产启动脚本

**后端**（`start-server.sh`）：

```bash
#!/bin/bash
nohup java -jar -Xms256m -Xmx512m \
  -Dspring.profiles.active=prod \
  expense-server-1.0.0.jar \
  > logs/server.log 2>&1 &
echo "Server PID: $!"
```

**前端构建**：

```bash
cd expense-web
npm run build
cp -r dist/* /opt/expense-web/dist/
```

### 生产环境配置

创建 `application-prod.yml` 覆盖开发环境配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-db-host:3306/expense_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: your_prod_user
    password: your_prod_password

logging:
  level:
    com.company.expense: info
    org.springframework: warn

jwt:
  secret: your-production-secret-key-at-least-256-bits-long
  expiration: 7200000
```

### 检查清单

- [ ] MySQL 生产数据库已创建并执行 init.sql
- [ ] 生产数据库密码已修改
- [ ] JWT secret 已更换为生产专用密钥
- [ ] BCrypt 管理员密码已更换
- [ ] 服务器防火墙已开放 80/443 端口
- [ ] Nginx 日志和错误页面已配置
- [ ] 应用日志路径可写
