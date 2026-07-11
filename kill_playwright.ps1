Write-Host "Searching for Playwright processes..." -ForegroundColor Cyan

# Find any processes with 'playwright' in their command line, 
# excluding the current powershell session to avoid killing ourselves
$processes = Get-CimInstance Win32_Process | Where-Object { 
    $_.CommandLine -match "playwright" -and $_.ProcessId -ne $PID 
}

if ($null -eq $processes -or $processes.Count -eq 0) {
    Write-Host "No Playwright processes found." -ForegroundColor Yellow
} else {
    foreach ($proc in $processes) {
        Write-Host "Killing Playwright process: $($proc.ProcessId) ($($proc.Name))" -ForegroundColor Red
        Stop-Process -Id $proc.ProcessId -Force -ErrorAction SilentlyContinue
    }
    Write-Host "Done." -ForegroundColor Green
}
