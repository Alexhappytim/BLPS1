#!/bin/bash

echo "========================================"
echo " HELIOS CLEANUP"
echo "========================================"
echo ""

USER_NAME="$(whoami)"
WILDFLY_DIR="$HOME/wildfly-37.0.0.Final"

# ============================================================
# 1. Останавливаем WildFly
# ============================================================

echo "[1/4] Stopping WildFly..."

pkill -TERM -u "$USER_NAME" -f "wildfly.*standalone" 2>/dev/null || true
pkill -TERM -u "$USER_NAME" -f "jboss-modules.jar" 2>/dev/null || true

sleep 3

echo "Killing remaining WildFly processes..."

pkill -KILL -u "$USER_NAME" -f "wildfly.*standalone" 2>/dev/null || true
pkill -KILL -u "$USER_NAME" -f "jboss-modules.jar" 2>/dev/null || true

echo "WildFly stopped."
echo ""

# ============================================================
# 2. Очищаем data
# ============================================================

echo "[2/4] Cleaning WildFly data..."

rm -rf "$WILDFLY_DIR/standalone/data/"*

echo "WildFly data cleaned."
echo ""

# ============================================================
# 3. Удаляем старые WAR
# ============================================================

echo "[3/4] Removing old deployments..."

rm -f "$WILDFLY_DIR/standalone/deployments/ROOT.war"
rm -f "$WILDFLY_DIR/standalone/deployments/email-service.war"
rm -f "$WILDFLY_DIR/standalone/deployments/telegram-bot-service.war"

rm -f "$WILDFLY_DIR/standalone/deployments/ROOT.war.deployed"
rm -f "$WILDFLY_DIR/standalone/deployments/email-service.war.deployed"
rm -f "$WILDFLY_DIR/standalone/deployments/telegram-bot-service.war.deployed"

echo "Old deployments removed."
echo ""

# ============================================================
# 4. Убиваем старые SSH-сессии
# ============================================================

echo "[4/4] Killing old SSH sessions..."

CURRENT_PID=$$

# Цепочка PID текущего SSH-подключения.
PROTECTED_PIDS="$CURRENT_PID"

PID=$CURRENT_PID

while true; do
    PPID_VALUE=$(ps -o ppid= -p "$PID" 2>/dev/null | tr -d ' ')

    if [ -z "$PPID_VALUE" ] || [ "$PPID_VALUE" = "0" ]; then
        break
    fi

    PROTECTED_PIDS="$PROTECTED_PIDS $PPID_VALUE"
    PID="$PPID_VALUE"
done

echo "Protected current process chain:"
echo "$PROTECTED_PIDS"
echo ""

# Убиваем старые sshd-session.
for PID in $(pgrep -u "$USER_NAME" -f "sshd-session" 2>/dev/null || true); do

    PROTECTED=false

    for PROTECTED_PID in $PROTECTED_PIDS; do
        if [ "$PID" = "$PROTECTED_PID" ]; then
            PROTECTED=true
            break
        fi
    done

    if [ "$PROTECTED" = false ]; then
        echo "Killing old sshd-session PID=$PID"
        kill -TERM "$PID" 2>/dev/null || true
    fi
done

# Убиваем старые bash.
for PID in $(pgrep -u "$USER_NAME" -f "^-bash$|/bash$" 2>/dev/null || true); do

    PROTECTED=false

    for PROTECTED_PID in $PROTECTED_PIDS; do
        if [ "$PID" = "$PROTECTED_PID" ]; then
            PROTECTED=true
            break
        fi
    done

    if [ "$PROTECTED" = false ]; then
        echo "Killing old bash PID=$PID"
        kill -TERM "$PID" 2>/dev/null || true
    fi
done

sleep 1

echo ""
echo "========================================"
echo " CLEANUP COMPLETE"
echo "========================================"
echo ""

# Текущая SSH-сессия закрывается.
exit 0