<?php
if (!defined('_GNUBOARD_'))
  exit; // 개별 페이지 접근 불가
//include_once(G5_PLUGIN_PATH.'/jquery-ui/datepicker.php');

// add_stylesheet('css 구문', 출력순서); 숫자가 작을 수록 먼저 출력됨
add_stylesheet('<link rel="stylesheet" href="' . $board_skin_url . '/wzappend.css">', 0);
add_stylesheet('<link rel="stylesheet" href="https://code.jquery.com/ui/1.13.2/themes/base/jquery-ui.css">', 1);
add_stylesheet('<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.min.css">', 2);
add_stylesheet('<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">', 4);
add_stylesheet('<link rel="stylesheet" href="' . $board_skin_url . '/dtp-theme.css">', 10);
add_stylesheet('<link rel="stylesheet" href="' . $board_skin_url . '/style.css">', 8);
?>

<style>
  .frm_input {
    height: 30px;
    width: 150px;
  }

  button.btn_frmline {
    display: inline-block;
    width: 180px;
    padding-top: 3px;
    padding-bottom: 5px;
    height: auto;
    border: 0;
    background: #4338ca;
    border-radius: 4px;
    color: #fff;
    text-decoration: none;
    vertical-align: top;
  }

  #wr_subject {
    width: 400px;
  }
</style>
<!-- 밑에 add_stylesheet 함수를 사용하지 않는이유은 가끔 홈페이지 개발시 오류로 add_stylesheet 함수가 먹지 않는 현상으로 인해 사용하지 않습니다. -->


<script src="<?php echo $board_skin_url; ?>/wz.js/jscolor.min.js"></script>

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


</script>

