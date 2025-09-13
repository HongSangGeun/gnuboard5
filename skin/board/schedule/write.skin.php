<?php
if (!defined('_GNUBOARD_')) exit; // 개별 페이지 접근 불가
//include_once(G5_PLUGIN_PATH.'/jquery-ui/datepicker.php');

// add_stylesheet('css 구문', 출력순서); 숫자가 작을 수록 먼저 출력됨
add_stylesheet('<link rel="stylesheet" href="'.$board_skin_url.'/wzappend.css">', 0);
add_stylesheet('<link rel="stylesheet" href="https://code.jquery.com/ui/1.13.2/themes/base/jquery-ui.css">', 1);
add_stylesheet('<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.min.css">', 2);
add_stylesheet('<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">', 4);
add_stylesheet('<link rel="stylesheet" href="'.$board_skin_url.'/dtp-theme.css">', 10);
add_stylesheet('<link rel="stylesheet" href="'.$board_skin_url.'/style.css">', 8);
?>

<style>
.frm_input {
    height: 30px;
    width: 120px;
}

button.btn_frmline {
    display: inline-block;
    width: 128px;
    padding: 0 5px;
    height: 30px;
    border: 0;
    background: #434a54;
    border-radius: 3px;
    color: #fff;
    text-decoration: none;
    vertical-align: top;
}
#wr_subject { width: 400px; }
</style>
<!-- 밑에 add_stylesheet 함수를 사용하지 않는이유은 가끔 홈페이지 개발시 오류로 add_stylesheet 함수가 먹지 않는 현상으로 인해 사용하지 않습니다. -->


<script src="<?php echo $board_skin_url;?>/wz.js/jscolor.min.js"></script>

<!-- jQuery (항상 먼저) -->
<script src="<?php echo G5_JS_URL; ?>/jquery-1.12.4.min.js"></script>

<!-- jQuery UI (datepicker + slider 포함된 빌드 필요) -->
<script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>

<!-- Timepicker Addon (UI Datepicker 뒤에 로드) -->
<script src="<?php echo G5_PLUGIN_URL; ?>/jquery-ui/jquery-ui-timepicker-addon.js"></script>

<script>
var categoryColors = {
  "유지보수": "#FF6B6B",   // 회의 → 빨강
  "하드웨어": "#4ECDC4",   // 교육 → 청록
  "데이터허브": "#4A90E2",   // 행사 → 파랑
  "응급의료": "#A29BFE"    // 기타 → 보라
};

$(function() {

  // ✅ 분류 선택시 배경색 자동 설정
  $("#ca_name").on("change", function() {
    var cat = $(this).val();
    var color = categoryColors[cat] || "#46E086"; // 기본색: 초록

    console.log("선택된 카테고리:", cat);     // ✅ 선택된 카테고리 출력
    console.log("적용된 색상:", color);       // ✅ 적용된 색상 출력

    $("#wr_4").val(color);
    if (typeof jscolor !== "undefined" && $("#wr_4")[0].jscolor) {
      $("#wr_4")[0].jscolor.fromString(color);
    }
  });
});
</script>

