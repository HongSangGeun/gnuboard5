<?php
// GNUBOARD5 FullCalendar 이벤트 피드 (올데이/시간 일정 자동 구분 + 다양한 날짜 포맷 대응)
// 파일 경로: skin/board/schedule/get-events1.php

include_once('../../../_common.php');

header('Content-Type: application/json; charset=utf-8');

// 필수 파라미터 처리: bo_table -> write_table 설정
$bo_table = isset($_GET['bo_table']) ? preg_replace('/[^a-z0-9_]/i', '', $_GET['bo_table']) : '';
if (!$bo_table) {
    echo json_encode([]);
    exit;
}
$write_table = $g5['write_prefix'] . $bo_table;

// FullCalendar가 start/end(YYYY-MM-DD)를 쿼리로 넘김
if (!isset($_GET['start']) || !isset($_GET['end'])) {
    echo json_encode([]);
    exit;
}

// 비교용: YYYY-MM-DD -> YYYYMMDD (숫자만 추출)
$frdate = preg_replace('/[^0-9]/', '', $_GET['start']);
$todate = preg_replace('/[^0-9]/', '', $_GET['end']);

// 날짜가 다양한 포맷(Ymd, YmdHi, YmdHis, Y-m-d, Y-m-d H:i, Y-m-d H:i:s, ISO8601 등)으로 섞여 저장되어도 파싱
function parse_datetime_str($str)
{
    $str = trim((string) $str);
    if ($str === '')
        return false;

    $formats = [
        'Y-m-d H:i:s',
        'Y-m-d H:i',
        'Y-m-d\TH:i:s',
        'Y-m-d\TH:i',
        'Y-m-d',
        'YmdHis',
        'YmdHi',
        'Ymd'
    ];
    foreach ($formats as $fmt) {
        $dt = DateTime::createFromFormat($fmt, $str);
        if ($dt !== false)
            return $dt;
    }
    $ts = strtotime($str);
    return $ts !== false ? (new DateTime())->setTimestamp($ts) : false;
}

// 범위 필터: 저장 포맷이 섞여 있어도 날짜(앞 8자리)만 비교해 빠지지 않도록 함
$sql = "
    SELECT wr_id, wr_subject, wr_1, wr_2, wr_3, wr_4, wr_5, ca_name, wr_comment
    FROM {$write_table}
    WHERE wr_1 <> '' 
      AND wr_2 <> ''
      AND LEFT(REPLACE(wr_1,'-',''),8) <= '{$todate}'
      AND LEFT(REPLACE(wr_2,'-',''),8) >= '{$frdate}'
";
$res = sql_query($sql);

$events = [];


// 카테고리별 색상 매핑 (배경색 + 글자색)
$categoryColors = array(
    "하드웨어" => ["bg" => "rgba(78, 205, 196, 0.8)", "text" => "#FFFFFF"],
    "데이터허브" => ["bg" => "rgba(74, 144, 226, 0.8)", "text" => "#FFFFFF"],
    "유지보수" => ["bg" => "rgba(255, 107, 107, 0.8)", "text" => "#ffffff"], // 밝은 배경 → 검은 글자
    "응급의료" => ["bg" => "rgba(162, 155, 254, 1)", "text" => "#ffffff"]
);


while ($row = sql_fetch_array($res)) {
    $startRaw = trim($row['wr_1']);
    $endRaw = trim($row['wr_2']);
    if ($startRaw === '')
        continue;

    $st = parse_datetime_str($startRaw);
    $en = parse_datetime_str($endRaw);
    if (!$st)
        continue; // 시작날짜 파싱 실패 시 스킵
    if (!$en)
        $en = clone $st; // 종료가 비었으면 시작으로 보정

    // 저장된 올데이 플래그(wr_5)가 있으면 최우선
    $isAllFlag = isset($row['wr_5']) && $row['wr_5'] === '1';

    $allDay = false;
    $startStr = '';
    $endStr = '';

    if ($isAllFlag) {
        // ✅ 올데이: start/end는 날짜만, end는 exclusive(+1일)
        $startStr = $st->format('Y-m-d');
        $en->modify('+1 day');
        $endStr = $en->format('Y-m-d');
        $allDay = true;
    } else {
        // 플래그가 없을 때는 시간 유무로 자동 판별
        $startHasTime = $st->format('H:i:s') !== '00:00:00';
        $endHasTime = $en->format('H:i:s') !== '00:00:00';

        if (!$startHasTime && !$endHasTime) {
            // ✅ 자동 올데이 (같은 날이든 범위든 end는 항상 +1일: FullCalendar exclusive 규칙)
            $startStr = $st->format('Y-m-d');
            $en->modify('+1 day');
            $endStr = $en->format('Y-m-d');
            $allDay = true;
        } else {
            // ⏱ 시간 일정
            $startStr = $st->format('Y-m-d\TH:i:s');
            $endStr = $en->format('Y-m-d\TH:i:s');
            $allDay = false;
        }
    }


    // ✅ 색상 결정: 카테고리 우선 → wr_4/wr_3 → 기본값
    if (isset($categoryColors[$row['ca_name']])) {
        $color = $categoryColors[$row['ca_name']]['bg'];
        $textColor = $categoryColors[$row['ca_name']]['text'];
    } else {
        $color = $row['wr_4'] ?: "rgba(70, 224, 134, 0.8)";
        $textColor = $row['wr_3'] ?: "#FFFFFF";
    }
    
    // 리소스 매핑
    $resourceMap = [
        "하드웨어" => "hw",
        "유지보수" => "ms",
        "데이터허브" => "dh",
        "응급의료" => "em"
    ];
    
    $events[] = [
        'id' => $row['wr_id'],
        'title' => $row['wr_subject'],
        'start' => $startStr,
        'end' => $endStr,
        'allDay' => $allDay,
        //'color'     => $row['wr_4'] ?: '#46E086',
        //'textColor' => $row['wr_3'] ?: '#FFFFFF',
        'color' => $color,       // ✅ 변수 사용
        'textColor' => $textColor,   // ✅ 변수 사용
        'url' => G5_BBS_URL . '/board.php?bo_table=' . $bo_table . '&wr_id=' . $row['wr_id'],
        'category' => $row['ca_name'] ?? '',
        'resourceId' => $resourceMap[$row['ca_name']] ?? '',   // ✅ 매핑 적용
        'commentCount' => (int) ($row['wr_comment'] ?? 0)   // ✅ wr_comment 사용
    ];
}

echo json_encode($events, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
