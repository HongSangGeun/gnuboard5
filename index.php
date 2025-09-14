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

<link rel="stylesheet" href="css/index.css">

<div id="dashboard" class="latest-grid">
<?php
    for ($i=0; $row=sql_fetch_array($result); $i++) {
    $lt_style = '';
    if ($i%3 !== 0 ) $lt_style = "margin-left:2%";
    ?>
        <div class="latest-card" data-id="<?php echo $row['bo_table']; ?>">
            <?php echo latest('basic', $row['bo_table'], 6, 24); ?>
            <a href="<?php echo G5_BBS_URL; ?>/member_confirm.php?url=<?php echo $bo_table; ?>" class="more-link"></a>
        </div>


<?php } ?>

<div class="latest-card calendar-card" data-id="calendar">
  <div class="card">
    <div class="card-header"> </div>
    <div class="card-body">
      <div id="calendar"></div>
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
</div>


<div class="latest-card restore-card" data-id="restore">
  <div class="card">
    <div class="card-body">
      <button id="showRestoreList" class="restore-btn">숨겨진 카드 복원</button>
      <ul id="restoreList" class="restore-list"></ul>
    </div>
  </div>
</div>

<!-- FullCalendar 라이브러리 -->
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@fullcalendar/google-calendar@6.1.15/index.global.min.js"></script>

<script src="https://cdn.jsdelivr.net/npm/sortablejs@1.15.0/Sortable.min.js"></script>

<style>
/* 드래그 중인 카드 스타일 */
.sortable-ghost {
  opacity: 0.5;
  background: #f0f0f0;
  border: 2px dashed #999;
}
</style>

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


<script>
// ====== 기본 참조 ======
document.addEventListener('DOMContentLoaded', function () {
  const container     = document.getElementById('dashboard');                // 카드 그리드 래퍼
  const restoreCard   = container.querySelector('.restore-card');            // "숨겨진 카드 관리" 카드
  const showRestoreBtn= document.getElementById('showRestoreList');          // 복원 버튼
  const restoreList   = document.getElementById('restoreList');              // 복원 목록 UL

  // ====== 상태 로드 ======
  const savedOrder  = safeParse(localStorage.getItem('dashboardOrder'), []);
  const hiddenCards = safeParse(localStorage.getItem('hiddenCards'), []);

  // ====== 초기 렌더: 순서/숨김 적용 ======
  applySavedOrder();
  applyHiddenState();

  // 항상 restore-card는 맨 아래 고정
  pinRestoreCard();

  // ====== SortableJS 활성화(restore-card, 숨김카드 드래그 금지) ======
  if (typeof Sortable !== 'undefined') {
    Sortable.create(container, {
      animation: 150,
      ghostClass: 'sortable-ghost',
      filter: '.restore-card, .hidden-card',
      preventOnFilter: false,
      onEnd: saveOrder
    });
  }

  // ====== 더블클릭 → 카드 숨김 ======
  container.addEventListener('dblclick', function (e) {
    const card = e.target.closest('.latest-card, .calendar-card');
    if (!card || card.classList.contains('restore-card')) return;

    // 이미 숨김이면 무시
    if (card.classList.contains('hidden-card')) return;

    card.classList.add('hidden-card');
    saveHidden();
    pinRestoreCard();      // restore 카드 맨 아래 유지
  });

  // ====== 복원 리스트 토글 버튼 ======
  showRestoreBtn.addEventListener('click', function () {
    const isOpen = restoreList.dataset.open === '1';
    if (isOpen) {
      closeRestoreList();
    } else {
      openRestoreList();
    }
  });

  // ====== 도우미들 ======
  function safeParse(json, fallback) {
    try { return JSON.parse(json || ''); } catch(e) { return fallback; }
  }

  function cardById(id) {
    return container.querySelector(`[data-id="${CSS.escape(id)}"], #${CSS.escape(id)}`);
  }

  function pinRestoreCard() {
    if (restoreCard && restoreCard.parentNode !== container) return;
    container.appendChild(restoreCard);
  }

    function applySavedOrder() {
  const savedOrder = safeParse(localStorage.getItem("dashboardOrder"), []);
  if (!Array.isArray(savedOrder) || !savedOrder.length) return;

  savedOrder
    .filter(id => id && id.trim() !== "")   // ✅ 빈 값 걸러내기
    .forEach(id => {
      const el = cardById(id);
      if (el) container.insertBefore(el, restoreCard);
    });

  pinRestoreCard();
}


  function applyHiddenState() {
    if (!Array.isArray(hiddenCards)) return;
    hiddenCards.forEach(id => {
      const el = cardById(id);
      if (el && !el.classList.contains('restore-card')) {
        el.classList.add('hidden-card');
      }
    });
  }

    function saveOrder() {
  const order = Array.from(container.children)
    .filter(card => !card.classList.contains("restore-card"))
    .map(card => card.dataset.id || card.id)
    .filter(id => id && id.trim() !== "");   // ✅ 빈 값 제거

  localStorage.setItem("dashboardOrder", JSON.stringify(order));

  if (restoreCard) container.appendChild(restoreCard);
}

  function saveHidden() {
    const hidden = Array.from(container.querySelectorAll('.hidden-card'))
      .filter(el => !el.classList.contains('restore-card'))
      .map(el => el.dataset.id || el.id);

    localStorage.setItem('hiddenCards', JSON.stringify(hidden));
    // console.log('saved hidden:', hidden);
  }

  function openRestoreList() {
    // 최신 hidden 목록으로 새로 그리기
    const hidden = safeParse(localStorage.getItem('hiddenCards'), []);
    restoreList.innerHTML = '';

    if (!hidden.length) {
      restoreList.dataset.open = '0';
      restoreList.style.display = 'none';
      showRestoreBtn.textContent = '숨겨진 카드 복원';
      alert('숨겨진 카드가 없습니다.');
      return;
    }

    hidden.forEach(id => {
      const li = document.createElement('li');
      li.className = 'restore-item';

      const label = document.createElement('span');
      label.className = 'restore-item__label';
      label.textContent = id;

      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'restore-item__btn';
      btn.textContent = '복원';
      btn.addEventListener('click', function () {
        const el = cardById(id);
        if (el) {
          el.classList.remove('hidden-card');
          // 복원된 카드는 restore-card 위로 올려주고 순서 저장
          container.insertBefore(el, restoreCard);
          saveHidden();
          saveOrder();
        }
        li.remove();
        // 항목이 더 없으면 자동 닫기
        if (!restoreList.querySelector('li')) {
          closeRestoreList();
        }
      });

      li.appendChild(label);
      li.appendChild(btn);
      restoreList.appendChild(li);
    });

    restoreList.dataset.open = '1';
    restoreList.style.display = 'block';
    showRestoreBtn.textContent = '복원 닫기';
  }

  function closeRestoreList() {
    restoreList.innerHTML = '';
    restoreList.dataset.open = '0';
    restoreList.style.display = 'none';
    showRestoreBtn.textContent = '숨겨진 카드 복원';
  }
});
</script>


<?php
include_once(G5_PATH.'/tail.php');
?>


