-- 접속자 로그 테이블
CREATE TABLE IF NOT EXISTS g5_visit (
    vi_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vi_ip VARCHAR(100) DEFAULT '' COMMENT '접속 IP',
    vi_datetime DATETIME DEFAULT NULL COMMENT '접속 일시',
    vi_referer VARCHAR(255) DEFAULT '' COMMENT '이전 페이지',
    vi_agent VARCHAR(255) DEFAULT '' COMMENT 'User Agent',
    INDEX idx_ip (vi_ip),
    INDEX idx_datetime (vi_datetime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='접속자 로그';

-- 접속자 집계 테이블
CREATE TABLE IF NOT EXISTS g5_visit_sum (
    vs_date DATE NOT NULL PRIMARY KEY COMMENT '날짜',
    vs_count INT DEFAULT 0 COMMENT '방문자수'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='접속자 집계';

-- 투표 테이블
CREATE TABLE IF NOT EXISTS g5_poll (
    po_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    po_subject VARCHAR(255) NOT NULL COMMENT '투표 제목',
    po_poll1 VARCHAR(255) DEFAULT '' COMMENT '항목1',
    po_poll2 VARCHAR(255) DEFAULT '' COMMENT '항목2',
    po_poll3 VARCHAR(255) DEFAULT '' COMMENT '항목3',
    po_poll4 VARCHAR(255) DEFAULT '' COMMENT '항목4',
    po_poll5 VARCHAR(255) DEFAULT '' COMMENT '항목5',
    po_poll6 VARCHAR(255) DEFAULT '' COMMENT '항목6',
    po_poll7 VARCHAR(255) DEFAULT '' COMMENT '항목7',
    po_poll8 VARCHAR(255) DEFAULT '' COMMENT '항목8',
    po_poll9 VARCHAR(255) DEFAULT '' COMMENT '항목9',
    po_cnt1 INT DEFAULT 0 COMMENT '항목1 투표수',
    po_cnt2 INT DEFAULT 0 COMMENT '항목2 투표수',
    po_cnt3 INT DEFAULT 0 COMMENT '항목3 투표수',
    po_cnt4 INT DEFAULT 0 COMMENT '항목4 투표수',
    po_cnt5 INT DEFAULT 0 COMMENT '항목5 투표수',
    po_cnt6 INT DEFAULT 0 COMMENT '항목6 투표수',
    po_cnt7 INT DEFAULT 0 COMMENT '항목7 투표수',
    po_cnt8 INT DEFAULT 0 COMMENT '항목8 투표수',
    po_cnt9 INT DEFAULT 0 COMMENT '항목9 투표수',
    po_etc VARCHAR(255) DEFAULT '' COMMENT '기타의견',
    po_level INT DEFAULT 0 COMMENT '참여 가능 레벨',
    po_point INT DEFAULT 0 COMMENT '참여 포인트',
    po_date DATE DEFAULT NULL COMMENT '투표 시작일',
    po_ips TEXT COMMENT '중복투표 방지 IP'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투표';

-- 투표 기타의견 테이블
CREATE TABLE IF NOT EXISTS g5_poll_etc (
    pc_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    po_id INT NOT NULL COMMENT '투표 ID',
    mb_id VARCHAR(20) DEFAULT '' COMMENT '회원 ID',
    pc_name VARCHAR(255) DEFAULT '' COMMENT '이름',
    pc_idea TEXT COMMENT '기타의견',
    pc_datetime DATETIME DEFAULT NULL COMMENT '작성일시',
    INDEX idx_po_id (po_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투표 기타의견';
