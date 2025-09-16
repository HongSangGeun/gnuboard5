
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



// ====== 숨김 적용 함수 수정 ======
function applyHiddenState() {
  const hiddenCards = safeParse(localStorage.getItem('hiddenCards'), []);
  hiddenCards.forEach(hc => {
    const card = container.querySelector(`.latest-card[data-id="${hc.id}"]`);
    if (card) card.classList.add('hidden-card');
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


    // ====== 숨김 저장 함수 수정 ======
    function saveHidden() {
        const hiddenCards = [];
        container.querySelectorAll('.hidden-card').forEach(card => {
        const id = card.dataset.id;
        // 카드 이름 추출 (원하는 위치 맞게 조정!)
        const name = card.querySelector('.lat_title')?.innerText.trim() || id;
        hiddenCards.push({ id, name });
        });
        localStorage.setItem('hiddenCards', JSON.stringify(hiddenCards));
    }

function openRestoreList() {
  // 최신 hidden 목록으로 새로 그리기
  const hidden = safeParse(localStorage.getItem('hiddenCards'), []);
  restoreList.innerHTML = '';

  if (!hidden.length) {
    restoreList.dataset.open = '0';
    restoreList.style.display = 'none';
    showRestoreBtn.textContent = '숨겨진 카드 복원';
    //alert('숨겨진 카드가 없습니다.');
    return;
  }

  hidden.forEach(hc => {
    const li = document.createElement('li');
    li.className = 'restore-item';

    // ✅ 사람이 읽는 이름 표시
    const label = document.createElement('span');
    label.className = 'restore-item__label';
    label.textContent = hc.name || hc.id;

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'restore-item__btn';
    btn.textContent = '복원';
    btn.addEventListener('click', function () {
      const el = cardById(hc.id);
      if (el) {
        el.classList.remove('hidden-card');
        container.insertBefore(el, restoreCard); // restore-card 위로 올림
        saveHidden();
        saveOrder();
      }
      li.remove();
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


document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".collapse-btn").forEach(btn => {
    btn.addEventListener("click", function () {
      const card = this.closest(".latest-card");
      card.classList.toggle("collapsed");

      // 상태 저장
      saveCollapsedState();
    });
  });

  // 저장된 상태 복원
  restoreCollapsedState();
});

function saveCollapsedState() {
  const collapsed = [];
  document.querySelectorAll(".latest-card").forEach(card => {
    if (card.classList.contains("collapsed")) {
      collapsed.push(card.dataset.id);
    }
  });
  localStorage.setItem("collapsedCards", JSON.stringify(collapsed));
}

function restoreCollapsedState() {
  const collapsed = JSON.parse(localStorage.getItem("collapsedCards") || "[]");
  collapsed.forEach(id => {
    const card = document.querySelector(`.latest-card[data-id="${id}"]`);
    if (card) card.classList.add("collapsed");
  });
}

// system status
async function updatePerfInfo() {
  try {
    const res = await fetch("api/system_status.php");
    const data = await res.json();
    document.getElementById("perfInfo").innerHTML = `
      <p><b>CPU:</b> ${data.cpu}
      <b> ,  메모리:</b> ${data.memory}
      <b> ,  디스크:</b> ${data.disk}</p>
    `;
  } catch (e) {
    console.error("퍼포먼스 정보 불러오기 실패", e);
  }
}

// 10초마다 갱신
setInterval(updatePerfInfo, 10000);
updatePerfInfo();
