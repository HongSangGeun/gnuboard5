<?php
require_once("config.php");

if (!function_exists('ft_nonce_is_valid')) {
    include_once('../editor.lib.php');
}

// 업로드 디렉토리
$upload_dir = G5_DATA_PATH . '/ckeditor';
if (!is_dir($upload_dir)) {
    mkdir($upload_dir, 0707, true);
}

// 파일이 업로드 되었는지 확인
if (isset($_FILES['upload']) && $_FILES['upload']['error'] == 0) {
    $tmp_name = $_FILES['upload']['tmp_name'];
    $name = preg_replace('/[^a-zA-Z0-9._-]/', '_', $_FILES['upload']['name']);
    $target = $upload_dir . '/' . time() . '_' . $name;

    if (move_uploaded_file($tmp_name, $target)) {
        $url = G5_DATA_URL . '/ckeditor/' . basename($target);

        // CKEditor가 이해할 수 있는 JSON 응답
        echo json_encode([
            "uploaded" => 1,
            "fileName" => $name,
            "url" => $url
        ]);
        exit;
    }
}

// 실패 시
echo json_encode([
    "uploaded" => 0,
    "error" => ["message" => "업로드 실패"]
]);