<script>
$(function () {

  
  function initDateTime() {
  $("#wr_1, #wr_2").datepicker("destroy").datetimepicker({
    dateFormat: "yy-mm-dd",
    timeFormat: "HH:mm",
    stepMinute: 10,
    controlType: "select",
    oneLine: true,
    showSecond: false
  });

  // ✅ 값이 있으면 강제로 Date 객체로 변환 후 setDate
  if ($("#wr_1").val()) {
    let d1 = parseInputDate($("#wr_1").val());
    if (d1) $("#wr_1").datetimepicker("setDate", d1);
  }
  if ($("#wr_2").val()) {
    let d2 = parseInputDate($("#wr_2").val());
    if (d2) $("#wr_2").datetimepicker("setDate", d2);
  }

  $("#wr_2").prop("readonly", false);
}

function initDateOnly() {
  $("#wr_1, #wr_2").datetimepicker("destroy").datepicker({
    dateFormat: "yy-mm-dd",
    changeMonth: true,
    changeYear: true
  });

  if ($("#wr_1").val()) {
    let d1 = parseInputDate($("#wr_1").val());
    if (d1) $("#wr_1").datepicker("setDate", d1);
  }
  if ($("#wr_2").val()) {
    let d2 = parseInputDate($("#wr_2").val());
    if (d2) $("#wr_2").datepicker("setDate", d2);
  }

  syncEndWithStart();
  $("#wr_2").prop("readonly", true);
}

// ✅ 안전하게 문자열 → Date 변환하는 함수
function parseInputDate(str) {
  if (!str) return null;

  // YYYYMMDD 형식 → YYYY-MM-DD 로 변환
  if (/^[0-9]{8}$/.test(str)) {
    str = str.replace(/([0-9]{4})([0-9]{2})([0-9]{2})/, "$1-$2-$3");
  }

  str = str.replace("T", " ");
  let parts = str.split(" ");
  let dateParts = parts[0].split("-");
  if (dateParts.length < 3) return null;

  let y = parseInt(dateParts[0], 10);
  let m = parseInt(dateParts[1], 10) - 1;
  let d = parseInt(dateParts[2], 10);

  let h = 0, min = 0;
  if (parts[1]) {
    let timeParts = parts[1].split(":");
    h = parseInt(timeParts[0], 10) || 0;
    min = parseInt(timeParts[1], 10) || 0;
  }

  return new Date(y, m, d, h, min);
}

function syncEndWithStart() {
  var startVal = $("#wr_1").val();
  if (!startVal) return;
  var dateOnly = startVal.split("T")[0].split(" ")[0];
  if ($("#is_all_day").is(":checked")) {
    var d = new Date(dateOnly);
    d.setDate(d.getDate() + 1);
    var y = d.getFullYear();
    var m = ("0" + (d.getMonth() + 1)).slice(-2);
    var day = ("0" + d.getDate()).slice(-2);
    $("#wr_2").val(y + "-" + m + "-" + day);
  } else {
    $("#wr_2").val(startVal);
  }
}


  // ✅ 초기 상태 적용
  if ($("#is_all_day").is(":checked")) initDateOnly();
  else initDateTime();

  // ✅ 종일 토글 (오타 주의: $("#is_all_day"))
$("#is_all_day").on("change", function () {
	$("#wr_5_hidden").val(this.checked ? "1" : "0");
    if (this.checked) {
      initDateOnly();
    } else {
      initDateTime();
    }
  });

  // ✅ 시작 변경 시: 종일이면 종료 자동 동기화
  // datepicker/datetimepicker 둘 다 change 이벤트 발생
$("#wr_1").on("change", function () {
	var startVal = $("#wr_1").val();
  var endVal   = $("#wr_2").val();

  if (!startVal) return;

  // Date 객체로 변환
  var startDate = new Date(startVal.replace(/-/g, "/"));
  var endDate   = new Date(endVal.replace(/-/g, "/"));

  // 종료일이 시작일보다 이전이면 → 종료일을 시작일과 동일하게 맞춤
  if (endVal && endDate < startDate) {
    $("#wr_2").val(startVal);
  }

  // 종일 체크 상태면 항상 시작일=종료일 동기화
  if ($("#is_all_day").is(":checked")) {
    $("#wr_2").val(startVal.split("T")[0]);
  }
    if ($("#is_all_day").is(":checked")) {
      syncEndWithStart();
    }
  });
});
</script>



<?php
// URL 파라미터 받기
$param_wr1 = $_GET['wr_1'] ?? '';
$param_wr2 = $_GET['wr_2'] ?? '';
$param_allday = $_GET['allday'] ?? '1';


// FullCalendar에서 YYYYMMDD 들어온 경우 변환
if (!empty($param_wr1) && preg_match('/^[0-9]{8}$/', $param_wr1)) {
    $wr_1_val = substr($param_wr1, 0, 4).'-'.substr($param_wr1, 4, 2).'-'.substr($param_wr1, 6, 2);
}
if (!empty($param_wr2) && preg_match('/^[0-9]{8}$/', $param_wr2)) {
    $wr_2_val = substr($param_wr2, 0, 4).'-'.substr($param_wr2, 4, 2).'-'.substr($param_wr2, 6, 2);
}

// wr_1 기본값 (우선순위: 게시글 값 > GET 파라미터 > 오늘 날짜)
if (!empty($write['wr_1'])) {
    $wr_1_val = preg_replace("/[^0-9:\-T ]/", "", $write['wr_1']);
} elseif (!empty($param_wr1)) {
    // FullCalendar에서 YYYY-MM-DD 넘어옴
    $wr_1_val = preg_replace("/[^0-9:\-T ]/", "", $param_wr1);
} else {
    $wr_1_val = date('Y-m-d');
}

