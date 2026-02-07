<?php
if (!defined('_GNUBOARD_')) exit; // 개별 페이지 접근 불가

// 에디터 HTML 출력
function editor_html($id, $content, $is_dhtml_editor=true) {
    global $bo_table; // 현재 게시판 ID

    // 기본 높이
    $editor_height = 400;

    // 게시판별 높이 지정
    if ($bo_table === 'schedule') {
        $editor_height = 200;
    } elseif ($bo_table === 'notice') {
        $editor_height = 500;
    }

    $html  = "<textarea style='font-size:12pt' id=\"{$id}\" name=\"{$id}\" style=\"width:100%; height:300px;\">{$content}</textarea>\n";
    // CKEditor 본체 로드
    $html .= "<script src=\"".G5_PLUGIN_URL."/editor/ckeditor4/ckeditor.js\"></script>\n";

    // 초기화
    $html .= "<script>\n";
    $html .= "document.addEventListener('DOMContentLoaded', function(){\n";
    $html .= "  if (typeof CKEDITOR !== 'undefined') {\n";
    $html .= "    if (CKEDITOR.instances['{$id}']) {\n";
    $html .= "      CKEDITOR.instances['{$id}'].destroy(true);\n"; // 중복 방지
    $html .= "    }\n";
    $html .= "    CKEDITOR.replace('{$id}', {\n";
    $html .= "      height: {$editor_height},\n";
    $html .= "      skin: 'office2013', // moono-dark 또는 'office2013'\n";
    $html .= "      filebrowserImageUploadUrl: '".G5_PLUGIN_URL."/editor/ckeditor4/imageUpload/upload.php?type=Images'\n";
    $html .= "    });\n";
    $html .= "  } else {\n";
    $html .= "    console.error('CKEditor not loaded: ckeditor.js 경로 확인 필요');\n";
    $html .= "  }\n";
    $html .= "});\n";
    $html .= "</script>\n";

    return $html;
}

// textarea → 값 되돌리기
function get_editor_js($id, $is_dhtml_editor=true) {
    return "if (CKEDITOR.instances['{$id}']) CKEDITOR.instances['{$id}'].updateElement();\n";
}

// 에디터 내용 검사
function chk_editor_js($id, $is_dhtml_editor=true) {
    return "if (CKEDITOR.instances['{$id}']) {\n"
         . "  var data = CKEDITOR.instances['{$id}'].getData().replace(/<[^>]*>/gi, '').trim();\n"
         . "  if (!data) { alert('내용을 입력해 주십시오.'); CKEDITOR.instances['{$id}'].focus(); return false; }\n"
         . "}\n";
}
