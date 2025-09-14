<?php
if (!defined('_GNUBOARD_')) exit;

// CSS 로드
add_stylesheet('<link rel="stylesheet" href="'.$board_skin_url.'/wzappend.css">', 0);
add_stylesheet('<link rel="stylesheet" href="https://code.jquery.com/ui/1.13.2/themes/base/jquery-ui.css">', 1);
add_stylesheet('<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.min.css">', 2);
add_stylesheet('<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR&display=swap" rel="stylesheet">', 4);
add_stylesheet('<link rel="stylesheet" href="'.$board_skin_url.'/dtp-theme.css">', 10);
add_stylesheet('<link rel="stylesheet" href="'.$board_skin_url.'/style.css">', 8);

// ---------- URL 파라미터 ----------
$param_wr1    = $_GET['wr_1']   ?? '';
$param_wr2    = $_GET['wr_2']   ?? '';
$param_allday = $_GET['allday'] ?? '';

// ---------- 날짜 포맷 정리 함수 ----------
function normalize_date($date) {
  if (empty($date)) return '';
  // YYYYMMDD → YYYY-MM-DD
  if (preg_match('/^\d{8}$/', $date)) {
    return substr($date,0,4).'-'.substr($date,4,2).'-'.substr($date,6,2);
  }
  // ISO → 공백 + 타임존 제거
  $date = str_replace('T', ' ', $date);
  $date = preg_replace('/([+-]\d{2}:\d{2}|Z)$/', '', $date);
  // 허용 문자만
  return preg_replace("/[^0-9:\- ]/", "", $date);
}

// ---------- wr_1 / wr_2 기본값 ----------
if (!empty($write['wr_1'])) {
  $wr_1_val = normalize_date($write['wr_1']);
} elseif (!empty($param_wr1)) {
  $wr_1_val = normalize_date($param_wr1);
} else {
  $wr_1_val = date('Y-m-d');
}

if (!empty($write['wr_2'])) {
  $wr_2_val = normalize_date($write['wr_2']);
} elseif (!empty($param_wr2)) {
  $wr_2_val = normalize_date($param_wr2);
} else {
  $wr_2_val = $wr_1_val;
}

// ---------- 종일 여부 ----------
$is_all_day = (!empty($write['wr_5']) && $write['wr_5'] === '1') || $param_allday === '1';
// YYYYMMDD 형식으로 wr_1/2가 들어오면 자동 종일 처리
if (!$is_all_day) {
  if (preg_match('/^\d{8}$/', $_GET['wr_1'] ?? '') && preg_match('/^\d{8}$/', $_GET['wr_2'] ?? '')) {
    $is_all_day = true;
  }
}
?>
<style>
  .frm_input { height:36px; width:150px; }
  #wr_subject { width:400px; }
</style>

<script src="<?php echo $board_skin_url;?>/wz.js/jscolor.min.js"></script>
<script src="<?php echo G5_JS_URL; ?>/jquery-1.12.4.min.js"></script>
<script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>
<script src="<?php echo G5_PLUGIN_URL; ?>/jquery-ui/jquery-ui-timepicker-addon.js"></script>

<script>
var categoryColors = {
  "유지보수": "#FF6B6B",
  "하드웨어": "#4ECDC4",
  "데이터허브": "#4A90E2",
  "응급의료": "#A29BFE",
  "집중관제": "#fef29bff"
};

