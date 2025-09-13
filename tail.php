<?php
if (!defined('_GNUBOARD_'))
    exit; // 개별 페이지 접근 불가

if (defined('G5_THEME_PATH')) {
    require_once(G5_THEME_PATH . '/tail.php');
    return;
}

if (G5_IS_MOBILE) {
    include_once(G5_MOBILE_PATH . '/tail.php');
    return;
}
?>

</div>
</div>

</div>
<!-- } 콘텐츠 끝 -->

<hr>

<!-- 하단 시작 { -->
<style>
    .btn-mobile {
        display: inline-block;
        padding: 10px 16px;
        border-radius: 8px;
        background: #4f46e5;
        color: #fff;
        text-decoration: none;
        font-weight: 600;
    }

    .btn-mobile:hover {
        background: #4338ca;
    }
</style>
<div id="ft">

    <div id="ft_wr">
        <div id="ft_link" class="ft_cnt_m">
            <!--a href="<?php echo get_pretty_url('content', 'company'); ?>">회사소개</a>
            <a href="<?php echo get_pretty_url('content', 'privacy'); ?>">개인정보처리방침</a>
            <a href="<?php echo get_pretty_url('content', 'provision'); ?>">서비스이용약관</a>
        <a href="<?php echo get_device_change_url(); ?>">모바일버전</a-->
            <!--a href="<?php echo get_device_change_url(); ?>" class="btn-mobile">모바일버전</a-->
            <div class="logo"><img src="<?= G5_URL ?>/img/logo_gray.svg"
                    style="width: 190px; height: auto; padding-bottom: 7px" alt="경상남도 스마트시티 데이터포털 로고 이미지"></div>
            <p class="ft_info">
                주민번호, 계좌번호 등 민감 정보는 절대 게시하지 마세요.<br>
                ⓒ 경남도청. All Rights Reserved.<br>
            </p>
            <a href="<?php echo get_device_change_url(); ?>">모바일버전</a>
        </div>
        <div id="ft_company" class="ft_cnt">
            <!--h2>사이트 정보</h2-->
        </div>
        <!--?php
        //공지사항
        // 이 함수가 바로 최신글을 추출하는 역할을 합니다.
        // 사용방법 : latest(스킨, 게시판아이디, 출력라인, 글자수);
        // 테마의 스킨을 사용하려면 theme/basic 과 같이 지정
        echo latest('notice', 'notice', 4, 13);
        ?-->

        <!--?php echo visit(); // 접속자집계, 테마의 스킨을 사용하려면 스킨을 theme/basic 과 같이 지정 ?-->
    </div>
    <!-- <div id="ft_catch"><img src="<?php echo G5_IMG_URL; ?>/ft_logo.png" alt="<?php echo G5_VERSION ?>"></div> -->
    <!--div id="ft_copy">Copyright &copy; <b>소유하신 도메인.</b> All rights reserved.</div-->

    <button type="button" id="top_btn">
        <i class="fa fa-arrow-up" aria-hidden="true"></i><span class="sound_only">상단으로</span>
    </button>
    <script>
        $(function () {
            $("#top_btn").on("click", function () {
                $("html, body").animate({ scrollTop: 0 }, '500');
                return false;
            });
        });
    </script>
</div>

<?php
if (G5_DEVICE_BUTTON_DISPLAY && !G5_IS_MOBILE) { ?>
<?php
}

if ($config['cf_analytics']) {
    echo $config['cf_analytics'];
}
?>

<!-- } 하단 끝 -->

<script>
    $(function () {
        // 폰트 리사이즈 쿠키있으면 실행
        font_resize("container", get_cookie("ck_font_resize_rmv_class"), get_cookie("ck_font_resize_add_class"));
    });
</script>

<?php
include_once(G5_PATH . "/tail.sub.php");
