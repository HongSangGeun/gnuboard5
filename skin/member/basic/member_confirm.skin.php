<?php
if (!defined('_GNUBOARD_')) exit;

// 스킨 CSS 로드(아래 2번 CSS를 style.css에 붙여주세요)
add_stylesheet('<link rel="stylesheet" href="'.$member_skin_url.'/style.css">', 0);

$is_leave = ($url === 'member_leave.php'); // 회원탈퇴 확인 화면인지 여부
?>
<div id="mb_confirm" class="mbskin confirm-skin<?php echo $is_leave ? ' is-leave' : ''; ?>">

  <div class="confirm-card">
    <div class="confirm-header">
      <img class="confirm-logo" src="http://192.168.4.104/img/logo_gn.svg" alt="경남 스마트시티 관리포털">
      <h1 class="confirm-title"><?php echo get_text($g5['title']); ?></h1>
      <p class="confirm-desc">
        <strong>비밀번호를 한 번 더 입력해 주세요.</strong><br>
        <?php if ($is_leave) { ?>
          비밀번호를 입력하시면 <span class="emph">회원탈퇴</span>가 완료됩니다.
        <?php } else { ?>
          회원님의 정보를 안전하게 보호하기 위해 비밀번호를 재확인합니다.
        <?php } ?>
      </p>
    </div>

    <form name="fmemberconfirm" action="<?php echo $url ?>" onsubmit="return fmemberconfirm_submit(this);" method="post" autocomplete="off">
      <input type="hidden" name="mb_id" value="<?php echo $member['mb_id'] ?>">
      <input type="hidden" name="w" value="u">

      <div class="confirm-body">
        <div class="form-row id-row">
          <label>회원아이디</label>
          <div class="id-box" id="mb_confirm_id"><?php echo get_text($member['mb_id']); ?></div>
        </div>

        <div class="form-row">
          <label for="confirm_mb_password">비밀번호 <span class="req">*</span></label>
          <input type="password"
                 name="mb_password"
                 id="confirm_mb_password"
                 class="frm_input"
                 required
                 maxlength="50"
                 placeholder="비밀번호를 입력하세요"
                 autocomplete="current-password">
        </div>
      </div>

      <div class="confirm-actions">
        <button type="submit" id="btn_submit" class="btn btn-primary">확인</button>
        <a href="javascript:history.back();" class="btn btn-ghost">취소</a>
      </div>
    </form>
  </div>
</div>

<script>
function fmemberconfirm_submit(f){
  var btn = document.getElementById('btn_submit');
  if(btn) btn.disabled = true;
  return true;
}
</script>