$(function () {
  // ---------- 유틸 ----------
  function formatToDateTimePicker(val) {
    if (!val) return "";
    val = val.replace("T", " ").replace(/([+-]\d{2}:\d{2}|Z)$/,'').trim();

    // YYYYMMDD or YYYYMMDD HH:mm:ss → YYYY-MM-DD [HH:mm]
    if (/^\d{8}/.test(val)) {
      var y = val.substr(0,4), m = val.substr(4,2), d = val.substr(6,2);
      var time = (val.length >= 13) ? val.substr(9,5) : ""; // HH:mm
      return time ? (y+"-"+m+"-"+d+" "+time) : (y+"-"+m+"-"+d);
    }
    return val;
  }
  function parseInputDate(str) {
    if (!str) return null;
    // YYYYMMDD → YYYY-MM-DD
    if (/^\d{8}$/.test(str)) str = str.replace(/(\d{4})(\d{2})(\d{2})/, "$1-$2-$3");
    str = str.replace("T"," ").replace(/([+-]\d{2}:\d{2}|Z)$/,'').trim();
    var parts = str.split(" ");
    var ymd = (parts[0]||"").split("-");
    if (ymd.length<3) return null;
    var y = +ymd[0], m = (+ymd[1])-1, d = +ymd[2];
    var h=0, min=0;
    if (parts[1]) {
      var hm = parts[1].split(":");
      h = +(hm[0]||0); min = +(hm[1]||0);
    }
    return new Date(y,m,d,h,min);
  }

  // ---------- 픽커 모드 ----------
  function initDateTime() {
    $("#wr_1, #wr_2").datepicker("destroy").datetimepicker({
      dateFormat: "yy-mm-dd",
      timeFormat: "HH:mm",
      stepMinute: 10,
      controlType: "select",
      oneLine: true,
      showSecond: false
    });
    // 값 있으면 setDate
    var d1 = parseInputDate($("#wr_1").val()); if (d1) $("#wr_1").datetimepicker("setDate", d1);
    var d2 = parseInputDate($("#wr_2").val()); if (d2) $("#wr_2").datetimepicker("setDate", d2);
  }
  function initDateOnly() {
    $("#wr_1, #wr_2").datetimepicker("destroy").datepicker({
      dateFormat: "yy-mm-dd",
      changeMonth: true,
      changeYear: true
    });
    var d1 = parseInputDate($("#wr_1").val()); if (d1) $("#wr_1").datepicker("setDate", d1);
    var d2 = parseInputDate($("#wr_2").val()); if (d2) $("#wr_2").datepicker("setDate", d2);
    // 종일은 종료를 시작과 같은 날짜로(값이 없을 때만)
    if (!$("#wr_2").val()) {
      var dateOnly = ($("#wr_1").val()||"").split(" ")[0];
      $("#wr_2").val(dateOnly);
    }
  }

  // ---------- 동기화 ----------
  function syncEndWithStart() {
    var startVal = $("#wr_1").val();
    if (!startVal) return;
    if ($("#wr_2").val()) return; // 이미 있으면 유지

    if ($("#is_all_day").is(":checked")) {
      // 종일: 날짜만 동일하게
      $("#wr_2").val(startVal.split(" ")[0]);
    } else {
      // 부분: 시작값 그대로
      $("#wr_2").val(startVal);
    }
  }

  // ---------- 초기값 정리 ----------
  $("#wr_1").val(formatToDateTimePicker($("#wr_1").val()));
  $("#wr_2").val(formatToDateTimePicker($("#wr_2").val()));

  if ($("#is_all_day").is(":checked")) initDateOnly(); else initDateTime();
  syncEndWithStart();

  // ---------- 이벤트 바인딩 ----------
  $("#is_all_day").on("change", function(){
    $("#wr_5_hidden").val(this.checked ? "1" : "0");
    if (this.checked) initDateOnly(); else initDateTime();
    syncEndWithStart();
  });

  $("#wr_1").on("change", function(){
    var s = parseInputDate($("#wr_1").val());
    var e = parseInputDate($("#wr_2").val());
    if ($("#is_all_day").is(":checked")) {
      $("#wr_2").val(($("#wr_1").val()||"").split(" ")[0]);
    } else {
      if (s && e && e < s) $("#wr_2").val($("#wr_1").val());
    }
  });

  // 카테고리 버튼
  $(".category-buttons button").on("click", function(){
    $(".category-buttons button").removeClass("active").attr("style","");
    var cat = $(this).data("value");
    var color = categoryColors[cat] || "#46E086";
    $(this).addClass("active").css({background: color, borderColor: color, color:"#fff"});
    $("#ca_name").val(cat).trigger("change");
  });

  // 초기 카테고리 버튼 스타일
  (function initCat(){
    var current = $("#ca_name").val();
    if (!current) return;
    var $btn = $('.category-buttons button[data-value="'+current+'"]');
    if ($btn.length) {
      var color = categoryColors[current] || "#46E086";
      $btn.addClass("active").css({background: color, borderColor: color, color:"#fff"});
    }
  })();

  // 분류 변경 시 wr_4 색상 반영
  $("#ca_name").on("change", function(){
    var cat = $(this).val();
    var color = categoryColors[cat] || "#46E086";
    $("#wr_4").val(color);
    if (typeof jscolor !== "undefined" && $("#wr_4")[0] && $("#wr_4")[0].jscolor) {
      $("#wr_4")[0].jscolor.fromString(color);
    }
  });

    $("#is_all_day").on("change", function() {
    if ($(this).is(":checked")) {
      $("#allDayLabel").text("종일일정");
    } else {
      $("#allDayLabel").text("부분일정");
    }
  });

});

