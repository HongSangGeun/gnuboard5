<?php
header('Content-Type: application/json; charset=utf-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);

try {
    // CPU Load
    $cpuLoad = sys_getloadavg();

    // 메모리 (/proc/meminfo 활용)
    $meminfo = file('/proc/meminfo');
    $mem = [];
    foreach ($meminfo as $line) {
        list($key, $val) = explode(":", $line);
        $mem[trim($key)] = (int) filter_var($val, FILTER_SANITIZE_NUMBER_INT);
    }
    if (!isset($mem['MemTotal']) || !isset($mem['MemAvailable'])) {
        throw new Exception("메모리 정보 부족");
    }
    $memory_usage = round((1 - $mem['MemAvailable'] / $mem['MemTotal']) * 100, 2);

    // 디스크
    $disk_total = @disk_total_space("/");
    $disk_free  = @disk_free_space("/");
    if ($disk_total === false || $disk_free === false) {
        throw new Exception("디스크 정보 조회 실패");
    }
    $disk_used = $disk_total - $disk_free;
    $disk_usage = round($disk_used / $disk_total * 100, 2);

    echo json_encode([
        "cpu"    => $cpuLoad[0] . "%",
        "memory" => $memory_usage . "%",
        "disk"   => $disk_usage . "%"
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "error" => $e->getMessage()
    ]);
}
