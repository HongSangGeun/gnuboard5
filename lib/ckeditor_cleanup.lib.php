<?php
if (!defined('_GNUBOARD_')) exit;

/**
 * 본문에 포함된 CKEditor 인라인 이미지( /data/ckeditor/... )를 즉시 삭제
 * - src, data-cke-saved-src 둘 다 지원
 * - /data/ckeditor/ 경로 이외는 건드리지 않음 (보안)
 * - 같은 이미지가 여러 번 등장해도 한 번만 삭제 시도
 */
function g5_delete_inline_ckeditor_images($html) {
    if (!is_string($html) || $html === '') return 0;

    // HTML 파싱 (깨진 HTML 대비 옵션)
    $prev = libxml_use_internal_errors(true);
    $doc  = new DOMDocument();

    // mb 문자열이면 임시로 UTF-8 보정
    $wrapped = '<!DOCTYPE html><meta http-equiv="Content-Type" content="text/html; charset=utf-8">'.$html;
    $doc->loadHTML($wrapped);
    libxml_clear_errors();
    libxml_use_internal_errors($prev);

    $imgs = $doc->getElementsByTagName('img');
    if (!$imgs || $imgs->length === 0) return 0;

    $deleted = 0;
    $seen    = [];

    // URL 베이스들
    $data_url_prefix  = rtrim(G5_DATA_URL, '/');   // 예: /data
    $data_path_prefix = rtrim(G5_DATA_PATH, DIRECTORY_SEPARATOR); // 예: /var/www/html/data

    foreach ($imgs as $img) {
        // src 우선, 없으면 data-cke-saved-src
        $src = $img->getAttribute('src');
        if ($src === '' && $img->hasAttribute('data-cke-saved-src')) {
            $src = $img->getAttribute('data-cke-saved-src');
        }
        if ($src === '') continue;

        // 절대/상대 모두 허용: 도메인 포함 시 호스트 제거
        // (예: http://site.com/data/ckeditor/... → /data/ckeditor/...)
        $parsed = parse_url($src);
        if (isset($parsed['path'])) {
            $src_path = $parsed['path'];
        } else {
            $src_path = $src;
        }

        // /data/ckeditor/ 만 대상
        $ckeditor_prefix = $data_url_prefix . '/ckeditor/';
        if (strpos($src_path, $ckeditor_prefix) !== 0) {
            continue; // 다른 경로(첨부파일 테이블, 외부 CDN 등)는 건너뜀
        }

        // 파일시스템 절대경로로 변환
        //   /data/ckeditor/2025/09/a.png  →  {G5_DATA_PATH}/ckeditor/2025/09/a.png
        $rel   = substr($src_path, strlen($data_url_prefix)); // "/ckeditor/..." 포함
        $fpath = $data_path_prefix . $rel;

        // 디렉토리 탈출 방지(realpath 비교)
        $real = realpath($fpath);
        if ($real === false) {
            // 없는 파일이면 스킵
            continue;
        }
        $safe_root = realpath($data_path_prefix . '/ckeditor');
        if ($safe_root === false) {
            // /data/ckeditor 가 없으면 스킵
            continue;
        }
        if (strpos($real, $safe_root) !== 0) {
            // /data/ckeditor 밖이면 절대 삭제하지 않음
            continue;
        }

        // 중복 삭제 방지
        if (isset($seen[$real])) continue;
        $seen[$real] = true;

        // 삭제 시도
        if (is_file($real) && is_writable($real)) {
            @unlink($real);
            $deleted++;
        }

        // (옵션) 썸네일/변형 파일 같이 지우고 싶다면 여기서 패턴 삭제 추가
        // 예: 파일명 뒤에 _thumb, _small 등의 규칙이 있다면 glob()로 함께 정리
        // $pattern = preg_replace('/(\.[a-z0-9]+)$/i', '_*.$1', $real);
        // foreach (glob($pattern) as $extra) { @unlink($extra); }
    }

    return $deleted;
}