<script>
  $(function () {

    // ISO 8601 → jQuery UI DateTimePicker 포맷으로 변환
    function formatToDateTimePicker(val) {
      if (!val) return "";

      // ISO 포맷 정리: 20250916T13:00:00+09:00 → 2025-09-16 13:00
      val = val.replace("T", " ").replace(/\+09:00/, "");

      // YYYYMMDD HH:mm:ss → YYYY-MM-DD HH:mm 으로 변환
      if (/^[0-9]{8}/.test(val)) {
        let y = val.substr(0, 4);
        let m = val.substr(4, 2);
        let d = val.substr(6, 2);
        let time = val.substr(9, 5); // HH:mm
        return `${y}-${m}-${d} ${time}`;
      }

      return val.trim();
    }

    function initDateTime() {
      $("#wr_1, #wr_2").datepicker("destroy").datetimepicker({
        dateFormat: "yy-mm-dd",
        timeFormat: "HH:mm",
        stepMinute: 10,
        controlType: "select",
        oneLine: true,
        showSecond: false
      });

      //  값이 있으면 강제로 Date 객체로 변환 후 setDate
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

    let wr1 = $("#wr_1").val();
    let wr2 = $("#wr_2").val();

    $("#wr_1").val(formatToDateTimePicker(wr1));
    $("#wr_2").val(formatToDateTimePicker(wr2));

    $("#wr_1, #wr_2").datetimepicker({
      dateFormat: "yy-mm-dd",
      timeFormat: "HH:mm",
      stepMinute: 10,
      controlType: "select",
      oneLine: true
    });

    console.log(event.allDay, event.start, event.end);


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

    //  안전하게 문자열 → Date 변환하는 함수
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

    var isEdit = <?php echo ($w == 'u') ? 'true' : 'false'; ?>;
    function syncEndWithStart(  ) {
      var startVal = $("#wr_1").val();
      if (!startVal) return;

      // wr_2 값이 이미 있으면 그대로 사용
        if ($("#wr_2").val()) { return; }

      var dateOnly = startVal.split("T")[0].split(" ")[0];
      if ($("#is_all_day").is(":checked")) {
          if (isEdit) {
                var d = new Date(dateOnly);
                var y = d.getFullYear();
                var m = ("0" + (d.getMonth() + 1)).slice(-2);
                var day = ("0" + d.getDate()).slice(-2);
                $("#wr_2").val(y + "-" + m + "-" + day);
             } else {
                // 신규등록 → FullCalendar 호환 위해 +1일
                var d = new Date(dateOnly);
                d.setDate(d.getDate() + 1);
                var y = d.getFullYear();
                var m = ("0" + (d.getMonth() + 1)).slice(-2);
                var day = ("0" + d.getDate()).slice(-2);
                $("#wr_2").val(y + "-" + m + "-" + day);
             }
        } else {
            $("#wr_2").val(startVal);
        }
    }


    //  초기 상태 적용
    if ($("#is_all_day").is(":checked")) initDateOnly();
    else initDateTime();

    //  종일 토글 (오타 주의: $("#is_all_day"))
    $("#is_all_day").on("change", function () {
      $("#wr_5_hidden").val(this.checked ? "1" : "0");
      if (this.checked) {
        initDateOnly();
      } else {
        initDateTime();
      }
    });


    //  분류 선택시 배경색 자동 설정
    $("#ca_name").on("change", function () {
      var cat = $(this).val();
      var color = categoryColors[cat] || "#46E086"; // 기본색: 초록

      console.log("선택된 카테고리:", cat);     //  선택된 카테고리 출력
      console.log("적용된 색상:", color);       //  적용된 색상 출력

      $("#wr_4").val(color);
      if (typeof jscolor !== "undefined" && $("#wr_4")[0].jscolor) {
        $("#wr_4")[0].jscolor.fromString(color);
      }
    });


    //  시작 변경 시: 종일이면 종료 자동 동기화
    // datepicker/datetimepicker 둘 다 change 이벤트 발생
    $("#wr_1").on("change", function () {
      var startVal = $("#wr_1").val();
      var endVal = $("#wr_2").val();

      if (!startVal) return;

      // Date 객체로 변환
      var startDate = new Date(startVal.replace(/-/g, "/"));
      var endDate = new Date(endVal.replace(/-/g, "/"));

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


    $(".category-buttons button").on("click", function (e) {
      // 모든 버튼 active 해제 후 현재 버튼만 active
      $(".category-buttons button").removeClass("active");
      // 모든 버튼을 원래 스타일로 초기화
  $(".category-buttons button").removeClass("active").css({
    "background": "",
    "border-color": "",
    "color": ""
  });

      // 선택된 카테고리 값 가져오기
      var catVal = $(this).data("value");
        var color = categoryColors[catVal] || "#007bff"; // fallback
      console.log("선택된 카테고리:", catVal);

    $(this).addClass("active").css({
      "background": color,
      "border-color": color,
      "color": "#fff"
    });
      // 숨겨진 select 값도 갱신
      $("#ca_name").val(catVal).trigger("change");
    });
  });



</script>

<?php
// URL 파라미터
$param_wr1 = $_GET['wr_1'] ?? '';
$param_wr2 = $_GET['wr_2'] ?? '';
$param_allday = $_GET['allday'] ?? '';

// -----------------------------
// 날짜 포맷 변환 함수
// -----------------------------
function normalize_date($date)
{
  if (empty($date))
    return '';
  // YYYYMMDD → YYYY-MM-DD
  if (preg_match('/^\d{8}$/', $date)) {
    return substr($date, 0, 4) . '-' . substr($date, 4, 2) . '-' . substr($date, 6, 2);
  }
  // YYYY-MM-DD HH:mm:ss or ISO → 그대로 숫자/문자만 필터링
  return preg_replace("/[^0-9:\-T ]/", "", $date);
}

// -----------------------------
// 시작일 wr_1
// -----------------------------
if (!empty($write['wr_1'])) {
  $wr_1_val = normalize_date($write['wr_1']);
} elseif (!empty($param_wr1)) {
  $wr_1_val = normalize_date($param_wr1);
} else {
  $wr_1_val = date('Y-m-d');
}

// -----------------------------
// 종료일 wr_2
// -----------------------------
if (!empty($write['wr_2'])) {
  $wr_2_val = normalize_date($write['wr_2']);
} elseif (!empty($param_wr2)) {
  $wr_2_val = normalize_date($param_wr2);
} else {
  $wr_2_val = $wr_1_val;
}

// -----------------------------
// 종일 여부
// -----------------------------
// 글 값이 있으면 그걸 우선, 아니면 파라미터 체크
$is_all_day = (
  (!empty($write['wr_5']) && $write['wr_5'] === '1')
  || $param_allday === '1'
);

//  YYYYMMDD 형식으로 wr_1, wr_2가 들어온 경우 자동 종일 처리
if (!$is_all_day) {
  if (preg_match('/^\d{8}$/', $param_wr1) && preg_match('/^\d{8}$/', $param_wr2)) {
    $is_all_day = true;
  }
}
?>

<div class="form-wrapper">

  <!-- 분류 -->
  <div class="form-group">
    <label>분류 <span class="required">*</span></label>
    <div class="category-buttons">
      <button type="button" data-value="유지보수" class="<?= ($write['ca_name'] ?? '')==='유지보수'?'active':'' ?>">유지보수</button>
      <button type="button" data-value="하드웨어" class="<?= ($write['ca_name'] ?? '')==='하드웨어'?'active':'' ?>">하드웨어</button>
      <button type="button" data-value="데이터허브" class="<?= ($write['ca_name'] ?? '')==='데이터허브'?'active':'' ?>">데이터허브</button>
      <button type="button" data-value="응급의료" class="<?= ($write['ca_name'] ?? '')==='응급의료'?'active':'' ?>">응급의료</button>
    </div>
    <input type="hidden" name="ca_name" id="ca_name" value="<?= $write['ca_name'] ?? '' ?>">
  </div>

  <!-- 제목 -->
  <div class="form-group">
    <label for="wr_subject">제목 <span class="required">*</span></label>
    <input type="text" name="wr_subject" value="<?= $subject ?>" id="wr_subject" 
           class="form-input" required placeholder="제목을 입력하세요">
  </div>

  <!-- 기간 -->
  <div class="form-group">
    <label>기간 <span class="required">*</span></label>
    <div class="date-range">
      <label><input type="checkbox" id="is_all_day" <?= $is_all_day?'checked':'' ?>> 종일</label>
      <input type="hidden" id="wr_5_hidden" name="wr_5" value="<?= $is_all_day?'1':'0' ?>">
      <input type="text" name="wr_1" id="wr_1" value="<?= $wr_1_val ?>" class="form-input" placeholder="시작일시" required>
      <span>~</span>
      <input type="text" name="wr_2" id="wr_2" value="<?= $wr_2_val ?>" class="form-input" placeholder="종료일시" required>
    </div>
  </div>

  <!-- 내용 -->
  <div class="form-group">
    <label for="wr_content">내용 <span class="required">*</span></label>
    <div class="editor-wrapper <?= $is_dhtml_editor ? $config['cf_editor'] : ''; ?>">
      <?= $editor_html ?>
    </div>
  </div>

  <!-- 파일 첨부 -->
  <?php for ($i=0; $is_file && $i<$file_count; $i++) { ?>
    <div class="form-group">
      <label>파일 <?= $i+1 ?></label>
      <input type="file" name="bf_file[]" id="bf_file_<?= $i+1 ?>" class="form-input">
      <?php if ($is_file_content) { ?>
        <input type="text" name="bf_content[]" value="<?= ($w=='u')?$file[$i]['bf_content']:'' ?>" 
               placeholder="파일 설명" class="form-input">
      <?php } ?>
    </div>
  <?php } ?>

  <!-- 액션 버튼 -->
  <div class="form-actions">
    <a href="./board.php?bo_table=<?= $bo_table ?>" class="btn cancel">취소</a>
    <button type="submit" id="btn_submit" class="btn submit">작성완료</button>
  </div>
</div>

<style>
.form-wrapper { max-width:720px; margin:0 auto; }
.form-group { margin-bottom:18px; }
.form-group label { font-weight:600; display:block; margin-bottom:6px; }
.form-input { width:100%; max-width:400px; padding:8px 10px; border:1px solid #ddd; border-radius:6px; }
.date-range { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.category-buttons button {
  padding:6px 14px; border:1px solid #ddd; border-radius:6px;
  background:#f9f9f9; cursor:pointer; margin-right:6px;
}
.category-buttons button.active { background:#4A90E2; color:#fff; border-color:#4A90E2; }
.form-actions { display:flex; justify-content:flex-end; gap:10px; margin-top:24px; }
.btn { padding:8px 16px; border-radius:6px; font-weight:600; text-decoration:none; }
.btn.cancel { background:#eee; color:#333; }
.btn.submit { background:#4A90E2; color:#fff; border:none; }
.required { color:red; }
</style>

<script>

  <?php if ($write_min || $write_max) { ?>
    // 글자수 제한
    var char_min = parseInt(<?php echo $write_min; ?>); // 최소
    var char_max = parseInt(<?php echo $write_max; ?>); // 최대
    check_byte("wr_content", "char_count");

    $(function () {
      $("#wr_content").on("keyup", function () {
        check_byte("wr_content", "char_count");
      });
    });

  <?php } ?>
  function html_auto_br(obj) {
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

  function fwrite_submit(f) {
    <?php echo $editor_js; // 에디터 사용시 자바스크립트에서 내용을 폼필드로 넣어주며 내용이 입력되었는지 검사함   ?>

    var subject = "";
    var content = "";
    $.ajax({
      url: g5_bbs_url + "/ajax.filter.php",
      type: "POST",
      data: {
        "subject": f.wr_subject.value,
        "content": f.wr_content.value
      },
      dataType: "json",
      async: false,
      cache: false,
      success: function (data, textStatus) {
        subject = data.subject;
        content = data.content;
      }
    });

    if (subject) {
      alert("제목에 금지단어('" + subject + "')가 포함되어있습니다");
      f.wr_subject.focus();
      return false;
    }

    if (content) {
      alert("내용에 금지단어('" + content + "')가 포함되어있습니다");
      if (typeof (ed_wr_content) != "undefined")
        ed_wr_content.returnFalse();
      else
        f.wr_content.focus();
      return false;
    }

    if (document.getElementById("char_count")) {
      if (char_min > 0 || char_max > 0) {
        var cnt = parseInt(check_byte("wr_content", "char_count"));
        if (char_min > 0 && char_min > cnt) {
          alert("내용은 " + char_min + "글자 이상 쓰셔야 합니다.");
          return false;
        }
        else if (char_max > 0 && char_max < cnt) {
          alert("내용은 " + char_max + "글자 이하로 쓰셔야 합니다.");
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
