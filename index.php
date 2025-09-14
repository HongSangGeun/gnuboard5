<?php
include_once('./_common.php');

define('_INDEX_', true);
if (!defined('_GNUBOARD_')) exit; // 개별 페이지 접근 불가

if (!$is_member) {
    goto_url(G5_BBS_URL.'/login.php?url='.urlencode($_SERVER['REQUEST_URI']));
}

if(defined('G5_THEME_PATH')) {
    require_once(G5_THEME_PATH.'/index.php');
    return;
}

if (G5_IS_MOBILE) {
    include_once(G5_MOBILE_PATH.'/index.php');
    return;
}

include_once(G5_PATH.'/head.php');
// 메인화면 게시판 설정 불러오기
//$sql = "SELECT * FROM g5_portal_main ORDER BY pm_id ASC";
//$result = sql_query($sql);
    //  최신글
    $sql = " select bo_table
                from `{$g5['board_table']}` a left join `{$g5['group_table']}` b on (a.gr_id=b.gr_id)
                where a.bo_device <> 'mobile' ";
    if(!$is_admin)
        $sql .= " and a.bo_use_cert = '' ";
    $sql .= " and a.bo_table not in ('notice', 'gallery', 'schedule') ";     //공지사항과 갤러리 게시판은 제외
    $sql .= " order by b.gr_order, a.bo_order ";
    $result = sql_query($sql);

?>

<div class="latest-grid">
<?php
for ($i=0; $row=sql_fetch_array($result); $i++) {
$lt_style = '';
if ($i%3 !== 0 ) $lt_style = "margin-left:2%";
?>
    <div class="latest-card">
        <?php echo latest('basic', $row['bo_table'], 6, 24); ?>
        <a href="<?php echo G5_BBS_URL; ?>/member_confirm.php?url=<?php echo $bo_table; ?>" class="more-link">
        </a>
    </div>


<?php } ?>
</div>

<link rel="stylesheet" href="css/index.css">
<div class="col-md-6">
  <div class="card">
    <div class="card-header">
      <!--a href="<?php echo G5_BBS_URL ?>/board.php?bo_table=schedule" class="more">더보기</a-->
    </div>
    <div class="card-body">
      <div id="calendar"></div>
    </div>
  </div>
</div>

<div class="calendar-legend">
<div class="legend-left">
  <span class="legend-item" data-category="" style="background:#999;"></span> 전체
  <span class="legend-item" data-category="유지보수" style="background:#FF6B6B;"></span> 유지보수
  <span class="legend-item" data-category="하드웨어" style="background:#4ECDC4;"></span> 하드웨어
  <span class="legend-item" data-category="데이터허브" style="background:#4A90E2;"></span> 데이터허브
  <span class="legend-item" data-category="응급의료" style="background:#A29BFE;"></span> 응급의료
  <span class="legend-item" data-category="집중관제" style="background:#fef29bff;"></span> 집중관제
  <span class="legend-item" data-category="기타" style="background:#7C7C7C;"></span> 기타
</div>
  <div class="legend-right">
    <a href="<?php echo G5_BBS_URL; ?>/write.php?bo_table=schedule" class="btn-add-event">+ 일정 등록하기</a>
  </div>
</div>

<!-- FullCalendar 라이브러리 -->
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@fullcalendar/google-calendar@6.1.15/index.global.min.js"></script>



<script>
document.addEventListener('DOMContentLoaded', function () {
  // 1) 캘린더 엘리먼트 확보
  var calendarEl = document.getElementById('calendar');   // ← 이 줄이 먼저!
  if (!calendarEl) return; // 안전장치

  // 2) 전역처럼 쓸 선택 상태
  var currentCategory = null;

  // 3) 캘린더 생성
  var calendar = new FullCalendar.Calendar(calendarEl, {
  initialView: 'dayGridMonth',
  locale: 'ko',
  slotMinTime: "07:00:00", // 오전 8시부터
  slotMaxTime: "20:30:00", // 오후 8시까지만 표시
  expandRows: false,   // 행이 꽉 차도록 늘림
  contentHeight: 'auto',
    locale: 'ko',
    googleCalendarApiKey: 'AIzaSyBDLbVPdJoidVskOO7iA7oeaQ5mm5QL7Qk',
    height: 600,
    headerToolbar: {
      left: 'prev,today,next',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    navLinks: false,
    eventSources: [
  {
    url: "<?php echo $board_skin_url; ?>/schedule/get-events1.php?bo_table=schedule",
    color: '#3788d8',
    textColor: '#fff'
  },
  {
    googleCalendarId: 'ko.south_korea#holiday@group.v.calendar.google.com',
    color: '#FF5C5C',
    textColor: '#fff',
    className: 'holiday-event',
    display: 'list-item',   // ✅ 점 스타일
    eventDataTransform: function(ev) {
        delete ev.url;
        return ev;
      }
  }
],

// 날짜 클릭 → 글쓰기로 (YYYYMMDD)
    dateClick: function(info) {
      var yyyymmdd = info.dateStr.replace(/-/g, "");
      window.location.href = "<?php echo G5_BBS_URL; ?>/write.php?bo_table=schedule&wr_1=" + yyyymmdd + "&wr_2=" + yyyymmdd;
    }, 
    // 일정 클릭 → 상세 새창
    eventClick: function(info) {
      if (info.event.url) {
        window.location.href(info.event.url, "_blank");
        info.jsEvent.preventDefault();
      }
    },

    // 렌더될 때 카테고리 속성 부여 + 필터 적용 유지
    eventDidMount: function(info) {
      var cat = info.event.extendedProps.category || '';
      info.el.setAttribute('data-category', cat);
      // 기본 title 속성 사용 → 브라우저 툴팁
      info.el.setAttribute("title", info.event.title);

      // (선택) 더 예쁘게 하려면 커스텀 툴팁 라이브러리도 가능
      // $(info.el).tooltip({ title: info.event.title, placement: 'top' });
        if (info.event.extendedProps.commentCount > 0) {
    let icon = document.createElement('span');
    icon.innerHTML = `<i class="fa fa-comment"></i> ${info.event.extendedProps.commentCount}`;
    icon.style.marginLeft = "4px";
    info.el.querySelector('.fc-event-title').appendChild(icon);
  }

      if (currentCategory && cat !== currentCategory) {
        info.el.style.display = 'none';
      }
    },
  });

  calendar.render();

  // 4) 범례 클릭 → 필터 갱신 후 이벤트만 다시 렌더

  document.querySelectorAll('.calendar-legend .legend-item').forEach(function(el){
  el.addEventListener('click', function(){
    currentCategory = el.dataset.category || null; // ""이면 전체
    calendar.refetchEvents();  // ✅ v5에서는 refetchEvents 사용
  });
});
});
</script>

<?php
include_once(G5_PATH.'/tail.php');
?>