</script>

<style>
  .form-wrapper { max-width:96%; margin:0 auto; padding-left: 40px; }

  .form-row { display:flex; gap:20px; }
  .form-row .form-group.half { flex:1; min-width:0; }

  /* CKEditor 가로폭 풀기 */
  .form-group .editor-wrapper { flex: 1; }

  .form-group {
    display:flex; align-items:center;
    padding: 5px 60px 0px 10px ; gap:10px;
  }
  .form-group label {
    font-size:16px; font-weight:600; color:#8b8b8b; margin:6px; line-height:1.4;
  }

  .date-range { display:flex; align-items:center; gap:10px; flex-wrap:nowrap; }
  .date-range label { display:flex; align-items:center; gap:4px; margin:0; font-size:18px; font-weight:600; }
  .date-range input[type="text"] { flex:0 0 180px; }
  .date-range span { margin:0 6px; }

  .required { color:#e74c3c; }
  .form-input { width:100%; max-width:400px; height:33px; padding:4px 10px; border:1px solid #ddd; border-radius:3px; }
  .form-input:focus { border-color:#4A90E2; outline:none; box-shadow:0 0 0 3px rgba(74,144,226,.15); }

  .category-buttons { display:flex; flex-wrap:wrap; gap:8px; }
  .category-buttons button { padding:8px 14px; border:1px solid #dde3ea; border-radius:22px; font-size: 12px; background:#f7f9fc; cursor:pointer; transition:.15s; }
  .category-buttons button:hover { transform:translateY(-1px); box-shadow:0 2px 6px rgba(0,0,0,.06); }
  .category-buttons button.active { background:#4A90E2; color:#fff; border-color:#4A90E2; }

  .form-actions { display:flex; justify-content:flex-end; padding-right: 60px; padding-bottom: 20px; gap:10px; margin-top:22px; }
  .btn { padding:0px 0px; border-radius:4px; text-decoration:none; }
  .btn.cancel { background:#f0f3f7; color:#34495e; }
  .btn.submit { width: 80px; background:#4A90E2; color:#fff; border:none; }
  a.btn, .btn { font-size: 16px !important; } 
</style>

<style>
#allDayLabel {
    font-size: 18px;
}
/* 토글 스위치 스타일 */
.switch {
  position: relative;
  display: inline-block;
  width: 52px;
  height: 21px;
}
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0;
  right: 0; bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 26px;
}
.slider:before {
  position: absolute;
  content: "";
  height: 15px; width: 15px;
  left: 5px; bottom: 3px;
  background-color: white;
  transition: .2s;
  border-radius: 50%;
}
input:checked + .slider {
  background-color: #ce3945; /* 선택 시 색상 */
}
input:checked + .slider:before {
  transform: translateX(16px);
}
</style>

<section id="bo_w">
  <h2 class="sound_only"><?php echo $g5['title'] ?></h2>

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

    <?php
      $option = '';
      $option_hidden = '';
      if ($is_notice || $is_html || $is_secret || $is_mail) {
        $option = '';
        if ($is_notice) {
          $option .= "\n" . '<input type="checkbox" id="notice" name="notice" value="1" ' . $notice_checked . '>' . "\n" . '<label for="notice">공지</label>';
        }

        if ($is_html) {
          if ($is_dhtml_editor) {
            $option_hidden .= '<input type="hidden" value="html1" name="html">';
          } else {
            $option .= "\n" . '<input type="checkbox" id="html" name="html" onclick="html_auto_br(this);" value="' . $html_value . '" ' . $html_checked . '>' . "\n" . '<label for="html">HTML</label>';
          }
        }

        if ($is_secret) {
          if ($is_admin || $is_secret == 1) {
            $option .= "\n" . '<input type="checkbox" id="secret" name="secret" value="secret" ' . $secret_checked . '>' . "\n" . '<label for="secret">비밀글</label>';
          } else {
            $option_hidden .= '<input type="hidden" name="secret" value="secret">';
          }
        }

        if ($is_mail) {
          $option .= "\n" . '<input type="checkbox" id="mail" name="mail" value="mail" ' . $recv_email_checked . '>' . "\n" . '<label for="mail">답변메일받기</label>';
        }
      }

      echo $option_hidden;
    ?>

    <div class="form-wrapper">
      <!-- 분류 -->
      <div class="form-row">
        <div class="form-group">
          <label>분류 <span class="required">*</span></label>
          <div class="category-buttons">
            <button type="button" data-value="유지보수" class="<?php echo ($write['ca_name'] ?? '')==='유지보수'?'active':''; ?>">유지보수</button>
            <button type="button" data-value="하드웨어" class="<?php echo ($write['ca_name'] ?? '')==='하드웨어'?'active':''; ?>">하드웨어</button>
            <button type="button" data-value="데이터허브" class="<?php echo ($write['ca_name'] ?? '')==='데이터허브'?'active':''; ?>">데이터허브</button>
            <button type="button" data-value="응급의료" class="<?php echo ($write['ca_name'] ?? '')==='응급의료'?'active':''; ?>">응급의료</button>
            <button type="button" data-value="집중관제" class="<?php echo ($write['ca_name'] ?? '')==='집중관제'?'active':''; ?>">집중관제</button>
          </div>
          <input type="hidden" name="ca_name" id="ca_name" value="<?php echo $write['ca_name'] ?? ''; ?>">
        </div>
      </div>

      <!-- 제목 -->
      <div class="form-group">
        <label for="wr_subject">제목 <span class="required">*</span></label>
        <input type="text" name="wr_subject" value="<?php echo $subject ?>" id="wr_subject" class="form-input" required maxlength="255" placeholder="제목을 입력하세요">
      </div>

      <!-- 기간 -->
      <div class="form-group">
        <label>기간 <span class="required">*</span></label>
        <div class="date-range">
          <input type="hidden" id="wr_5_hidden" name="wr_5" value="<?php echo $is_all_day ? '1' : '0'; ?>">
          <input type="text" name="wr_1" id="wr_1" value="<?php echo $wr_1_val; ?>" class="form-input" placeholder="시작일시" required>
          <span>~</span>
          <input type="text" name="wr_2" id="wr_2" value="<?php echo $wr_2_val; ?>" class="form-input" placeholder="종료일시" required>
          <input type="hidden" name="wr_3" value="<?php echo isset($write['wr_3']) ? $write['wr_3'] : '#ffffff'; ?>" id="wr_3">
          <input type="hidden" name="wr_4" value="<?php echo isset($write['wr_4']) ? $write['wr_4'] : '#46E086'; ?>" id="wr_4">
          <div class="form-group">
  <label class="switch">
    <input type="checkbox" id="is_all_day" name="wr_5" value="1" <?php echo $is_all_day ? 'checked' : ''; ?>>
    <span class="slider round"></span>
  </label>
  <span id="allDayLabel"><?php echo $is_all_day ? '종일일정' : '부분일정'; ?></span>
</div>
        </div>
      </div>

      <!-- 내용 -->
      <div class="form-group">
        <label for="wr_content">내용 <span class="required">*</span></label>
        <div class="editor-wrapper <?php echo $is_dhtml_editor ? $config['cf_editor'] : ''; ?>">
          <?php echo $editor_html; ?>
        </div>
      </div>

      <!-- 파일 첨부 -->
      <?php for ($i=0; $is_file && $i<$file_count; $i++) { ?>
        <div class="form-group">
          <label>파일 <?php echo $i+1 ?></label>
          <input type="file" name="bf_file[]" id="bf_file_<?php echo $i+1 ?>" class="form-input frm_file">
          <?php if ($is_file_content) { ?>
            <input type="text" name="bf_content[]" value="<?php echo ($w=='u')?$file[$i]['bf_content']:'' ?>" placeholder="파일 설명" class="form-input">
          <?php } ?>
        </div>
      <?php } ?>

      <!-- 액션 버튼 -->
      <div class="form-actions">
        <a href="./board.php?bo_table=<?php echo $bo_table ?>" class="btn cancel">취소</a>
        <button type="submit" id="btn_submit" accesskey="s" class="btn submit">작성완료</button>
      </div>
    </div>
  </form>

  <script>
  <?php if($write_min || $write_max) { ?>
    var char_min = parseInt(<?php echo $write_min; ?>);
    var char_max = parseInt(<?php echo $write_max; ?>);
    check_byte("wr_content", "char_count");
    $(function(){ $("#wr_content").on("keyup", function(){ check_byte("wr_content", "char_count"); }); });
  <?php } ?>

  function html_auto_br(obj) {
    if (obj.checked) {
      result = confirm("자동 줄바꿈을 하시겠습니까?\n\n자동 줄바꿈은 게시물 내용중 줄바뀐 곳을<br>태그로 변환하는 기능입니다.");
      obj.value = result ? "html2" : "html1";
    } else obj.value = "";
  }

  function fwrite_submit(f) {
    <?php echo $editor_js; ?>

    var subject = "", content = "";
    $.ajax({
      url: g5_bbs_url+"/ajax.filter.php",
      type:"POST",
      data:{ subject:f.wr_subject.value, content:f.wr_content.value },
      dataType:"json", async:false, cache:false,
      success:function(d){ subject=d.subject; content=d.content; }
    });

    if (subject) { alert("제목에 금지단어('"+subject+"')가 포함되어있습니다"); f.wr_subject.focus(); return false; }
    if (content) { alert("내용에 금지단어('"+content+"')가 포함되어있습니다");
      if (typeof(ed_wr_content)!="undefined") ed_wr_content.returnFalse(); else f.wr_content.focus(); return false; }

    if (document.getElementById("char_count")) {
      var cnt = parseInt(check_byte("wr_content","char_count"));
      if (<?php echo (int)$write_min; ?> > 0 && cnt < <?php echo (int)$write_min; ?>) { alert("내용은 <?php echo (int)$write_min; ?>글자 이상 쓰셔야 합니다."); return false; }
      if (<?php echo (int)$write_max; ?> > 0 && cnt > <?php echo (int)$write_max; ?>) { alert("내용은 <?php echo (int)$write_max; ?>글자 이하로 쓰셔야 합니다."); return false; }
    }

    <?php echo $captcha_js; ?>

    document.getElementById("btn_submit").disabled = "disabled";
    return true;
  }
  </script>
</section>
