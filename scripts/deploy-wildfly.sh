#!/bin/bash

set -e

WILDFLY_DIR="$HOME/wildfly-37.0.0.Final"
DEPLOY_DIR="$HOME/blps-deploy"

# ============================================================
# Параметр
# ============================================================

DEPLOY=false

if [ "$1" = "--deploy" ]; then
    DEPLOY=true
fi

echo "========================================"
echo " WILDFLY START"
echo "========================================"
echo ""

# ============================================================
# Переменные окружения
# ============================================================

export DB_URL="jdbc:postgresql://localhost:5432/studs"
export DB_USERNAME="s408285"

# Эти две переменные должны быть заданы заранее
# в ~/blps-env.sh
#
# DB_PASSWORD
# MAIL_USERNAME
# MAIL_PASSWORD

if [ -f "$HOME/blps-env.sh" ]; then
    source "$HOME/blps-env.sh"
else
    echo "ERROR: $HOME/blps-env.sh not found"
    exit 1
fi

export SERVER_PORT=24127

export KAFKA_BOOTSTRAP_SERVERS=localhost:27777

export CRM_BASE_URL="http://localhost:1313/BLPS/hs/skillbox"

# ============================================================
# Полностью повторяем очистку Java environment
# ============================================================

unset JAVA_OPTS
unset _JAVA_OPTIONS

export JAVA_VERSION=17

# ============================================================
# JVM options
# ============================================================

export JAVA_OPTS='-Xms128m -Xmx256m'

# ============================================================
# Deploy WARs
# ============================================================

if [ "$DEPLOY" = true ]; then

    echo "[1/2] Installing WAR files..."

    if [ ! -f "$DEPLOY_DIR/ROOT.war" ]; then
        echo "ERROR: ROOT.war not found"
        exit 1
    fi

    if [ ! -f "$DEPLOY_DIR/email-service.war" ]; then
        echo "ERROR: email-service.war not found"
        exit 1
    fi

    if [ ! -f "$DEPLOY_DIR/telegram-bot-service.war" ]; then
        echo "ERROR: telegram-bot-service.war not found"
        exit 1
    fi

    cp "$DEPLOY_DIR/ROOT.war" \
       "$WILDFLY_DIR/standalone/deployments/ROOT.war"

    cp "$DEPLOY_DIR/email-service.war" \
       "$WILDFLY_DIR/standalone/deployments/email-service.war"

    cp "$DEPLOY_DIR/telegram-bot-service.war" \
       "$WILDFLY_DIR/standalone/deployments/telegram-bot-service.war"

    echo "WAR files installed."
    echo ""

else

    echo "[1/2] WAR deployment skipped."
    echo ""

fi

# ============================================================
# Запуск WildFly
# ============================================================

echo "[2/2] Starting WildFly..."
echo ""

cd "$HOME"

./wildfly-37.0.0.Final/bin/standalone.sh