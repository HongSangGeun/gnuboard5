<?php
include_once('./_common.php');

// clean the output buffer
ob_end_clean();

$no = isset($_REQUEST['no']) ? (int) $_REQUEST['no'] : 0;

@include_once($board_skin_path.'/download.head.skin.php');

// 쿠키에 저장된 ID값과 넘어온 ID값을 비교하여 같지 않을 경우 오류 발생
if (!get_session('ss_view_'.$bo_table.'_'.$wr_id))
    alert('잘못된 접근입니다.');

// 다운로드 차감일 때 비회원은 다운로드 불가
if ($board['bo_download_point'] < 0 && $is_guest)
    alert('다운로드 권한이 없습니다.\n회원이시라면 로그인 후 이용해 보십시오.', G5_BBS_URL.'/login.php?wr_id='.$wr_id.'&amp;'.$qstr.'&amp;url='.urlencode(get_pretty_url($bo_table, $wr_id)));

$sql = " select * from {$g5['board_file_table']} 
          where bo_table = '$bo_table' 
            and wr_id = '$wr_id' 
            and bf_no = '$no' ";
$file = sql_fetch($sql);
if (!$file['bf_file'])
    alert_close('파일 정보가 존재하지 않습니다.');

$nonce = isset($_REQUEST['nonce']) ? preg_replace('/[^0-9a-z\|]/i', '', $_REQUEST['nonce']) : '';

if (function_exists('download_file_nonce_is_valid') && !defined('G5_DOWNLOAD_NONCE_CHECK')){
    if(! download_file_nonce_is_valid($nonce, $bo_table, $wr_id)){
        alert('토큰 유효시간이 지났거나 토큰이 유효하지 않습니다.\n브라우저를 새로고침 후 다시 시도해 주세요.', G5_URL);
    }
}

// 권한 체크
if ($member['mb_level'] < $board['bo_download_level']) {
    $alert_msg = '다운로드 권한이 없습니다.';
    if ($is_member)
        alert($alert_msg);
    else
        alert($alert_msg.'\n회원이시라면 로그인 후 이용해 보십시오.', G5_BBS_URL.'/login.php?wr_id='.$wr_id.'&amp;'.$qstr.'&amp;url='.urlencode(get_pretty_url($bo_table, $wr_id)));
}

$filepath = G5_DATA_PATH.'/file/'.$bo_table.'/'.$file['bf_file'];
$original = $file['bf_source'];

if (!is_file($filepath) || !file_exists($filepath))
    alert_close('파일이 존재하지 않습니다.');

// ========================
// 🔹 웹에서 바로보기 처리
// ========================
$finfo = finfo_open(FILEINFO_MIME_TYPE);
$mime  = finfo_file($finfo, $filepath);
finfo_close($finfo);

$filesize = filesize($filepath);

if (preg_match('/^(image|video|audio|text|application\/pdf)/', $mime)) {
    header("Content-Type: $mime");
    header("Content-Disposition: inline; filename=\"{$original}\"");
} else {
    header("Content-Type: application/octet-stream");
    header("Content-Disposition: attachment; filename=\"{$original}\"");
}
header("Content-Length: $filesize");

readfile($filepath);
exit;
