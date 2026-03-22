# ==========================================================
# === 1. Builder Stage: アプリケーションのビルド (JDK 21) ===
# ==========================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

RUN groupadd --gid 1000 vscode && \
    useradd --uid 1000 --gid 1000 --shell /bin/bash --create-home vscode && \
    mkdir -p /home/vscode/workspace && \
    chown -R vscode:vscode /home/vscode

# 1. sudo 導入
USER root
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get install -y --no-install-recommends \
        openssh-server \
        sudo \
        git \
        procps \
        lsb-release \
    && rm -rf /var/lib/apt/lists/*

# 2. 'vscode' ユーザーが sudo をパスワードなしで実行できるように設定
RUN echo 'vscode ALL=(ALL) NOPASSWD: ALL' > /etc/sudoers.d/vscode \
    && chmod 0440 /etc/sudoers.d/vscode

USER vscode
WORKDIR /home/vscode/workspace

COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/
COPY build.gradle .
COPY settings.gradle .

RUN ./gradlew dependencies
COPY src src
RUN ./gradlew build -x test

# ==========================================================
# === 2. Runner Stage: アプリケーションの実行環境 (JRE 21) ===
# ==========================================================
FROM eclipse-temurin:21-jre-jammy AS runner

USER root
RUN groupadd --gid 1000 vscode && \
    useradd --uid 1000 --gid 1000 --shell /bin/bash --create-home vscode

WORKDIR /app
COPY --from=builder --chown=vscode:vscode /home/vscode/workspace/build/libs/app.jar /app/app.jar

USER vscode
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]