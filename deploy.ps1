param(
    [switch]$Deploy
)

$ErrorActionPreference = "Stop"

$ProjectRoot = $PSScriptRoot
$ScriptsDir = Join-Path $ProjectRoot "scripts"

$SshUser = "s408285"
$SshHost = "helios.cs.ifmo.ru"
$SshPort = 2222

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " BLPS DEPLOY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# 1. Kafka
# ============================================================

Write-Host "[1/5] Starting Kafka..." -ForegroundColor Yellow

docker compose up -d zookeeper kafka

if ($LASTEXITCODE -ne 0) {
    throw "Failed to start Kafka/Zookeeper"
}

Write-Host "Kafka started." -ForegroundColor Green
Write-Host ""

# ============================================================
# 2. Cleanup HELIOS
# ============================================================

Write-Host "[2/5] Cleaning HELIOS..." -ForegroundColor Yellow
Write-Host ""

$CleanupScript = Join-Path $ScriptsDir "cleanup-helios.sh"

if (-not (Test-Path $CleanupScript)) {
    throw "Cleanup script not found: $CleanupScript"
}

# SSH №1.
# Этот SSH специально будет убит cleanup-скриптом.
Get-Content $CleanupScript -Raw |
    ssh -p $SshPort "$SshUser@$SshHost" "bash -s"

Write-Host ""
Write-Host "HELIOS cleanup completed." -ForegroundColor Green
Write-Host ""

# ============================================================
# 3. Build WARs
# ============================================================

if ($Deploy) {

    Write-Host "[3/5] Building WAR files..." -ForegroundColor Yellow
    Write-Host ""

    Push-Location $ProjectRoot

    try {

        .\gradlew.bat clean :bootWar :EmailService:bootWar :TelegramBotService:bootWar

        if ($LASTEXITCODE -ne 0) {
            throw "Gradle build failed"
        }

    }
    finally {
        Pop-Location
    }

    Write-Host ""
    Write-Host "WAR files built." -ForegroundColor Green
    Write-Host ""

}
else {

    Write-Host "[3/5] Build skipped - no -Deploy flag." -ForegroundColor DarkGray
    Write-Host ""

}

# ============================================================
# 4. Upload WARs + deploy script
# ============================================================

if ($Deploy) {

    Write-Host "[4/5] Uploading WAR files..." -ForegroundColor Yellow
    Write-Host ""

    ssh -p $SshPort `
        "$SshUser@$SshHost" `
        "mkdir -p ~/blps-deploy"

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create ~/blps-deploy"
    }

    $WarFiles = @(
        @{
            Local = Join-Path $ProjectRoot "build\libs\ROOT.war"
            Remote = "ROOT.war"
        },
        @{
            Local = Join-Path $ProjectRoot "EmailService\build\libs\email-service.war"
            Remote = "email-service.war"
        },
        @{
            Local = Join-Path $ProjectRoot "TelegramBotService\build\libs\telegram-bot-service.war"
            Remote = "telegram-bot-service.war"
        }
    )

    foreach ($War in $WarFiles) {

        if (-not (Test-Path $War.Local)) {
            throw "WAR not found: $($War.Local)"
        }

        Write-Host "Uploading $($War.Remote)..." -ForegroundColor DarkGray

        scp -P $SshPort `
            $War.Local `
            "${SshUser}@${SshHost}:~/blps-deploy/$($War.Remote)"

        if ($LASTEXITCODE -ne 0) {
            throw "Failed to upload $($War.Remote)"
        }
    }

    Write-Host ""
    Write-Host "Uploading deploy-wildfly.sh..." -ForegroundColor DarkGray

    scp -P $SshPort `
        (Join-Path $ScriptsDir "deploy-wildfly.sh") `
        "${SshUser}@${SshHost}:~/deploy-wildfly.sh"

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to upload deploy-wildfly.sh"
    }

    ssh -p $SshPort `
        "$SshUser@$SshHost" `
        "chmod +x ~/deploy-wildfly.sh"

    Write-Host ""
    Write-Host "WAR files uploaded." -ForegroundColor Green
    Write-Host ""

}
else {

    Write-Host "[4/5] WAR upload skipped - no -Deploy flag." -ForegroundColor DarkGray
    Write-Host ""

}

# ============================================================
# 5. New SSH + tunnels + WildFly
# ============================================================

Write-Host "[5/5] Starting new SSH connection..." -ForegroundColor Yellow
Write-Host ""

if ($Deploy) {
    $RemoteCommand = "bash ~/deploy-wildfly.sh --deploy"
}
else {
    $RemoteCommand = "bash ~/deploy-wildfly.sh"
}

Write-Host "SSH tunnels:" -ForegroundColor DarkGray
Write-Host "  localhost:24127 -> HELIOS:24127" -ForegroundColor DarkGray
Write-Host "  localhost:24129 -> HELIOS:24129" -ForegroundColor DarkGray
Write-Host "  HELIOS:1313 -> localhost:1313" -ForegroundColor DarkGray
Write-Host "  HELIOS:27777 -> localhost:9092" -ForegroundColor DarkGray
Write-Host ""

ssh `
    -p $SshPort `
    -L 24127:localhost:24127 `
    -L 24129:localhost:24129 `
    -R 1313:localhost:1313 `
    -R 27777:localhost:9092 `
    "$SshUser@$SshHost" `
    $RemoteCommand