// wr_2 기본값 (우선순위: 게시글 값 > GET 파라미터 > wr_1과 동일)
if (!empty($write['wr_2'])) {
    $wr_2_val = preg_replace("/[^0-9:\-T ]/", "", $write['wr_2']);
} elseif (!empty($param_wr2)) {
    $wr_2_val = preg_replace("/[^0-9:\-T ]/", "", $param_wr2);
} else {
    $wr_2_val = $wr_1_val;
}

// 종일 플래그 (wr_5)
$is_all_day = (!empty($write['wr_5']) && $write['wr_5'] == '1') || $param_allday == '1';
?>



<section id="bo_w">
    <h2 class="sound_only"><?php echo $g5['title'] ?></h2>

    <!-- 게시물 작성/수정 시작 { -->
    <form name="fwrite" id="fwrite" action="<?php echo $action_url ?>" onsubmit="return fwrite_submit(this);" method="post" enctype="multipart/form-data" autocomplete="off" style="width:<?php echo $width; ?>">
    <input type="hidden" name="uid" value="<?php echo get_uniqid(); ?>">
    <input type="hidden" name="w" value="<?php echo $w ?>">
    <input type="hidden" name="bo_table" value="<?php echo $bo_table ?>">
    <input type="hidden" name="wr_id" value="<?php echo $wr_id ?>">
    <input type="hidden" name="sca" value="<?php echo $sca ?>">
    <input type="hidden" name="sfl" value="<?php echo $sfl ?>">
    <input type="hidden" name="stx" value="<?php echo $stx ?>">
    <input type="hidden" name="spt" value="<?php echo $spt ?>">
    <input type="hidden" name="sst" value="<?php echo $sst ?>">
    <input type="hidden" name="sod" value="<?php echo $sod ?>">
    <input type="hidden" name="page" value="<?php echo $page ?>">

    <div class="wz_tbl_1">
    <table>
    <tbody>
    <?php
    $option = '';
    $option_hidden = '';
    if ($is_notice || $is_html || $is_secret || $is_mail) {
        $option = '';
        if ($is_notice) {
            $option .= "\n".'<input type="checkbox" id="notice" name="notice" value="1" '.$notice_checked.'>'."\n".'<label for="notice">공지</label>';
        }

        if ($is_html) {
            if ($is_dhtml_editor) {
                $option_hidden .= '<input type="hidden" value="html1" name="html">';
            } else {
                $option .= "\n".'<input type="checkbox" id="html" name="html" onclick="html_auto_br(this);" value="'.$html_value.'" '.$html_checked.'>'."\n".'<label for="html">HTML</label>';
            }
        }

        if ($is_secret) {
            if ($is_admin || $is_secret==1) {
                $option .= "\n".'<input type="checkbox" id="secret" name="secret" value="secret" '.$secret_checked.'>'."\n".'<label for="secret">비밀글</label>';
            } else {
                $option_hidden .= '<input type="hidden" name="secret" value="secret">';
            }
        }

        if ($is_mail) {
            $option .= "\n".'<input type="checkbox" id="mail" name="mail" value="mail" '.$recv_email_checked.'>'."\n".'<label for="mail">답변메일받기</label>';
        }
    }

    echo $option_hidden;
    ?>

    <?php if ($is_category) { ?>
    <tr>
        <th>분류<span class="sound_only">필수</span></th>
        <td>
        <select name="ca_name" id="ca_name" required>
            <option value="">분류를 선택하세요</option>
            <?php echo $category_option ?>
        </select>
        </td>
    </tr>
    <?php } ?>


    <?php if ($is_name) { ?>
    <tr>
        <th>이름<span class="sound_only">필수</span></th>
        <td><input type="text" name="wr_name" value="<?php echo $name ?>" id="wr_name" required class="frm_input required" placeholder="이름"></td>
    </tr>
    <?php } ?>

    <?php if ($is_password) { ?>
    <tr>
        <th>비밀번호<span class="sound_only">필수</span></th>
        <td><input type="password" name="wr_password" id="wr_password" <?php echo $password_required ?> class="frm_input <?php echo $password_required ?>" placeholder="비밀번호"></td>
    </tr>
    <?php } ?>

    <?php if ($is_email) { ?>
    <tr>
        <th>이메일</th>
        <td><input type="text" name="wr_email" value="<?php echo $email ?>" id="wr_email" class="frm_input email " placeholder="이메일"></td>
    </tr>
    <?php } ?>

    <?php if ($is_homepage) { ?>
    <tr>
        <th>홈페이지</th>
        <td><input type="text" name="wr_homepage" value="<?php echo $homepage ?>" id="wr_homepage" class="frm_input full_input" size="50" placeholder="홈페이지"></td>
    </tr>
    <?php } ?>

    <?php if ($option) { ?>
    <tr>
        <th>옵션</th>
        <td><?php echo $option ?></td>
    </tr>
    <?php } ?>

    <tr>
        <th>제목<span class="sound_only">필수</span></th>
        <td>
            <div id="autosave_wrapper write_div">
                <input type="text" name="wr_subject" value="<?php echo $subject ?>" id="wr_subject" required class="frm_input required" size="50" maxlength="255" placeholder="제목">
                <?php if ($is_member) { // 임시 저장된 글 기능 ?>
                <script src="<?php echo G5_JS_URL; ?>/autosave.js"></script>
                <?php if($editor_content_js) echo $editor_content_js; ?>
                <button type="button" id="btn_autosave" class="btn_frmline">임시 저장된 글 (<span id="autosave_count"><?php echo $autosave_count; ?></span>)</button>
                <div id="autosave_pop">
                    <strong>임시 저장된 글 목록</strong>
                    <ul></ul>
                    <div><button type="button" class="autosave_close">닫기</button></div>
                </div>
                <?php } ?>
            </div>
        </td>
    </tr>

    <tr>
        <th>기간<span class="sound_only">필수</span></th>
	<td>




<!-- 종일 여부 -->

<div class="dtp-thema"  id="time-inputs">
<input type="hidden" name="wr_5" value="0">
        <label style="margin-right:8px">
<!--input checked type="checkbox" id="is_all_day" name="wr_5" value="1" <?php echo $is_all_day ? 'checked' : ''; ?>-->
<input type="hidden" id="wr_5_hidden" name="wr_5" value="<?php echo $is_all_day ? '1' : '0'; ?>">
<input type="checkbox" id="is_all_day" <?php echo $is_all_day ? 'checked' : ''; ?>>
            종일
	</label>
  <label for="wr_1">시작일시</label>
<input type="text" name="wr_1" value="<?php echo $wr_1_val; ?>" readonly id="wr_1" required class="frm_input required" size="8" maxlength="8"> ~
  <label for="wr_2">종료일시</label>
<input type="text" name="wr_2" value="<?php echo $wr_2_val; ?>" readonly id="wr_2" required class="frm_input required" size="8" maxlength="8">
<input type="hidden" name="wr_3" value="<?php echo isset($write['wr_3']) ? $write['wr_3'] : '#ffffff'; ?>" id="wr_3" class="frm_input jscolor {hash:true}" size="8">
<input type="hidden" name="wr_4" value="<?php echo isset($write['wr_4']) ? $write['wr_4'] : '#46E086'; ?>" id="wr_4" class="frm_input jscolor {hash:true}" size="8">
</div>

        </td>
    </tr>

    <tr>
        <th>내용<span class="sound_only">필수</span></th>
        <td>
            <div class="wr_content <?php echo $is_dhtml_editor ? $config['cf_editor'] : ''; ?>">
                <?php if($write_min || $write_max) { ?>
                <!-- 최소/최대 글자 수 사용 시 -->
                <p id="char_count_desc">이 게시판은 최소 <strong><?php echo $write_min; ?></strong>글자 이상, 최대 <strong><?php echo $write_max; ?></strong>글자 이하까지 글을 쓰실 수 있습니다.</p>
                <?php } ?>
                <?php echo $editor_html; // 에디터 사용시는 에디터로, 아니면 textarea 로 노출 ?>
                <?php if($write_min || $write_max) { ?>
                <!-- 최소/최대 글자 수 사용 시 -->
                <div id="char_count_wrap"><span id="char_count"></span>글자</div>
                <?php } ?>
            </div>
        </td>
    </tr>


    <?php for ($i=0; $is_file && $i<$file_count; $i++) { ?>
    <tr>
        <th>파일 <?php echo $i+1 ?></th>
        <td>
            <input type="file" name="bf_file[]" id="bf_file_<?php echo $i+1 ?>" title="파일첨부 <?php echo $i+1 ?> : 용량 <?php echo $upload_max_filesize ?> 이하만 업로드 가능" class="frm_file ">

            <?php if ($is_file_content) { ?>
            <input type="text" name="bf_content[]" value="<?php echo ($w == 'u') ? $file[$i]['bf_content'] : ''; ?>" title="파일 설명을 입력해주세요." class="full_input frm_input" size="50" placeholder="파일 설명을 입력해주세요.">
            <?php } ?>

            <?php if($w == 'u' && $file[$i]['file']) { ?>
            <span class="file_del">
                <input type="checkbox" id="bf_file_del<?php echo $i ?>" name="bf_file_del[<?php echo $i;  ?>]" value="1"> <label for="bf_file_del<?php echo $i ?>"><?php echo $file[$i]['source'].'('.$file[$i]['size'].')';  ?> 파일 삭제</label>
            </span>
            <?php } ?>
        </td>
    </tr>
    <?php } ?>


    <?php if ($is_use_captcha) { //자동등록방지  ?>
    <tr>
        <th>자동등록방지</th>
        <td><?php echo $captcha_html ?></td>
    </tr>
    <?php } ?>
    </tbody>
    </table>

    <div class="btn_confirm write_div">
        <a href="./board.php?bo_table=<?php echo $bo_table ?>" class="btn_cancel btn">취소</a>
        <input type="submit" value="작성완료" id="btn_submit" accesskey="s" class="btn_submit btn">
    </div>
    </form>
</section>

    <script>

    <?php if($write_min || $write_max) { ?>
    // 글자수 제한
    var char_min = parseInt(<?php echo $write_min; ?>); // 최소
    var char_max = parseInt(<?php echo $write_max; ?>); // 최대
    check_byte("wr_content", "char_count");

    $(function() {
        $("#wr_content").on("keyup", function() {
            check_byte("wr_content", "char_count");
        });
    });

    <?php } ?>
    function html_auto_br(obj)
    {
        if (obj.checked) {
            result = confirm("자동 줄바꿈을 하시겠습니까?\n\n자동 줄바꿈은 게시물 내용중 줄바뀐 곳을<br>태그로 변환하는 기능입니다.");
            if (result)
                obj.value = "html2";
            else
                obj.value = "html1";
        }
        else
            obj.value = "";
    }

    function fwrite_submit(f)
    {
        <?php echo $editor_js; // 에디터 사용시 자바스크립트에서 내용을 폼필드로 넣어주며 내용이 입력되었는지 검사함   ?>

        var subject = "";
        var content = "";
        $.ajax({
            url: g5_bbs_url+"/ajax.filter.php",
            type: "POST",
            data: {
                "subject": f.wr_subject.value,
                "content": f.wr_content.value
            },
            dataType: "json",
            async: false,
            cache: false,
            success: function(data, textStatus) {
                subject = data.subject;
                content = data.content;
            }
        });

        if (subject) {
            alert("제목에 금지단어('"+subject+"')가 포함되어있습니다");
            f.wr_subject.focus();
            return false;
        }

        if (content) {
            alert("내용에 금지단어('"+content+"')가 포함되어있습니다");
            if (typeof(ed_wr_content) != "undefined")
                ed_wr_content.returnFalse();
            else
                f.wr_content.focus();
            return false;
        }

        if (document.getElementById("char_count")) {
            if (char_min > 0 || char_max > 0) {
                var cnt = parseInt(check_byte("wr_content", "char_count"));
                if (char_min > 0 && char_min > cnt) {
                    alert("내용은 "+char_min+"글자 이상 쓰셔야 합니다.");
                    return false;
                }
                else if (char_max > 0 && char_max < cnt) {
                    alert("내용은 "+char_max+"글자 이하로 쓰셔야 합니다.");
                    return false;
                }
            }
        }

	<?php echo $captcha_js; // 캡챠 사용시 자바스크립트에서 입력된 캡챠를 검사함  ?>


        document.getElementById("btn_submit").disabled = "disabled";
        return true;
    }
    </script>
<!-- } 게시물 작성/수정 끝 -->
