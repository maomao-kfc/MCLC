# 基础镜像 (eclipse-temurin 是 openjdk 的继任者，CA证书保持更新)
FROM eclipse-temurin:17-jre

# 维护者信息
LABEL maintainer="youlai <youlaitech@163.com>"

# 设置时区
ENV TZ=Asia/Shanghai

# 安装 LibreOffice（DOCX转PDF）和中文字体（PDF中文渲染）
# 注意：libreoffice-writer 不使用 --no-install-recommends，因为它依赖推荐包才能正常运行
RUN apt-get update && \
    apt-get install -y \
        libreoffice-writer \
        fonts-wqy-zenhei \
        fonts-noto-cjk \
        fontconfig \
        ca-certificates && \
    # 刷新字体缓存，确保 OpenPDF 能通过 fontconfig 找到中文字体
    fc-cache -f && \
    # 验证 LibreOffice 和字体安装成功
    echo "=== 验证 LibreOffice 安装 ===" && \
    which soffice && \
    soffice --version && \
    echo "=== 验证中文字体安装 ===" && \
    fc-list :lang=zh | head -5 && \
    echo "=== 验证字体文件路径 ===" && \
    ls -la /usr/share/fonts/truetype/wqy/ 2>/dev/null || echo "wqy目录不存在" && \
    ls -la /usr/share/fonts/opentype/noto/ 2>/dev/null || echo "noto opentype目录不存在" && \
    ls -la /usr/share/fonts/truetype/noto/ 2>/dev/null || echo "noto truetype目录不存在" && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 在运行时自动挂载 /tmp 目录为匿名卷
VOLUME /tmp

# 添加应用
ADD youlai-boot.jar app.jar

# 启动脚本：清理LibreOffice残留锁文件（解决容器重启后exit code 81问题），再启动应用
CMD rm -rf /tmp/.jodconverter_socket_host-127.0.0.1_port-*.lock /tmp/.jodconverter_socket_host-127.0.0.1_port-*/.lock /tmp/lu*.tmp 2>/dev/null; \
    java \
    -Xms512m -Xmx512m \
    -Djava.security.egd=file:/dev/./urandom \
    -jar /app.jar \
    --spring.profiles.active=prod

# 暴露端口
EXPOSE 8989
