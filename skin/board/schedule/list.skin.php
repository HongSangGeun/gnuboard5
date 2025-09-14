<?php
if (!defined('_GNUBOARD_')) exit; // 개별 페이지 접근 불가
// 로그인 안 했으면 로그인 페이지로 이동
if (!$is_member) {
    alert('로그인 하셔야 이용하실 수 있습니다.', G5_BBS_URL.'/login.php?url='.urlencode($_SERVER['REQUEST_URI']));
}

add_stylesheet('<link rel="stylesheet" href="'.$board_skin_url.'/wzappend.css?v=190811">', 0);
add_stylesheet('<link rel="stylesheet" href="'.$board_skin_url.'/style.css">', 1);
?>

<!-- 밑에 add_stylesheet 함수를 사용하지 않는이유은 가끔 홈페이지 개발시 오류로 add_stylesheet 함수가 먹지 않는 현상으로 인해 사용하지 않습니다. -->

<script type="text/javascript" src="<?php echo $board_skin_url;?>/wz.js/moment.min.js"></script>
<script type="text/javascript" src="<?php echo $board_skin_url;?>/wz.js/fullcalendar.js"></script>
<script type="text/javascript" src="<?php echo $board_skin_url;?>/wz.js/ko.js"></script>

<!-- FullCalendar 라이브러리 -->
<link href="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.0/main.min.css" rel="stylesheet" />
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.0/main.min.js"></script>

<script>

var lastView = get_cookie("fc_view") || "dayGridMonth";


document.addEventListener('DOMContentLoaded', function () {
    // 1) 캘린더 엘리먼트 확보
    var calendarEl = document.getElementById('calendar');   // ← 이 줄이 먼저!
    if (!calendarEl) return; // 안전장치

    // 2) 전역처럼 쓸 선택 상태
    var currentCategory = null;

    // 3) 캘린더 생성
    var calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: lastView,
    //initialView: 'dayGridMonth',
    googleCalendarApiKey: 'AIzaSyBDLbVPdJoidVskOO7iA7oeaQ5mm5QL7Qk',
    locale: 'ko',
    slotEventOverlap: true,   // ✅ 이벤트 겹치면 병렬 배치
    slotMinTime: "07:00:00", // 오전 8시부터
    slotMaxTime: "20:30:00", // 오후 8시까지만 표시
    expandRows: false,   // 행이 꽉 차도록 늘림
    contentHeight: 'auto',
    locale: 'ko',
    height: 600,
    headerToolbar: {
      left: 'prev,today,next',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    navLinks: false,
    eventSources: [
        {
            url: '<?php echo $board_skin_url;?>/get-events1.php?bo_table='+g5_bo_table,
            color: '#3788d8',
            textColor: '#fff'
        },
        {
            googleCalendarId: 'ko.south_korea#holiday@group.v.calendar.google.com',
            color: '#FF5C5C',
            textColor: '#ffffff',
            className: 'holiday-event',
            display: 'list-item',   // ✅ 점 스타일
            eventDataTransform: function(ev) {
            delete ev.url;
            return ev;
            }
        }
    ],
    viewDidMount: function(arg) {
        set_cookie("fc_view", arg.view.type, 30);
    },
    eventOverlap: function(stillEvent, movingEvent) {
    // 예: 같은 카테고리면 겹치지 않게
    return stillEvent.extendedProps.category !== movingEvent.extendedProps.category;
    },
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
</div>
  <div class="legend-right">
    <a href="<?php echo G5_BBS_URL; ?>/write.php?bo_table=schedule" class="btn-add-event">+ 일정 등록하기</a>
  </div>
</div>


